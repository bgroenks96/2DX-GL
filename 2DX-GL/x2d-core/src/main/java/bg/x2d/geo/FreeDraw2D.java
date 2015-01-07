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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import bg.x2d.Background;

/**
 * Superclass of all undefined geometric construction tools, most prominently
 * {@link Paintbrush2D}. FreeDraw2D defines certain generic methods regarding a
 * {@link Background} object and Graphics2D <code>canvas</code> object that all
 * subclasses of it will utilize.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class FreeDraw2D {

    @Deprecated
    Background background;
    @Deprecated
    Graphics2D canvas;

    Paint paint;

    public void setCanvasPaint(final Paint p) {

        if (p != null) {
            paint = p;
        }
    }

    // ----- DEPRECATED ------ //
    @Deprecated
    public void setBackground(final Background b) {

        if (b != null) {
            background = b;
        }
    }

    @Deprecated
    public Background getBackground() {

        return background;
    }

    @Deprecated
    public Graphics2D getCanvas() {

        return canvas;
    }

    @Deprecated
    public void setGraphics(final Graphics g) {

        setGraphics((Graphics2D) g);
    }

    @Deprecated
    public void setGraphics(final Graphics2D g2) {

        canvas = g2;
    }
}
