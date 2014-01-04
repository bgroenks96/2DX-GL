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

package com.snap2d.light;

import java.awt.geom.*;

/**
 * @author Brian Groenke
 * 
 */
public class RadialLight extends Light {

	Ellipse2D geom;

	/**
	 * @param x
	 * @param y
	 * @param dist
	 * @param lightColor
	 * @param lightAlpha
	 */
	public RadialLight(int x, int y, int dist, int lightColor) {
		super(x, y, dist, lightColor);
		geom = new Ellipse2D.Double(x - dist, y - dist, dist * 2, dist * 2);
	}

	/**
	 *
	 */
	@Override
	public boolean contains(int x, int y) {
		return geom.contains(x, y);
	}
}
