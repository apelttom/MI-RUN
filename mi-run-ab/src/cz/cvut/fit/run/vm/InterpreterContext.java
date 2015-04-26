package cz.cvut.fit.run.vm;

import java.util.Stack;

public class InterpreterContext {

	private static InterpreterContext instance = null;
	private Stack<Object> stack;
	
	private InterpreterContext(){
		stack = new Stack<Object>();
	}

	public static InterpreterContext getInstance() {
		if (instance != null) {
			return instance;
		} else {
			InterpreterContext.instance = new InterpreterContext();
			return instance;
		}
	}
	
	public Object pushToStack(Object obj){
		return stack.push(obj);
	}

	public Object popFromStack() {
		return stack.pop();
	}
	
	
}
