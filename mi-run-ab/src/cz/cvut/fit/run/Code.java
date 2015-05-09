package cz.cvut.fit.run;

public class Code {

	public void main() {
		int a = 10;
		int b = myMethod(a);
	}
	
	public final int myMethod(int i){
		int five = 5;
		return five-i;
	}

}
