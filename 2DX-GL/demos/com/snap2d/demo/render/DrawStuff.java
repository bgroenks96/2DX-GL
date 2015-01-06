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

package com.snap2d.demo.render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import bg.x2d.geo.Shape2D;
import bg.x2d.geo.UniformPolygon2D;

import com.snap2d.gl.Display;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.RenderControl;
import com.snap2d.gl.Renderable;

/**
 * @author Brian Groenke
 * 
 */
public class DrawStuff {

    public static void main(final String[] args) {

        // create the Display; note that Type is an inner-type of Display
        // you will need to import com.snap2d.gl.Display.Type for this to work
        Display disp = new Display(800, 600, Type.WINDOWED);
        final RenderControl rc = disp.getRenderControl(2);

        Renderable obj = new Renderable() {

            @Override
            public void render(final Graphics2D g, final float interpolation) {

                g.setColor(Color.BLACK);
                g.drawString("Hello Snap2D!", 50, 50);
                g.setColor(Color.RED);
                g.fillOval(100, 100, 200, 200);

                // create regular hexagon and store it as its supertype, Shape2D
                Shape2D hex = new UniformPolygon2D(6, 300, 100, 200, Color.BLUE, true);
                hex.draw(g);
            }

            @Override
            public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

            }

            @Override
            public void onResize(final Dimension oldSize, final Dimension newSize) {

            }

        };
        rc.addRenderable(obj, RenderControl.POSITION_LAST);

        disp.show();
        rc.startRenderLoop();

        // quick and dirty example of shutting down the system cleanly on exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {

                rc.dispose();
            }
        }));
    }
}
