/*
 *  Copyright (C) 2012-2014 Brian Groenke
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

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Contains static convenience method for Geometric operations. All angular operations take and
 * return values in radians.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class GeoUtils {

	@Deprecated
	public static volatile boolean degrees;

	private GeoUtils() {}
	
	/**
	 * Computes the intersection point between two lines, if one exists.
	 * @param x1 a first x point of the first line
	 * @param y1 a first y point of the first line
	 * @param x2 a second x point of the first line
	 * @param y2 a second y point of the first line
	 * @param x3 a first x point of the second line
	 * @param y3 a first y point of the second line
	 * @param x4 a second x point of the second line
	 * @param y4 a second y point of the second line
	 * @return the intersection point, or null if one doesn't exist.
	 */
	public static PointUD lineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		boolean m1u = (x2 - x1) == 0;
		boolean m2u = (x4 - x3) == 0;
		if(m1u && m2u)
			return null;
		else if(m1u) {
			double m = (y4 - y3) / (x4 - x3);
			double spx = m * -x3;
			double yint = spx + y3;
			return new PointUD(x1, m * x1 + yint);
		} else if(m2u) {
			double m = (y2 - y1) / (x2 - x1);
			double spx = m * -x1;
			double yint = spx + y1;
			return new PointUD(x3, m * x3 + yint);
		}
		
		double m1 = (y2 - y1) / (x2 - x1);
		double spx1 = m1 * -x1;
		double yint1 = spx1 + y1;
		double m2 = (y4 - y3) / (x4 - x3);
		double spx2 = m2 * -x3;
		double yint2 = spx2 + y3;
		double nt = yint2 - yint1;
		double terms = m1 - m2;
		if(terms == 0)
			return null;
		double xp = nt / terms;
		double yp = m1 * xp + yint1;
		return new PointUD(xp, yp);
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
	public static PointUD getMdpt(PointUD p1, PointUD p2) {
		double x1 = p1.getX();
		double x2 = p2.getX();
		double mdptx = (x1 + x2) / 2;
		double y1 = p1.getY();
		double y2 = p2.getY();
		double mdpty = (y1 + y2) / 2;
		return new PointUD(mdptx, mdpty);
	}
	
	public static double pythag(double a, double b) {
		return sqrt(pow(a,2) + pow(b, 2));
	}
	
	public static double dist(PointUD p1, PointUD p2) {
		return sqrt(pow(p2.ux - p1.ux,2) + pow(p2.uy - p1.uy, 2));
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
	 *            true if the polygon should be filled completely, false if only an outline should
	 *            be drawn
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
	 * Algorithm that uses an algebraic rotation formula to transform a point from its current
	 * location x rads to a new position on the coordinate plane.
	 * 
	 * This method is for convenience. It uses the given points to call the actual rotatePoint
	 * method, which takes double parameters.
	 * 
	 * @param p
	 *            point to be rotated
	 * @param origin
	 *            point to rotate around
	 * @param angle
	 *            angle measure to rotate, in radians.
	 * @return the rotated Point object.
	 */
	public static Point2D.Double rotatePoint(Point2D point, Point2D origin,
			double angle) {
		double[] pts = rotatePoint(point.getX(), point.getY(), origin.getX(),
				origin.getY(), angle);
		return new Point2D.Double(pts[0], pts[1]);
	}
	
	public static PointUD rotatePoint(PointUD point, PointUD origin, double angle) {
		double[] pts = rotatePoint(point.ux, point.uy, origin.ux, origin.uy, angle);
		return new PointUD(pts[0], pts[1]);
	}

	/**
	 * Algorithm that uses an algebraic rotation formula to transform a point from its current
	 * location x rads to a new position on the coordinate plane.
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param x0
	 *            x origin coordinate
	 * @param y0
	 *            y origin coordinate
	 * @param angle
	 *            radians to rotate around the origin
	 * @return
	 */
	public static double[] rotatePoint(double x, double y, double x0,
			double y0, double angle) {
		if (degrees) {
			angle = Math.toRadians(angle);
		}
		double wx = x - x0;
		double wy = y - y0;
		/* xprime = x*cos(theta) - y*sin(theta) */
		double xresult = (wx * (Math.cos(angle))) - (wy * (Math.sin(angle)));
		/* yprime = x*sin(theta) + y*cos(theta) */
		double yresult = (wx * (Math.sin(angle))) + (wy * (Math.cos(angle)));

		double x2 = xresult + x0;
		double y2 = yresult + y0;
		return new double[] { x2, y2 };
	}

	/**
	 * Computes the terminal position of an angle using the given x, y coordinates drawn from the
	 * origin. The value returned from this method will be 0-2pi.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double terminal(double x, double y) {
		double ref = PI / 2;
		if (x != 0) {
			ref = abs(atan(y / x));
		}
		int quad = GeoUtils.quadrant(x, y) - 1;
		return (quad * (PI / 2)) + ref;
	}

	/**
	 * Shorthand method for calling <code>{@link #terminal(double, double)}</code>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double term(double x, double y) {
		return terminal(x, y);
	}

	/**
	 * Computes the reference angle for the given x, y coordinates drawn from the origin. The value
	 * returned from this method will be between -pi/2 and pi/2.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double ref(double x, double y) {
		double term = term(x, y);
		int quad = GeoUtils.quadrant(x, y) - 1;
		return term - (quad * (PI / 2));
	}

	/**
	 * Computes the quadrant the given point lies in based on the origin (0, 0).
	 * 
	 * @param x
	 *            x-coord of the point
	 * @param y
	 *            y-coord of the point
	 * @return int value 1, 2, 3 or 4 representing the point's quadrant based on the origin.
	 */
	public static int quadrant(double x, double y) {
		if (x >= 0 && y >= 0) {
			return 1;
		} else if (x < 0 && y > 0) {
			return 2;
		} else if (x <= 0 && y <= 0) {
			return 3;
		} else if (x > 0 && y < 0) {
			return 4;
		} else {
			return 0;
		}
	}
}
