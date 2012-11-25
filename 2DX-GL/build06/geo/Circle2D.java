package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Paint;

import bg.x2d.Background;

public class Circle2D extends Shapes2D {

	private int locx, locy, w, h;

	public Circle2D(Graphics g, Background b) {
		super(g,b);
	}

	public void drawCircle(int x, int y, int size, Paint p, boolean fill) {
		canvas.setPaint(p);
		if (fill) {
			canvas.fillOval(x, y, size, size);
		} else {
			canvas.drawOval(x, y, size, size);
		}
		locx = x;
		locy = y;
		w = size;
		h = size;
		filled = fill;
		paint = p;
	}

	public void drawOval(int x, int y, int width, int height, Paint p,
			boolean fill) {
		canvas.setPaint(p);
		if (fill) {
			canvas.fillOval(x, y, width, height);
		} else {
			canvas.drawOval(x, y, width, height);
		}
		w = width;
		h = height;
		locx = x;
		locy = y;
		filled = fill;
		paint = p;
	}

	@Override
	public void undraw() {
		if(background == null) throw(new NullPointerException("Unable to undraw: no Background object available"));
		Paint c = background.getPaint();
		drawOval(locx, locy, w, h, c, true);
	}
	
	@Override
	public void undraw(Paint c) {
		drawOval(locx, locy, w, h, c, true);
	}
	
	/**
	 * Currently does nothing.
	 */
	@Override
	public void rotate(double degrees, Rotation type) {
		//Do nothing
	}

	@Override
	public void setLocation(int x, int y) {
		
	}

	@Override
	public void setProperties(int x, int y, int size, Paint p, boolean fill) {
		locx = x;
		locy = y;
		polySize = size;
		paint = p;
		filled = fill;
	}
}
