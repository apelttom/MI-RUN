package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Interpreter {

	private List<Object> heap = null;
	private List<ClassFile> classFiles = null;
	private FrameFactory frameFactory = null;

	public Interpreter(List<ClassFile> classFiles) {
		this.classFiles = classFiles;
		this.heap = new ArrayList<Object>();
		this.frameFactory = new FrameFactory();
	}

	public void execute() throws Exception {
		ByteCode main = classFiles.get(0).getMethod(0).getBytecode();
		Frame mainFrame = frameFactory.makeFrame();
		for (int PC = 0; PC <= main.size() - 1; PC++) {
			try {
				handleInstruction(main.get(PC), mainFrame);
			} catch (GotoException e) {
				// System.out.println(e.toString());

				// PC have to be one lesser, cause it is automatically
				// incremented in the for clause.
				PC = e.getJumpToPC() - 1;
			}
		}
	}

	private void handleInstruction(Instruction inst, Frame frame)
			throws Exception {
		InsSet instr = inst.getInstructionCode();
		List<String> instrArgs = inst.getOperands();
		StackOperations methods = StackOperations.getInstance();

		// System.out.println("Interpreter.handleInstruction() instruction "
//				+ instr.toString() + " size " + inst.getSize());

		/* Bytecode instruction without arguments */
		if (inst.getSize() == 0) {
			if (instr.equals(InsSet.iadd)) {
				methods.iaddition(frame);
			} else if (instr.equals(InsSet.isub)) {
				methods.isubtraction(frame);
			} else if (instr.equals(InsSet.imul)) {
				methods.imultiplication(frame);
			} else if (instr.equals(InsSet.re_turn)) {
				return;
			} else {
				throw new UnsupportedOperationException();
			}
		}

		/* Bytecode instruction with one argument */
		else if (inst.getSize() == 1) {
			if (isLogicalCondition(instr)) {
				handleLogicalCondition(frame, instr,
						Integer.parseInt(instrArgs.get(0)));
			}
			// for cycle
			// else if (isForCycle) {}
			else if (instr.equals(InsSet.bipush)) {
				frame.pushToStack(Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.istore)) {
				methods.istoreVar(frame, Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.iload)) {
				methods.iloadVar(frame, Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.go_to)) {
				throw new GotoException(Integer.parseInt(instrArgs.get(0)));
			}
		}

		else if (inst.getSize() == 2) {
			if (instr.equals(InsSet.iinc)) {
				methods.incVar(frame, Integer.parseInt(instrArgs.get(0)),
						Integer.parseInt(instrArgs.get(1)));
			}
		}

//		System.out.println(FrameFactory.getInstance().toString());
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.if_icmpgt)
				|| instr.equals(InsSet.if_icmpeq)
				|| instr.equals(InsSet.if_icmplt)
				|| instr.equals(InsSet.if_icmpne)
				|| instr.equals(InsSet.if_icmpge) || instr
					.equals(InsSet.if_icmple));
	}

	private void handleLogicalCondition(Frame frame, InsSet instr, int jumpToPC)
			throws InvalidObjectException {
		StackOperations methods = StackOperations.getInstance();

		if (instr.equals(InsSet.if_icmpeq)) {
			if (methods.iequal(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		} else if (instr.equals(InsSet.if_icmpne)) {
			if (!methods.iequal(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		} else if (instr.equals(InsSet.if_icmplt)) {
			if (methods.ilesser(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		} else if (instr.equals(InsSet.if_icmpge)) {
			if (!methods.ilesser(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		} else if (instr.equals(InsSet.if_icmpgt)) {
			if (methods.igreater(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		} else if (instr.equals(InsSet.if_icmple)) {
			if (!methods.igreater(frame)) {
				throw new GotoException(jumpToPC);
			}
			return;
		}
	}
}
