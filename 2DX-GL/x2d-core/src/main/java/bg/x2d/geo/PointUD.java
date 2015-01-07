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

package bg.x2d.geo;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Point Universal-Double. Implementation of a 2D point that stores its value as
 * a double and allows quick access to it as long, int, and float. Qualifies
 * polymorphically as a Point or Point2D, so it can often be passed to Java2D
 * functions (including AWT/Swing). <br/>
 * <br/>
 * Changes to the data should be made through <code>setLocation</code> to ensure
 * that the changes are also reflected in all of methods/fields inherited by PointUD.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class PointUD extends Point {

    /**
     * 
     */
    private static final long serialVersionUID = 6891695259903881318L;

    public double ux, uy;

    public PointUD(final double x, final double y) {

        this.ux = x;
        this.uy = y;
        super.setLocation(x, y);
    }

    public PointUD(final long x, final long y) {

        this((double) x, (double) y);
    }

    public PointUD(final int x, final int y) {

        this((double) x, (double) y);
    }

    public PointUD(final Point2D p2d) {

        this(p2d.getX(), p2d.getY());
    }

    @Override
    public double getX() {

        return ux;
    }

    @Override
    public double getY() {

        return uy;
    }

    @Override
    public void setLocation(final double x, final double y) {

        this.ux = x;
        this.uy = y;
        super.setLocation(x, y);
    }

    @Override
    public void setLocation(final Point2D p) {

        setLocation(p.getX(), p.getY());
    }

    @Override
    public void setLocation(final Point p) {

        setLocation(p.getX(), p.getY());
    }

    public void translate(final double x, final double y) {

        setLocation(ux + x, uy + y);
    }

    public PointUD translateNew(final double x, final double y) {

        return new PointUD(ux + x, uy + y);
    }

    public double distance(final PointUD p) {

        return GeoUtils.dist(this, p);
    }

    public long getLongX() {

        return Math.round(ux);
    }

    public long getLongY() {

        return Math.round(uy);
    }

    public int getIntX() {

        return (int) Math.round(ux);
    }

    public int getIntY() {

        return (int) Math.round(uy);
    }

    public float getFloatX() {

        return (float) ux;
    }

    public float getFloatY() {

        return (float) uy;
    }

    public Point2D.Double getDoublePoint() {

        return new Point2D.Double(ux, uy);
    }

    public Point2D.Float getFloatPoint() {

        return new Point2D.Float((float) ux, (float) uy);
    }

    @Override
    public String toString() {

        return new String("[" + ux + ", " + uy + "]");
    }
}
