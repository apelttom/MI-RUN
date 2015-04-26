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
		for (Instruction ins : bytecode) {
			handleInstruction(ins);
		}
	}

	private void handleInstruction(Instruction inst) throws Exception {
		InsSet instr = inst.getInstructionCode();
		List<String> instrArgs = inst.getOperands();
		String type;

		Methods methods = new Methods();
		InterpreterContext context = InterpreterContext.getInstance();

		System.out.println("Interpreter.handleInstruction() instruction "
				+ instr.toString());

		/* Bytecode instruction without arguments */
		if (inst.getSize() == 0) {
			if (instr.equals(InsSet.iadd)) {
				methods.addition();
			} else if (instr.equals(InsSet.isub)) {
				methods.subtraction();
			} else if (instr.equals(InsSet.imul)) {
				methods.multiplication();
			} else if (instr.equals(InsSet.re_turn)) {
				return;
			} else {
				throw new UnsupportedOperationException();
			}
		}

		/* Bytecode instruction with one parameter */
		else if (inst.getSize() == 1) {
			if (isLogicalCondition(instr)) {
				// handleLogicalCondition(instr, instrParam);
			}
			// for cycle
			// else if (isForCycle) {}
			else if (instr.equals(InsSet.bipush)) {
				context.pushToStack(Integer.parseInt(instrArgs.get(0)));
			} else if (instr.equals(InsSet.iload)) {
				// ValuePair varVal =
				// InterpreterContext.getInstance().getFromVarPool(instrParam);
				// InterpreterContext.getInstance().pushToStack(varVal);
			} else if (instr.equals(InsSet.istore)) {
				// ValuePair varVal =
				// InterpreterContext.getInstance().getFromVarPool(instrParam);
				// InterpreterContext.getInstance().pushToStack(varVal);
			} else if (instr.equals(InsSet.go_to)) {
				// currPC = Integer.parseInt(instrParam) - 1; // gotta
				// decrement because the PC is automatically incremented in
				// executeByteCode
			}
		}

//		else if (inst.getSize() == 2) { // Nazev instrukce, parametr a typ
										// parametru
			// instrParam = lineParams[2];
			// type = lineParams[3];
//			if (instr.equals("istore")) {
				// Object varVal =
				// InterpreterContext.getInstance().popFromStack();
				// if (varVal instanceof ValuePair)
				// InterpreterContext.getInstance().insertIntoVarPool(instrParam,
				// (ValuePair) varVal);
				// else
				// InterpreterContext.getInstance().insertIntoVarPool(instrParam,
				// new ValuePair(varVal, type));
//			}
//		}
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.if_icmpgt)
				|| instr.equals(InsSet.if_icmpeq)
				|| instr.equals(InsSet.if_icmplt)
				|| instr.equals(InsSet.if_icmpne)
				|| instr.equals(InsSet.if_icmpge) || instr
					.equals(InsSet.if_icmple));
	}
}
