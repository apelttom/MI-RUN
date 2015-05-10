package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;

public class Natives {
	private List<INativeMethod> natives;

	public INativeMethod get(String name) {
		for (INativeMethod method : natives) {
			if (name.equals(method.getName())) {
				return method;
			}
		}
		return null;
	}

	public Natives() {
		natives = new ArrayList<INativeMethod>();
		FileWrite fw = new FileWrite();
		fw.setName("fileWrite");
		fw.setArgs(2);
		natives.add(fw);
	}

	public boolean contains(String name) {
		return get(name) != null;
	}
	
	public int getNumberOfArgs(String name) {
		return contains(name) ? get(name).getArgs() : -1;
	}

	public void invoke(String name, Object... args) {
		INativeMethod method = get(name);
		if (method != null) {
			method.execute(args);
		}
	}
}
