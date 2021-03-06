/*
 *  Copyright (C) 2011-2013 Brian Groenke
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

import java.awt.Paint;
import java.awt.Point;

/**
 * Shape2D implementation for a regular polygon with 3 or more vertices.
 * 
 * @author Brian Groenke
 */
public final class UniformPolygon2D extends Shape2D {

    private int nverts;

    /**
     * @param nverts
     *            number of vertices (and sides) this UniformPolygon2D should be
     *            created with
     * @param x
     *            x coordinate of the polygon
     * @param y
     *            y coordinate of the polygon
     * @param size
     *            radius from center to each vertex of the regular polygon
     * @param p
     *            color of the polygon
     * @param fill
     *            true if should be filled, false otherwise
     */
    public UniformPolygon2D(final int nverts,
                            final int x,
                            final int y,
                            final int size,
                            final Paint p,
                            final boolean fill) {

        super(x, y, size, p, fill);
        this.nverts = nverts;
    }

    @Override
    public void rotate(final double degrees, final Rotation type) {

        if (degrees < 0) {
            throw (new IllegalArgumentException(
                            "Illegal theta value: specify a positive integer for degree of rotation"));
        }
        if (type == null) {
            throw (new IllegalArgumentException("passed Rotation type cannot be null"));
        }
        if (shape == null) {
            try {
                throw (new GeoException(
                                "shape must have been drawn or have set properties before a rotation can be performed"));
            } catch (GeoException e) {
                e.printStackTrace();
            }
        }
        if (type == Rotation.COUNTER_CLOCKWISE) {
            double deg = -degrees;
            rotate(nverts, deg);
        } else if (type == Rotation.CLOCKWISE) {
            rotate(nverts, degrees);
        }
    }

    @Override
    public void setProperties(final int x, final int y, final int size, final Paint p, final boolean fill) {

        // use super tags for clarity - these are fields provided by parent type
        // Shapes2D
        super.locx = x;
        super.locy = y;
        super.polySize = size;
        super.paint = p;
        super.filled = fill;
        super.shape = drawRegularPolygon(new Point(x, y), new Point(x + (size / 2), y), size, p, fill, false, nverts);
    }

    /**
     * @param nverts
     *            number of vertices (sides); must be a value >= 3
     * @throws IllgalArgumentException
     *             if <code>nverts</code> is < 3
     */
    public void setVertexCount(final int nverts) {

        if (nverts < 3) throw new IllegalArgumentException("illegal value for nverts: must be > 3");
        this.nverts = nverts;
    }

    public int getVertexCount() {

        return nverts;
    }
}
