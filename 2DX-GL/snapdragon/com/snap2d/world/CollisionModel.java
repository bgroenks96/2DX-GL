/*
 *  Copyright © 2012-2013 Brian Groenke
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

import java.awt.*;
import java.util.*;

import bg.x2d.geo.*;

import com.snap2d.editor.*;


/**
 * Allows for precise, efficient collision detection between two entities with polygon bounding.
 * The Snapdragon2D SpriteEditor can be used to create and save polygon bounds for images.
 * @author Brian Groenke
 * 
 */
public class CollisionModel {
	
	LineSeg[] poly;
	PointLD[] wpts;
	
	/**
	 * Note: width/height of the polygon is defined by the greatest dist between its points along the X/Y axes, or in other words,
	 * the dimensions of its rectangular bounds.
	 * @param pts the points of the polygon bounds (in order)
	 * @param wt width of the polygon
	 * @param ht height of the polygon
	 * @param world the world in which the entity represented by this model resides
	 */
	public CollisionModel(Point[] pts, int wt, int ht, World2D world) {
		this.wpts = new PointLD[pts.length];
		double ppu = world.getPixelsPerUnit();
		for(int i=0; i < pts.length; i++) {
			wpts[i] = new PointLD(pts[i].x / ppu, (ht - pts[i].y) / ppu);
		}
		poly = new LineSeg[pts.length];
		PointLD last = null;
		ArrayList<LineSeg> lazyList = new ArrayList<LineSeg>();
		for(PointLD p:wpts) {
			if(last == null) {
				last = p;
				continue;
			}
			
			LineSeg seg = new LineSeg(last, p);
			lazyList.add(seg);
			last = p;
		}
		lazyList.add(new LineSeg(last, wpts[0]));
		lazyList.toArray(poly);
	}
	
	public CollisionModel(SpriteData spriteData, World2D world) {
		this(spriteData.vertices, spriteData.wt, spriteData.ht, world);
	}
	
	public boolean collidesWith(double x, double y, double cx, double cy, CollisionModel coll) {
		return testCollision(x, y, cx, cy, coll) || coll.testCollision(cx, cy, x, y, this);
	}
	
	private boolean testCollision(double x, double y, double cx, double cy, CollisionModel coll) {
		double minx = Double.MAX_VALUE;
		for(PointLD p:wpts) {
			double px = p.getX() + x;
			minx = Math.min(px, minx);
		}
		double lx = minx - 1;
		for(PointLD p:coll.wpts) {
			double px = p.getX() + cx;
			double py = p.getY() + cy;
			int crossCount = 0;
			for(LineSeg seg:poly) {
				PointLD intrsec = GeoUtils.lineIntersection(lx, py, px, py, seg.x1 + x, seg.y1 + y, seg.x2 + x, seg.y2 + y);
				if(intrsec == null)
					continue;
				float sx = (float) (intrsec.getX() - x);
				float sy = (float) (intrsec.getY() - y);
				if(seg.hasPoint(sx, sy) && sx < (px-x))
					crossCount++;
			}
			if(crossCount % 2 != 0) // if the ray intersects an odd number of times, there is a collision
				return true;
		}
		
		return false;
	}
	
	private class LineSeg {
		
		float x1, y1, x2, y2;
		
		/**
		 * @param p1 first point - all values casted to float
		 * @param p2 second point - all values casted to float
		 */
		LineSeg(Point p1, Point p2) {
			this.x1 = (float) p1.getX();
			this.y1 = (float) p1.getY();
			this.x2 = (float) p2.getX();
			this.y2 = (float) p2.getY();
		}
		
		boolean hasPoint(float x, float y) {
			return x <= Math.max(x1, x2) && x >= Math.min(x1, x2) &&
					y <= Math.max(y1, y2) && y >= Math.min(y1, y2);
		}
		
		@Override
		public String toString() {
			return "[("+x1+", "+y1+"), ("+x2+", "+y2+")]";
		}
	}
	
	/**
	 * Creates an approximation of a circular bounding area represented by a polygon.
	 * The smaller the angle increment, the more accurate the polygon will be.  The larger
	 * the increment, the better performance/memory-use will be.
	 * @param size diameter of the circle in pixels.
	 * @param angleIncrem the angle (in radians) between each computed point on the circle for drawing lines
	 * @return an array of ordered polygon points in screen space
	 */
	public static Point[] createCircleBounds(int size, double angleIncrem) {
		if(size <= 0)
			throw(new IllegalArgumentException("circle size must be greater than zero"));
		
		int radius = size / 2;
		ArrayList<Point> ptlist = new ArrayList<Point>();
		ptlist.add(new Point(radius * 2, radius));
		for(double angle=angleIncrem;angle < Math.PI * 2; angle += angleIncrem) {
			double x = radius * Math.cos(angle);
			double y = radius * Math.sin(angle);
			float tx = (float) (x + radius);
			float ty = (float) (size - (y + radius));
			ptlist.add(new Point(Math.round(tx), Math.round(ty)));
		}
		Point[] ptarr = new Point[ptlist.size()];
		ptlist.toArray(ptarr);
		return ptarr;
	}
}
