package cz.cvut.fit.run.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class InterpreterContext {

	private static InterpreterContext instance = null;
	private Stack<Object> stack = null;
	private Map<Integer, Object> locals = null;

	private InterpreterContext() {
		stack = new Stack<Object>();
		locals = new HashMap<Integer, Object>();
	}

	public static InterpreterContext getInstance() {
		if (instance != null) {
			return instance;
		} else {
			InterpreterContext.instance = new InterpreterContext();
			return instance;
		}
	}

	public Object pushToStack(Object obj) {
		return stack.push(obj);
	}

	public Object popFromStack() {
		return stack.pop();
	}
	
	public boolean isStackEmpty(){
		return stack.isEmpty();
	}

	/**
	 * 
	 * @param varIndex
	 * @param a
	 * @return the element previously at the specified position
	 */
	public Object istoreVar(int varIndex, Object a) {
		return locals.put(varIndex, a);
	}
	
	public Object iloadVar(int varIndex){
		return locals.get(varIndex);
	}

	public String toString(){
		String partA = "STACK:\n" + this.stack.toString() + "\n";
		String partB = "LOCALS:\n" + this.locals.toString() + "\n";
		return partA + partB;
	}
}
