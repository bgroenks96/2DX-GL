/*
 *  Copyright © 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl.light;


/**
 * Abstract implementation of 2D light. Light uses double values to store distance and location data
 * but can be initialized using world or screen space coordinates. However, the distance and
 * location, as well as any values passed into Light for calculations, should refer to the same
 * coordinate space in which Light was created.
 * 
 * @author Brian Groenke
 * 
 */
public abstract class Light {

	protected int x, y;

	public abstract boolean contains(int x, int y);
}
