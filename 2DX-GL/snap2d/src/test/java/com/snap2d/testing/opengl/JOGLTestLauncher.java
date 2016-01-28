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

package com.snap2d.testing.opengl;

import java.awt.Font;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import bg.x2d.gen.ColorGenerator;
import bg.x2d.geo.GeoUtils;
import bg.x2d.geo.PointUD;
import bg.x2d.geo.Vector2f;
import bg.x2d.utils.Utils;

import com.jogamp.common.nio.Buffers;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.opengl.*;
import com.snap2d.gl.opengl.GLConfig.Property;
import com.snap2d.input.GLKeyAdapter;
import com.snap2d.input.GLKeyEvent;
import com.snap2d.light.PointLight;
import com.snap2d.world.Rect2D;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 *
 */
class JOGLTestLauncher {

    public static void main(final String[] args) {

        /*
         * Display newtDisp = NewtFactory.createDisplay(null); Screen newtScreen
         * = NewtFactory.createScreen(newtDisp, 0); GLWindow glWin =
         * GLWindow.create(newtScreen, new
         * GLCapabilities(GLProfile.get(GLProfile.GL2)));
         * glWin.setFullscreen(false); glWin.setVisible(true);
         * Utils.sleep(1000); glWin.destroy(); System.exit(0);
         */

        Arrays.sort(args);
        GLConfig config = new GLConfig();
        config.set(Property.GL_RENDER_MSAA, "16");
        final GLDisplay gldisp = new GLDisplay(1100, 825, Type.WINDOWED, config);
        gldisp.setExitOnClose(true);
        gldisp.initInputSystem(false);
        gldisp.addKeyListener(new GLKeyAdapter() {

            @Override
            public void keyPressed(final GLKeyEvent event) {

                if (event.getKeyCode() == GLKeyEvent.VK_ESCAPE) {
                    gldisp.dispose();
                }
            }

        });
        final GLRenderControl rc = gldisp.getRenderControl();
        rc.addRenderable(new TestObj(gldisp.getWidth(), gldisp.getHeight()), GLRenderControl.POSITION_LAST);
        rc.addRenderable(new TestBack(), 0);
        // rc.addRenderable(new NiftyRenderable(gldisp),
        // GLRenderControl.POSITION_LAST);
        rc.addRenderable(new RenderText(rc), GLRenderControl.POSITION_LAST);
        gldisp.show();
        if (Arrays.binarySearch(args, "vsync") >= 0) {
            rc.setVSync(true);
        } else {
            rc.setVSync(false);
        }
        rc.startRenderLoop();
        rc.setTargetFPS(5000);
    }

    static GLProgram prog;
    static GLShader vert, frag;
    static Random rand = new Random();

    static class TestObj implements GLRenderable {

        World2D world;
        int vwt, vht;
        Rect2D bounds = new Rect2D( -500, -500, 100, 100);

        int rectBuff, polyBuff;

        public TestObj(final int vwt, final int vht) {

        }

        final int N = 3, rwt = 200, rht = 200;
        float theta, sx = 1, sy = 1;
        PointUD p0 = new PointUD( -400, -300), p1 = new PointUD( -150, -50);
        Vector2f v0 = new Vector2f(2f, 1f), v1 = new Vector2f(2f, -1f);
        PointUD[] points = new PointUD[5];
        PointUD basePoint = new PointUD(200, 500), origin = new PointUD(basePoint.ux, basePoint.uy - 100);

        float interpolation = 1;

        @Override
        public void render(final GLHandle handle, final float interpolation) {

            this.interpolation = interpolation;

            handle.setTextureMinFilter(GLHandle.FILTER_LINEAR, GLHandle.FILTER_NEAREST);
            handle.setEnabled(GLFeature.BLENDING, true);
            handle.setBlendFunc(AlphaFunc.SRC_BLEND);

            prog.enable();

            handle.setTextureEnabled(false);
            prog.setUniformi("tex_bound", 0);

            PointUD sp0 = world.worldToScreen(p0.ux, p0.uy);
            PointUD sp1 = world.worldToScreen(p1.ux, p1.uy);

            handle.setColor4f(1f, 0f, 0.2f, 1f);

            handle.putQuad2f(rectBuff, sp0.getFloatX(), sp0.getFloatY(), rwt, rht, null);
            handle.setColor4f(0, 0.7f, 1, 1);
            handle.putQuad2f(rectBuff, sp1.getFloatX(), sp1.getFloatY(), rwt, rht, null);
            handle.draw2f(rectBuff);

            handle.setColor4f(1, 1, 0, 1);
            handle.putPoly2f(polyBuff, null, points);
            handle.draw2f(polyBuff);
            handle.resetBuff(polyBuff);

            prog.disable();

            handle.setEnabled(GLFeature.BLENDING, false);

            /*
             * Rect2D coll = world.checkCollision(bounds, b2); if(coll == null)
             * return; Rectangle r3 = world.convertWorldRect(coll);
             * handle.setColor3f(0, 0, 1f); handle. //handle.putQuad2f(buff2,
             * 500 + tx++, 500 + ty++, 200, 2drawRect2f(r3.x, r3.y, r3.width,
             * r3.height);
             */
        }

        @Override
        public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

            Rect2D r0 = new Rect2D(p0.ux, p0.uy, 200, 200), r1 = new Rect2D(p1.ux, p1.uy, rwt, rht);
            if ( !world.viewContains(r0)) {
                Rect2D bounds = world.checkCollision(world.getBounds(), r0);
                if (bounds == null) {
                    bounds = world.getBounds();
                }
                if (bounds.getHeight() < r0.getHeight()) {
                    v0.negateY();
                }
                if (bounds.getWidth() < r0.getWidth()) {
                    v0.negateX();
                }
            }
            if ( !world.viewContains(r1)) {
                Rect2D bounds = world.checkCollision(world.getBounds(), r1);
                if (bounds == null) {
                    bounds = world.getBounds();
                }
                if (bounds.getHeight() < r1.getHeight()) {
                    v1.negateY();
                }
                if (bounds.getWidth() < r1.getWidth()) {
                    v1.negateX();
                }
            }

            v0.applyTo(p0, interpolation);
            v1.applyTo(p1, interpolation);

            for (PointUD p : points) {
                p.setLocation(p.ux + 0.5, p.uy -= 0.5);
            }
        }

        float ppu = 1f;

        @Override
        public void resize(final GLHandle handle, final int wt, final int ht) {

            vwt = wt;
            vht = ht;
            world = GLUtils.createGLWorldSystem( -vwt / 2, -vht / 2, vwt, vht, ppu);
            world.setViewSize(vwt, vht, ppu);
        }

        /**
         *
         */
        @Override
        public void init(final GLHandle handle) {

            rectBuff = handle.createQuadBuffer2f(BufferUsage.STREAM_DRAW, 2, false);
            polyBuff = handle.createPolyBuffer2f(BufferUsage.STREAM_DRAW,
                                                 GeomFunc.TRIANGLE_FAN,
                                                 points.length,
                                                 5,
                                                 false);
            for (int i = 0; i < points.length; i++ ) {
                points[i] = GeoUtils.rotatePoint(basePoint, origin, 2 * Math.PI / points.length * i);
            }
        }

        /**
         *
         */
        @Override
        public void dispose(final GLHandle handle) {

        }

    }

    static class TestBack implements GLRenderable {

        int wt, ht, buffId;
        ArrayList<PointLight> lights = new ArrayList<PointLight>();

        long last = System.currentTimeMillis();

        @Override
        public void render(final GLHandle handle, final float interpolation) {

            handle.setColor4f(0.2f, 0.2f, 0.2f, 1f);
            prog.enable();
            prog.setUniformi("tex_bound", 0);
            handle.draw2f(buffId);

            if (System.currentTimeMillis() - last > 250) {
                ColorGenerator colorgen = ColorGenerator.createRGB();
                for (PointLight ptlight : lights) {
                    ptlight.setLocation(150 + rand.nextInt(3 * wt / 4), 150 + rand.nextInt(3 * ht / 4));
                    ptlight.setColor(colorgen.generate().getColorComponents(new float[4]));
                }
                handle.asGL3().updateLightData();
                last += 250;
            }
            prog.disable();
        }

        @Override
        public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        }

        @Override
        public void resize(final GLHandle handle, final int wt, final int ht) {

            this.wt = wt;
            this.ht = ht;
            prog.enable();
            handle.setViewport(0, 0, wt, ht, 1);
            handle.setColor4f(0.2f, 0.2f, 0.2f, 1f);
            handle.putQuad2f(buffId, 0, 0, wt, ht, null);
            prog.disable();
        }

        /**
         *
         */
        @Override
        public void init(final GLHandle handle) {

            buffId = handle.createQuadBuffer2f(BufferUsage.STATIC_DRAW, 1, false);
            prog = new GLProgram();
            try {
                try {
                    String vertName = "test.vert";
                    String fragName = "test.frag";
                    vert = GLShader.loadLibraryShader(GLShader.TYPE_VERTEX, vertName);
                    frag = GLShader.loadLibraryShader(GLShader.TYPE_FRAGMENT, fragName);
                } catch (GLShaderException e) {
                    System.err.println("error compiling shader");
                    System.err.println(e.getExtendedMessage());
                }

                prog.attachShader(vert);
                prog.attachShader(frag);
                if ( !prog.link()) {
                    prog.printLinkLog();
                }

                // handle.addLightSource(new PointLight(500, 500, new float[]
                // {0.6f, 0.4f, 0.75f}, 1, 100));
                // handle.addLightSource(new PointLight(1000, 600, new float[]
                // {0.75f,0.6f,0.3f}, 2.0f, 200));
                ColorGenerator colorgen = ColorGenerator.createRGB();
                for (int i = 0; i < 20; i++ ) {
                    PointLight ptlight = new PointLight(100 + 50 + rand.nextInt(800), 100 + 50 + rand.nextInt(600),
                                    colorgen.generate().getColorComponents(new float[4]), 3f, 50);
                    handle.asGL3().addLightSource(ptlight);
                    lights.add(ptlight);
                }
                handle.asGL3().setAmbientLightFactor(0.1f);
                prog.enable();
                prog.bindFragDataLoc("frag_out", 0);
                handle.asGL3().updateLightData();
                prog.disable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         *
         */
        @Override
        public void dispose(final GLHandle handle) {

            prog.dispose();
        }
    }

    static class RenderText implements GLRenderable {

        String text0 = "", text1 = "Seconds Elapsed: 00";

        int x0 = 10, y0 = 10, x1 = 50, y1;

        GLRenderControl rc;

        RenderText(final GLRenderControl rc) {

            this.rc = rc;
            int ht = rc.getGLWindow().getHeight();
            y1 = ht - ht / 4;
        }

        /**
         *
         */
        @Override
        public void init(final GLHandle handle) {

            handle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        }

        /**
         *
         */
        @Override
        public void dispose(final GLHandle handle) {

        }

        /**
         *
         */
        @Override
        public void render(final GLHandle handle, final float interpolation) {

            String[] strs = new String[] { text0, text1 };
            IntBuffer posBuff = Buffers.newDirectIntBuffer(new int[] { x0, y0, x1, y1 });
            FloatBuffer colorBuff = Buffers.newDirectFloatBuffer(new float[] { 1, 1, 1, 1, 0, 0, 1, 1 });
            handle.drawTextBatch(strs, posBuff, colorBuff);
        }

        int secs = 0;
        long last = 0;

        /**
         *
         */
        @Override
        public void update(final long nanoTimeNow, final long nanoTimeLast) {

            text0 = rc.getCurrentFPS() + " fps";
            if (Utils.nanoToSecs(nanoTimeNow - last) > 1) {
                text1 = "Seconds Elapsed: " + ( (secs < 10) ? "0" + secs++ : secs++ );
                last = nanoTimeNow;
            }
        }

        /**
         *
         */
        @Override
        public void resize(final GLHandle handle, final int wt, final int ht) {

        }
    }

}
