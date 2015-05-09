package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ClassFile {

	// flags (private, static, volatile, ...)
	// constant pool
	// this class
	// super class
	// interfaces
	// fields
	private List<MethodInfo> methods = null;
	// attributes

	public ClassFile() {
		this.methods = new ArrayList<MethodInfo>();
	}

	public boolean addMethod(MethodInfo m) {
		return this.methods.add(m);
	}
	
	public List<MethodInfo> getMethods() {
		return this.methods;
	}

	public MethodInfo getMethod(int index) {
		return this.methods.get(index);
	}

	public MethodInfo getMethod(String name) throws Exception {
		for (MethodInfo m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		throw new NoSuchElementException("No method "+name+" in "+methods);
	}
}
