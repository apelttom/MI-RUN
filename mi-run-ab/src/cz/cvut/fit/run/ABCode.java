package cz.cvut.fit.run;

public class ABCode {

	int b;

	public int main() {
		int c = 2;
		b = c * 2;
		Factorial f = new Factorial();
		int result = f.factorial(b);
		return result;
	}
}

public class Factorial {

	public int factorial(int n) {
		if (n == 0) {
			return 1;
		}
		int result = n * factorial(n - 1);
		return result;
	}
}