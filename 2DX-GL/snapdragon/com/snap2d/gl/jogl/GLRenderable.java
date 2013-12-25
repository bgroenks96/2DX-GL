/*
 *  Copyright � 2012-2013 Brian Groenke
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


/**
 * @author Brian Groenke
 *
 */
public interface GLRenderable {
	
	/**
	 * Called during initialization and/or recreation of the current rendering environment.
	 * This method will always be called at least once for every GLRenderable registered with an active
	 * GLRenderControl.
	 * @param handle
	 */
	public void init(GLHandle handle);
	
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
	 */
	public void onResize(GLHandle handle, int wt, int ht);
}
