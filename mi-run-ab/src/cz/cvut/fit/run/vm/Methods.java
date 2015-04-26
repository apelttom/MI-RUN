package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;

public class Methods {

	private static final String excTypes =
			"Two objects from the top of the stack are not Integers.";

	public Object addition() throws InvalidObjectException {
		InterpreterContext context = InterpreterContext.getInstance();
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a + (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(excTypes);
		}
	}

	public Object subtraction() throws InvalidObjectException {
		InterpreterContext context = InterpreterContext.getInstance();
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a - (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(excTypes);
		}
	}

	public Object multiplication() throws InvalidObjectException {
		InterpreterContext context = InterpreterContext.getInstance();
		Object a = context.popFromStack();
		Object b = context.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a * (Integer) b;
			context.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(excTypes);
		}
	}
}
