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

package bg.x2d.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Contains static convenience method for Geometric operations.
 * 
 * @author Brian Groenke
 * 
 */
public class GeoUtils {

	public static volatile boolean degrees;

	private GeoUtils() {
	}

	/**
	 * Finds the midpoint of the line segment connecting the two given points.
	 * 
	 * @param p1
	 *            the first point of the line segment
	 * @param p2
	 *            the second point of the line segment
	 * @return the midpoint of the segment connecting p1 to p2
	 */
	public static PointLD getMdpt(PointLD p1, PointLD p2) {
		double x1 = p1.getX();
		double x2 = p2.getX();
		double mdptx = (x1 + x2) / 2;
		double y1 = p1.getY();
		double y2 = p2.getY();
		double mdpty = (y1 + y2) / 2;
		return new PointLD(mdptx, mdpty);
	}

	/**
	 * Draws a polygon on screen using the points provided as vertices.
	 * 
	 * @param points
	 *            vertices of the polygon
	 * @param g2d
	 *            graphics object to draw on
	 * @param paint
	 *            paint to be applied to the polygon
	 * @param fill
	 *            true if the polygon should be filled completely, false if only
	 *            an outline should be drawn
	 * @return
	 */
	public static Polygon drawPolygon(Point2D[] points, Graphics2D g2d,
			Paint paint, boolean fill) {
		Polygon poly = new Polygon();
		for (Point2D p : points) {
			poly.addPoint((int) p.getX(), (int) p.getY());
		}
		g2d.setPaint(paint);
		if (fill) {
			g2d.fillPolygon(poly);
		} else {
			g2d.drawPolygon(poly);
		}
		return poly;
	}

	/**
	 * Algorithm that uses an algebraic rotation formula to transform a point
	 * from its current location x rads/degrees to a new position on the coordinate
	 * plane.
	 * 
	 * @param p
	 *            point to be rotated
	 * @param origin
	 *            point to rotate around
	 * @param angle
	 *            angle measure to rotate (theta); in degrees or radians depending
	 *            on the GeoUtils 'degrees' value.
	 * @return the rotated Point object.
	 */
	public static Point2D.Double rotatePoint(Point2D point, Point2D origin,
			double angle) {
		if(degrees)
			angle = Math.toRadians(angle);
		
		double x = point.getX();
		double y = point.getY();
		double x0 = origin.getX();
		double y0 = origin.getY();
		double wx = x - x0;
		double wy = y - y0;
		/* xprime = x*cos(theta) - y*sin(toRadians(theta)) */
		double xresult = (wx * (Math.cos(angle)))
				- (wy * (Math.sin(angle)));
		/* yprime = x*sin(theta) + y*cos(toRadians(theta)) */
		double yresult = (wx * (Math.sin(angle)))
				+ (wy * (Math.cos(angle)));

		double x2 = xresult + x0;
		double y2 = yresult + y0;
		return new Point2D.Double(x2, y2);
	}

	/**
	 * Computes the terminal position of an angle using the given x, y coordinates drawn from the origin.
	 * The value returned from this method will be 0-2pi or 0-360 degrees.
	 * @param x
	 * @param y
	 * @return
	 */
	public static double terminal(double x, double y) {
		double ref = PI / 2;
		if(x != 0)
			ref = abs(atan(y / x));
		int quad = GeoUtils.quadrant(x, y) - 1;
		return GeoUtils.chk((quad * (PI / 2)) + ref);
	}

	/**
	 * Computes the quadrant the given point lies in based on the origin (0, 0).
	 * @param x x-coord of the point
	 * @param y y-coord of the point
	 * @return int value 1, 2, 3 or 4 representing the point's quadrant based on the origin.
	 */
	public static int quadrant(double x, double y) {
		if(x >= 0 && y>= 0)
			return 1;
		else if(x <= 0 && y >= 0)
			return 2;
		else if(x <= 0 && y <= 0)
			return 3;
		else if(x >= 0 && y <= 0)
			return 4;
		else
			return 0;
	}

	/**
	 * Checks the 'degrees' boolean to see if the given angle should be converted to degrees.
	 * @param angle
	 * @return
	 */
	private static double chk(double angle) {
		if(GeoUtils.degrees)
			return toDegrees(angle);
		else
			return angle;
	}
}
