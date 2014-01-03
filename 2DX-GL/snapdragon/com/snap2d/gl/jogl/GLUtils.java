/*
 *  Copyright © 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl;

import java.awt.*;

import bg.x2d.geo.*;

import com.snap2d.world.*;

/**
 * @author Brian Groenke
 *
 */
public class GLUtils {
	
	private GLUtils() {}
	
	public static final World2D createGLWorldSystem(double minx, double miny, int viewWt, int viewHt, float ppu) {
		return new GLWorld2D(minx, miny, viewWt, viewHt, ppu);
	}
	
	/**
	 * Internal implementation of World2D for use with OpenGL coordinate system.  This implementation removes World2D's
	 * Y-axis inversion.  The only real conversions made are translation and PPU (scale).
	 * @author Brian Groenke
	 *
	 */
	private static class GLWorld2D extends World2D {
		
		/*
		 * Note that ALL uses of variable 'minY' are in this implementation actually the maximum Y but
		 * the same variable from World2D's inverted Y axis screen space has to be used.
		 */

		/**
		 * @param minX
		 * @param maxY
		 * @param viewWidth
		 * @param viewHeight
		 * @param ppu
		 */
		GLWorld2D(double minX, double minY, int viewWidth,
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
			double y1 = (y / ppu) + viewY;
			return new PointLD(x1, y1);
		}
		
		@Override
		public PointLD screenToWorld(int x, int y, int ht) {
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
		public Point worldToScreen(double x, double y) {
			int x1 = (int) Math.round((x - viewX) * ppu);
			int y1 = (int) Math.round((y - viewY) * ppu);
			return new Point(x1, y1);
		}
		
		@Override
		public Point worldToScreen(double x, double y, double worldHt) {
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
			Point sp = worldToScreen(r.getX(), r.getY());
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
		@Override
		public Rect2D convertScreenRect(Rectangle r) {
			PointLD wp = screenToWorld(r.x, r.y);
			double wt = r.width / ppu;
			double ht = r.height / ppu;
			return new Rect2D(wp.dx, wp.dy, wt, ht);
		}
	}
}
