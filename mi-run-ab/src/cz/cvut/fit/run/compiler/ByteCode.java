package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ByteCode implements Iterable<Instruction> {
	private List<Instruction> instructions;

	public ByteCode() {
		this.instructions = new ArrayList<Instruction>();
	}

	public boolean add(Instruction arg0) {
		return instructions.add(arg0);
	}

	public void clear() {
		instructions.clear();
	}

	public Instruction get(int index) {
		return instructions.get(index);
	}

	public boolean isEmpty() {
		return instructions.isEmpty();
	}

	@Override
	public Iterator<Instruction> iterator() {
		return instructions.iterator();
	}

	public Instruction remove(int arg0) {
		return instructions.remove(arg0);
	}

	public int size() {
		return instructions.size();
	}

	public List<Instruction> subList(int arg0, int arg1) {
		return instructions.subList(arg0, arg1);
	}

	public Instruction[] toArray() {
		return instructions.toArray(new Instruction[size()]);
	}

	public void changeOperand(int indexOfInstruction, int indexOfOperand, String operand) {
		this.instructions.get(indexOfInstruction).getOperands().set(indexOfOperand, operand);
	}

	public void merge(ByteCode byteCode) {
		for (Instruction instr : byteCode) {
			this.add(instr);
		}
	}
}
