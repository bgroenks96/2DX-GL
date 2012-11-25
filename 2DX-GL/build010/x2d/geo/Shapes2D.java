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

/**
 * Contains all Geometry based classes that perform Graphical tasks such as drawing figures, managing coordinates, and working with 2-dimensional shapes.
 */
package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import bg.x2d.Background;

/**
 * The superclass class for all generic 2-Dimensional figures. Subclasses of
 * Shapes2D in the geo package are generally pre-defined, regular shapes. A
 * Graphics object MUST be passed to to an instance of any class
 * that subclasses it so that the figures can be directly painted to the component.
 * Shapes2D is not able to be instantiated, however, its getCanvas() method get
 * can be called by referencing any one of its subclasses.
 */
public abstract class Shapes2D {

	Graphics2D canvas;
	
	Background background;
	
	int locx,locy,polySize;
	Paint paint;
	Polygon shape;
	boolean filled;

	public Shapes2D(Graphics g, Background b) {
		canvas = (Graphics2D) g;
		background = b;
	}
	
	public abstract void setProperties(int x, int y, int size, Paint p,
			boolean fill);
	public abstract void rotate(double degrees, Rotation type);
	public abstract void undraw(Paint p);
	public abstract void setLocation(int x, int y);
	
	public Background getBackground() {
		return background;
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
		if(b !=null) background = b;
	}
	
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
	
	public static Polygon drawPolygon(Point2D[] points, Graphics2D g2d, Paint paint, boolean fill) {
		Polygon poly = new Polygon();
		for(Point2D p:points)
			poly.addPoint((int)p.getX(), (int)p.getY());
		g2d.setPaint(paint);
		if(fill) 
			g2d.fillPolygon(poly);
		else
			g2d.drawPolygon(poly);
		return poly;
	}
	
	/**
	 * Algorithm that uses an algebraic rotation formula to translate a point from its current location x degrees to a new position on the coordinate plane.
	 * @param p
	 * @param origin
	 * @param degrees
	 * @return the rotated Point object.
	 */
	public static Point2D.Double rotatePoint(Point2D point, Point2D origin, double degrees) {
		double x = point.getX();
		double y = point.getY();
		double x0 = (int) origin.getX();
		double y0 = (int) origin.getY();
		double wx = x-x0;
		double wy = y-y0;
		double xresult = (wx*(Math.cos(Math.toRadians(degrees)))) - (wy*(Math.sin(Math.toRadians(degrees))));
		double yresult = (wx*(Math.sin(Math.toRadians(degrees)))) + (wy*(Math.cos(Math.toRadians(degrees))));
		double x2 = xresult+x0;
		double y2 = yresult+y0;
		return new Point2D.Double(x2,y2);
	}
	
	/**
	 * Algorithm that uses an algebraic rotation formula to translate a point from its current location x degrees to a new position on the coordinate plane.
	 * This method is used internally and is intended for use in calculating Polygon coordinate values.  Note:  This method uses and returns integer values.
	 * Mathematical operations resulting in non-integer values are rounded.
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
		int x1,y1;
		int wx = x-x0;
		int wy = y-y0;
		double xresult = (wx*(Math.cos(Math.toRadians(degrees)))) - (wy*(Math.sin(Math.toRadians(degrees))));
		double yresult = (wx*(Math.sin(Math.toRadians(degrees)))) + (wy*(Math.cos(Math.toRadians(degrees))));
		x1 = (int) Math.round(xresult);
		y1 = (int) Math.round(yresult);
		int x2 = x1+x0;
		int y2 = y1+y0;
		return new Point(x2,y2);
	}
	
	/**
	 * Draws any regular polygon using the given arguments and number of sides.  This is called by each subclass's individual draw...() method.
	 * @param loc Top left corner of the bounding box this shape should be drawn in.
	 * @param start Starting point for rotations.
	 * @param size The desired size of the regular shape.
	 * @param p
	 * @param fill
	 * @param nsides
	 * @return the Polygon that has been drawn.
	 */
	protected Polygon drawRegularPolygon(Point loc, Point start, int size, Paint p, boolean fill, boolean draw, int nsides) {
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		int degrees = 360/nsides;
		Polygon poly = new Polygon();
		Point origin = new Point(x+(size/2),y+(size/2));
		for(int i=0;i<nsides;i++) {
			start = rotatePolyPoint(start, origin, degrees);
			poly.addPoint((int)start.getX(), (int)start.getY());
		}
		canvas.setPaint(p);
		if(draw) {
			if (fill)
				canvas.fillPolygon(poly);
			else
				canvas.drawPolygon(poly);
		}
		return poly;
	}
	
	protected void rotate(int sides, double degrees) {
		
		Point start = new Point(locx+(polySize/2), locy);
		Point origin = new Point(locx+(polySize/2), locy+(polySize/2));
		start = rotatePolyPoint(start, origin, degrees);
		shape = drawRegularPolygon(new Point(locx,locy), start, polySize, paint, filled, true, sides);
	}
}
