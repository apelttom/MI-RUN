package cz.cvut.fit.run.vm;

import cz.cvut.fit.run.compiler.ByteCode;

public class MiniJavaMethod {

	private ByteCode bytecode = null;
	private String name = "undefined";
	
	public MiniJavaMethod(String name){
		this.name = name;
		this.bytecode = new ByteCode();
	}

	public ByteCode getBytecode() {
		return bytecode;
	}

	public void setBytecode(ByteCode bytecode) {
		this.bytecode = bytecode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		String result = "Method "+this.name+":\n";
		for (int PC = 0; PC <= this.bytecode.size() - 1; PC++) {
			result += PC + ": " + this.bytecode.get(PC).toString()+"\n";
		}
		return result;
	}
	
}
