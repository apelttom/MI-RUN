package cz.cvut.fit.run.vm;

import java.util.List;

import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Interpreter {

	private ByteCode bytecode;

	public Interpreter(ByteCode bytecode) {
		this.bytecode = bytecode;
	}

	public void execute() throws Exception {
		for (int PC = 0; PC <= bytecode.size() - 1; PC++) {
			try {
				handleInstruction(bytecode.get(PC));
			} catch (GotoException e) {
				// System.out.println(e.toString());
				
				// PC have to be one lesser, cause it is automatically
				// incremented in the for clause.
				PC = e.getJumpToPC() - 1;
			}
		}
	}

	private void handleInstruction(Instruction inst) throws Exception {
		InsSet instr = inst.getInstructionCode();
		List<String> instrArgs = inst.getOperands();

		Methods methods = new Methods();
		InterpreterContext context = InterpreterContext.getInstance();

		// System.out.println("Interpreter.handleInstruction() instruction "
		// + instr.toString() + " size " + inst.getSize());

		/* Bytecode instruction without arguments */
		if (inst.getSize() == 0) {
			if (instr.equals(InsSet.iadd)) {
				methods.iaddition();
			} else if (instr.equals(InsSet.isub)) {
				methods.isubtraction();
			} else if (instr.equals(InsSet.imul)) {
				methods.imultiplication();
			} else if (instr.equals(InsSet.re_turn)) {
				return;
			} else {
				throw new UnsupportedOperationException();
			}
		}

		/* Bytecode instruction with one argument */
		else if (inst.getSize() == 1) {
			if (isLogicalCondition(instr)) {
				handleLogicalCondition(instr,
						Integer.parseInt(instrArgs.get(0)));
			}
			// for cycle
			// else if (isForCycle) {}
			else if (instr.equals(InsSet.bipush)) {
				context.pushToStack(Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.istore)) {
				methods.istoreVar(Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.iload)) {
				methods.iloadVar(Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.go_to)) {
				throw new GotoException(Integer.parseInt(instrArgs.get(0)));
			}
		}
		
		System.out.println(InterpreterContext.getInstance().toString());
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.if_icmpgt)
				|| instr.equals(InsSet.if_icmpeq)
				|| instr.equals(InsSet.if_icmplt)
				|| instr.equals(InsSet.if_icmpne)
				|| instr.equals(InsSet.if_icmpge) || instr
					.equals(InsSet.if_icmple));
	}
	

	private void handleLogicalCondition(InsSet instr, int jumpToPC) {
		
	}
}
