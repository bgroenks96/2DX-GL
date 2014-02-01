/*
 *  Copyright (C) 2012-2014 Brian Groenke
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

import java.awt.Point;
import java.util.ArrayList;

import bg.x2d.geo.*;

import com.snap2d.editor.SpriteData;


/**
 * Allows for precise, efficient collision detection between two entities with polygon bounding.
 * The Snap2D SpriteEditor can be used to create and save polygon bounds for images.
 * @author Brian Groenke
 * 
 */
public class CollisionModel {

	LineSeg[] poly;
	PointUD[] wpts;

	/**
	 * Note: width/height of the polygon is defined by the greatest dist between its points along the X/Y axes, or in other words,
	 * the dimensions of its rectangular bounds.
	 * @param pts the points of the polygon bounds (in order)
	 * @param wt width of the polygon
	 * @param ht height of the polygon
	 * @param world the world in which the entity represented by this model resides
	 */
	public CollisionModel(Point[] pts, int wt, int ht, World2D world) {
		this.wpts = new PointUD[pts.length];
		double ppu = world.getPixelsPerUnit();
		for(int i=0; i < pts.length; i++) {
			wpts[i] = new PointUD(pts[i].x / ppu, (ht - pts[i].y) / ppu);
		}
		poly = new LineSeg[pts.length];
		PointUD last = null;
		ArrayList<LineSeg> lazyList = new ArrayList<LineSeg>();
		for(PointUD p:wpts) {
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

	/**
	 * Tests to see if the given point lies within this CollisionModel
	 * @param p the point to test
	 * @param tx how much the point should be translated along the X axis relative to the origin -
	 * for a point in a World2D, this would be {@link World2D.getX()}
	 * @param ty how much the point should be translated along the Y axis relative to the origin -
	 * for a point in a World2D, this would be {@link World2D.getY()}
	 * @return true if this CollisionModel's polygon bounds contain the given PointUD, false otherwise
	 */
	public boolean contains(PointUD p, double tx, double ty) {
		PointUD basePoint = new PointUD(-1, -1);
		PointUD testPoint = new PointUD(p);
		int crossCount = 0;
		for(LineSeg seg : poly) {
			PointUD intrsec = GeoUtils.lineIntersection(basePoint.ux, basePoint.uy, 
					testPoint.ux, testPoint.uy, seg.x1, seg.y1, seg.x2, seg.y2);
			if(intrsec == null)
				continue;
			float sx = (float) (intrsec.getX());
			float sy = (float) (intrsec.getY());
			if(seg.hasPoint(sx, sy))
				crossCount++;
		}
		return crossCount % 2 != 0;
	}

	public boolean collidesWith(double x, double y, double cx, double cy, CollisionModel coll) {
		return testCollision(x, y, cx, cy, coll) || coll.testCollision(cx, cy, x, y, this);
	}

	/**
	 * Resolves the assumed collision between this CollisionModel and 'coll' by testing and modifying
	 * the given PointUD locations.
	 * @param loc the position of this CollisionModel in world space
	 * @param cloc the position of the other CollisionModel in world space
	 * @param coll the other (colliding) CollisionModel
	 * @param vel the velocity of this CollisionModel as a Vector2d
	 * @param cvel the velocity of this CollisionModel as a Vector2d
	 * @param velFactor the multiplier to use when applying the vectors to the points -
	 * this could be a time ratio or just 1 for basic vector/point relationships
	 * @param resolutionThreshold the maximum magnitude of distance that must be between the two
	 * models for the collision to be considered resolved - for most 2D coordinate systems, 1 is
	 * a good value to use, although it could be lower if 1 world unit < 1 screen unit.
	 */
	public void resolve(PointUD loc, PointUD cloc, CollisionModel coll, 
			Vector2d vel, Vector2d cvel, double velFactor, double resolutionThreshold) {
		if(resolutionThreshold <= 0)
			throw(new IllegalArgumentException("resolution thershold must be > 0"));
		vel = vel.negateNew().mult(0.5);
		cvel = cvel.negateNew().mult(0.5);
		boolean resolved = false, colliding = true;
		while(!resolved) {
			vel.applyTo(loc, velFactor);
			cvel.applyTo(cloc, velFactor);
			// if collision test status changes, negate and half testing vectors - 
			// the collision is resolved when the test status changes
			// while both vectors have a magnitude of <= resolutionThreshold
			if(testCollision(loc.ux, loc.uy, cloc.ux, cloc.uy, coll) != colliding) {
				if(colliding && vel.getMagnitude() <= resolutionThreshold && cvel.getMagnitude() <= resolutionThreshold) {
					resolved = true;
				} else {
					vel.negate(); cvel.negate();
					vel.mult(0.75); cvel.mult(0.75);
					colliding = !colliding;
				}
			}
		}
	}

	private boolean testCollision(double x, double y, double cx, double cy, CollisionModel coll) {
		double minx = Double.MAX_VALUE;
		for(PointUD p:wpts) {
			double px = p.getX() + x;
			minx = Math.min(px, minx);
		}
		double lx = minx - 1;
		for(PointUD p:coll.wpts) {
			double px = p.getX() + cx;
			double py = p.getY() + cy;
			int crossCount = 0;
			for(LineSeg seg:poly) {
				PointUD intrsec = GeoUtils.lineIntersection(lx, py, px, py, seg.x1 + x, seg.y1 + y, seg.x2 + x, seg.y2 + y);
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
