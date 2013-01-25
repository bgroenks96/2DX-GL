/*
 *  Copyright © 2011-2012 Brian Groenke
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
 * from the display's coordinate grid.  World2D is created with a minimum x and y value; this point
 * corresponds in world space to the display's origin [0,0] (top, left corner).  World units can be
 * represented on-screen to a varying scale using ppu (pixels-per-unit).  This allows the implementer
 * to specify how many pixels should represent one full unit in the 2D world.
 * <br/><br/>
 * World coordinates are stored as <code>double</code> values and are rounded to <code>int</code> values on each conversion.  
 * It is therefore recommended that you <b>always</b> keep Entities, physics and game logic based on your 2D world 
 * coordinate system.  Repeatedly converting and back-converting between world and screen coordinates will, naturally, 
 * cause significant precision loss due to decimal rounding.
 * @author Brian Groenke
 * @since Snapdragon2D 1.0
 *
 */
public class World2D {

	protected double minX, minY, maxX, maxY, wt, ht, ppu;
	protected int swt, sht;

	/**
	 * Creates this World2D with the specified starting x,y coordinates and view size.
	 * @param minX the current minimum value for x in world space; corresponds to x=0 in screen space.
	 * @param minY the current minimum value for y in world space; corresponds to y=0 in screen space.
	 * @param viewWidth the width of the area on screen to which world coordinates should be translated
	 * @param viewHeight the height of the area on screen to which world coordinates should be translated.
	 * @param ppu the number of pixels per unit in world space
	 */
	public World2D(double minX, double minY, int viewWidth, int viewHeight, double ppu) {
		this.minX = minX;
		this.minY = minY;
		setViewSize(viewWidth, viewHeight, ppu);
	}

	/**
	 * 
	 * @return the viewport of the 2D coordinate system currently on screen
	 */
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(minX, minY, wt, ht);
	}

	/**
	 * 
	 * @return the minimum x value in world space
	 */
	public double getX() {
		return minX;
	}

	/**
	 * 
	 * @return the minimum y value in world space
	 */
	public double getY() {
		return minY;
	}

	/**
	 * Moves the world's viewport to the specified location.
	 * @param minX the new x position in world space
	 * @param minY the new y position in world space
	 */
	public void setLocation(double minX, double minY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = minX + wt;
		this.maxY = minY + ht;
	}

	/**
	 * Sets the dimensions and scale of the world's view.
	 * @param viewWidth the new width of the area drawn on screen
	 * @param viewHeight the new height of the area drawn on screen
	 * @param ppu the new pixels per unit of world space.
	 */
	public void setViewSize(int viewWidth, int viewHeight, double ppu) {
		this.swt = viewWidth;
		this.sht = viewHeight;
		if(ppu < 1)
			throw(new IllegalArgumentException("illegal pixel-per-unit value: " + ppu));
		this.ppu = ppu;
		maxX = minX + ((double)viewWidth / ppu);
		maxY = minY + ((double)viewHeight / ppu);
		wt = maxX - minX;
		ht = maxY - minY;
		if(wt <= 0 || ht <= 0)
			throw(new IllegalArgumentException("illegal min/max values"));
	}

	public int getViewWidth() {
		return swt;
	}

	public int getViewHeight() {
		return sht;
	}

	public double getPixelsPerUnit() {
		return ppu;
	}

	/**
	 * Checks for a collision between the two rectangles and returns the calculated area
	 * of collision (if one exists).
	 * @param r1 rectangle to test for collision with second
	 * @param r2 rectangle to test for collision with first
	 * @return a rectangle representing the overlap of the two rectangles in world space.
	 */
	public Rectangle2D.Double checkCollision(Rectangle2D r1, Rectangle2D r2) {
		double x1 = r1.getMinX();
		double x1m = r1.getMaxX();
		double y1 = r1.getMinY();
		double y1m = r1.getMaxY();
		double x2 = r2.getMinX();
		double x2m = r2.getMaxX();
		double y2 = r2.getMinY();
		double y2m = r2.getMaxY();

		double xOverlap = Math.max(0, Math.min(x1m, x2m) - Math.max(x1, x2));
		double yOverlap = Math.max(0, Math.min(y1m, y2m) - Math.max(y1, y2));
		
		
		if(xOverlap == 0 || yOverlap == 0)
			return null;
		else
			return new Rectangle2D.Double(Math.max(x1, x2), Math.min(y1, y2), xOverlap, yOverlap);
	}

	/**
	 * Converts the given coordinates from screen space to world space.
	 * @param x x coordinate on screen
	 * @param y y coordinate on screen
	 * @return the corresponding point in world space
	 */
	public PointLD screenToWorld(int x, int y) {
		double x1 = (x + minX) / ppu;
		double y1 = (maxY - y) / ppu;
		return new PointLD(x1, y1);
	}

	/**
	 * Converts the given coordinates from world space to screen space.
	 * @param x x coordinate in the world
	 * @param y y coordinate in the world
	 * @return the corresponding point in screen space
	 */
	public Point worldToScreen(double x, double y) {
		int x1 = (int) Math.round(x*ppu - minX);
		int y1 = (int) Math.round(ht - (y*ppu - minY));
		return new Point(x1, y1);
	}
	 
	/**
	 * Converts the given Rectangle2D representing bounds in world space to corresponding
	 * Rectangle bounds in screen space.  This method first converts the x,y coordinates, then
	 * scales the rectangle by this World2D's current pixels-per-unit value.
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
	 * Converts the given Rectangle representing bounds in screen space to corresponding
	 * Rectangle2D bounds in world space.  This method first converts the x,y coordinates, then
	 * scales the rectangle by this World2D's current pixels-per-unit value.
	 * @param r
	 * @return
	 */
	public Rectangle2D.Double convertScreenRect(Rectangle r) {
		PointLD wp = screenToWorld(r.x, r.y);
		double wt = r.width / ppu;
		double ht = r.height / ppu;
		return new Rectangle2D.Double(wp.x, wp.y, wt, ht);
	}
}
