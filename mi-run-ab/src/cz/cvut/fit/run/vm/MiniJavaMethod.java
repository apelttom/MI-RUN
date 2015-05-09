package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.fit.run.compiler.ByteCode;

public class MiniJavaMethod {

	private List<String> flags; //(private, static, volatile, ...)
	private String name = "undefined";
	// descriptor
	// attributes (anotation, source code, exceptions, bytecode)
	private List<String> argTypes;
	private String returnType;
	private ByteCode bytecode = null;

	public MiniJavaMethod(String name){
		this.name = name;
		this.bytecode = new ByteCode();
		this.argTypes = new ArrayList<String>();
		this.flags = new ArrayList<String>();
	}

	public List<String> getArgTypes() {
		return argTypes;
	}

	public ByteCode getBytecode() {
		return bytecode;
	}

	public List<String> getFlags() {
		return flags;
	}

	public String getName() {
		return name;
	}
	public String getReturnType() {
		return returnType;
	}
	
	public void addArgType(String argType) {
		this.argTypes.add(argType);
	}

	public void setBytecode(ByteCode bytecode) {
		this.bytecode = bytecode;
	}

	public void addFlag(String flag) {
		this.flags.add(flag);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String flag : flags) {
			builder.append(flag).append(" ");
		}
		builder.append(name);
		builder.append(" (");
		builder.append(argTypes.size());
		builder.append(" args ");
		builder.append(argTypes);
		builder.append(", return ");
		// TODO locals
		builder.append(returnType);
		builder.append(")\n");
		for (int PC = 0; PC <= this.bytecode.size() - 1; PC++) {
			builder.append(PC).append(": ").append(this.bytecode.get(PC)).append("\n");
		}
		return builder.toString();
	}
}
