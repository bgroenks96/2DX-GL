package com.snap2d.gl;

import java.awt.Dimension;

public interface Renderable {

	public void render(RenderControl rc);

	public void onResize(Dimension oldSize, Dimension newSize);
}
