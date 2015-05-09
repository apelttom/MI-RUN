package cz.cvut.fit.run;

public class ABCode {

	public void main() {
		int result = factorial(12);
	}
	
	public final int factorial(int n){
		if (n == 0) {
			return 1;
		}
		return n * factorial(n - 1);
	}

}
