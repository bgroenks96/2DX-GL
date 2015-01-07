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
 * Implementation of a floating point vector in 2-dimensional coordinate space.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Vector2f {

    private static int defaultPrecision = 5;

    public volatile float x, y;

    private volatile float mag, angle;

    public volatile int precision = defaultPrecision;

    /**
     * 
     * @param x
     * @param y
     */
    public Vector2f(final float x, final float y) {

        this.x = x;
        this.y = y;

        init();
    }

    public Vector2f(final Vector2f copy) {

        x = copy.x;
        y = copy.y;
        checkPrecision();
        mag = copy.mag;
        angle = copy.angle;
    }

    public static Vector2f fromPolar(final float mag, final float angle) {

        return new Vector2f(0, 0).setFromPolar(mag, angle);
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
        x = bd.floatValue();
        bd = BigDecimal.valueOf(y).setScale(precision, RoundingMode.HALF_UP);
        y = bd.floatValue();
        init();
    }

    public Vector2d toDoubleVec() {

        return new Vector2d(x, y);
    }

    public float degs() {

        checkPrecision();
        return (float) toDegrees(angle);
    }

    public float rads() {

        checkPrecision();
        return angle;
    }

    public float getMagnitude() {

        checkPrecision();
        return mag;
    }

    public Vector2f setXY(final float x, final float y) {

        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets the direction and magnitude of this vector from polar coordinates.
     * 
     * @param mag
     * @param angle
     *            direction angle in radians
     * @return
     */
    public Vector2f setFromPolar(float mag, final float angle) {

        if (mag < 0) {
            mag = -mag;
        }
        x = (float) (mag * cos(angle));
        y = (float) (mag * sin(angle));
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
    public Vector2f add(final Vector2f arg) {

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
    public Vector2f sub(final Vector2f arg) {

        x -= arg.x;
        y -= arg.y;
        checkPrecision();
        return this;
    }

    public Vector2f mult(final float factor) {

        x *= factor;
        y *= factor;
        init();
        return this;
    }

    public Vector2f div(final float factor) {

        x /= factor;
        y /= factor;
        checkPrecision();
        return this;
    }

    public Vector2f addNew(final Vector2f arg) {

        return new Vector2f(x += arg.x, y += arg.y);
    }

    public Vector2f subNew(final Vector2f arg) {

        return new Vector2f(x -= arg.x, y -= arg.y);
    }

    public Vector2f multNew(final float factor) {

        return new Vector2f(this).mult(factor);
    }

    public Vector2f divNew(final float factor) {

        return new Vector2f(this).div(factor);
    }

    /**
     * 
     * @param arg
     * @return
     */
    public float dot(final Vector2f arg) {

        checkPrecision();
        return x * arg.x + y * arg.y;
    }

    public float det(final Vector2f arg) {

        return x * arg.y - y * arg.x;
    }

    public float cross(final Vector2f arg) {

        return det(arg);
    }

    public final void normalize() {

        float norm;
        norm = (float) (1.0 / Math.sqrt(this.x * this.x + this.y * this.y));
        this.x *= norm;
        this.y *= norm;
        checkPrecision();
    }

    public float angleBetween(final Vector2f vec) {

        return (float) Math.acos(dot(vec) / (mag * vec.mag));
    }

    public Vector2f interpolate(final Vector2f vec, final float alpha) {

        return this.mult(1 - alpha).add(vec.multNew(alpha));
    }

    /**
     * Rotates this vector by given radians.
     * 
     * @param angle
     *            rotation value IN RADIANS
     * @return this vector for chain calls.
     */
    public Vector2f rotate(final float rads) {

        double[] coord = GeoUtils.rotatePoint(x, y, 0, 0, rads);
        this.x = (float) coord[0];
        this.y = (float) coord[1];
        checkPrecision();
        return this;
    }

    /**
     * 
     * @param theta
     * @return
     */
    public Vector2f rotateNew(final float rads) {

        return new Vector2f(x, y).rotate(rads);
    }

    public Vector2f negate() {

        x = -x;
        y = -y;
        checkPrecision();
        return this;
    }

    public Vector2f negateNew() {

        float x = -this.x;
        float y = -this.y;
        return new Vector2f(x, y);
    }

    public Vector2f negateX() {

        x = -x;
        checkPrecision();
        return this;
    }

    public Vector2f negateY() {

        y = -y;
        checkPrecision();
        return this;
    }

    public Vector2f clamp(final float min, final float max) {

        float mag = getMagnitude();
        if (mag < min) {
            setFromPolar(min, angle);
        }
        if (mag > max) {
            setFromPolar(max, angle);
        }
        return this;
    }

    public Vector2f clampNew(final float min, final float max) {

        Vector2f copy = new Vector2f(this);
        float mag = getMagnitude();
        if (mag < min) {
            copy.setFromPolar(min, angle);
        }
        if (mag > max) {
            copy.setFromPolar(max, angle);
        }
        return copy;
    }

    public Point2D.Float applyTo(final Point2D.Float p, final float multiplier) {

        p.setLocation(p.x + (x * multiplier), p.y + (y * multiplier));
        return p;
    }

    public Point2D.Float applyToNew(final Point2D.Float p, final float multiplier) {

        return new Point2D.Float(p.x + (x * multiplier), p.y + (y * multiplier));
    }

    public PointUD applyTo(final PointUD p, final float multiplier) {

        p.setLocation(p.getFloatX() + (x * multiplier), p.getFloatY() + (y * multiplier));
        return p;
    }

    public PointUD applyToNew(final PointUD p, final float multiplier) {

        return new PointUD(p.getFloatX() + (x * multiplier), p.getFloatY() + (y * multiplier));
    }

    @Override
    public String toString() {

        checkPrecision();
        return "mag=" + mag + " theta=" + Math.toDegrees(angle) + " [" + x + ", " + y + "]";
    }
}
