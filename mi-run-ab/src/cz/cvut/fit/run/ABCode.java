package cz.cvut.fit.run;

public class ABCode {
	
	int a;
	int b;
	int foo;

	public void main() {
		a = 0;
		ABCode o = new ABCode();
		o.foo(a);

	}
	
	public int foo(int i) {
		return i;
	}
}
