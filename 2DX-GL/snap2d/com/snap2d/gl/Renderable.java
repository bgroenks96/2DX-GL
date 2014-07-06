/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl;

import java.awt.*;

import com.snap2d.gl.spi.RenderableSpi;

/**
 * Defines a standard for objects that are rendered on screen and called on each tick of the game
 * loop.
 * 
 * @author Brian Groenke
 * 
 */
public interface Renderable extends RenderableSpi {

	/**
	 * Called when the engine is rendering a new frame.
	 * 
	 * @param g
	 *            the Graphics object to draw onto.
	 * @param interpolation
	 *            the interpolation amount based on time since <code>update</code> was last called.
	 */
	public void render(Graphics2D g, float interpolation);

	/**
	 * Called on each tick of the game loop. This method is where logic and position updates should
	 * take place.
	 * 
	 * @param nanoTimeNow the fixed nano time at the start of the update cycle
	 * @param nanoTimeLast  the fixed nano time of the last update cycle
	 */
	public void update(long nanoTimeNow, long nanoTimeLast);

	/**
	 * Called when the Graphics context invoking the Renderable has been resized, allowing the
	 * Renderable object to perform necessary scaling.
	 * 
	 * @param oldSize
	 *            the <b>original</b> size of the context (may be null if not yet available).
	 * @param newSize
	 *            the new size of the grahpics context. This should never be null.
	 */
	public void onResize(Dimension oldSize, Dimension newSize);
}
