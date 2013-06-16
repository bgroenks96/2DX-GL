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
public class World2D extends WorldImpl {

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
			double ppu) {
		super(minX, maxY, viewWidth, viewHeight, ppu);
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
	@Override
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
	@Override
	public Point worldToScreen(double x, double y) {
		int x1 = (int) Math.round((x - viewX) * ppu);
		int y1 = (int) Math.round((ht - (y - minY)) * ppu);
		return new Point(x1, y1);
	}
	
	public Point worldToScreen(double x, double y, double worldHt) {
		int sht = (int) Math.round(worldHt * ppu);
		int x1 = (int) Math.round((x - viewX) * ppu);
		int y1 = (int) Math.round((ht - (y - minY)) * ppu);
		return new Point(x1, y1 - sht);
	}

	/**
	 * Converts the given Rectangle2D representing bounds in world space to corresponding Rectangle
	 * bounds in screen space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public Rectangle convertWorldRect(Rectangle2D r) {
		Point sp = worldToScreen(r.getX(), r.getY());
		int wt = (int) Math.round(r.getWidth() * ppu);
		int ht = (int) Math.round(r.getHeight() * ppu);
		return new Rectangle(sp.x, sp.y - ht, wt, ht);
	}

	/**
	 * Converts the given Rectangle representing bounds in screen space to corresponding Rectangle2D
	 * bounds in world space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public Rectangle2D.Double convertScreenRect(Rectangle r) {
		PointLD wp = screenToWorld(r.x, r.y);
		double wt = r.width / ppu;
		double ht = r.height / ppu;
		return new Rectangle2D.Double(wp.dx, wp.dy - ht, wt, ht);
	}
}
