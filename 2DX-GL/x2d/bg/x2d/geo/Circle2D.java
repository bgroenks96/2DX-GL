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

import java.awt.Graphics2D;
import java.awt.Paint;

public class Circle2D extends Shapes2D {

    public Circle2D(final int x, final int y, final int size, final Paint p, final boolean fill) {

        super(x, y, size, p, fill);
    }

    /**
     * Currently does nothing.
     */
    @Override
    public void rotate(final double degrees, final Rotation type) {

        // Do nothing
    }

    @Override
    public void setProperties(final int x, final int y, final int size, final Paint p, final boolean fill) {

        locx = x;
        locy = y;
        polySize = size;
        paint = p;
        filled = fill;
    }

    @Override
    public void draw(final Graphics2D g) {

        g.setPaint(paint);
        if (filled) {
            g.fillOval(locx, locy, polySize, polySize);
        } else {
            g.drawOval(locx, locy, polySize, polySize);
        }
    }
}
