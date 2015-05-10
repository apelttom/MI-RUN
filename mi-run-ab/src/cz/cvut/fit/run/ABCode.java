package cz.cvut.fit.run;

public class ABCode {
	int a;

	public void main() {
		ABCode o = new ABCode();
		o.foo(a);
	}

	public int foo(int i) {
		Factorial f = new Factorial();
		return f.count();
	}
}

class Factorial {
	int n;

	public Factorial() {
		n = 5;
	}

	public int count() {
		return count2(n);
	}

	public int count2(int n) {
		if (n == 0) {
			return 1;
		}
		return n * count2(n - 1);
	}
}