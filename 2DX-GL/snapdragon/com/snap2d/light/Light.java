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

import java.awt.*;

import bg.x2d.math.*;
import bg.x2d.utils.*;

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

	private int dist;
	private int[] colors;

	/**
	 * 
	 * @param x
	 *            x location of the light source
	 * @param y
	 *            y location of the light source
	 * @param dist
	 *            the
	 * @param lightColor
	 *            ARGB 32-bit integer color value to use for the light
	 */
	public Light(int x, int y, int dist, int lightColor) {
		this.x = x;
		this.y = y;
		this.dist = dist;
		colors = ColorUtils.unpackInt(lightColor, ColorUtils.TYPE_ARGB);
	}

	public int getLinearDistance() {
		return dist;
	}

	public int[] getColor() {
		return colors;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	/**
	 * Computes luminosity as a fraction that can be multiplied against 255 to get the resulting
	 * shadow alpha value (0.0 = total light/no shadow - 1.0 = total darkness). The luminosity
	 * fraction should be independent of world/screen space differences. The default implementation
	 * uses magnitudes to calculate luminosity, so the calculated light values will map a radial
	 * pattern.
	 * 
	 * @param x
	 *            the x location to measure brightness for (should be in same coordinate space as
	 *            light)
	 * @param y
	 *            the y location to measure brightness for (should be in same coordinate space as
	 *            light)
	 * @return the luminosity fraction measuring shadow alpha
	 */
	public double computeLuminosity(int x, int y) {
		double mag = Math.sqrt(DoubleMath.pow((x - this.x), 2)
				+ DoubleMath.pow((y - this.y), 2));
		double luminosity = Math.min(1.0, mag / dist);
		return luminosity;
	}

	public abstract boolean contains(int x, int y);
}
