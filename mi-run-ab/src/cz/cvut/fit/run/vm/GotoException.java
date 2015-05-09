package cz.cvut.fit.run.vm;

@SuppressWarnings("serial")
public class GotoException extends RuntimeException {
	
	private final String go_to = "bytecode instruction GOTO ";
	private int jumpToPC = -1;

	public GotoException(int jumpToPC){
		super();
		this.jumpToPC = jumpToPC;
	}
	
	public GotoException(String message, int jumpToPC){
		super(message);
		this.jumpToPC = jumpToPC;
	}

	public int getJumpToPC() {
		return jumpToPC;
	}

	public void setJumpToPC(int jumpToPC) {
		this.jumpToPC = jumpToPC;
	}
	
	@Override
	public String toString(){
		return go_to + jumpToPC;
	}
}
