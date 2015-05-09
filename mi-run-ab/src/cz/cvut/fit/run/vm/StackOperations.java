package cz.cvut.fit.run.vm;

import java.io.InvalidObjectException;

/*
 * Is Singleton
 */
public class StackOperations {

	private static final String invTypesArithm = "Two objects from the top of the stack are not Integers.";
	private static final String invTypeiStore = "Object from the top of the stack is not an Integer.";
	private static final String invTypeiLoad = "Loaded object is not an Integer.";

	private static StackOperations instance = null;

	private StackOperations() {
	}

	public static StackOperations getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new StackOperations();
			return instance;
		}
	}

	public Object iaddition(Frame frame) throws InvalidObjectException {
		Object a = frame.popFromStack();
		Object b = frame.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a + (Integer) b;
			frame.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public Object isubtraction(Frame frame) throws InvalidObjectException {
		Object a = frame.popFromStack();
		Object b = frame.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a - (Integer) b;
			frame.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public Object imultiplication(Frame frame) throws InvalidObjectException {
		Object a = frame.popFromStack();
		Object b = frame.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			int res = (Integer) a * (Integer) b;
			frame.pushToStack(res);
			return res;
		} else {
			throw new InvalidObjectException(invTypesArithm);
		}
	}

	public void istoreVar(Frame frame, int varIndex)
			throws InvalidObjectException {
		Object a = frame.popFromStack();
		if (a instanceof Integer) {
			frame.istoreVar(varIndex, a);
		} else {
			throw new InvalidObjectException(invTypeiStore);
		}
	}

	public void iloadVar(Frame frame, int varIndex)
			throws InvalidObjectException {
		Object a = frame.iloadVar(varIndex);
		if (a instanceof Integer) {
			frame.pushToStack(a);
		} else {
			throw new InvalidObjectException(invTypeiLoad);
		}
	}

	public boolean iequal(Frame frame) throws InvalidObjectException {
		Object a = frame.popFromStack();
		Object b = frame.popFromStack();
		if ((a instanceof Integer) && (b instanceof Integer)) {
			return ((Integer) a == (Integer) b);
		}
		throw new InvalidObjectException(invTypesArithm);
	}

	public boolean ilesser(Frame frame) throws InvalidObjectException {
		Object value1 = frame.popFromStack();
		Object value2 = frame.popFromStack();
		if ((value1 instanceof Integer) && (value2 instanceof Integer)) {
			return ((Integer) value2 < (Integer) value1);
		}
		throw new InvalidObjectException(invTypesArithm);
	}

	public boolean igreater(Frame frame) throws InvalidObjectException {
		Object value1 = frame.popFromStack();
		Object value2 = frame.popFromStack();
		if ((value1 instanceof Integer) && (value2 instanceof Integer)) {
			return ((Integer) value2 > (Integer) value1);
		}
		throw new InvalidObjectException(invTypesArithm);
	}

	public void incVar(Frame frame, int index, int n)
			throws InvalidObjectException {
		Object a = frame.iloadVar(index);
		if (a instanceof Integer) {
			frame.pushToStack((Integer) a + n);
			istoreVar(frame, index);
		} else {
			throw new InvalidObjectException(invTypeiLoad);
		}
	}

}
