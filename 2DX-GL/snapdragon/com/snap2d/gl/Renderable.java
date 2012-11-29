package com.snap2d.gl;

import java.awt.*;

public interface Renderable {

	public void render(RenderControl rc, float interpolation);
	
	public void update(long nanosSinceLastUpdate);

	public void onResize(Dimension oldSize, Dimension newSize);
}
