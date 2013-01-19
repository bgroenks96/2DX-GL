/*
 * Copyright � 2011-2012 Brian Groenke
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

import java.awt.*;
import java.awt.geom.*;

/**
 * Point Long-Double.  Implementation of a 2D point that stores its value as a double and allows
 * quick access also as a rounded int or long.  Qualifies polymorphically as a Point or Point2D, so
 * it can often be passed to Java2D functions (including AWT/Swing).
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class PointLD extends Point {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6891695259903881318L;

	private double x, y;

	public PointLD(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public PointLD(long x, long y) {
		this.x = x;
		this.y = y;
	}

	public PointLD(Point2D p2d) {
		x = p2d.getX();
		y = p2d.getY();
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
	
	@Override
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void setLocation(Point2D p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	public long getLongX() {
		return Math.round(x);
	}

	public long getLongY() {
		return Math.round(y);
	}

	public int getIntX() {
		return (int) Math.round(x);
	}

	public int getIntY() {
		return (int) Math.round(y);
	}
	
	public Point2D.Double getDoublePoint() {
		return new Point2D.Double(x, y);
	}
	
	public Point2D.Float getFloatPoint() {
		return new Point2D.Float((float)x, (float)y);
	}

	@Override
	public String toString() {
		return new String("[" + x + ", " + y + "]");
	}
}
