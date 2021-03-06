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

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of a double precision vector in 2-dimensional coordinate
 * space.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Vector2d {

    private static int defaultPrecision = 5;

    public volatile double x, y;

    private volatile double mag, angle;

    public volatile int precision = defaultPrecision;

    /**
     * 
     * @param x
     * @param y
     */
    public Vector2d(final double x, final double y) {

        this.x = x;
        this.y = y;

        init();
    }

    public Vector2d(final Vector2d copy) {

        x = copy.x;
        y = copy.y;
        checkPrecision();
        mag = copy.mag;
        angle = copy.angle;
    }

    public static Vector2d fromPolar(final double mag, final double angle) {

        return new Vector2d(0, 0).setFromPolar(mag, angle);
    }

    /**
     * Sets the number of decimal places to the right where vector math should
     * be rounded. All Vector2f objects will be initialized to this value.
     * 
     * @param precision
     */
    public static void setDefaultPrecision(final int precision) {

        if (precision >= 0) {
            defaultPrecision = precision;
        }
    }

    /**
     * Returns the current decimal precision value for vector math. Default is 5
     * (i.e. x.#####).
     * 
     * @return
     */
    public static int getDecimalPrecision() {

        return defaultPrecision;
    }

    private void init() {

        angle = (float) GeoUtils.terminal(x, y);
        mag = (float) sqrt(pow(x, 2) + pow(y, 2));
    }

    private void checkPrecision() {

        BigDecimal bd = BigDecimal.valueOf(x).setScale(precision, RoundingMode.HALF_UP);
        x = bd.doubleValue();
        bd = BigDecimal.valueOf(y).setScale(precision, RoundingMode.HALF_UP);
        y = bd.doubleValue();
        init();
    }

    public Vector2f toFloatVec() {

        return new Vector2f((float) x, (float) y);
    }

    public double degs() {

        checkPrecision();
        return toDegrees(angle);
    }

    public double rads() {

        checkPrecision();
        return angle;
    }

    public double getMagnitude() {

        checkPrecision();
        return mag;
    }

    public Vector2d setXY(final double x, final double y) {

        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2d setFromPolar(double mag, final double angle) {

        if (mag < 0) {
            mag = -mag;
        }
        x = mag * cos(angle);
        y = mag * sin(angle);
        this.mag = mag;
        this.angle = angle;
        checkPrecision();
        return this;
    }

    /**
     * 
     * @param arg
     * @return
     */
    public Vector2d add(final Vector2d arg) {

        x += arg.x;
        y += arg.y;
        checkPrecision();
        return this;
    }

    /**
     * 
     * @param arg
     * @return
     */
    public Vector2d sub(final Vector2d arg) {

        x -= arg.x;
        y -= arg.y;
        checkPrecision();
        return this;
    }

    public Vector2d mult(final double factor) {

        x *= factor;
        y *= factor;
        init();
        return this;
    }

    public Vector2d div(final double factor) {

        x /= factor;
        y /= factor;
        checkPrecision();
        return this;
    }

    public Vector2d addNew(final Vector2d arg) {

        return new Vector2d(x += arg.x, y += arg.y);
    }

    public Vector2d subNew(final Vector2d arg) {

        return new Vector2d(x -= arg.x, y -= arg.y);
    }

    public Vector2d multNew(final double factor) {

        return new Vector2d(this).mult(factor);
    }

    public Vector2d divNew(final double factor) {

        return new Vector2d(this).div(factor);
    }

    /**
     * 
     * @param arg
     * @return
     */
    public double dot(final Vector2d arg) {

        checkPrecision();
        return x * arg.x + y * arg.y;
    }

    public double det(final Vector2d arg) {

        return (x * arg.y - y * arg.x);
    }

    public double cross(final Vector2d arg) {

        return det(arg);
    }

    public final void normalize() {

        double norm;
        norm = (1.0 / Math.sqrt(this.x * this.x + this.y * this.y));
        this.x *= norm;
        this.y *= norm;
        checkPrecision();
    }

    public double angleBetween(final Vector2d vec) {

        return Math.acos(dot(vec) / (mag * vec.mag));
    }

    public Vector2d interpolate(final Vector2d vec, final double alpha) {

        return this.mult(1 - alpha).add(vec.multNew(alpha));
    }

    /**
     * Rotates this vector by given radians.
     * 
     * @param angle
     *            rotation value IN RADIANS
     * @return this vector for chain calls.
     */
    public Vector2d rotate(final double rads) {

        double[] coord = GeoUtils.rotatePoint(x, y, 0, 0, rads);
        this.x = coord[0];
        this.y = coord[1];
        checkPrecision();
        return this;
    }

    /**
     * 
     * @param theta
     * @return
     */
    public Vector2d rotateNew(final double rads) {

        return new Vector2d(x, y).rotate(rads);
    }

    public Vector2d negate() {

        x = -x;
        y = -y;
        checkPrecision();
        return this;
    }

    public Vector2d negateNew() {

        double x = -this.x;
        double y = -this.y;
        return new Vector2d(x, y);
    }

    public Vector2d negateX() {

        x = -x;
        checkPrecision();
        return this;
    }

    public Vector2d negateY() {

        y = -y;
        checkPrecision();
        return this;
    }

    public Vector2d clamp(final double min, final double max) {

        double mag = getMagnitude();
        if (mag < min) {
            setFromPolar(min, angle);
        }
        if (mag > max) {
            setFromPolar(max, angle);
        }
        return this;
    }

    public Vector2d clampNew(final double min, final double max) {

        Vector2d copy = new Vector2d(this);
        double mag = getMagnitude();
        if (mag < min) {
            copy.setFromPolar(min, angle);
        }
        if (mag > max) {
            copy.setFromPolar(max, angle);
        }
        return copy;
    }

    public Point2D.Double applyTo(final Point2D.Double p, final double multiplier) {

        p.setLocation(p.x + (x * multiplier), p.y + (y * multiplier));
        return p;
    }

    public Point2D.Double applyToNew(final Point2D.Double p, final double multiplier) {

        return new Point2D.Double(p.x + (x * multiplier), p.y + (y * multiplier));
    }

    public PointUD applyTo(final PointUD p, final double multiplier) {

        p.setLocation(p.ux + (x * multiplier), p.uy + (y * multiplier));
        return p;
    }

    public PointUD applyToNew(final PointUD p, final double multiplier) {

        return new PointUD(p.ux + (x * multiplier), p.uy + (y * multiplier));
    }

    @Override
    public String toString() {

        checkPrecision();
        return "mag=" + mag + " theta=" + Math.toDegrees(angle) + " [" + x + ", " + y + "]";
    }
}
