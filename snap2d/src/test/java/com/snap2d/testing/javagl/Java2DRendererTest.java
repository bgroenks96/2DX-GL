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

package com.snap2d.testing.javagl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Random;

import bg.x2d.gen.ColorGenerator;
import bg.x2d.gen.PointGenerator;
import bg.x2d.geo.PointUD;

import com.snap2d.gl.Display.Type;
import com.snap2d.gl.Renderable;
import com.snap2d.gl.opengl.*;
import com.snap2d.gl.opengl.GLConfig.Property;

/**
 * @author brian
 *
 */
public class Java2DRendererTest {

    /**
     * @param args
     */
    public static void main(final String[] args) {

        GLConfig glConfig = GLConfig.getDefaultSystemConfig();
        glConfig.set(Property.GL_RENDER_MSAA, "8");
        glConfig.set(Property.GL_PROFILE, "GL2");
        GLDisplay disp = new GLDisplay(1200, 768, Type.WINDOWED, glConfig);
        GLRenderControl rc = disp.getRenderControl();
        rc.addRenderable(new BasicGLRenderable(), GLRenderControl.POSITION_LAST);
        // rc.addRenderable(new QuadGradientTestRenderable(), 0);
        disp.show();
        rc.setVSync(true);
        rc.startRenderLoop();
        rc.setTargetFPS(1000);
    }

    static class BasicGLRenderable implements GLRenderable {

        ColorGenerator colorGen = ColorGenerator.createRGBA();
        Random r = new Random();
        int dx = 4, dy = 2, wt, ht;
        Color color = colorGen.generate();

        long lastColorUpdate = 0;

        PointUD[] pts;
        int buffId;

        /**
         *
         */
        @Override
        public void init(final GLHandle handle) {

            buffId = handle.createQuadBuffer2f(BufferUsage.DYNAMIC_DRAW, 10, false);
            PointGenerator ptGen = new PointGenerator(0, 0, 600, 500);
            pts = new PointUD[30];
            for (int i = 0; i < pts.length; i++ ) {
                pts[i] = ptGen.generate();
            }
        }

        /**
         *
         */
        @Override
        public void dispose(final GLHandle handle) {

            handle.destroyBuff(buffId);
        }

        /**
         *
         */
        @Override
        public void render(final GLHandle handle, final float interpolation) {

            float[] rgba = GLUtils.convertColorAwtToGL(color);
            handle.setEnabled(GLFeature.BLENDING, true);
            handle.setBlendFunc(AlphaFunc.SRC_OVER);
            handle.setColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            for (PointUD p : pts) {
                handle.putQuad2f(buffId, p.getFloatX(), p.getFloatY(), 200, 200, null);
            }
            handle.draw2f(buffId);
            handle.resetBuff(buffId);
        }

        /**
         *
         */
        @Override
        public void update(final long nanoTimeNow, final long nanoTimeLast) {

            for (int i = 0; i < pts.length; i++ ) {
                float x = pts[i].getFloatX();
                float y = pts[i].getFloatY();
                if (x + 200 > wt || x < 0) {
                    dx = -dx;
                }
                if (y + 200 > ht || y < 0) {
                    dy = -dy;
                }
                pts[i].setLocation(x + dx, y + dy);
            }

            if (nanoTimeNow / 1000000 - lastColorUpdate > 500) {
                color = colorGen.generate();
                lastColorUpdate = nanoTimeNow / 1000000;
            }
        }

        /**
         *
         */
        @Override
        public void resize(final GLHandle handle, final int wt, final int ht) {

            this.wt = wt;
            this.ht = ht;
            handle.setViewport(0, 0, wt, ht, 1);
        }

    }

    static class TypicalRenderable implements Renderable {

        ColorGenerator colorGen = ColorGenerator.createRGBA();
        Random r = new Random();
        int x = r.nextInt(1000), y = r.nextInt(600), lx, ly, dx = 4, dy = 2, wt, ht;
        Color color = colorGen.generate();

        long lastColorUpdate = 0;

        /**
         *
         */
        @Override
        public void render(final Graphics2D g, final float interpolation) {

            g.setColor(color);
            g.fillRect((int) (lx + (x - lx) * interpolation), (int) (ly + (y - ly) * interpolation), 200, 200);
        }

        /**
         *
         */
        @Override
        public void update(final long nanoTimeNow, final long nanoTimeLast) {

            if (x + 200 > wt || x < 0) {
                dx = -dx;
            }
            if (y + 200 > ht || y < 0) {
                dy = -dy;
            }
            lx = x;
            ly = y;
            x += dx;
            y += dy;

            if (nanoTimeNow / 1000000 - lastColorUpdate > 500) {
                color = colorGen.generate();
                lastColorUpdate = nanoTimeNow / 1000000;
            }
        }

        /**
         *
         */
        @Override
        public void onResize(final Dimension oldSize, final Dimension newSize) {

            this.wt = (int) newSize.getWidth();
            this.ht = (int) newSize.getHeight();
        }

    }
}
