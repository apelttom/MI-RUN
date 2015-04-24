package cz.cvut.fit.run.vm;

import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Interpreter {
	private ByteCode bytecode;

	public Interpreter(ByteCode bytecode) {
		this.bytecode = bytecode;
	}

	public void execute() {
		for (Instruction ins : bytecode) {
			handleInstruction(ins);
		}
	}

	private void handleInstruction(Instruction ins) {
		InsSet instr = ins.getInstructionCode();
		String instrParam;
		String type;

		System.out.println("Interpreter.handleInstruction() instruction "+instr.toString());
		if (ins.getSize() == 0) { // Jen jedna hodnota, coz je nazev instrukce bajtkodu bez dalsich hodnot
			if (instr.equals(InsSet.imul) || instr.equals(InsSet.iadd) || instr.equals(InsSet.isub)) {
//				InterpreterContext.getInstance().pushToStack(new ValuePair(MethodLookup.performArithmetic(instr), "int"));
			}
		}

		else if (ins.getSize() == 1) { // Dve hodnoty, nazev instrukce a parametr
			if (instr.equals(InsSet.bipush))
//				InterpreterContext.getInstance().pushToStack(new ValuePair(instrParam, "int"));
			if (isLogicalCondition(instr)) {
//				handleLogicalCondition(instr, instrParam);
			} else if (instr.equals(InsSet.JUMP)) {
//				currPC = Integer.parseInt(instrParam) - 1; 	// gotta decrement because the PC is automatically incremented in executeByteCode
			}												// for iteration
			else if (instr.equals(InsSet.NOP))
				return;
			else if (instr.equals(InsSet.iload)) {
//				ValuePair varVal = InterpreterContext.getInstance().getFromVarPool(instrParam);
//				InterpreterContext.getInstance().pushToStack(varVal);
			}
		}

		else if (ins.getSize() == 2) { // Nazev instrukce, parametr a typ parametru
//			instrParam = lineParams[2];
//			type = lineParams[3];
			if (instr.equals("istore")) {
//				Object varVal = InterpreterContext.getInstance().popFromStack();
//				if (varVal instanceof ValuePair)
//					InterpreterContext.getInstance().insertIntoVarPool(instrParam, (ValuePair) varVal);
//				else
//					InterpreterContext.getInstance().insertIntoVarPool(instrParam, new ValuePair(varVal, type));
			}
		}
	}

	private static boolean isLogicalCondition(InsSet instr) {
		return (instr.equals(InsSet.IF_GT_JUMP) || instr.equals(InsSet.IF_EQ_JUMP) || instr.equals(InsSet.IF_LT_JUMP)
				|| instr.equals(InsSet.IF_NEQ_JUMP) || instr.equals(InsSet.IF_GTE_JUMP) || instr.equals(InsSet.IF_LTE_JUMP));
	}
}
