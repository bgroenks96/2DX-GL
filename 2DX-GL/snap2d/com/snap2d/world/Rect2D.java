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

package com.snap2d.world;

import bg.x2d.geo.*;

/**
 * Represents a rectangular area in standard Cartesian coordinate space.
 * Rect2D follows the World2D standard of representing object location as
 * the coordinates of the bottom left corner.
 * @author Brian Groenke
 *
 */
public class Rect2D {
	
	private double x, y, mx, my, wt, ht;
	
	public Rect2D(double x, double y, double wt, double ht) {
		setRect(x,y,wt,ht);
	}
	
	public Rect2D(PointUD p, double wt, double ht) {
		this(p.ux, p.uy, wt, ht);
	}
	
	public Rect2D checkCollision(Rect2D r2) {
		double x1 = x;
		double x1m = mx;
		double y1 = y;
		double y1m = my; ///
		double x2 = r2.getX();
		double x2m = r2.getMaxX();
		double y2 = r2.getY();
		double y2m = r2.getMaxY(); ///

		double xOverlap = Math.max(0, Math.min(x1m, x2m) - Math.max(x1, x2));
		double yOverlap = Math.max(0, Math.min(y1m, y2m) - Math.max(y1, y2));

		if (xOverlap == 0 || yOverlap == 0) {
			return null;
		} else {
			return new Rect2D(Math.max(x1, x2), Math.max(y1, y2),
					xOverlap, yOverlap);
		}
	}
	
	public boolean contains(double x1, double y1) {
		return x1 > x && x1 < mx && y1 > y && y1 < my;
	}
	
	public PointUD getLocation() {
		return new PointUD(x, y);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getMaxX() {
		return mx;
	}
	
	public double getMaxY() {
		return my;
	}
	
	public double getWidth() {
		return wt;
	}
	
	public double getHeight() {
		return ht;
	}
	
	public float getFloatX() {
		return (float) x;
	}
	
	public float getFloatY() {
		return (float) y;
	}
	
	public float getFloatMaxX() {
		return (float) mx;
	}
	
	public float getFloatMaxY() {
		return (float) my;
	}
	
	public void setRect(double x, double y, double wt, double ht) {
		this.x = x;
		this.y = y;
		this.mx = x + wt;
		this.my = y + ht;
		this.wt = wt;
		this.ht = ht;
	}
	
	public void setLocation(double x, double y) {
		setRect(x, y, wt, ht);
	}
	
	public void setDimensions(double wt, double ht) {
		setRect(x, y, wt, ht);
	}
	
	@Override
	public String toString() {
		return "[" + x + " + " + wt + ", " + y + " + " + ht + "]";
	}
}
