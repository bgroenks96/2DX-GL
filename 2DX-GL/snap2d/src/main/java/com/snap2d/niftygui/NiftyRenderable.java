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

package com.snap2d.niftygui;

import com.jogamp.newt.opengl.GLWindow;
import com.snap2d.gl.opengl.GLDisplay;
import com.snap2d.gl.opengl.GLHandle;
import com.snap2d.gl.opengl.GLRenderable;
import com.snap2d.sound.SoundAPI;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

/**
 * @author Brian Groenke
 *
 */
public class NiftyRenderable implements GLRenderable {

    Nifty nifty;
    GLWindow window;

    public NiftyRenderable(final GLDisplay disp) {

        this.window = disp.getNewtWindow();
        SoundAPI.init();
    }

    /**
     *
     */
    @Override
    public void init(final GLHandle handle) {

        nifty = new Nifty(new BatchRenderDevice(JoglBatchRenderBackendCoreProfileFactory.create()),
                        new Snap2DSoundDevice(SoundAPI.getSound2D()), new JoglInputSystem(window),
                        new AccurateTimeProvider());
        fromXml("ui/map_screen_ui.xml", "screen0");
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

        nifty.update();
        nifty.render(false);
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long nanoTimeLast) {

    }

    /**
     *
     */
    @Override
    public void resize(final GLHandle handle, final int wt, final int ht) {

    }

    public void fromXml(final String fileName, final String startScreen, final NiftyScreenController... controllers) {

        if (controllers != null && controllers.length == 0) {
            nifty.fromXml(fileName, startScreen, controllers);
        } else {
            nifty.fromXml(fileName, startScreen);
        }
    }

    public void addXml(final String fileName) {

        nifty.addXml(fileName);
    }

    public Nifty getNifty() {

        return nifty;
    }

    /**
     * Forward, blank implementation of
     * de.lessvoid.nifty.screen.ScreenController
     * 
     * @author Brian Groenke
     */
    public interface NiftyScreenController extends ScreenController {
        //
    }
}
