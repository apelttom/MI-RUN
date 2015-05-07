package cz.cvut.fit.run.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/*
 * Creates frames for invoked methods
 */
public class FrameFactory {

	private int frameCounter = 0;

	public Frame makeFrame() {
		Frame result = new Frame(this.frameCounter);
		this.frameCounter++;
		return result;
	}

}
