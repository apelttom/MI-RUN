package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction {

	public enum InsSet {
		IF_EQ_JUMP, IF_GT_JUMP, IF_GTE_JUMP, IF_LT_JUMP, IF_LTE_JUMP, IF_NEQ_JUMP, JUMP, LOAD_VAR, MINUS, MULTIPLY, NOP, PLUS, PUSH_NUMBER, STORE_VAR
	};

	private int label;
	private InsSet opcode;

	private List<String> operands;

	public Instruction(InsSet opcode) {
		this(opcode, null, null, -1);
	}

	public Instruction(InsSet opcode, String operand1) {
		this(opcode, operand1, null, -1);
	}

	public Instruction(InsSet opcode, String operand1, int label) {
		this(opcode, operand1, null, label);
	}

	public Instruction(InsSet opcode, String operand1, String operand2) {
		this(opcode, operand1, operand2, -1);
	}

	public Instruction(InsSet opcode, String operand1, String operand2, int label) {
		this.opcode = opcode;

		operands = new ArrayList<String>();
		if (operand1 != null) {
			operands.add(operand1);
		}
		if (operand2 != null) {
			operands.add(operand2);
		}

		this.label = label;
	}

	public InsSet getInvertedForInstruction() {
		switch (this.opcode) {
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
		switch (this.opcode) {
		case IF_GT_JUMP:
			return InsSet.IF_LTE_JUMP;
		case IF_LT_JUMP:
			return InsSet.IF_GTE_JUMP;
		default:
			break;
		}

		return null;
	}

	public int getLabel() {
		return label;
	}

	public List<String> getOperands() {
		return operands;
	}

	public String operandsToString() {
		String out = "";
		for (String op : operands) {
			out = out.concat(op + " ");
		}

		return (out);
	}

	public void setOpcode(InsSet opcode) {
		this.opcode = opcode;
	}

	@Override
	public String toString() {
		String s = opcode + "" + (operandsToString().length() > 0 ? " " + operandsToString() : "") + "" + (label != -1 ? " " + label : "");
		return s;
	}
}
