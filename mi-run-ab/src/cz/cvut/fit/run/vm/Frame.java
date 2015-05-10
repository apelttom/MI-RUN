package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Frame {

	private int frameID = -1;
	private Frame parent;

	private Stack<Object> stack = null;
	private List<Object> locals = null;
	private ABObject thisClass = null;
	private int globalVarBound = 0;

	public Frame(int ID, Frame parent) {
		this.frameID = ID;
		this.parent = parent;
		this.stack = new Stack<Object>();
		this.locals = new ArrayList<Object>();
	}

	public Frame(int ID, Frame parent, ABObject wrappingObj) {
		this(ID, parent);
		this.thisClass = wrappingObj;
		loadGlobalVariables(wrappingObj);
		this.globalVarBound = locals.size();
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

	public ABObject getThis() {
		return thisClass;
	}

	/**
	 * @param varIndex
	 * @param a
	 * @return the element previously at the specified position
	 */
	public void storeVar(int varIndex, Object a) {
		if (varIndex < this.globalVarBound) {
			// method is changing global variable -> change it in object
			thisClass.changeVariableValue(varIndex, a);
		}
		if (varIndex >= locals.size()) {
			locals.add(a);
		} else {
			locals.set(varIndex, a);
		}
	}

	public Object loadVar(int varIndex) {
		return locals.get(varIndex);
	}

	public void storeArgument(Object a) {
		locals.add(a);
	}

	private void loadGlobalVariables(ABObject wrappingObj) {
		for (ABClassVar global : wrappingObj.getGlobals()) {
			locals.add(global.getVariableValue());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Frame ");
		sb.append(frameID);
		sb.append(":\n");
		sb.append("STACK: ");
		sb.append(stack);
		sb.append("\nLOCALS:[");
		boolean first = true;
		for (int i = 0; i < locals.size(); i++) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(i).append("=").append(locals.get(i));
		}
		sb.append("]\n");
		return sb.toString();
	}

}
