package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.fit.run.compiler.ClassFile;

public class ABObject {

	private ClassFile classfile;
	private List<ABClassVar> globals;

	public ABObject(ClassFile classfile) {
		this.classfile = classfile;
		this.globals = new ArrayList<ABClassVar>();
	}

	public ABObject(ClassFile classfile, List<ABClassVar> globals) {
		this.classfile = classfile;
		this.globals = globals;
	}

	public ClassFile getClassfile() {
		return classfile;
	}

	public List<ABClassVar> getGlobals() {
		return globals;
	}

	public void addGlobal(ABClassVar variable) {
		globals.add(variable);
	}

	public void changeVariableValue(Integer index, Object newValue) {
		ABClassVar prevVar = globals.get(index);
		// verify that the types are compatible
		ABClassVar newVar = new ABClassVar(prevVar.getName(),
				prevVar.getVariableProtection(), prevVar.isStatic(), newValue,
				prevVar.getVariableType());
		globals.set(index, newVar);
	}

	public String toString() {
		return "Dynamic object of " + classfile.getThis();
	}
}
