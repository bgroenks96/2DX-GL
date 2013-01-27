/*
 *  Copyright © 2011-2013 Brian Groenke
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

import java.awt.*;
import java.util.*;

/**
 * Paintbrush provides an easy to use utility to draw figures and polygons.
 * Calling its <code>nextStroke</code> method will add the specified coordinate
 * to the object's list, and can be immediately drawn onto the canvas if the
 * boolean passed is true.<br>
 * The width and height parameters in the constructors should be the size of the
 * component of which you are painting on, while the Graphics parameter is the
 * Graphics object the component uses in its <code>paint</code> method.<br>
 * Paintbrush will allow you to draw arbitrary, open figures, however, they are
 * not able to be treated as objects and cannot be specifically redrawn/undrawn.
 * It will also allow you to create a Polygon from your figure which will
 * automatically close the first and last points with a straight line. The
 * <code>drawPolygon</code> method will return the Polygon object you have
 * created as well as draw/fill the shape on screen.<br>
 * <br>
 * <code>
 * public void exampleMethod() {<br>
 *     //Create a paintbrush object, assuming the two integer values and the Graphics object have been defined elsewhere in the class.<br>
 *     Paintbrush brush = new Paintbrush(width,height,graphics);<br>
 *     //If you were to set these boolean arguments to true, the line would be immediately painted onto the canvas.<br>
 *     brush.nextStroke(20,20,false);<br>
 *     brush.nextStroke(30,40,false);<br>
 *     brush.nextStroke(40,20,false);<br>
 *     //We have added all of our coordinates (similar to how the Polygon object works) so now we can draw it.<br>
 *     brush.drawPolygon(true);<br>
 * }<br>
 * </code><br>
 * An example of drawing an arbitrary figure with Paintbrush:<br>
 * <code><br>
 * public void exampleTwo() {<br>
 *     //Create the paintbrush object, assuming all variables are defined elsewhere.<br>
 *     Paintbrush brush = new Paintbrush(width,height,graphics);<br>
 *     brush.nextStroke(30,42,true);<br>
 *     //Note that this stroke won't show up after this method is called since the boolean passed is false.<br>
 *     brush.nextStroke(40,20,false);<br>
 *     brush.nextStroke(56,104,false);<br>
 *     //This method will take all of the coordinates you have given it and draw the connected lines onto the canvas.<br>
 *     brush.drawStrokes();<br>
 *     //This clears the coordinate data so a new figure can be drawn.<br>
 *     brush.clear();<br>
 * }<br>
 * </code><br>
 * 
 * @since 2DX 1.0 (1st Edition)
 * @see java.awt.Polygon
 * 
 */

public class Paintbrush2D extends FreeDraw2D {

	ArrayList<int[]> coords;
	
	public Paintbrush2D() {
		coords = new ArrayList<int[]> ();
	}

	@Deprecated
	public Paintbrush2D(int width, int height, Graphics g) {
		//super(g);
		coords = new ArrayList<int[]>();
	}

	@Deprecated
	/**
	 * @param width never used
	 * @param height never used
	 * @param g
	 */
	public Paintbrush2D(int width, int height, Graphics2D g) {
		//super(g);
		coords = new ArrayList<int[]>();
	}

	/**
	 * Adds the passed coordinate to the Paintbrush's list and optionally draws
	 * it onto the canvas.
	 * 
	 * @param x
	 *            the x coordinate of the point
	 * @param y
	 *            the y coordinate of the point
	 * @param draw
	 *            if true, the line will be drawn onto the canvas immediately.
	 * @see #drawStrokes()
	 */

	public void nextStroke(int x, int y, boolean draw, Graphics canvas) {
		if (coords.size() == 0) {
			int[] xy = { x, y };
			coords.add(xy);
		} else {
			int[] xy = { x, y };
			if (draw) {
				int[] last = lastStroke();
				canvas.drawLine(last[0], last[1], x, y);
			}
			coords.add(xy);
		}
	}

	/**
	 * Clears the currently stored coordinates list and sets it to the passed x
	 * and y point arrays. Each position will correspond to its equivalent in
	 * the opposite array. i.e. (xpoints[0],ypoints[0]) is a point.
	 * 
	 * @param xpoints
	 *            An array of x points corresponding to an array of y points.
	 * @param ypoints
	 *            An array of y points corresponding to an array of x points.
	 * @throws GeoException
	 */

	public void setPoints(int[] xpoints, int[] ypoints) throws GeoException {
		if (xpoints.length != ypoints.length) {
			throw (new GeoException(
					"Invalid argument: arrays must be equal in size"));
		} else if (xpoints.length == 0 || ypoints.length == 0) {
			throw (new GeoException("Invalid argument: array cannot be empty"));
		} else {
			coords.clear();
			for (int i = 0; i < xpoints.length; i++) {
				int[] xy = new int[2];
				xy[0] = xpoints[i];
				xy[1] = ypoints[i];
				coords.add(xy);
			}
		}
	}

	/**
	 * Draws all currently set lines to the canvas.
	 * 
	 * @see #drawPolygon(boolean fill)
	 */

	public void drawStrokes(Graphics canvas) {
		for (int i = 0; i < coords.size(); i++) {
			if (i == 0) {
				continue;
			}
			int[] dest = coords.get(i);
			int[] src = coords.get(i - 1);
			canvas.drawLine(src[0], src[1], dest[0], dest[1]);
		}
	}

	/**
	 * Draws a polygon using the currently set coordinates to the canvas.
	 * 
	 * @param fill
	 *            determines whether or not to fill the drawn polygon, or just
	 *            draw an outline.
	 * @return the constructed Polygon object.
	 * @throws GeoException
	 * @see java.awt.Polygon
	 */

	public Polygon drawPolygon(Graphics canvas, boolean fill) throws GeoException {
		if (coords.size() == 0) {
			throw (new GeoException("Empty coordinate data."));
		} else {
			Polygon p = new Polygon();
			for (int i = 0; i < coords.size(); i++) {
				int[] points = coords.get(i);
				p.addPoint(points[0], points[1]);
			}
			if (fill) {
				canvas.fillPolygon(p);
			} else {
				canvas.drawPolygon(p);
			}
			return p;
		}
	}

	/**
	 * Removes the last set point from the coordinate list. This will NOT undraw
	 * anything from the canvas.
	 */

	public void deleteLast() {
		coords.remove(coords.size() - 1);
	}

	/**
	 * @return the coordinates of the last stroke made.
	 */

	public int[] lastStroke() {
		return coords.get(coords.size() - 1);
	}

	/**
	 * Retrieves the specified stroke if existent.
	 * 
	 * @param index
	 * @return an int array representing a x and y coordinate.
	 * @throws GeoException
	 *             if argument is less than zero or greater than the current
	 *             array capacity.
	 */

	public int[] getStrokeAt(int index) throws GeoException {
		if (index < 0) {
			throw (new GeoException(
					"Illegal argument: index cannot be less than zero"));
		} else if (index > (coords.size() - 1)) {
			throw (new GeoException(
					"Illegal argument: requested index exceeds the array capacity.  Size = "
							+ coords.size()));
		} else {
			return coords.get(index);
		}
	}

	/**
	 * Clears this Paintbrush object's coordinate/stroke data.  Does nothing to the screen.
	 * 
	 * @see #isCleared()
	 */

	public void clear() {
		coords.clear();
	}

	/**
	 * @return <code>true</code> if the internal coordinate ArrayList is empty.
	 *         <code>false</code> otherwise.
	 */

	public boolean isCleared() {
		if (coords.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
}
