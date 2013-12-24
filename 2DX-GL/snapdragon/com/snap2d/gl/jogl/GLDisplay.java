/*
 *  Copyright © 2012-2013 Brian Groenke
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
import java.io.*;
import java.net.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import bg.x2d.utils.*;

import com.snap2d.*;
import com.snap2d.gl.Display.Type;
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
	 * 
	 */
	public GLDisplay(int width, int height, Type type, JOGLConfig config) {
		initDisplay(width, height, type, config);
	}
	
	private void initDisplay(int width, int height, Type type, JOGLConfig config) {
		config.applyProperties();
		this.wt = width;
		this.ht = height;
		this.type = type;
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities glc = new GLCapabilities(glp);
		glc.setDoubleBuffered(true);
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
		frame.setIgnoreRepaint(false);
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
		GLDisplay gldisp = new GLDisplay(1600, 900, Type.WINDOWED, new JOGLConfig());
		gldisp.setExitOnClose(true);
		gldisp.show();
		final GLRenderControl rc = gldisp.getRenderControl();
		rc.addRenderable(new TestObj(), GLRenderControl.POSITION_LAST);
		rc.addRenderable(new TestBack(), 0);
		rc.setRenderActive(true);
		
		new ThreadManager().submitJob(new Runnable() {

			@Override
			public void run() {
				for(int i=0;i<10;i++) {
					rc.setVSync(!rc.isVSyncEnabled());
					System.out.println("Updated config");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
	}
	
	static class TestObj implements GLRenderable {
		
		World2D world;
		Texture2D tex;
		int vwt, vht;
		Rect2D bounds = new Rect2D(1, 1, 300, 200);
		Rect2D b2 = new Rect2D(100, 100, 300, 200);

		@Override
		public void render(GLHandle handle, float interpolation) {
			if(tex == null) {
				try {
					tex = ImageLoader.loadTexture(new URL("file:/media/WIN7/Users/Brian/Pictures/fnrr_flag.png"), ImageLoader.PNG, false);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			bounds.setLocation(bounds.getX() + 0.5, bounds.getY() + 0.5);
			//b2.setLocation(b2.getX() + 1, b2.getY() - 1);

			handle.setTextureEnabled(true);
			handle.bindTexture(tex);
			Rectangle r = world.convertWorldRect(bounds);
			handle.drawRect2f(r.x, r.y, r.width, r.height);
			Rectangle r2 = world.convertWorldRect(b2);
			handle.drawRect2f(r2.x, r2.y, r2.width, r2.height);
			handle.setTextureEnabled(false);
			Rect2D coll = world.checkCollision(bounds, b2);
			if(coll == null)
				return;
			Rectangle r3 = world.convertWorldRect(coll);
			handle.setColor3f(0, 0, 1f);
			handle.drawRect2f(r3.x, r3.y, r3.width, r3.height);
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			
		}


		float ppu = 1f;
		@Override
		public void onResize(GLHandle handle, int wt, int ht) {
			vwt = wt; vht = ht;
			if(world == null) {
				world = new World2D(0, 500, vwt, vht, ppu);
			} else {
				world.setViewSize(vwt, vht, ppu);
			}
		}
		
	}
	
	static class TestBack implements GLRenderable {
		
		int wt, ht;
		
		@Override
		public void render(GLHandle handle, float interpolation) {
			handle.setViewport(0, 0, wt, ht, 1);
			handle.setColor3f(0.5f, 0.5f, 0.5f);
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
	}

}
