package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ClassFile {

	private List<String> flags; // (private, static, volatile, ...)
	// constant pool
	private String thisClass;
	private String superClass;
	private List<String> interfaces;
	// fields
	private List<MethodInfo> methods = null;
	// attributes
	private Map<Integer, Object> attributes = null;
	
	public ClassFile() {
		this.methods = new ArrayList<MethodInfo>();
		this.attributes = new HashMap<Integer, Object>();
		this.flags = new ArrayList<String>();
		this.interfaces = new ArrayList<String>();
	}
	
	public Object addAttribute(Integer index, Object attribute){
		return this.attributes.put(index, attribute);
	}

	public boolean addMethod(MethodInfo m) {
		return this.methods.add(m);
	}

	public Map<Integer, Object> getAttributes() {
		return attributes;
	}

	public List<String> getFlags() {
		return flags;
	}

	public List<String> getInterfaces() {
		return interfaces;
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

	public List<MethodInfo> getMethods() {
		return this.methods;
	}

	public String getSuper() {
		return superClass;
	}

	public String getThis() {
		return thisClass;
	}
	
	public void addFlag(String flag) {
		this.flags.add(flag);
	}
	
	public void addInterface(String iface) {
		this.interfaces.add(iface);
	}

	public void setSuper(String superClass) {
		this.superClass = superClass;
	}

	public void setThis(String thisClass) {
		this.thisClass = thisClass;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClassFile [flags=");
		builder.append(flags);
		builder.append(", thisClass=");
		builder.append(thisClass);
		builder.append(", superClass=");
		builder.append(superClass);
		builder.append(", interfaces=");
		builder.append(interfaces);
		builder.append(", methods=");
		builder.append(methods);
		builder.append(", attributes=");
		builder.append(attributes);
		builder.append("]");
		return builder.toString();
	}
}
