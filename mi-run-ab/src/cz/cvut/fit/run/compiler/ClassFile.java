package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ClassFile {

	private List<String> flags; // (private, static, volatile, ...)
	// constant pool
	private String thisClass;
	private String superClass;
	private List<String> interfaces;
	private List<MethodInfo> methods;
	private List<FieldInfo> fields;

	public ClassFile(String name) {
		this.thisClass = name;
		this.flags = new ArrayList<String>();
		this.interfaces = new ArrayList<String>();
		this.fields = new ArrayList<FieldInfo>();
		this.methods = new ArrayList<MethodInfo>();
	}

	public Object addField(FieldInfo field) {
		return this.fields.add(field);
	}

	public boolean addMethod(MethodInfo m) {
		return this.methods.add(m);
	}

	public List<FieldInfo> getFields() {
		return fields;
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

	public MethodInfo getMethod(String name) throws NoSuchElementException {
		for (MethodInfo m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		throw new NoSuchElementException("No method " + name + " in " + methods);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String flag : flags) {
			builder.append(flag).append(" ");
		}
		builder.append("classFile ");
		builder.append(thisClass);
		if (superClass != null) {
			builder.append(", super=");
			builder.append(superClass);
		}
		if (!interfaces.isEmpty()) {
			builder.append(", interfaces=");
			builder.append(interfaces);
		}
		builder.append("\n");
		for (FieldInfo field : fields) {
			builder.append(field);
		}
		for (MethodInfo m : methods) {
			builder.append("\n");
			builder.append(m);
		}
		builder.append("--- end class ");
		builder.append(thisClass);
		builder.append(" ---\n");
		return builder.toString();
	}
}
