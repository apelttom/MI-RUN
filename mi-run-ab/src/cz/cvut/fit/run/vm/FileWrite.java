package cz.cvut.fit.run.vm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileWrite implements INativeMethod{

	private String name;
	private int args;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getArgs() {
		return args;
	}

	@Override
	public void setArgs(int count) {
		args = count;
	}

	@Override
	public void execute(Object... args) {
		try {
			File f = new File(args[0].toString());
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(args[1].toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
