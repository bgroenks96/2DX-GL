/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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
 * Provides a method of interfacing between the screen and 2-dimensional world coordinate systems.
 * World2D creates a "viewport" of a standard Cartesian coordinate system and converts points to and
 * from the display's coordinate grid. World2D is created with a minimum x and y value; this point
 * corresponds in world space to the display's origin [0,0] (top, left corner). Rectangle (and any other
 * object) coordinates represent the bottom left corner of the bounding box.  World units can be
 * represented on-screen to a varying scale using ppu (pixels-per-unit). This allows the implementer
 * to specify how many pixels should represent one full unit in the 2D world. <br/>
 * <br/>
 * World coordinates are stored as <code>double</code> values and are rounded to <code>int</code>
 * values on each conversion. It is therefore recommended that you <b>always</b> keep Entities,
 * physics and game logic based on your 2D world coordinate system. Repeatedly converting and
 * back-converting between world and screen coordinates will, naturally, cause significant precision
 * loss due to decimal rounding.
 * 
 * @author Brian Groenke
 * @since Snapdragon2D 1.0
 * 
 */
public class World2D {
	
	protected double viewX, viewY, maxX, minY, wt, ht;
	protected float ppu;
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
	public World2D(double minX, double maxY, int viewWidth, int viewHeight,
			float ppu) {
		this.viewX = minX;
		this.viewY = maxY;
		setViewSize(viewWidth, viewHeight, ppu);
	}

	public Rect2D getBounds() {
		return new Rect2D(viewX, minY, wt, ht);
	}

	/**
	 * @return the viewport's starting x value in world space
	 */
	public double getX() {
		return viewX;
	}

	/**
	 * @return the viewport's starting y value in world space
	 */
	public double getY() {
		return minY;
	}
	
	/**
	 * @return the viewport's maximum x value in world space
	 */
	public double getMaxX() {
		return maxX;
	}
	
	/**
	 * @return the viewport's maximum y value in world space
	 */
	public double getMaxY() {
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
	public void setViewSize(int viewWidth, int viewHeight, float ppu) {
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

	public float getPixelsPerUnit() {
		return ppu;
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
		double y1 = viewY - (y / ppu);
		return new PointLD(x1, y1);
	}
	
	public PointLD screenToWorld(int x, int y, int ht) {
		double wht = ht / ppu;
		double x1 = (x / ppu) + viewX;
		double y1 = viewY - (y / ppu);
		return new PointLD(x1, y1 - wht);
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
		int y1 = (int) Math.round(sht - (y - minY) * ppu);
		return new Point(x1, y1);
	}
	
	public Point worldToScreen(double x, double y, double worldHt) {
		int x1 = (int) Math.round((x - viewX) * ppu);
		int y1 = (int) Math.round(sht - (y + worldHt - minY)* ppu);
		return new Point(x1, y1);
	}

	/**
	 * Converts the given Rect2D representing bounds in world space to corresponding Rectangle
	 * bounds in screen space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	public Rectangle convertWorldRect(Rect2D r) {
		Point sp = worldToScreen(r.getX(), r.getY() + r.getHeight());
		int wt = (int) Math.round(r.getWidth() * ppu);
		int ht = (int) Math.round(r.getHeight() * ppu);
		return new Rectangle(sp.x, sp.y, wt, ht);
	}

	/**
	 * Converts the given Rectangle representing bounds in screen space to corresponding Rect2D
	 * bounds in world space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	public Rect2D convertScreenRect(Rectangle r) {
		PointLD wp = screenToWorld(r.x, r.y + r.height);
		double wt = r.width / ppu;
		double ht = r.height / ppu;
		return new Rect2D(wp.dx, wp.dy, wt, ht);
	}

	/**
	 * Checks for a collision between the two rectangles and returns the calculated area of
	 * collision (if one exists).
	 * 
	 * @param r1
	 *            rectangle to test for collision with second
	 * @param r2
	 *            rectangle to test for collision with first
	 * @return a rectangle representing the overlap of the two rectangles in world space.
	 */
	public Rect2D checkCollision(Rect2D r1, Rect2D r2) {
		double x1 = r1.getX();
		double x1m = r1.getMaxX();
		double y1 = r1.getY();
		double y1m = y1 + r1.getHeight(); ///
		double x2 = r2.getX();
		double x2m = r2.getMaxX();
		double y2 = r2.getY();
		double y2m = y2 + r2.getHeight(); ///

		double xOverlap = Math.max(0, Math.min(x1m, x2m) - Math.max(x1, x2));
		double yOverlap = Math.max(0, Math.min(y1m, y2m) - Math.max(y1, y2));

		if (xOverlap == 0 || yOverlap == 0) {
			return null;
		} else {
			return new Rect2D(Math.max(x1, x2), Math.max(y1, y2),
					xOverlap, yOverlap);
		}
	}

	/**
	 * Checks if the given Rect2D is fully contained within this World2D's viewport.
	 * 
	 * @param rect
	 *            the bounding box in world coordinates to check.
	 * @return
	 */
	public boolean viewContains(Rect2D rect) {
		double vx = getX();
		double vy = getY();
		double vwt = getWorldWidth();
		double vht = getWorldHeight();
		boolean inYBounds = rect.getY() > vy
				&& rect.getY() + rect.getHeight() < vy + vht;
		boolean inXBounds = rect.getX() > vx
				&& rect.getX() + rect.getWidth() < vx + vwt;
		return inYBounds && inXBounds;
	}

	/**
	 * Checks if the given Rect2D intersects with this World2D's viewport.
	 * 
	 * @param rect
	 *            the bounding box in world coordinates to check for intersection
	 * @return
	 */
	public boolean viewIntersects(Rect2D rect) {
		double vx = getX();
		double vy = getY();
		double vwt = getWorldWidth();
		double vht = getWorldHeight();
		boolean inYBounds = rect.getY() + rect.getHeight() > vy && 
				rect.getY() < vy + vht;
		boolean inXBounds = rect.getX() + rect.getWidth() > vx && 
				rect.getX() < vx + vwt;
		return inYBounds && inXBounds;
	}
	
	/**
	 * Replaced by more appropriately named {@link #viewContains}
	 * 
	 * @param rect
	 * @return
	 */
	@Deprecated
	public boolean worldContains(Rectangle2D rect) {
		return viewContains(new Rect2D(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
	}

	/**
	 * Replaced by more appropriately named {@link #viewIntersects}
	 * 
	 * @param rect
	 * @return
	 */
	@Deprecated
	public boolean worldIntersects(Rectangle2D rect) {
		return viewIntersects(new Rect2D(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
	}
}
