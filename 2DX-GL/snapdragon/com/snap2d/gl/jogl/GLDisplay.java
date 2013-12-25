/*
 *  Copyright � 2012-2013 Brian Groenke
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
import java.awt.image.*;
import java.net.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import bg.x2d.*;
import bg.x2d.utils.*;

import com.snap2d.*;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.*;
import com.snap2d.gl.jogl.GLHandle.AlphaFunc;
import com.snap2d.gl.jogl.GLHandle.GLFeature;
import com.snap2d.gl.jogl.JOGLConfig.Property;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 *
 */
public class GLDisplay {

	static final Dimension SCREEN = Utils.getScreenSize();

	int wt, ht;
	Type type;
	GLCanvas canvas;
	GLRenderControl rc;
	Frame frame;

	ExitOnClose ecl = new ExitOnClose();
	boolean eclEnabled = false;

	/**
	 * Creates this GLDisplay with the given window dimensions and graphics configuration.
	 * JOGLConfig.applyProperties is called before the environment is initialized.
	 */
	public GLDisplay(int width, int height, Type type, JOGLConfig config) {
		initDisplay(width, height, type, config);
	}

	private void initDisplay(int width, int height, Type type, JOGLConfig config) {
		config.applyProperties();
		this.wt = width;
		this.ht = height;
		this.type = type;
		// make sure Java2D doesn't try to use the OpenGL pipeline
		System.setProperty(GLConfig.Property.USE_OPENGL.getProperty(), "false");
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities glc = new GLCapabilities(glp);
		setCapabilities(glc);
		canvas = new GLCanvas(glc);
		rc = new GLRenderControl(canvas, config);

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
		frame.setIgnoreRepaint(false);
	}

	private void setCapabilities(GLCapabilities glc) {
		glc.setDoubleBuffered(true);
		int msaa = getMSAASetting();
		if(msaa > 0) {
			glc.setSampleBuffers(true);
			glc.setNumSamples(msaa);
		} else
			glc.setSampleBuffers(false);
	}

	private int getMSAASetting() {
		String msaaProp = System.getProperty(JOGLConfig.Property.SNAP2D_RENDER_MSAA.getProperty());
		int msaa = 0;
		try {
			Integer.parseInt(msaaProp);
		} catch (NumberFormatException e) {
			System.err.println("error parsing MSAA configuration string: " + msaaProp);
			msaa = Integer.parseInt(Property.SNAP2D_RENDER_MSAA.getValue());
		}
		return msaa;
	}

	public void updateConfig(JOGLConfig config) throws InterruptedException {
		config.applyProperties();
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities glc = new GLCapabilities(glp);
		setCapabilities(glc);
		boolean renderActive = rc.isRenderActive(), loopRunning = rc.isLoopRunning();
		rc.stopRenderLoop();
		GLCanvas old = this.canvas;
		old.removeGLEventListener(rc);
		canvas = new GLCanvas(glc);
		GLRenderControl newRc = new GLRenderControl(canvas, config);
		rc.copyRenderablesTo(newRc);
		rc.dispose();
		rc = newRc;
		frame.remove(old);
		frame.add(canvas);
		frame.validate();
		if(loopRunning)
			rc.startRenderLoop();
		rc.setRenderActive(renderActive);
		SnapLogger.println("updated GLDisplay config");
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
				frame.setUndecorated(true);
				frame.setResizable(false);
				frame.addFocusListener(new FocusListener() {

					@Override
					public void focusGained(FocusEvent arg0) {
						frame.setAlwaysOnTop(true);
					}

					@Override
					public void focusLost(FocusEvent arg0) {
						frame.setAlwaysOnTop(false);
					}
				});
				d.setFullScreenWindow(frame);
			} else {
				frame.setVisible(true);
			}
		} else {
			frame.setVisible(true);
		}
		frame.requestFocus();
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

	/**
	 * Disposes of this GLDisplay and all of its internally held resources, including
	 * the current GLRenderControl.  This method will block until GLRenderControl.dispose
	 * returns.
	 */
	public void dispose() {
		hide();
		canvas.removeGLEventListener(rc);
		rc.dispose();
		frame.dispose();
		rc = null;
		canvas = null;
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
		JOGLConfig config = new JOGLConfig();
		config.set(Property.SNAP2D_RENDER_MSAA, "16");
		GLDisplay gldisp = new GLDisplay(1600, 900, Type.WINDOWED, config);
		gldisp.setExitOnClose(true);
		gldisp.show();
		final GLRenderControl rc = gldisp.getRenderControl();
		rc.addRenderable(new TestObj(), GLRenderControl.POSITION_LAST);
		rc.addRenderable(new TestBack(), 0);
		rc.startRenderLoop();
	}

	static class TestObj implements GLRenderable {

		World2D world;
		Texture2D tex;
		int vwt, vht;
		Rect2D bounds = new Rect2D(-300, -300, 200, 200);
		Rect2D b2 = new Rect2D(-225, -150, 200, 200);

		@Override
		public void render(GLHandle handle, float interpolation) {

			bounds.setLocation(bounds.getX() + 0.5, bounds.getY() + 0.5);
			//b2.setLocation(b2.getX() + 1, b2.getY() - 1);

			handle.setTextureEnabled(true);
			handle.setTexCoords(tex);
			handle.setTextureMinFilter(GLHandle.FILTER_LINEAR, GLHandle.FILTER_LINEAR);
			handle.setEnabled(GLFeature.BLENDING, true);
			handle.setBlendFunc(AlphaFunc.SRC_OVER);
			handle.bindTexture(tex);

			Rectangle r = world.convertWorldRect(bounds);
			handle.drawRect2f(r.x, r.y, r.width, r.height);
			Rectangle r2 = world.convertWorldRect(b2);
			handle.drawRect2f(r2.x, r2.y, r2.width, r2.height);

			handle.setTextureEnabled(false);
			handle.setEnabled(GLFeature.BLENDING, false);

			/*
			Rect2D coll = world.checkCollision(bounds, b2);
			if(coll == null)
				return;
			Rectangle r3 = world.convertWorldRect(coll);
			handle.setColor3f(0, 0, 1f);
			handle.drawRect2f(r3.x, r3.y, r3.width, r3.height);
			 */
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {

		}


		float ppu = 1f;
		@Override
		public void onResize(GLHandle handle, int wt, int ht) {
			vwt = wt; vht = ht;
			if(world == null) {
				world = GLUtils.createGLWorldSystem(-800, -450, vwt, vht, ppu);
			} else {
				world.setViewSize(vwt, vht, ppu);
			}
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			try {
				BufferedImage bimg = ImageLoader.load(new URL("file:/media/WIN7/Users/Brian/Pictures/test_alpha.png"));
				bimg = ImageUtils.convertBufferedImage(bimg, BufferedImage.TYPE_INT_ARGB_PRE);
				tex = ImageLoader.loadTexture(bimg, true);

				//tex = ImageLoader.loadTexture(new URL("file:/media/WIN7/Users/Brian/Pictures/fnrr_flag.png"), ImageLoader.PNG, true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

	}

	static class TestBack implements GLRenderable {

		int wt, ht;

		@Override
		public void render(GLHandle handle, float interpolation) {
			handle.setColor3f(0.5f, 0.5f, 0.6f);
			handle.drawRect2f(0, 0, wt, ht);
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {

		}

		@Override
		public void onResize(GLHandle handle, int wt, int ht) {
			this.wt = wt;
			this.ht = ht;
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			handle.setViewport(0, 0, wt, ht, 1);
		}
	}

}
