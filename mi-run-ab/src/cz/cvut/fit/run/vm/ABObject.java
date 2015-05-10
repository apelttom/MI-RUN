package cz.cvut.fit.run.vm;

import java.lang.instrument.IllegalClassFormatException;
import java.util.HashMap;
import java.util.Map;

import cz.cvut.fit.run.compiler.ClassFile;

public class ABObject {

	private ClassFile classfile;
	private Map<String, ABClassVar> globals;

	public ABObject(ClassFile classfile) {
		this.classfile = classfile;
		this.globals = new HashMap<String, ABClassVar>();
	}

	public ABObject(ClassFile classfile, Map<String, ABClassVar> globals) {
		this.classfile = classfile;
		this.globals = globals;
	}

	public ClassFile getClassfile() {
		return classfile;
	}

	public void addGlobal(String name, ABClassVar variable) {
		globals.put(name, variable);
	}

	public void changeVariableValue(String name, ABClassVar variable)
			throws IllegalClassFormatException {
		ABClassVar prevVar = globals.get(name);
		// verify that the types are compatible
		if (variable.getVariableType() != prevVar.getVariableType()) {
			throw new IllegalClassFormatException("Global variable "
					+ name + " is of type " + prevVar.getVariableType()
					+ "! Incompatible with type " + variable.getVariableType());
		}
		globals.put(name, variable);
	}
	
	public String toString(){
		return "Dynamic object of "+classfile.getThis();
	}
}
