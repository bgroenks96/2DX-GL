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

package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;

import bg.x2d.Background;

public class Pentagon2D extends Shapes2D {

	public Pentagon2D(Graphics g, Background b) {
		super(g,b);
	}
	
	public void drawPentagon(int x, int y, int size, Paint p, boolean fill) {
		
		Polygon pent = drawRegularPolygon(new Point(x,y),new Point(x+(size/2),y),size, p,fill,true,5);
				
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		shape = pent;
		filled = fill;
	}
	
	public void undraw() {
		if(background == null) throw(new NullPointerException("Unable to undraw: no Background object available"));
		Paint c = background.getPaint();
		drawPentagon(locx,locy,polySize, c,true);
	}
	
	public void undraw(Paint p) {
		drawPentagon(locx,locy,polySize, p,true);
	}

	@Override
	public void rotate(double degrees, Rotation type) {
		if(degrees < 0)
			throw(new IllegalArgumentException("Illegal theta value: specify a positive integer for degree of rotation"));
		if(type == null)
			throw(new IllegalArgumentException("passed Rotation type cannot be null"));
		if(shape == null)
			try {
				throw(new GeoException("shape must have been drawn or have set properties before a rotation can be performed"));
			} catch (GeoException e) {
				e.printStackTrace();
			}
		if(type == Rotation.COUNTER_CLOCKWISE) {
			double deg = -degrees;
			rotate(5,deg);
		} else if(type == Rotation.CLOCKWISE) {
			rotate(5,degrees);
		}
	}

	@Override
	public void setLocation(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(int x, int y, int size, Paint p, boolean fill) {
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
		shape = drawRegularPolygon(new Point(x,y),new Point(x+(size/2),y),size,p,fill,false,5);
	}
}
