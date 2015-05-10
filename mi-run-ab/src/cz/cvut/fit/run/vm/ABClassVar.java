package cz.cvut.fit.run.vm;

public class ABClassVar {

	private String name;
	// private, public, protected, etc.
	private String variableProtection;
	private boolean isStatic = false;
	private Object variableValue;
	// String, Integer, custom, etc.
	private String variableType;

	public ABClassVar(String name, String prot, boolean isStatic, Object val,
			String type) {
		this.name = name;
		this.variableProtection = prot;
		this.isStatic = isStatic;
		this.variableValue = val;
		this.variableType = type;
	}

	public ABClassVar(String name, Object val, String type) {
		this.name = name;
		this.variableValue = val;
		this.variableType = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVariableProtection() {
		return variableProtection;
	}

	public void setVariableProtection(String variableProtection) {
		this.variableProtection = variableProtection;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public Object getVariableValue() {
		return variableValue;
	}

	public void setVariableValue(Object variableValue) {
		this.variableValue = variableValue;
	}

	public String getVariableType() {
		return variableType;
	}

	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}

}
