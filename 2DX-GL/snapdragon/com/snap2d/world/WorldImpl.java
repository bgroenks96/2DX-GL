/*
 *  Copyright © 2011-2013 Brian Groenke
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
import java.awt.geom.*;

import bg.x2d.geo.*;

/**
 * Base implementation for converting between 2D standard coordinates and screen coordinates.
 * This implementation does not compensate for inverted axes.
 * @author Brian Groenke
 *
 */
public abstract class WorldImpl {
	
	protected double viewX, viewY, maxX, minY, wt, ht, ppu;
	protected int swt, sht;

	/**
	 * Creates this World2D with the specified starting x,y coordinates and view size.
	 * 
	 * @param minX
	 *            the current minimum value for x in world space; corresponds to x=0 in screen
	 *            space.
	 * @param maxY
	 *            the current maximum value for y in world space; corresponds to y=0 in screen
	 *            space.
	 * @param viewWidth
	 *            the width of the area on screen to which world coordinates should be translated
	 * @param viewHeight
	 *            the height of the area on screen to which world coordinates should be translated.
	 * @param ppu
	 *            the number of pixels per unit in world space
	 */
	public WorldImpl(double minX, double maxY, int viewWidth, int viewHeight,
			double ppu) {
		this.viewX = minX;
		this.viewY = maxY;
		setViewSize(viewWidth, viewHeight, ppu);
	}

	@Deprecated
	/**
	 * The Rectangle2D returned is not compatible with the built in Java2D functions due to the
	 * inverted Y-axis.  If you need a compatible world bounds for geometry checking, use
	 * #getCompatibleBounds.
	 * <br/><br/>
	 * It is recommended that you explicitly 
	 * use #getX, #getY, #getWorldWidth, and #getWorldWidth as opposed
	 * to this method to avoid bugs produced by incompatible coordinate system geometry.
	 * @return the viewport of the 2D coordinate system currently on screen (in world coordinates)
	 * @see getCompatibleBounds
	 */
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(viewX, viewY, wt, ht);
	}

	/**
	 * @return the viewport's minimum x value in world space
	 */
	public double getX() {
		return viewX;
	}

	/**
	 * @return the viewport's maximum y value in world space
	 */
	public double getY() {
		return viewY;
	}

	/**
	 * Moves the world's viewport to the specified location.
	 * 
	 * @param minX
	 *            the new x position in world space
	 * @param maxY
	 *            the new y position in world space
	 */
	public void setLocation(double minX, double maxY) {
		this.viewX = minX;
		this.viewY = maxY;
		this.maxX = minX + wt;
		this.minY = maxY - ht;
	}

	/**
	 * Sets the dimensions and scale of the world's view.
	 * 
	 * @param viewWidth
	 *            the new width of the area drawn on screen
	 * @param viewHeight
	 *            the new height of the area drawn on screen
	 * @param ppu
	 *            the new pixels per unit of world space.
	 */
	public void setViewSize(int viewWidth, int viewHeight, double ppu) {
		this.swt = viewWidth;
		this.sht = viewHeight;
		if (ppu <= 0) {
			throw (new IllegalArgumentException(
					"illegal pixel-per-unit value: " + ppu));
		}
		this.ppu = ppu;
		maxX = viewX + (viewWidth / ppu);
		minY = viewY - (viewHeight / ppu);
		wt = Math.abs(maxX - viewX);
		ht = Math.abs(minY - viewY);
	}

	public int getViewWidth() {
		return swt;
	}

	public int getViewHeight() {
		return sht;
	}

	public double getWorldWidth() {
		return wt;
	}

	public double getWorldHeight() {
		return ht;
	}

	public double getPixelsPerUnit() {
		return ppu;
	}

	/**
	 * Checks for a collision between the two rectangles and returns the calculated area of
	 * collision (if one exists). Note that the returned Rectangle2D is not compatible and
	 * represents the collision area in world coordinates.
	 * 
	 * @param r1
	 *            rectangle to test for collision with second
	 * @param r2
	 *            rectangle to test for collision with first
	 * @return a rectangle representing the overlap of the two rectangles in world space.
	 */
	public Rectangle2D.Double checkCollision(Rectangle2D r1, Rectangle2D r2) {
		double x1 = r1.getMinX();
		double x1m = r1.getMaxX();
		double y1 = r1.getMinY();
		double y1m = y1 + r1.getHeight(); ///
		double x2 = r2.getMinX();
		double x2m = r2.getMaxX();
		double y2 = r2.getMinY();
		double y2m = y2 + r2.getHeight(); ///

		double xOverlap = Math.max(0, Math.min(x1m, x2m) - Math.max(x1, x2));
		double yOverlap = Math.max(0, Math.min(y1m, y2m) - Math.max(y1, y2));

		if (xOverlap == 0 || yOverlap == 0) {
			return null;
		} else {
			return new Rectangle2D.Double(Math.max(x1, x2), Math.max(y1, y2),
					xOverlap, yOverlap);
		}
	}

	/**
	 * Checks if the given Rectangle2D is fully contained within this World2D's viewport.
	 * 
	 * @param rect
	 *            the bounding box in world coordinates to check.
	 * @return
	 */
	public boolean viewContains(Rectangle2D rect) {
		double vx = getX();
		double vy = getY();
		double vwt = getWorldWidth();
		double vht = getWorldHeight();
		boolean inYBounds = rect.getY() + rect.getHeight() < vy
				&& rect.getY() > vy - vht;
		boolean inXBounds = rect.getX() > vx
				&& rect.getX() + rect.getWidth() < vx + vwt;
		return inYBounds && inXBounds;
	}

	/**
	 * Checks if the given Rectangle2D intersects with this World2D's viewport.
	 * 
	 * @param rect
	 *            the bounding box in world coordinates to check for intersection
	 * @return
	 */
	public boolean viewIntersects(Rectangle2D rect) {
		double vx = getX();
		double vy = getY();
		double vwt = getWorldWidth();
		double vht = getWorldHeight();
		boolean inYBounds = rect.getY() < vy && 
				rect.getY() + rect.getHeight() > vy - vht;
		boolean inXBounds = rect.getX() + rect.getWidth() > vx && 
				rect.getX() < vx + vwt;
		return inYBounds && inXBounds;
	}

	/**
	 * Converts the given coordinates from screen space to world space.
	 * 
	 * @param x
	 *            x coordinate on screen
	 * @param y
	 *            y coordinate on screen
	 * @return the corresponding point in world space
	 */
	public PointLD screenToWorld(int x, int y) {
		double x1 = (x / ppu) + viewX;
		double y1 = (y / ppu) + (viewY - minY) / 2;
		return new PointLD(x1, y1);
	}

	/**
	 * Converts the given coordinates from world space to screen space.
	 * 
	 * @param x
	 *            x coordinate in the world
	 * @param y
	 *            y coordinate in the world
	 * @return the corresponding point in screen space
	 */
	public Point worldToScreen(double x, double y) {
		int x1 = (int) Math.round((x - viewX) * ppu);
		int y1 = (int) Math.round((y - (viewY - minY) / 2) * ppu);
		return new Point(x1, y1);
	}

	/**
	 * Converts the given Rectangle2D representing bounds in world space to corresponding Rectangle
	 * bounds in screen space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	public Rectangle convertWorldRect(Rectangle2D r) {
		Point sp = worldToScreen(r.getX(), r.getY());
		int wt = (int) Math.round(r.getWidth() * ppu);
		int ht = (int) Math.round(r.getHeight() * ppu);
		return new Rectangle(sp.x, sp.y, wt, ht);
	}

	/**
	 * Converts the given Rectangle representing bounds in screen space to corresponding Rectangle2D
	 * bounds in world space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	public Rectangle2D.Double convertScreenRect(Rectangle r) {
		PointLD wp = screenToWorld(r.x, r.y);
		double wt = r.width / ppu;
		double ht = r.height / ppu;
		return new Rectangle2D.Double(wp.dx, wp.dy, wt, ht);
	}

	/**
	 * Replaced by more appropriately named {@link #viewContains}
	 * 
	 * @param rect
	 * @return
	 */
	@Deprecated
	public boolean worldContains(Rectangle2D rect) {
		return viewContains(rect);
	}

	/**
	 * Replaced by more appropriately named {@link #viewIntersects}
	 * 
	 * @param rect
	 * @return
	 */
	@Deprecated
	public boolean worldIntersects(Rectangle2D rect) {
		return viewIntersects(rect);
	}
}
