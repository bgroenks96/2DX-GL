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

/**
 * Contains all Geometry based classes that perform Graphical tasks such as drawing figures, managing coordinates, and working with 2-dimensional shapes.
 */
package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;

import bg.x2d.Background;

/**
 * The superclass class for all generic 2-Dimensional figures. Subclasses of
 * Shapes2D in the geometry package are generally pre-defined, regular shapes
 * (drawn using rotations). The shape and all of its attributes are created when
 * the constructor is invoked but not drawn until <code>draw(Graphics2D)</code>
 * is called.
 */
public abstract class Shapes2D {

	@Deprecated
	Graphics2D canvas;

	@Deprecated
	Background background;

	int locx, locy, polySize;
	Paint paint;
	Polygon shape;
	boolean filled;

	@Deprecated
	/**
	 * Replaced with a more logical and OO-friendly constructor.
	 * @param g
	 * @param b
	 */
	public Shapes2D(Graphics g, Background b) {
		canvas = (Graphics2D) g;
		background = b;
	}

	@Deprecated
	public Shapes2D(Background b) {
		background = b;
	}

	/**
	 * Create this Shapes2D with all of the given attributes. This
	 * implementation just calls setProperties
	 * 
	 * @param x
	 * @param y
	 * @param size
	 * @param p
	 * @param fill
	 */
	public Shapes2D(int x, int y, int size, Paint p, boolean fill) {
		setProperties(x, y, size, p, fill);
	}

	public abstract void setProperties(int x, int y, int size, Paint p,
			boolean fill);

	public abstract void rotate(double degrees, Rotation type);

	public void setLocation(int x, int y) {
		locx = x;
		locy = y;
	}

	public void draw(Graphics2D g) {
		g.setPaint(paint);
		if (filled) {
			g.fillPolygon(shape);
		} else {
			g.drawPolygon(shape);
		}
	}

	public Background getBackground() {
		return background;
	}

	@Deprecated
	public void undraw(Paint p) {

	}

	/**
	 * Returns the Graphics2D object currently associated with the Shapes2D
	 * object.
	 * 
	 * @return the Graphics2D object being painted on.
	 */
	public Graphics2D getCanvas() {
		return canvas;
	}

	public void setBackground(Background b) {
		if (b != null) {
			background = b;
		}
	}

	@Deprecated
	public void undraw() {
		background.redraw(canvas);
	}

	public Polygon getShape() {
		return shape;
	}

	public Paint getPaint() {
		return paint;
	}

	public boolean isFilled() {
		return filled;
	}

	public Point getLocation() {
		return new Point(locx, locy);
	}

	public int getSize() {
		return polySize;
	}

	/**
	 * Algorithm that uses an algebraic rotation formula to translate a point
	 * from its current location x degrees to a new position on the coordinate
	 * plane. This method is used internally and is intended for use in
	 * calculating Polygon coordinate values. Note: This method uses and returns
	 * integer values. Mathematical operations resulting in non-integer values
	 * are rounded.
	 * 
	 * @param p
	 * @param origin
	 * @param degrees
	 * @return the rotated Point object.
	 */
	protected Point rotatePolyPoint(Point p, Point origin, double degrees) {
		int x = (int) p.getX();
		int y = (int) p.getY();
		int x0 = (int) origin.getX();
		int y0 = (int) origin.getY();
		int x1, y1;
		int wx = x - x0;
		int wy = y - y0;
		double xresult = (wx * (Math.cos(Math.toRadians(degrees))))
				- (wy * (Math.sin(Math.toRadians(degrees))));
		double yresult = (wx * (Math.sin(Math.toRadians(degrees))))
				+ (wy * (Math.cos(Math.toRadians(degrees))));
		x1 = (int) Math.round(xresult);
		y1 = (int) Math.round(yresult);
		int x2 = x1 + x0;
		int y2 = y1 + y0;
		return new Point(x2, y2);
	}

	/**
	 * Draws any regular polygon using the given arguments and number of sides.
	 * This is called by each subclass's individual draw...() method.
	 * 
	 * @param loc
	 *            Top left corner of the bounding box this shape should be drawn
	 *            in.
	 * @param start
	 *            Starting point for rotations.
	 * @param size
	 *            The desired size of the regular shape.
	 * @param p
	 * @param fill
	 * @param nsides
	 * @return the Polygon that has been drawn.
	 */
	protected Polygon drawRegularPolygon(Point loc, Point start, int size,
			Paint p, boolean fill, boolean draw, int nsides) {
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		int degrees = 360 / nsides;
		Polygon poly = new Polygon();
		Point origin = new Point(x + (size / 2), y + (size / 2));
		for (int i = 0; i < nsides; i++) {
			start = rotatePolyPoint(start, origin, degrees);
			poly.addPoint((int) start.getX(), (int) start.getY());
		}

		return poly;
	}

	protected void rotate(int sides, double degrees) {

		Point start = new Point(locx + (polySize / 2), locy);
		Point origin = new Point(locx + (polySize / 2), locy + (polySize / 2));
		start = rotatePolyPoint(start, origin, degrees);
		shape = drawRegularPolygon(new Point(locx, locy), start, polySize,
				paint, filled, true, sides);
	}
}
