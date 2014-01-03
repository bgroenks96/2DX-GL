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

public class Pentagon2D extends Shapes2D {

	public Pentagon2D(int x, int y, int size, Paint p, boolean fill) {
		super(x, y, size, p, fill);
	}

	@Override
	public void rotate(double degrees, Rotation type) {
		if (degrees < 0) {
			throw (new IllegalArgumentException(
					"Illegal theta value: specify a positive integer for degree of rotation"));
		}
		if (type == null) {
			throw (new IllegalArgumentException(
					"passed Rotation type cannot be null"));
		}
		if (shape == null) {
			try {
				throw (new GeoException(
						"shape must have been drawn or have set properties before a rotation can be performed"));
			} catch (GeoException e) {
				e.printStackTrace();
			}
		}
		if (type == Rotation.COUNTER_CLOCKWISE) {
			double deg = -degrees;
			rotate(5, deg);
		} else if (type == Rotation.CLOCKWISE) {
			rotate(5, degrees);
		}
	}

	@Override
	public void setProperties(int x, int y, int size, Paint p, boolean fill) {
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
		shape = drawRegularPolygon(new Point(x, y),
				new Point(x + (size / 2), y), size, p, fill, false, 5);
	}
}
