package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;

public class Methods {

	private static final String invTypesArithm =
			"Two objects from the top of the stack are not Integers.";
	private static final String invTypeiStore =
			"Object from the top of the stack is not an Integer.";
	private static final String invTypeiLoad =
			"Loaded object is not an Integer.";
	
	private InterpreterContext context = null;
	
	public Methods(){
		this.context = InterpreterContext.getInstance();
	}

	public Object iaddition() throws InvalidObjectException {
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a + (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public Object isubtraction() throws InvalidObjectException {
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a - (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public Object imultiplication() throws InvalidObjectException {
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a * (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public void istoreVar(int varIndex) throws InvalidObjectException {
		Object a = context.popFromStack();
		if (a instanceof Integer){
			context.istoreVar(varIndex, (Integer) a);
		} else {
			throw new InvalidObjectException(invTypeiStore);
		}
	}

	public void iloadVar(int varIndex) throws InvalidObjectException {
		Object a = context.iloadVar(varIndex);
		if (a instanceof Integer){
			context.pushToStack((Integer) a);
		} else {
			throw new InvalidObjectException(invTypeiLoad);
		}
	}

}
