package cz.cvut.fit.run.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Frame {

	private int frameID = -1;
	private Frame parent;

	private Stack<Object> stack = null;
	private Map<Integer, Object> locals = null;

	public Frame(int ID, Frame parent) {
		this.frameID = ID;
		this.parent = parent;
		this.stack = new Stack<Object>();
		this.locals = new HashMap<Integer, Object>();
	}

	public Frame getParent() {
		return parent;
	}
	
	public Object pushToStack(Object obj) {
		return stack.push(obj);
	}

	public Object popFromStack() {
		return stack.pop();
	}

	public boolean isStackEmpty() {
		return stack.isEmpty();
	}

	/**
	 * 
	 * @param varIndex
	 * @param a
	 * @return the element previously at the specified position
	 */
	public Object istoreVar(int varIndex, Integer a) {
		return locals.put(varIndex, a);
	}

	public Integer iloadVar(int varIndex) {
		return (Integer) locals.get(varIndex);
	}

	@Override
	public String toString() {
		String partA = "Frame " + frameID + ":\n";
		String partB = "STACK:  " + this.stack.toString() + "\n";
		String partC = "LOCALS: " + this.locals.toString() + "\n";
		return partA + partB + partC;
	}

}
