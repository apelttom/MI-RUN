package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction {

	public enum InsSet {
		bipush, istore, iload, iadd, isub, imul, if_icmpeq, if_icmpne, 
		if_icmplt, if_icmpge, if_icmpgt, if_icmple, go_to, re_turn
	};

	/**
	 * According to the java spec:
	 * 
	 * bipush n = pushes byte n on the stack (bipush 42 will push number 43 on
	 * 		the top of the stack
	 *
	 * istore index = store int into local variable (pops value from the top of
	 * 		the stack [MUST BE INT] and stores it into local variable on the 
	 * 		position of the index in local variable table of the current frame)
	 * 
	 * iload index = the value of the local int variable at index is pushed on
	 * 	the stack
	 * 
	 * iadd = two top values (MUST BE INT) are popped from the stack and on the 
	 * 		top of the stack push their addition (+)
	 * 
	 * isubb = first two top values (MUST BE INT) are popped out of the stack 
	 * 		and their subtraction is pushed on the top of the stack
	 * 
	 * imul = first two top values (MUST BE INT) are popped out of the stack 
	 * 		and their multiplication is then pushed on the top of the stack
	 * 
	 * if_icmp<condition> branchindex = poppes two top values (MUST BE INT) 
	 * 		from the stack and compared. Results:
	 * 		if_icmpeq succeeds if and only if value1 = value2
	 * 		if_icmpne succeeds if and only if value1 != value2
	 * 		if_icmplt succeeds if and only if value1 < value2
	 * 		if_icmpge succeeds if and only if value1 >= value2
	 * 		if_icmpgt succeeds if and only if value1 > value2
	 * 		if_icmple succeeds if and only if value1 =< value2
	 * 		
	 * 		If the comparison succeeds code continues on the instruction at 
	 * 		branchindex. If not, code continues on the next instruction.
	 * 
	 * go_to index = code proceeds on the instruction at index.
	 * 
	 * re_turn = return void from method
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
		case if_icmpgt:
		case if_icmpge:
			return InsSet.if_icmplt;
		case if_icmplt:
		case if_icmple:
			return InsSet.if_icmpgt;
		default:
			break;
		}
		return null;
	}

	public InsSet getInvertedIfInstruction() {
		switch (this.insCode) {
		case if_icmpgt:
			return InsSet.if_icmple;
		case if_icmplt:
			return InsSet.if_icmpge;
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
		String s = insCode + ""
				+ (operands.size() > 0 ? " " + operandsToString() : "");
		return s;
	}

	public int getSize() {
		return operands.size();
	}

	public InsSet getInstructionCode() {
		return insCode;
	}
}
