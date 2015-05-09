package cz.cvut.fit.run.vm;

import java.util.ArrayList;
import java.util.List;

/*
 * Creates frames for invoked methods
 */
public class FrameFactory {

	private int frameCounter = 0;
	private List<Frame> frames = new ArrayList<Frame>();

	public Frame makeFrame() {
		Frame result = new Frame(this.frameCounter);
		this.frameCounter++;
		frames.add(result);
		return result;
	}

	public void print() {
		StringBuilder sb = new StringBuilder();
		for (Frame f : frames) {
			sb.append(f.toString());
		}
		System.out.println(sb.toString());
	}
}
