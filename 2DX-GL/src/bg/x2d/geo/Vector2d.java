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

public class Vector2d {
	
	public volatile double x, y, mag, angle;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param angle
	 */
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
		
		init();
	}
	
	public Vector2d(Vector2d copy) {
		x = copy.x;
		y = copy.y;
		mag = copy.mag;
		angle = copy.angle;
	}
	
	private void init() {
		mag = sqrt(pow(x, 2) + pow(y, 2));
		angle = GeoUtils.terminal(x, y);
	}
	
	public double degs() {
		return toDegrees(angle);
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2d add(Vector2d arg) {
		x += arg.x;
		y += arg.y;
		init();
		return this;
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2d sub(Vector2d arg) {
		x -= arg.x;
		y -= arg.y;
		init();
		return this;
	}
	
	public Vector2d mult(double factor) {
		x *= factor;
		y *= factor;
		init();
		return this;
	}
	
	public Vector2d div(double factor) {
		x /= factor;
		y /= factor;
		init();
		return this;
	}
	
	public Vector2d addNew(Vector2d arg) {
		return new Vector2d(x += arg.x, y+= arg.y);
	}
	
	public Vector2d subNew(Vector2d arg) {
		return new Vector2d(x -= arg.x, y-= arg.y);
	}
	
	public Vector2d multNew(double factor) {
		return new Vector2d(this).mult(factor);
	}
	
	public Vector2d divNew(double factor) {
		return new Vector2d(this).div(factor);
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public double dot(Vector2d arg) {
		return (mag * arg.mag * cos(angleBetween(arg)));
	}
	
	public double det(Vector2d arg) {
		return (x * arg.y - y * arg.x);
	}
	
	public double cross(Vector2d arg) {
		return det(arg);
	}
	
	public double angleBetween(Vector2d vec) {
		return abs(vec.angle - angle);
	}
	
	public Vector2d interpolate(Vector2d vec, double alpha) {
		return this.mult(1 - alpha).add(vec.multNew(alpha));
	}
	
	/**
	 * Rotates this vector by given radians.
	 * @param angle rotation value IN RADIANS
	 * @return this vector for chain calls.
	 */
	public Vector2d rotate(double rads) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), rads);
		this.x = p.getX();
		this.y = p.getY();
		init();
		return this;
	}
	
	/**
	 * 
	 * @param theta
	 * @return
	 */
	public Vector2d rotateNew(double theta) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), theta);
		double x = p.getX();
		double y = p.getY();
		return new Vector2d(x, y);
	}
	
	public Vector2d negate() {
		x = -x;
		y = -y;
		init();
		return this;
	}
	
	public Vector2d negateNew() {
		double x = -this.x;
		double y = -this.y;
		return new Vector2d(x,y);
	}
	
	public final void normalize() {
		double norm;
		norm = (1.0/Math.sqrt(this.x * this.x + this.y * this.y));
		this.x *= norm;
		this.y *= norm;
	}
	
	public Point2D applyTo(Point2D p, double multiplier) {
		p.setLocation(p.getX() + (x * multiplier), p.getY() + (y * multiplier));
		return p;
	}
	
	@Override
	public String toString() {
		return "mag="+mag+" theta="+Math.toDegrees(angle)+" ["+x+", "+y+"]";
	}
}
