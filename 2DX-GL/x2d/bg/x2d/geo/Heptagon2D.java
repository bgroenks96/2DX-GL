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

import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;

public class Heptagon2D extends Shapes2D {

    public Heptagon2D(final int x, final int y, final int size, final Paint p, final boolean fill) {

        super(x, y, size, p, fill);
    }

    public void drawHeptagon(final int x, final int y, final int size, final Paint p, final boolean fill) {

        Polygon hept = drawRegularPolygon(new Point(x, y), new Point(x + (size / 2), y), size, p, fill, true, 7);

        shape = hept;
        locx = x;
        locy = y;
        polySize = size;
        paint = p;
        filled = fill;
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
            rotate(7, deg);
        } else if (type == Rotation.CLOCKWISE) {
            rotate(7, degrees);
        }
    }

    @Override
    public void setProperties(final int x, final int y, final int size, final Paint p, final boolean fill) {

        locx = x;
        locy = y;
        polySize = size;
        paint = p;
        filled = fill;
        shape = drawRegularPolygon(new Point(x, y), new Point(x + (size / 2), y), size, p, fill, false, 7);
    }
}
