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

import static java.lang.Math.*;

import java.awt.geom.*;
import java.math.*;

/**
 * Implementation of a floating point vector in 2-dimensional coordinate space.
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Vector2f {

	private static int defaultPrecision = 5;
	
	public volatile float x, y;
	
	private volatile float mag, angle;
	
	public volatile int precision = defaultPrecision;

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;

		init();
	}

	public Vector2f(Vector2f copy) {
		x = copy.x;
		y = copy.y;
		checkPrecision();
		mag = copy.mag;
		angle = copy.angle;
	}
	
	public static Vector2f fromPolar(float mag, float angle) {
		return new Vector2f(0, 0).setFromPolar(mag, angle);
	}

	/**
	 * Sets the number of decimal places to the right where vector math should be rounded.
	 * All Vector2f objects will be initialized to this value.
	 * @param precision
	 */
	public static void setDefaultPrecision(int precision) {
		if(precision >= 0)
			defaultPrecision = precision;
	}
	
	/**
	 * Returns the current decimal precision value for vector math.  Default is 5 (i.e. x.#####).
	 * @return
	 */
	public static int getDecimalPrecision() {
		return defaultPrecision;
	}
	
	private void init() {
		angle = (float) GeoUtils.terminal(x, y);
		mag = (float) sqrt(pow(x, 2) + pow(y, 2));
	}
	
	private void checkPrecision() {
		BigDecimal bd = BigDecimal.valueOf(x).setScale(precision, RoundingMode.HALF_UP);
		x = bd.floatValue();
		bd = BigDecimal.valueOf(y).setScale(precision, RoundingMode.HALF_UP);
		y = bd.floatValue();
		init();
	}

	public float degs() {
		checkPrecision();
		return (float) toDegrees(angle);
	}
	
	public float rads() {
		checkPrecision();
		return angle;
	}
	
	public float getMagnitude() {
		checkPrecision();
		return mag;
	}
	
	public Vector2f setFromPolar(float mag, float angle) {
		if(mag < 0)
			mag = -mag;
		x = (float) (mag * cos(angle));
		y = (float) (mag * sin(angle));
		this.mag = mag;
		this.angle = angle;
		checkPrecision();
		return this;
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2f add(Vector2f arg) {
		x += arg.x;
		y += arg.y;
		checkPrecision();
		return this;
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2f sub(Vector2f arg) {
		x -= arg.x;
		y -= arg.y;
		checkPrecision();
		return this;
	}

	public Vector2f mult(float factor) {
		x *= factor;
		y *= factor;
		init();
		return this;
	}

	public Vector2f div(float factor) {
		x /= factor;
		y /= factor;
		checkPrecision();
		return this;
	}

	public Vector2f addNew(Vector2f arg) {
		return new Vector2f(x += arg.x, y+= arg.y);
	}

	public Vector2f subNew(Vector2f arg) {
		return new Vector2f(x -= arg.x, y-= arg.y);
	}

	public Vector2f multNew(float factor) {
		return new Vector2f(this).mult(factor);
	}

	public Vector2f divNew(float factor) {
		return new Vector2f(this).div(factor);
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	public float dot(Vector2f arg) {
		checkPrecision();
		return (float) (mag * arg.mag * cos(angleBetween(arg)));
	}

	public float det(Vector2f arg) {
		return (float) (x * arg.y - y * arg.x);
	}

	public float cross(Vector2f arg) {
		return det(arg);
	}

	public final void normalize() {
		float norm;
		norm = (float) 
				(1.0/Math.sqrt(this.x * this.x + this.y * this.y));
		this.x *= norm;
		this.y *= norm;
		checkPrecision();
	}

	public float angleBetween(Vector2f vec) {
		return abs(vec.angle - angle);
	}

	public Vector2f interpolate(Vector2f vec, float alpha) {
		return this.mult(1 - alpha).add(vec.multNew(alpha));
	}

	/**
	 * Rotates this vector by given radians.
	 * @param angle rotation value IN RADIANS
	 * @return this vector for chain calls.
	 */
	public Vector2f rotate(float rads) {
		double[] coord = GeoUtils.rotatePoint(x, y, 0, 0, rads);
		this.x = (float) coord[0];
		this.y = (float) coord[1];
		checkPrecision();
		return this;
	}

	/**
	 * 
	 * @param theta
	 * @return
	 */
	public Vector2f rotateNew(float rads) {
		return new Vector2f(x, y).rotate(rads);
	}

	public Vector2f negate() {
		x = -x;
		y = -y;
		checkPrecision();
		return this;
	}

	public Vector2f negateNew() {
		float x = -this.x;
		float y = -this.y;
		return new Vector2f(x,y);
	}
	
	public Vector2f negateX() {
		x = -x;
		checkPrecision();
		return this;
	}
	
	public Vector2f negateY() {
		y = -y;
		checkPrecision();
		return this;
	}
	
	public Point2D.Float applyTo(Point2D.Float p, float multiplier) {
		return new Point2D.Float(p.x + (x * multiplier), p.y + (y * multiplier));
	}
	
	@Override
	public String toString() {
		return "mag="+mag+" theta="+Math.toDegrees(angle)+" ["+x+", "+y+"]";
	}
}
