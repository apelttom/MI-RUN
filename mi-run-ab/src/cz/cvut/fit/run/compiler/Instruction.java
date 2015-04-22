package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction implements IInstruction {

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

	@Override
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
	
	@Override
	public int getSize() {
		return operands.size();
	}

	@Override
	public InsSet getInstructionCode() {
		return insCode;
	}
}
