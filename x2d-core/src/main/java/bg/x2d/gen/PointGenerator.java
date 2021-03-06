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

import java.awt.Point;

import bg.x2d.geo.PointUD;

/**
 * Generates a random point using the given x and y bounds.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class PointGenerator implements Generator<Point> {

    private double x1, x2, y1, y2;

    public PointGenerator(final double x1, final double y1, final double x2, final double y2) {

        setBounds(x1, y1, x2, y2);
    }

    @Override
    public PointUD generate() {

        NumberGenerator<Double> gen = new NumberGenerator<Double>(x1, x2);
        double x = gen.generate();
        gen.setBounds(y1, y2);
        double y = gen.generate();
        return new PointUD(x, y);
    }

    public void setBounds(final double x1, final double y1, final double x2, final double y2) {

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

}
