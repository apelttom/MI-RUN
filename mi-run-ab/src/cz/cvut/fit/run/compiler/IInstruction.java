package cz.cvut.fit.run.compiler;

import java.util.List;

public interface IInstruction {
	
	public enum InsSet {
		IF_EQ_JUMP, IF_GT_JUMP, IF_GTE_JUMP, IF_LT_JUMP, IF_LTE_JUMP, IF_NEQ_JUMP, JUMP, LOAD_VAR, MINUS, MULTIPLY, NOP, PLUS, PUSH_NUMBER, STORE_VAR
	};

	public List<String> getOperands();
	public InsSet getInstructionCode();
	public int getSize();

}