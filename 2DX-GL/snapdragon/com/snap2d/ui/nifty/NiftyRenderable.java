/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.ui.nifty;

import com.jogamp.newt.opengl.GLWindow;
import com.snap2d.gl.opengl.*;
import com.snap2d.sound.SoundAPI;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.renderer.jogl.render.JoglRenderDevice;
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
		nifty = new Nifty(new JoglRenderDevice(), new SnapdragonSoundDevice(SoundAPI.getSound2D()), 
				new NewtInputSystem(window), new AccurateTimeProvider());
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

}
