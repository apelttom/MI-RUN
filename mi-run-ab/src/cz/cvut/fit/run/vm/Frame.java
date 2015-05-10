package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.text.AsyncBoxView;

public class Frame {

	private int frameID = -1;
	private Frame parent;

	private Stack<Object> stack = null;
	private Map<Integer, Object> locals = null;
	private int localsOffset = 0;

	public Frame(int ID, Frame parent) {
		this.frameID = ID;
		this.parent = parent;
		this.stack = new Stack<Object>();
		this.locals = new HashMap<Integer, Object>();
	}

	public Frame(int ID, Frame parent, ABObject wrappingObj) {
		this.frameID = ID;
		this.parent = parent;
		this.stack = new Stack<Object>();
		this.locals = new HashMap<Integer, Object>();
		this.locals.put(0, wrappingObj);
		loadGlobalVariables(wrappingObj);
		this.localsOffset = locals.size();
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

	public ABObject getThis() throws InvalidObjectException {
		Object o = locals.get(0);
		if (o instanceof ABObject) {
			return (ABObject) o;
		}
		throw new InvalidObjectException(o.toString());
	}

	/**
	 * 
	 * @param varIndex
	 * @param a
	 * @return the element previously at the specified position
	 */
	public Object storeVar(int varIndex, Object a) {
		return locals.put(varIndex + localsOffset, a);
	}

	public Object loadVar(int varIndex) {
		return locals.get(varIndex + localsOffset);
	}
	
	public Object storeArgument(Object a) {
		int localsPointer = locals.size();
		return locals.put(localsPointer, a);
	}


	private void loadGlobalVariables(ABObject wrappingObj) {
		int localsPointer = locals.size();
		for (ABClassVar global : wrappingObj.getGlobals()) {
			locals.put(localsPointer++, global.getVariableValue());
		}
	}

	@Override
	public String toString() {
		String partA = "Frame " + frameID + ":\n";
		String partB = "STACK:  " + this.stack.toString() + "\n";
		String partC = "LOCALS: " + this.locals.toString() + "\n";
		return partA + partB + partC;
	}

}
