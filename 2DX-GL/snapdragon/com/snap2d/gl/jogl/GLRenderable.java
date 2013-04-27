/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl;

import java.awt.*;

/**
 * @author Brian Groenke
 *
 */
public interface GLRenderable {
	
	/**
	 * Called on each frame by the rendering engine.  The receiving GLRenderable can use the
	 * GLHandle to draw to the GL context.
	 * @param handle
	 * @param interpolation
	 */
	public void render(GLHandle handle, float interpolation);
	
	/**
	 * Called on each tick of the game loop. This method is where logic and position updates should
	 * take place.
	 * 
	 * @param nanoTimeNow
	 * @param nanosSinceLastUpdate
	 */
	public void update(long nanoTimeNow, long nanosSinceLastUpdate);

	/**
	 * Called when the OpenGL context invoking the GLRenderable has been resized, allowing the
	 * GLRenderable object to perform necessary scaling.
	 * 
	 * @param oldSize
	 *            the <b>original</b> size of the context (may be null if not yet available).
	 * @param newSize
	 *            the new size of the grahpics context. This should never be null.
	 */
	public void onResize(Dimension oldSize, Dimension newSize);
}
