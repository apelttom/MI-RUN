package cz.cvut.fit.run;

public class Code {

	public void main() {
		int result = factorial(3);
	}
	
	public final int factorial(int n){
		if (n == 0) {
			return 1;
		}
		int result = n * factorial(n - 1);
		return result;
	}

}
