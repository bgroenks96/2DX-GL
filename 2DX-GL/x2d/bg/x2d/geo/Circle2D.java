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

package bg.x2d.geo;

import java.awt.*;

public class Circle2D extends Shapes2D {

	public Circle2D(int x, int y, int size, Paint p, boolean fill) {
		super(x, y, size, p, fill);
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
