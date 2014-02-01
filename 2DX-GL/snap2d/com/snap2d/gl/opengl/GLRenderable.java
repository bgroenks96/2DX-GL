/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.opengl;


/**
 * Rendering interface for the Snap2D OpenGL pipeline.  Any Object that should
 * be drawn or updated per frame/tick must implement this interface and register with
 * an active {@link #GLRenderControl}.
 * @author Brian Groenke
 *
 */
public interface GLRenderable {
	
	/**
	 * Called during initialization and/or recreation of the current rendering environment.
	 * This method will be called as soon as a GLRenderControl has added the GLRenderable to its
	 * rendering queue.
	 * @param handle
	 */
	public void init(GLHandle handle);
	
	/**
	 * Called when the current GL rendering context is being destroyed.  GLRenderable implementations should
	 * release any resources held both in and out of the OpenGL system.
	 * @param handle
	 * @return
	 */
	public void dispose(GLHandle handle);
	
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
	 * @param nanoTimeNow the fixed nano time at the start of the update cycle
	 * @param nanoTimeLast the fixed nano time of the last update
	 */
	public void update(long nanoTimeNow, long nanoTimeLast);

	/**
	 * Called when the OpenGL context invoking the GLRenderable has been resized, allowing the
	 * GLRenderable object to perform necessary scaling.  This method will be called after the
	 * {@link #init(GLHandle)} method if and only if the GLRenderControl is being displayed in
	 * a valid window upon the GLRenderable being added to a rendering queue.
	 * @param the GLHandle to the GL context
	 * @param wt the total width of the current rendering context in screen space
	 * @param ht the total height of the current rendering context in screen space
	 */
	public void resize(GLHandle handle, int wt, int ht);
}
