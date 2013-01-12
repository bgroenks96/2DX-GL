/*
 * Copyright © 2011-2012 Brian Groenke
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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import bg.x2d.Background;

/**
 * Superclass of all undefined geometric construction tools, most prominently
 * {@link Paintbrush2D}. FreeDraw2D defines certain generic methods regarding a
 * {@link Background} object and Graphics2D <code>canvas</code> object that all
 * subclasses of it will utilize.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class FreeDraw2D {

	@Deprecated
	Background background;
	@Deprecated
	Graphics2D canvas;
	
	Paint paint;
	
	public void setCanvasPaint(Paint p) {
		if(p != null)
			paint = p;
	}

	
	// ----- DEPRECATED ------ //
	@Deprecated
	public void setBackground(Background b) {
		if (b != null) {
			background = b;
		}
	}

	@Deprecated
	public Background getBackground() {
		return background;
	}

	@Deprecated
	public Graphics2D getCanvas() {
		return canvas;
	}

	@Deprecated
	public void setGraphics(Graphics g) {
		setGraphics((Graphics2D) g);
	}

	@Deprecated
	public void setGraphics(Graphics2D g2) {
		canvas = g2;
	}
}
