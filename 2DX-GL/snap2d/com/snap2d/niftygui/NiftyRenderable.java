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
import com.snap2d.gl.opengl.*;
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

	public NiftyRenderable(GLDisplay disp) {
		this.window = disp.getNewtWindow();
		SoundAPI.init();
	}

	/**
	 *
	 */
	@Override
	public void init(GLHandle handle) {
		nifty = new Nifty(new BatchRenderDevice(JoglBatchRenderBackendCoreProfileFactory.create()), new Snap2DSoundDevice(SoundAPI.getSound2D()), 
				new JoglInputSystem(window), new AccurateTimeProvider());
		fromXml("ui/map_screen_ui.xml", "screen0");
	}

	/**
	 *
	 */
	@Override
	public void dispose(GLHandle handle) {

	}

	/**
	 *
	 */
	@Override
	public void render(GLHandle handle, float interpolation) {
		nifty.update();
		nifty.render(false);
	}

	/**
	 *
	 */
	@Override
	public void update(long nanoTimeNow, long nanoTimeLast) {

	}

	/**
	 *
	 */
	@Override
	public void resize(GLHandle handle, int wt, int ht) {

	}

	public void fromXml(String fileName, String startScreen, NiftyScreenController... controllers) {
		if(controllers != null && controllers.length == 0)
			nifty.fromXml(fileName, startScreen, controllers);
		else
			nifty.fromXml(fileName, startScreen);
	}

	public void addXml(String fileName) {
		nifty.addXml(fileName);
	}

	public Nifty getNifty() {
		return nifty;
	}

	/**
	 * Forward, blank implementation of de.lessvoid.nifty.screen.ScreenController
	 * @author Brian Groenke
	 */
	public interface NiftyScreenController extends ScreenController {
		//
	}
}
