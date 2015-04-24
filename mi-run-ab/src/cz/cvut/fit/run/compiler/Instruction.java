package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction {

	public enum InsSet {
		bipush, istore, iload, iadd, isub, imul, IF_GT_JUMP, IF_LT_JUMP, IF_EQ_JUMP, IF_NEQ_JUMP, IF_GTE_JUMP, IF_LTE_JUMP, JUMP, NOP
	};
	
	/**
	 * According to the java spec:
	 * 
	 * bipush n = pushes byte n on the stack (bipush 42 will push number 43 on
	 *	the top of the stack
	 *
	 * istore index = store int into local variable (pops value from the top of 
	 * 	the stack and stores it into local variable on the position of the index 
	 * 	in local variable table of the current frame)
	 * 
	 * iload = the value of the local int variable at index is pushed onto stack
	 * 
	 * iadd = two top int values are popped from the stack, on the top of the
	 * 	stack goes their addition (+)
	 * 
	 * isubb = first two top int values are popped out of the stack and their 
	 * 	subtraction is pushed on the top of the stack
	 * 
	 *  imul = first two top int values are popped out of the stack and their 
	 *  	multiplication is then pushed on the stack
	 */

	private InsSet insCode;
	private List<String> operands;

	public Instruction(InsSet insCode) {
		this(insCode, null, null);
	}

	public Instruction(InsSet insCode, String operand1) {
		this(insCode, operand1, null);
	}

	public Instruction(InsSet insCode, String operand1, String operand2) {
		this.insCode = insCode;

		operands = new ArrayList<String>();
		if (operand1 != null) {
			operands.add(operand1);
		}
		if (operand2 != null) {
			operands.add(operand2);
		}
	}

	public InsSet getInvertedForInstruction() {
		switch (this.insCode) {
		case IF_GT_JUMP:
		case IF_GTE_JUMP:
			return InsSet.IF_LT_JUMP;
		case IF_LT_JUMP:
		case IF_LTE_JUMP:
			return InsSet.IF_GT_JUMP;
		default:
			break;
		}
		return null;
	}

	public InsSet getInvertedIfInstruction() {
		switch (this.insCode) {
		case IF_GT_JUMP:
			return InsSet.IF_LTE_JUMP;
		case IF_LT_JUMP:
			return InsSet.IF_GTE_JUMP;
		default:
			break;
		}

		return null;
	}

	public List<String> getOperands() {
		return operands;
	}

	private String operandsToString() {
		String out = "";
		for (String op : operands) {
			out = out.concat(op + " ");
		}
		return out;
	}

	public void setInsCode(InsSet insCode) {
		this.insCode = insCode;
	}

	@Override
	public String toString() {
		String s = insCode + "" + (operands.size() > 0 ? " " + operandsToString() : "");
		return s;
	}

	public int getSize() {
		return operands.size();
	}

	public InsSet getInstructionCode() {
		return insCode;
	}
}
