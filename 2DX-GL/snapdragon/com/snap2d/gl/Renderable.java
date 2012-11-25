package com.snap2d.gl;

import java.awt.Dimension;

public interface Renderable {

	public void render();

	public void onResize(Dimension oldSize, Dimension newSize);
}
