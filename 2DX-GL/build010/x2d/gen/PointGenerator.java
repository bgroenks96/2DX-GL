/*
 * Copyright © 2011-2012 Brian Groenke, Private Proprietary Software
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

import java.awt.Point;

import bg.x2d.geo.PointLD;

public class PointGenerator implements Generator<Point> {
	
	private double x1, x2, y1, y2;
	
	public PointGenerator(double x1, double y1, double x2, double y2) {
		setBounds(x1,y1,x2,y2);
	}

	@Override
	public PointLD generate() {
		NumberGenerator<Double> gen = new NumberGenerator<Double>(x1,x2);
		double x = gen.generate();
		gen.setBounds(y1,y2);
		double y = gen.generate();
		return new PointLD(x, y);
	}
	
	public void setBounds(double x1, double y1, double x2, double y2) {
		this.x1  = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

}
