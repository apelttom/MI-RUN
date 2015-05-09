package cz.cvut.fit.run;

public class ABCode {

	public void main() {
		int n = 3;
		ABCode object = new ABCode(1+1, n);
	}
	
	public final int factorial(int n){
		if (n == 0) {
			return 1;
		}
		return n * factorial(n - 1);
	}

}
