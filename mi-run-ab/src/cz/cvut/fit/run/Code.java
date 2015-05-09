package cz.cvut.fit.run;

public class Code {

	public void main() {
		int a = 1;
		int b = myMethod(a, 2);
	}
	
	public final int myMethod(int i, int j){
		int c = 3;
		return c-i;
	}

}
