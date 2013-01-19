/*
 * Copyright � 2011-2012 Brian Groenke
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

package bg.x2d.gen;

import java.awt.*;

/**
 * Generates a random polygon with n sides and inside of the specified
 * Rectangle.
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class PolyGenerator implements Generator<Polygon> {

	private int x, y, width, height, n;
	private NumberGenerator<Integer> xgen, ygen;

	public PolyGenerator(Rectangle bounds, int nsides) {
		setBounds(bounds, nsides);
	}

	public void setBounds(Rectangle bounds, int nsides) {
		if (bounds != null) {
			x = (int) Math.round(bounds.getX());
			y = (int) Math.round(bounds.getY());
			width = (int) Math.round(bounds.getWidth());
			height = (int) Math.round(bounds.getHeight());
		} else {
			x = 0;
			y = 0;
			width = 10;
			height = 10;
		}
		n = nsides;
		xgen = new NumberGenerator<Integer>(x, x + width);
		ygen = new NumberGenerator<Integer>(y, y + height);
	}

	@Override
	public Polygon generate() {
		Polygon p = new Polygon();

		for (int i = n; i > 0; i--) {
			int x = xgen.generate();
			int y = ygen.generate();
			p.addPoint(x, y);
		}
		return p;
	}

}
