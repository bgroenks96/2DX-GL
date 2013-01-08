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

public class Circle2D extends Shapes2D {

	@Deprecated
	public Circle2D(Graphics g, Background b) {
		super(g, b);
	}

	public Circle2D(int x, int y, int size, Paint p, boolean fill) {
		super(x, y, size, p, fill);
	}

	@Deprecated
	public void drawCircle(int x, int y, int size, Paint p, boolean fill) {
		canvas.setPaint(p);
		if (fill) {
			canvas.fillOval(x, y, size, size);
		} else {
			canvas.drawOval(x, y, size, size);
		}
	}

	@Deprecated
	public void drawOval(int x, int y, int width, int height, Paint p,
			boolean fill) {
		canvas.setPaint(p);
		if (fill) {
			canvas.fillOval(x, y, width, height);
		} else {
			canvas.drawOval(x, y, width, height);
		}
	}

	/**
	 * Currently does nothing.
	 */
	@Override
	public void rotate(double degrees, Rotation type) {
		// Do nothing
	}

	@Override
	public void setProperties(int x, int y, int size, Paint p, boolean fill) {
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setPaint(paint);
		if (filled) {
			g.fillOval(locx, locy, polySize, polySize);
		} else {
			g.drawOval(locx, locy, polySize, polySize);
		}
	}
}
