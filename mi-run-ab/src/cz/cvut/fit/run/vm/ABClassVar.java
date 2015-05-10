package cz.cvut.fit.run.vm;

public class ABClassVar {

	// private, public, protected, etc.
	private String variableProtection;
	private boolean isStatic;
	private Object variableValue;
	// String, Integer, custom, etc.
	private String variableType;

	public ABClassVar(String prot, Object val, String type) {
		this.variableProtection = prot;
		this.variableValue = val;
		this.variableType = type;
	}

	public ABClassVar(Object val, String type) {
		this.variableValue = val;
		this.variableType = type;
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
