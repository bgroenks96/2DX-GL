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

package bg.x2d.gen;

import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * Generates a random polygon with n sides and inside of the specified
 * Rectangle.
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class PolyGenerator implements Generator <Polygon> {

    private int x, y, width, height, n;
    private NumberGenerator <Integer> xgen, ygen;

    public PolyGenerator(final Rectangle bounds, final int nsides) {

        setBounds(bounds, nsides);
    }

    public void setBounds(final Rectangle bounds, final int nsides) {

        if (bounds != null) {
            x = (int) Math.round(bounds.getX());
            y = (int) Math.round(bounds.getY());
            width = (int) Math.round(bounds.getWidth());
            height = (int) Math.round(bounds.getHeight());
        } else {
            x = 0;
            y = 0;
            width = 10;
            height = 10;
        }
        n = nsides;
        xgen = new NumberGenerator <Integer>(x, x + width);
        ygen = new NumberGenerator <Integer>(y, y + height);
    }

    @Override
    public Polygon generate() {

        Polygon p = new Polygon();

        for (int i = n; i > 0; i-- ) {
            int x = xgen.generate();
            int y = ygen.generate();
            p.addPoint(x, y);
        }
        return p;
    }

}
