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

package bg.x2d.geo;

import java.awt.*;

/**
 * Represents a line segment in 2D space.  By standard definition of a line segment,
 * LineSeg has definite bounds between its start/end points.
 * @author Brian Groenke
 *
 */
public class LineSeg {

	private double x1, y1, x2, y2;

	/**
	 * @param p1 first point
	 * @param p2 second point
	 */
	LineSeg(Point p1, Point p2) {
		this.x1 = p1.getX();
		this.y1 = p1.getY();
		this.x2 = p2.getX();
		this.y2 = p2.getY();
	}

	/**
	 * Tests to see if the given point lies on the line segment using point-slope:
	 * y-y1 = m(x - x1) and the segment bounds.
	 * @param x
	 * @param y
	 * @return
	 */
	boolean hasPoint(double x, double y) {
		boolean inBounds = x <= Math.max(x1, x2) && x >= Math.min(x1, x2) &&
				y <= Math.max(y1, y2) && y >= Math.min(y1, y2);
		boolean inLine = y - y1 == ((y2 - y1) / (x2 - x1)) * (x - x1);
		return inBounds && inLine;
	}

	@Override
	public String toString() {
		return "[("+x1+", "+y1+"), ("+x2+", "+y2+")]";
	}
}
