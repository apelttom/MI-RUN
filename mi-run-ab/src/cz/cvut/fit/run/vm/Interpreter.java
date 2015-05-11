package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.ClassFile;
import cz.cvut.fit.run.compiler.FieldInfo;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.compiler.Instruction.InsSet;
import cz.cvut.fit.run.compiler.MethodInfo;

public class Interpreter {

	private static final String MAIN = "main";
	private static final String MAIN_CLASS = "ABCode";
	private static final int CONSTRUCTOR_INDEX = 0;

	private List<ABObject> heap = null; // stores dynamic objects
	private List<ClassFile> classFiles = null;
	private FrameFactory frameFactory = null;
	private Natives natives = null;

	public Interpreter(List<ClassFile> classFiles) {
		this.classFiles = classFiles;
		this.heap = new ArrayList<ABObject>();
		this.frameFactory = new FrameFactory();
		this.natives = new Natives();
	}

	public void execute() throws Exception {
		ClassFile mainCF = findClassFile(MAIN_CLASS);
		ABObject mainObject = createObject(mainCF);
		ByteCode main = mainCF.getMethod(MAIN).getBytecode();
		Frame mainFrame = frameFactory.makeFrame(null, mainObject);

		executeInternal(main, mainFrame);

		frameFactory.print();
	}

	private void executeInternal(ByteCode bytecode, Frame frame) throws Exception {
		for (int PC = 0; PC <= bytecode.size() - 1; PC++) {
			try {
				// System.out.println(bytecode.get(PC));
				handleInstruction(bytecode.get(PC), frame);
			} catch (GotoException e) {
				// System.out.println(e.toString());

				// PC have to be one lesser, cause it is automatically
				// incremented in the for clause.
				PC = e.getJumpToPC() - 1;
			} catch (ReturnException e1) {
				break;
			}
		}
	}

	private void handleInstruction(Instruction inst, Frame frame) throws Exception {
		InsSet instr = inst.getInstructionCode();
		List<String> instrArgs = inst.getOperands();

		// System.out.println("Interpreter.handleInstruction() instruction "
		// + instr.toString() + " size " + inst.getSize());

		switch (inst.getSize()) {
		case 0:
			noArgumentsInstruction(frame, instr);
			break;
		case 1:
			oneArgumentInstruction(frame, instr, instrArgs.get(0));
			break;
		case 2:
			twoArgumentsInstruction(frame, instr, instrArgs.get(0), instrArgs.get(1));
			break;
		default:
			System.err.println("More than 2 arguments instruction " + inst);
		}
	}

	private void noArgumentsInstruction(Frame frame, InsSet instr) throws Exception {
		if (instr.equals(InsSet.iadd)) {
			FrameOperations.iaddition(frame);
		} else if (instr.equals(InsSet.isub)) {
			FrameOperations.isubtraction(frame);
		} else if (instr.equals(InsSet.imul)) {
			FrameOperations.imultiplication(frame);
		} else if (instr.equals(InsSet.re_turn)) {
			return;
		} else if (instr.equals(InsSet.ireturn)) {
			FrameOperations.ireturn(frame);
			throw new ReturnException();
		} else {
			throw new UnsupportedOperationException(instr.name());
		}
	}

	private void oneArgumentInstruction(Frame frame, InsSet instr, String op1) throws Exception {
		if (isLogicalCondition(instr)) {
			handleLogicalCondition(frame, instr, Integer.parseInt(op1));
		}
		// for cycle
		// else if (isForCycle) {}
		else if (instr.equals(InsSet.bipush)) {
			frame.pushToStack(Integer.parseInt(op1));
		} else if (instr.equals(InsSet.istore)) {
			FrameOperations.istoreVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.astore)) {
			FrameOperations.astoreVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.iload)) {
			FrameOperations.iloadVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.aload)) {
			FrameOperations.aloadVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.go_to)) {
			throw new GotoException(Integer.parseInt(op1));
		} else if (instr.equals(InsSet.invoke)) {
			if (natives.contains(op1)) {
				// FIXME toto resime spatne, tady by neslo nativni metody pretezovat, tedy by se
				// nikde v kodu nemohla objevit nase metoda fileWrite, zde by to stejne vzdycky
				// skocilo na nativni implementaci. Jednodussi reseni je hledat v nativkach az
				// tehdy, kdyz jsme ji nenasli v seznamu instancnich metod. Pak je ale stejne
				// problem v dedeni, ta metoda muze byt v predcich. Resi se to tak, ze se vsechny
				// nativni metody pridaji do dane instance a az kdyz je zavolana, rozlisuje se
				// jestli je to nativka (a volame kod mimo VM) nebo nase vlastni, tedy i pretizena,
				// metoda, a volame Frame a bytecode.
				nativeMethodInvoke(frame, op1);
				return;
			}
			ABObject wrappingObj = frame.getThis();
			Frame newFrame = frameFactory.makeFrame(frame, wrappingObj);
			MethodInfo method = frame.getThis().getClassfile().getMethod(op1);
			addArgumentsToMethod(newFrame, method);
			executeInternal(method.getBytecode(), newFrame);
		} else {
			throw new UnsupportedOperationException(instr.name());
		}
	}

	private void twoArgumentsInstruction(Frame frame, InsSet instr, String op1, String op2)
			throws Exception {
		if (instr.equals(InsSet.iinc)) {
			FrameOperations.incVar(frame, Integer.parseInt(op1), Integer.parseInt(op2));
		} else if (instr.equals(InsSet.new_class)) {
			// Create dynamically new object on Heap. It will be generic object
			// ABObject
			createObject(frame, op1);
		} else if (instr.equals(InsSet.invoke)) {
			// invoking method on a dynamic object, op1=methodName op2=object index
			int index = Integer.parseInt(op2);
			ABObject variable = (ABObject) frame.loadVar(index);
			ClassFile classfile = variable.getClassfile();
			Frame newFrame = frameFactory.makeFrame(frame, variable);
			MethodInfo method = classfile.getMethod(op1);
			addArgumentsToMethod(newFrame, method);
			ByteCode bytecode = method.getBytecode();
			executeInternal(bytecode, newFrame);
		} else {
			throw new UnsupportedOperationException(instr.name());
		}
	}

	private void nativeMethodInvoke(Frame frame, String methodName) {
		int nArgs = natives.getNumberOfArgs(methodName);
		List<Object> args = new ArrayList<Object>(nArgs);
		for (int i = 0; i < nArgs; i++) {
			args.add(frame.popFromStack());
		}
		// we pop object in reverse order
		Collections.reverse(args);
		natives.invoke(methodName, args.toArray());
	}

	private void addArgumentsToMethod(Frame frame, MethodInfo method) {
		Frame parent = frame.getParent();
		for (int i = 0; i < method.getArgTypes().size(); i++) {
			frame.storeArgument(parent.popFromStack());
		}
	}

	private ABObject createObject(Frame frame, String classFileName) throws Exception {
		ClassFile result = null;
		// here I find ClassFile of class I am creating
		ClassFile[] classFilesArray = classFiles.toArray(new ClassFile[classFiles.size()]);
		for (ClassFile cf : classFilesArray) {
			if (cf.getThis().equals(classFileName)) {
				result = cf;
			}
		}
		// dynamic creation of object
		ABObject dynamicObj = new ABObject(result, createGlobalVariables(result));
		// start constructor for newly created dynamic object
		runConstructor(dynamicObj, result);
		// add object to the heap
		heap.add(dynamicObj);
		frame.pushToStack(dynamicObj);
		return dynamicObj;
	}

	private ABObject createObject(ClassFile classFile) throws Exception {
		ABObject object = new ABObject(classFile, createGlobalVariables(classFile));
		// start constructor for new object
		runConstructor(object, classFile);
		// add object to the heap
		heap.add(object);
		return object;
	}

	private List<ABClassVar> createGlobalVariables(ClassFile classFile) {
		// creation of global variables for new dynamic class
		List<ABClassVar> globals = new ArrayList<ABClassVar>();
		for (FieldInfo fieldInfo : classFile.getFields()) {
			ABClassVar globalVar = new ABClassVar(fieldInfo.name, null, fieldInfo.type);
			// flags setup
			for (String flag : fieldInfo.flags) {
				if (flag.equals("private") || flag.equals("public")) {
					globalVar.setVariableProtection(flag);
				}
				if (flag.equals("static")) {
					globalVar.setStatic(true);
				}
			}
			globals.add(globalVar);
		}
		return globals;
	}

	private void runConstructor(ABObject object, ClassFile classFile) throws Exception {
		Frame constructorFrame = this.frameFactory.makeFrame(null, object);
		ByteCode constructorBC = classFile.getMethod(CONSTRUCTOR_INDEX).getBytecode();
		// we have frame and bytecode prepared -> run the constructor
		executeInternal(constructorBC, constructorFrame);
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.if_icmpgt) || instr.equals(InsSet.if_icmpeq)
				|| instr.equals(InsSet.if_icmplt) || instr.equals(InsSet.if_icmpne)
				|| instr.equals(InsSet.if_icmpge) || instr.equals(InsSet.if_icmple));
	}

	private void handleLogicalCondition(Frame frame, InsSet instr, int jumpToPC)
			throws InvalidObjectException {

		if (instr.equals(InsSet.if_icmpeq)) {
			if (FrameOperations.iequal(frame)) {
				throw new GotoException(jumpToPC);
			}
		} else if (instr.equals(InsSet.if_icmpne)) {
			if (!FrameOperations.iequal(frame)) {
				throw new GotoException(jumpToPC);
			}
		} else if (instr.equals(InsSet.if_icmplt)) {
			if (FrameOperations.ilesser(frame)) {
				throw new GotoException(jumpToPC);
			}
		} else if (instr.equals(InsSet.if_icmpge)) {
			if (!FrameOperations.ilesser(frame)) {
				throw new GotoException(jumpToPC);
			}
		} else if (instr.equals(InsSet.if_icmpgt)) {
			if (FrameOperations.igreater(frame)) {
				throw new GotoException(jumpToPC);
			}
		} else if (instr.equals(InsSet.if_icmple)) {
			if (!FrameOperations.igreater(frame)) {
				throw new GotoException(jumpToPC);
			}
		}
	}

	/**
	 * Search for a ClassFile by given name. Returns null if not found.
	 */
	private ClassFile findClassFile(String name) {
		for (ClassFile classFile : classFiles) {
			if (classFile.getThis().equals(name)) {
				return classFile;
			}
		}
		return null;
	}
}
