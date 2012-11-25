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

package bg.x2d.geo;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 * Contains static convenience method for Geometric operations.
 * @author Brian Groenke
 *
 */
public class GeoUtils {

	private GeoUtils() {}
	
	/**
	 * Finds the midpoint of the line segment connecting the two given points.
	 * @param p1 the first point of the line segment
	 * @param p2 the second point of the line segment
	 * @return the midpoint of the segment connecting p1 to p2
	 */
	public static PointLD getMdpt(PointLD p1, PointLD p2) {
		double x1 = p1.getX();
		double x2 = p2.getX();
		double mdptx = (x1+x2) / 2;
		double y1 = p1.getY();
		double y2 = p2.getY();
		double mdpty = (y1 + y2) / 2;
		return new PointLD(mdptx, mdpty);
	}

	/**
	 * Draws a polygon on screen using the points provided as vertices.
	 * @param points vertices of the polygon
	 * @param g2d graphics object to draw on
	 * @param paint paint to be applied to the polygon
	 * @param fill true if the polygon should be filled completely, false if only an outline should be drawn
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
	 * from its current location x degrees to a new position on the coordinate
	 * plane.
	 * 
	 * @param p point to be rotated
	 * @param origin point to rotate around
	 * @param degrees angle measure to rotate (theta); in degrees
	 * @return the rotated Point object.
	 */
	public static Point2D.Double rotatePoint(Point2D point, Point2D origin,
			double degrees) {
		double x = point.getX();
		double y = point.getY();
		double x0 = origin.getX();
		double y0 = origin.getY();
		double wx = x - x0;
		double wy = y - y0;
		/* xprime = x*cos(theta) - y*sin(toRadians(theta)) */
		double xresult = (wx * (Math.cos(Math.toRadians(degrees))))
				- (wy * (Math.sin(Math.toRadians(degrees))));
		/* yprime = x*sin(theta) + y*cos(toRadians(theta)) */
		double yresult = (wx * (Math.sin(Math.toRadians(degrees))))
				+ (wy * (Math.cos(Math.toRadians(degrees))));

		double x2 = xresult + x0;
		double y2 = yresult + y0;
		return new Point2D.Double(x2, y2);
	}
}
