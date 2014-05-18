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

import java.awt.*;

import bg.x2d.geo.*;

/**
 * Implementation of World2D for use with OpenGL coordinate system.  This implementation
 * removes World2D's y-axis inversion and does not round Point conversions between world
 * space and screen space since OpenGL accepts float and double values for vertex
 * coordinates.  Use of this class is entirely optional with OpenGL since the orthographic
 * projection matrix can take care of the same transformation between world-space
 * and the screen viewport.
 * @author Brian Groenke
 *
 */
public final class GLWorld2D extends World2D {
	
	/*
	 * Note that ALL uses of variable 'minY' are in this implementation actually the maximum Y but
	 * the same variable from World2D's inverted Y axis screen space has to be used.
	 * 
	 * Yes this is ridiculously counter-intuitive and poor foresight in design.  I'm sorry :(
	 */

	/**
	 * @param minX
	 * @param maxY
	 * @param viewWidth
	 * @param viewHeight
	 * @param ppu
	 */
	public GLWorld2D(double minX, double minY, int viewWidth,
			int viewHeight, float ppu) {
		super(minX, minY, viewWidth, viewHeight, ppu);
	}
	
	@Override
	public void setLocation(double minX, double minY) {
		this.viewX = minX;
		this.viewY = minY;
		this.maxX = minX + wt;
		this.minY = minY + ht; // this is really max Y!
	}
	
	@Override
	public void setViewSize(int viewWt, int viewHt, float ppu) {
		this.swt = viewWt;
		this.sht = viewHt;
		if (ppu <= 0) {
			throw (new IllegalArgumentException(
					"illegal pixel-per-unit value: " + ppu));
		}
		this.ppu = ppu;
		wt = Math.abs(maxX - viewX);
		ht = Math.abs(minY - viewY);
		maxX = viewX + (viewWt / ppu);
		minY = viewY + (viewHt / ppu);
	}
	
	@Override
	public double getY() {
		return viewY;
	}
	
	@Override
	public double getMaxY() {
		return minY;
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
	public PointUD screenToWorld(int x, int y) {
		double x1 = (x / ppu) + viewX;
		double y1 = (y / ppu) + viewY;
		return new PointUD(x1, y1);
	}
	
	@Override
	public PointUD screenToWorld(int x, int y, int ht) {
		return screenToWorld(x, y);
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
	public PointUD worldToScreen(double x, double y) {
		double x1 = (x - viewX) * ppu;
		double y1 = (y - viewY) * ppu;
		return new PointUD(x1, y1);
	}
	
	@Override
	public PointUD worldToScreen(double x, double y, double worldHt) {
		return worldToScreen(x, y);
	}

	/**
	 * Converts the given Rect2D representing bounds in world space to corresponding Rectangle
	 * bounds in screen space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public Rectangle convertWorldRect(Rect2D r) {
		PointUD sp = worldToScreen(r.getX(), r.getY());
		double wt = r.getWidth() * ppu;
		double ht = r.getHeight() * ppu;
		return new RectangleDouble(sp.x, sp.y, wt, ht);
	}

	/**
	 * Converts the given Rectangle representing bounds in screen space to corresponding Rect2D
	 * bounds in world space. This method first converts the x,y coordinates, then scales the
	 * rectangle by this World2D's current pixels-per-unit value.
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public Rect2D convertScreenRect(Rectangle r) {
		PointUD wp = screenToWorld(r.x, r.y);
		double wt = r.width / ppu;
		double ht = r.height / ppu;
		return new Rect2D(wp.ux, wp.uy, wt, ht);
	}
	
	private class RectangleDouble extends Rectangle {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6190258337544853573L;

		RectangleDouble(double x, double y, double wt, double ht) {
			super.setRect(x, y, wt, ht);
		}
	}
}
