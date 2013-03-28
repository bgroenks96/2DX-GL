/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import com.snap2d.gl.Display.Type;

/**
 * @author Brian Groenke
 *
 */
public class GLDisplay {
	
	static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();
	
	int wt, ht;
	Type type;
	GLCanvas canvas;
	GLRenderControl rc;
	Frame frame;
	
	ExitOnClose ecl = new ExitOnClose();
	boolean eclEnabled = false;

	/**
	 * 
	 */
	public GLDisplay(int width, int height, Type type, JOGLConfig config) {
		initDisplay(width, height, type, config);
	}
	
	private void initDisplay(int width, int height, Type type, JOGLConfig config) {
		config.apply();
		this.wt = width;
		this.ht = height;
		this.type = type;
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities glc = new GLCapabilities(glp);
		canvas = new GLCanvas(glc);
		rc = new GLRenderControl(canvas);
		frame = new Frame();
		switch(type) {
		case WINDOWED:
			frame.setSize(width, height);
			break;
		case FULLSCREEN:
			if(!type.isNativeFullscreen())
				frame.setSize(SCREEN);
		}
		frame.add(canvas);
	}
	
	public GLRenderControl getRenderControl() {
		return rc;
	}
	
	public void show() {
		if (type == Type.FULLSCREEN) {
			GraphicsDevice d = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if (d.isFullScreenSupported() && type.isNativeFullscreen()) {
				frame.enableInputMethods(false);
				d.setFullScreenWindow(frame);
			} else {
				frame.setVisible(true);
			}
		} else {
			frame.setVisible(true);
		}
		wt = frame.getWidth();
		ht = frame.getHeight();
	}
	
	public void hide() {
		rc.setRenderActive(false);
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		d.setFullScreenWindow(null);
		frame.setVisible(false);
	}
	
	public void dispose() {
		hide();
		canvas.removeGLEventListener(rc);
		canvas.destroy();
		frame.dispose();
	}
	
	public void setExitOnClose(boolean exitOnWindowClosed) {
		if(exitOnWindowClosed && !eclEnabled) {
			frame.addWindowListener(ecl);
			eclEnabled = true;
		} else if(!exitOnWindowClosed && eclEnabled) {
			frame.removeWindowListener(ecl);
			eclEnabled = false;
		}
	}
	
	public Frame getFrame() {
		return frame;
	}
	
	private class ExitOnClose extends WindowAdapter {

		/**
		 *
		 */
		@Override
		public void windowClosing(WindowEvent arg0) {
			dispose();
			System.exit(0);
		}
		
	}
	
	public static void main(String[] args) {
		GLDisplay gldisp = new GLDisplay(800, 600, Type.FULLSCREEN, new JOGLConfig());
		gldisp.setExitOnClose(true);
		gldisp.show();
		gldisp.getRenderControl().setRenderActive(true);
	}

}
