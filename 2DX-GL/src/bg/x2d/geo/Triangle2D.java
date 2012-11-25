/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;

import bg.x2d.Background;

public class Triangle2D extends Shapes2D {

	@SuppressWarnings("deprecation")
	public Triangle2D(Graphics g, Background b) {
		super(g, b);
	}

	public Triangle2D(int x, int y, int size, Paint p, boolean fill) {
		super(x, y, size, p, fill);
	}

	@Deprecated
	/**
	 * Draws the given triangle to the canvas.
	 * 
	 * @param x
	 *            The x coordinate of the triangle.
	 * @param y
	 *            The y coordinate of the triangle.
	 * @param size
	 *            The height/width of the triangle.
	 * @param p
	 *            The Paint object that should be applied to the triangle. This
	 *            can be a Color, Gradient or any other object that subclasses
	 *            Paint.
	 * @param fill
	 *            If <code>true</code> the triangle will be filled. Otherwise,
	 *            an outline will be drawn.
	 */
	public void drawTriangle(int x, int y, int size, Paint p, boolean fill) {

		Polygon tri = drawRegularPolygon(new Point(x, y), new Point(x
				+ (size / 2), y), size, p, fill, true, 3);

		shape = tri;
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
	}

	/**
	 * Draws a rotated version of the shape on screen. The properties of the
	 * shape must have been set either by having been drawn or calling the
	 * <code>setProperties(...)</code> method. Else, a GeoException will be
	 * thrown. <br>
	 * <br>
	 * Note: The user is responsible for clearing any previously drawn figure.
	 * This method will draw the rotated shape on screen using the currently set
	 * properties. It does not make any attempt to clear the screen or the last
	 * shape drawn.
	 * 
	 * @param degrees
	 *            the number of degrees
	 */
	@Override
	public void rotate(double degrees, Rotation type) {
		if (degrees < 0) {
			throw (new IllegalArgumentException(
					"Illegal theta value: specify a positive integer for degree of rotation"));
		}
		if (type == null) {
			throw (new IllegalArgumentException(
					"passed Rotation type cannot be null"));
		}
		if (shape == null) {
			try {
				throw (new GeoException(
						"shape must have been drawn or have set properties before a rotation can be performed"));
			} catch (GeoException e) {
				e.printStackTrace();
			}
		}
		if (type == Rotation.COUNTER_CLOCKWISE) {
			double deg = -degrees;
			rotate(3, deg);
		} else if (type == Rotation.CLOCKWISE) {
			rotate(3, degrees);
		}
	}

	@Override
	public void setProperties(int x, int y, int size, Paint p, boolean fill) {
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
		shape = drawRegularPolygon(new Point(x, y),
				new Point(x + (size / 2), y), size, p, fill, false, 3);
	}
}
