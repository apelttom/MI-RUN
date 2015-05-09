package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Interpreter {
	
	private static final String MAIN = "main";
	private List<ABObject> heap = null; // FIXME what is it for?
	private List<ClassFile> classFiles = null;
	private FrameFactory frameFactory = null;

	public Interpreter(List<ClassFile> classFiles) {
		this.classFiles = classFiles;
		this.heap = new ArrayList<ABObject>();
		this.frameFactory = new FrameFactory();
	}

	public void execute() throws Exception {
		ByteCode main = classFiles.get(0).getMethod(MAIN).getBytecode();
		Frame mainFrame = frameFactory.makeFrame(null);
		
		executeInternal(main, mainFrame);
		
		frameFactory.print();
	}

	private void executeInternal(ByteCode bytecode, Frame frame) throws Exception {
		for (int PC = 0; PC <= bytecode.size() - 1; PC++) {
			try {
//				System.out.println(bytecode.get(PC));

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
		case 0: noArgumentsInstruction(frame, instr);
			break;
		case 1: oneArgumentInstruction(frame, instr, instrArgs.get(0));
			break;
		case 2: twoArgumentsInstruction(frame, instr, instrArgs.get(0), 
				instrArgs.get(1));
			break;
		default: System.err.println("More than 2 arguments instruction "+inst);
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

	private void oneArgumentInstruction(Frame frame, InsSet instr, String op1) 
			throws Exception {
		if (isLogicalCondition(instr)) {
			handleLogicalCondition(frame, instr, Integer.parseInt(op1));
		}
		// for cycle
		// else if (isForCycle) {}
		else if (instr.equals(InsSet.bipush)) {
			frame.pushToStack(Integer.parseInt(op1));
		} else if (instr.equals(InsSet.istore)) {
			FrameOperations.istoreVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.iload)) {
			FrameOperations.iloadVar(frame, Integer.parseInt(op1));
		} else if (instr.equals(InsSet.go_to)) {
			throw new GotoException(Integer.parseInt(op1));
		} else if (instr.equals(InsSet.invoke)) {
			Frame newFrame = frameFactory.makeFrame(frame);
			MethodInfo method = classFiles.get(0).getMethod(op1);
			for (int i = 0; i < method.getArgTypes().size(); i++) {
				newFrame.istoreVar(i, (Integer) frame.popFromStack());
				// inverse declaration?
			}
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
			createObject(op1);
		} 
		else {
			throw new UnsupportedOperationException(instr.name());
		}
	}

	private void createObject(String name) {
		ClassFile[] classFilesArray = classFiles.toArray(new ClassFile[classFiles.size()]);
		for (ClassFile cf : classFilesArray) {
//			if (cf.get)
		}
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.if_icmpgt) || 
				instr.equals(InsSet.if_icmpeq) || 
				instr.equals(InsSet.if_icmplt) || 
				instr.equals(InsSet.if_icmpne) || 
				instr.equals(InsSet.if_icmpge) || 
				instr.equals(InsSet.if_icmple));
	}

	private void handleLogicalCondition(Frame frame, InsSet instr, int jumpToPC) throws InvalidObjectException {

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
}
