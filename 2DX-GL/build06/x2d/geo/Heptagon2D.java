package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;

import bg.x2d.Background;

public class Heptagon2D extends Shapes2D {

	public Heptagon2D(Graphics g, Background b) {
		super(g, b);
	}
	
	public void drawHeptagon(int x, int y, int size, Paint p, boolean fill) {
		
		Polygon hept = drawRegularPolygon(new Point(x,y),new Point(x+(size/2),y),size, p,fill,true,7);
		
		shape = hept;
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
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
			rotate(7,deg);
		} else if(type == Rotation.CLOCKWISE) {
			rotate(7,degrees);
		}
	}

	@Override
	public void undraw() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undraw(Paint p) {
		// TODO Auto-generated method stub
		
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
		shape = drawRegularPolygon(new Point(x,y),new Point(x+(size/2),y),size,p,fill,false,7);
	}

}
