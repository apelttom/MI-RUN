package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;

public class ClassFile {

	private List<MiniJavaMethod> methods = null;

	public ClassFile() {
		this.methods = new ArrayList<MiniJavaMethod>();
	}

	public boolean addMethod(MiniJavaMethod m) {
		return this.methods.add(m);
	}

	public MiniJavaMethod getMethod(int index) {
		return this.methods.get(index);
	}
}
