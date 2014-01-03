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

package bg.x2d.geo;

import java.awt.*;
import java.awt.geom.*;

/**
 * Point Long-Double. Implementation of a 2D point that stores its value as a double and allows
 * quick access also as a rounded int or long. Qualifies polymorphically as a Point or Point2D, so
 * it can often be passed to Java2D functions (including AWT/Swing).
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class PointLD extends Point {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6891695259903881318L;

	public double dx, dy;

	public PointLD(double x, double y) {
		this.dx = x;
		this.dy = y;
	}

	public PointLD(long x, long y) {
		this.dx = x;
		this.dy = y;
	}

	public PointLD(Point2D p2d) {
		dx = p2d.getX();
		dy = p2d.getY();
	}

	@Override
	public double getX() {
		return dx;
	}

	@Override
	public double getY() {
		return dy;
	}

	@Override
	public void setLocation(double x, double y) {
		this.dx = x;
		this.dy = y;
		super.setLocation(x, y);
	}

	@Override
	public void setLocation(Point2D p) {
		setLocation(p.getX(), p.getY());
	}

	@Override
	public void setLocation(Point p) {
		setLocation(p.getX(), p.getY());
	}

	public long getLongX() {
		return Math.round(dx);
	}

	public long getLongY() {
		return Math.round(dy);
	}

	public int getIntX() {
		return (int) Math.round(dx);
	}

	public int getIntY() {
		return (int) Math.round(dy);
	}

	public Point2D.Double getDoublePoint() {
		return new Point2D.Double(dx, dy);
	}

	public Point2D.Float getFloatPoint() {
		return new Point2D.Float((float) dx, (float) dy);
	}

	@Override
	public String toString() {
		return new String("[" + dx + ", " + dy + "]");
	}
}
