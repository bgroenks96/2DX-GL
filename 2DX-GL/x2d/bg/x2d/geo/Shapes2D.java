/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
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
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public abstract class Shapes2D {

    int locx, locy, polySize;
    Paint paint;
    Polygon shape;
    boolean filled;

    @Deprecated
    /**
     * Replaced with a more logical and OO-friendly constructor.
     * Note: This deprecated constructor currently does nothing.
     * @param g
     * @param b
     */
    public Shapes2D(final Graphics g, final Background b) {

    }

    @Deprecated
    public Shapes2D(final Background b) {

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
    public Shapes2D(final int x, final int y, final int size, final Paint p, final boolean fill) {

        setProperties(x, y, size, p, fill);
    }

    public abstract void setProperties(int x, int y, int size, Paint p, boolean fill);

    public abstract void rotate(double degrees, Rotation type);

    public void setLocation(final int x, final int y) {

        locx = x;
        locy = y;
    }

    public void setPaint(final Paint p) {

        if (p != null) {
            this.paint = p;
        }
    }

    public void draw(final Graphics2D g) {

        g.setPaint(paint);
        g.translate(locx, locy);
        if (filled) {
            g.fillPolygon(shape);
        } else {
            g.drawPolygon(shape);
        }
        g.translate(-locx, -locy);
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
     * Algorithm that uses a trigonometric rotation formula to translate a point
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
    protected Point rotatePolyPoint(final Point p, final Point origin, final double degrees) {

        int x = (int) p.getX();
        int y = (int) p.getY();
        int x0 = (int) origin.getX();
        int y0 = (int) origin.getY();
        int x1, y1;
        int wx = x - x0;
        int wy = y - y0;
        double xresult = (wx * (Math.cos(Math.toRadians(degrees)))) - (wy * (Math.sin(Math.toRadians(degrees))));
        double yresult = (wx * (Math.sin(Math.toRadians(degrees)))) + (wy * (Math.cos(Math.toRadians(degrees))));
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
    protected Polygon drawRegularPolygon(final Point loc, Point start, final int size, final Paint p,
            final boolean fill, final boolean draw, final int nsides) {

        int x = (int) loc.getX();
        int y = (int) loc.getY();
        int degrees = 360 / nsides;
        Polygon poly = new Polygon();
        Point origin = new Point(x + (size / 2), y + (size / 2));
        for (int i = 0; i < nsides; i++ ) {
            start = rotatePolyPoint(start, origin, degrees);
            poly.addPoint((int) start.getX(), (int) start.getY());
        }

        return poly;
    }

    protected void rotate(final int sides, final double degrees) {

        Point start = new Point(locx + (polySize / 2), locy);
        Point origin = new Point(locx + (polySize / 2), locy + (polySize / 2));
        start = rotatePolyPoint(start, origin, degrees);
        shape = drawRegularPolygon(new Point(locx, locy), start, polySize, paint, filled, true, sides);
    }
}
