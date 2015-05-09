package cz.cvut.fit.run;

public class Code {

	public void main() {
		int result = factorial(10);
	}
	
	public final int factorial(int n){
		if (n == 0) {
			return 1;
		}
		return n * factorial(n - 1);
	}

}
