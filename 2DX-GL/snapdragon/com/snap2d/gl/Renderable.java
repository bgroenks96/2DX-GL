/*
 *  Copyright © 2011-2012 Brian Groenke
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

/**
 * Defines a standard for objects that are rendered on screen and called on each tick
 * of the game loop.
 * @author Brian Groenke
 *
 */
public interface Renderable {

	public void render(Graphics2D g, float interpolation);
	
	public void update(long nanoTimeNow, long nanosSinceLastUpdate);

	public void onResize(Dimension oldSize, Dimension newSize);
}
