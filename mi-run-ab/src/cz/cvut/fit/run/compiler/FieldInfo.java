package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.List;

public class FieldInfo {
	public List<String> flags;
	public String name;
	public String type;

	public FieldInfo(String name, String type) {
		super();
		this.name = name;
		this.type = type;
		flags = new ArrayList<String>(1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!flags.isEmpty()) {
			builder.append(flags);
			builder.append(" ");
		}
		builder.append(type);
		builder.append(" ");
		builder.append(name);
		builder.append(";\n");
		return builder.toString();
	}
}
