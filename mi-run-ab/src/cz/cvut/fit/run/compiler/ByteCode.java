package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ByteCode implements Iterable<IInstruction> {
	private List<IInstruction> instructions;

	public ByteCode() {
		this.instructions = new ArrayList<IInstruction>();
	}

	public boolean add(IInstruction arg0) {
		return instructions.add(arg0);
	}

	public void clear() {
		instructions.clear();
	}

	public IInstruction get(int index) {
		return instructions.get(index);
	}

	public boolean isEmpty() {
		return instructions.isEmpty();
	}

	public Iterator<IInstruction> iterator() {
		return instructions.iterator();
	}

	public IInstruction remove(int arg0) {
		return instructions.remove(arg0);
	}

	public int size() {
		return instructions.size();
	}

	public List<IInstruction> subList(int arg0, int arg1) {
		return instructions.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return instructions.toArray();
	}

	public void changeOperand(int indexOfIInstruction, int indexOfOperand, String operand) {
		this.instructions.get(indexOfIInstruction).getOperands().add(indexOfOperand, operand);
	}
}
