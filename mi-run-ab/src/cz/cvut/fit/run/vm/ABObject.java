package cz.cvut.fit.run.vm;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Map;

public class ABObject {

	private ClassFile classfile;
	private Map<Integer, ABClassVar> globals;

	public ABObject(ClassFile classfile) {
		this.classfile = classfile;
	}

	public ABObject(ClassFile classfile, Map<Integer, ABClassVar> globals) {
		this.classfile = classfile;
		this.globals = globals;
	}

	public ClassFile getClassfile() {
		return classfile;
	}

	public void addGlobal(Integer index, ABClassVar variable) {
		globals.put(index, variable);
	}

	public void changeVariableValue(Integer index, ABClassVar variable)
			throws IllegalClassFormatException {
		ABClassVar prevVar = globals.get(index);
		// verify that the types are compatible
		if (variable.getVariableType() != prevVar.getVariableType()) {
			throw new IllegalClassFormatException("Global variable on index "
					+ index + " is of type " + prevVar.getVariableType()
					+ "! Incompatible with type " + variable.getVariableType());
		}
		globals.put(index, variable);
	}
}
