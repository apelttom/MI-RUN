package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private Map<Integer, Object> attributes = null;

	public ClassFile() {
		this.methods = new ArrayList<MethodInfo>();
		this.attributes = new HashMap<Integer, Object>();
	}

	public Map<Integer, Object> getAttributes() {
		return attributes;
	}

	public List<MethodInfo> getMethods() {
		return this.methods;
	}
	
	public Object addAttribute(Integer index, Object attribute){
		return this.attributes.put(index, attribute);
	}
	
	public boolean addMethod(MethodInfo m) {
		return this.methods.add(m);
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
