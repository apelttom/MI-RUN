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
	private List<MiniJavaMethod> methods = null;
	// attributes

	public ClassFile() {
		this.methods = new ArrayList<MiniJavaMethod>();
	}

	public boolean addMethod(MiniJavaMethod m) {
		return this.methods.add(m);
	}
	
	public List<MiniJavaMethod> getMethods() {
		return this.methods;
	}

	public MiniJavaMethod getMethod(int index) {
		return this.methods.get(index);
	}

	public MiniJavaMethod getMethod(String name) throws Exception {
		for (MiniJavaMethod m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		throw new NoSuchElementException("No method "+name+" in "+methods);
	}
}
