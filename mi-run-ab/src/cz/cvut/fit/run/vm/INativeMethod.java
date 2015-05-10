package cz.cvut.fit.run.vm;

public interface INativeMethod {
	public String getName();
	public void setName(String name);
	public int getArgs();
	public void setArgs(int count);
	public void execute(Object... args);
}
