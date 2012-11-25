/*
 * Copyright � 2011-2012 Brian Groenke, Private Proprietary Software
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

import java.awt.Point;
import java.awt.geom.Point2D;


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
		this.x = (double) x;
		this.y = (double) y;
	}
	
	public PointLD(Point p) {
		x = p.getX();
		y = p.getY();
	}
	
	public PointLD(Point2D.Double p2d) {
		x = p2d.x;
		y = p2d.y;
	}
	
	@Override
	public double getX() {
		return x;
	}
	
	@Override
	public double getY() {
		return y;
	}
	
	public long getLongX() {
		return (long) x;
	}
	
	public long getLongY() {
		return (long) y;
	}
	
	@Override
	public String toString() {
		return new String("[x="+x+", y="+y+"]");
	}
}
