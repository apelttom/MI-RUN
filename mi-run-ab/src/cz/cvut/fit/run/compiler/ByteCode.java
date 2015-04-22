package cz.cvut.fit.run.compiler;

import java.util.List;

public class ByteCode {
	private List<String> instructions;

	public void add(String instruction) {
		instructions.add(instruction);
	}

	public void clear() {
		instructions.clear();
	}

	public int size() {
		return instructions.size();
	}
}
