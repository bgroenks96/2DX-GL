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

package com.snap2d.gl.opengl;

import java.util.logging.Logger;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.*;

import bg.x2d.utils.Utils;

import com.jogamp.newt.*;
import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.opengl.GLConfig.Property;
import com.snap2d.input.*;

/**
 * A class representing a native window onto which an OpenGL context can be rendered.
 * GLDisplay is backed internally by the JOGL NEWT windowing system.
 * @author Brian Groenke
 *
 */
public class GLDisplay {

	final int SCREEN_WIDTH = Utils.getScreenSize().width, SCREEN_HEIGHT = Utils.getScreenSize().height;
	
	private final Logger log = Logger.getLogger(GLDisplay.class.getCanonicalName());

	int wt, ht;
	Type type;
	Display newtDisp;
	Screen newtScreen;
	GLWindow glWin;
	GLRenderControl rc;

	NEWTInputDispatcher inputDispatcher;

	ProcessCloseRequest onClose;
	boolean exitOnClose = false;

	/**
	 * Creates this GLDisplay with the given window properties and GL configuration.
	 * @param width
	 * @param height
	 * @param type the window type; windowed or fullscreen
	 * @param config GL configuration; may be null (default configuration will be used)
	 */
	public GLDisplay(int width, int height, Type type, GLConfig config) {
		initDisplay(width, height, type, config);
	}

	private void initDisplay(int width, int height, Type type, GLConfig config) {
		if(config == null)
			config = GLConfig.getDefaultSystemConfig();
		this.wt = width;
		this.ht = height;
		this.type = type;
		GLProfile.initSingleton();
		GLProfile glp = GLConfig.loadGLProfile(config);
		GLCapabilities glc = new GLCapabilities(glp);
		setCapabilities(config, glc);
		newtDisp = NewtFactory.createDisplay(null);
		newtScreen = NewtFactory.createScreen(newtDisp, 0);
		glWin = GLWindow.create(newtScreen, glc);
		glWin.setDefaultCloseOperation(WindowClosingMode.DO_NOTHING_ON_CLOSE);
		onClose = new ProcessCloseRequest();
		glWin.addWindowListener(onClose);
		rc = new GLRenderControl(glWin, config);

		switch(type) {
		case WINDOWED:
			glWin.setSize(width, height);
			break;
		case FULLSCREEN:
			if(!type.isNativeFullscreen())
				glWin.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		}
	}

	private void setCapabilities(GLConfig config, GLCapabilities glc) {
		glc.setDoubleBuffered(true);
		int msaa = getMSAASetting(config);
		if(msaa > 0) {
			glc.setSampleBuffers(true);
			glc.setNumSamples(msaa);
		} else
			glc.setSampleBuffers(false);
	}

	private int getMSAASetting(GLConfig config) {
		String msaaProp = config.get(Property.GL_RENDER_MSAA);
		int msaa = 0;
		try {
			Integer.parseInt(msaaProp);
		} catch (NumberFormatException e) {
			System.err.println("error parsing MSAA configuration string: " + msaaProp);
			msaa = Integer.parseInt(Property.GL_RENDER_MSAA.getValue());
		}
		return msaa;
	}

	public void setTitle(String title) {
		if(glWin != null)
			glWin.setTitle(title);
	}

	public void setLocatoin(int x, int y) {
		glWin.setPosition(x, y);
	}

	public void centerOnScreen() {
		int screenCenterX = SCREEN_WIDTH / 2, screenCenterY = SCREEN_HEIGHT / 2;
		glWin.setPosition(screenCenterX - glWin.getWidth() / 2, screenCenterY - glWin.getHeight() / 2);
	}

	public int getX() {
		return glWin.getX();
	}

	public int getY() {
		return glWin.getY();
	}

	public void updateConfig(GLConfig config) throws InterruptedException {
		GLProfile glp = GLConfig.loadGLProfile(config);
		GLCapabilities glc = new GLCapabilities(glp);
		setCapabilities(config, glc);
		boolean renderActive = rc.isRenderActive(), loopRunning = rc.isRunning();
		rc.stopRenderLoop();
		GLWindow old = glWin;
		old.disposeGLEventListener(rc, true);
		glWin = GLWindow.create(newtScreen, glc);
		GLRenderControl newRc = new GLRenderControl(glWin, config);
		rc.copyRenderablesTo(newRc);
		rc.dispose();
		rc = newRc;
		if(loopRunning)
			rc.startRenderLoop();
		rc.setRenderActive(renderActive);
		log.info("updated GLDisplay config");
	}

	/**
	 * Initializes this GLDisplay's input system.
	 * @param consumeEvents true if you want this GLDisplay to consume input events.
	 */
	public void initInputSystem(boolean consumeEvents) {
		if(inputDispatcher != null)
			return;
		inputDispatcher = new NEWTInputDispatcher(consumeEvents);
		addNewtKeyListener(inputDispatcher);
		addNewtMouseListener(inputDispatcher);
	}

	/**
	 * Add a GLKeyListener to this GLDisplay.  NEWT KeyEvent[s] will
	 * be wrapped in a GLKeyEvent and dispatched to registered listeners.
	 * You must call {@link #initInputSystem(boolean)} before calling
	 * this method.
	 * @param key
	 */
	public void addKeyListener(GLKeyListener key) {
		if(inputDispatcher == null)
			return;
		inputDispatcher.registerKeyListener(key);
	}

	/**
	 * Add a GLMouseListener to this GLDisplay.  NEWT MouseEvent[s] will
	 * be wrapped in a GLMouseEvent and dispatched to registered listeners.
	 * You must call {@link #initInputSystem(boolean)} before calling
	 * this method.
	 * @param mouse
	 */
	public void addMouseListener(GLMouseListener mouse) {
		if(inputDispatcher == null)
			return;
		inputDispatcher.registerMouseListener(mouse);
	}

	public void removeKeyListener(GLKeyListener listener) {
		if(inputDispatcher == null)
			return;
		inputDispatcher.removeKeyListener(listener);
	}

	public void removeMouseListener(GLMouseListener listener) {
		if(inputDispatcher == null)
			return;
		inputDispatcher.removeMouseListener(listener);
	}

	/**
	 * Add a NEWT KeyEvent listener to this GLDisplay. You must have JOGL
	 * on your build path to use these interfaces directly.
	 * @param key
	 */
	public void addNewtKeyListener(KeyListener key) {
		glWin.addKeyListener(key);
	}

	/**
	 * Add a NEWT MouseEvent listener to this GLDisplay. You must have JOGL
	 * on your build path to use these interfaces directly.
	 * @param mouse
	 */
	public void addNewtMouseListener(MouseListener mouse) {
		glWin.addMouseListener(mouse);
	}

	public void removeNewtKeyListener(KeyListener key) {
		glWin.removeKeyListener(key);
	}

	public void removeNewtMouseListener(MouseListener mouse) {
		glWin.removeMouseListener(mouse);
	}

	public void setConfineMouse(boolean confine) {
		glWin.confinePointer(confine);
	}

	public void setMousePos(int x, int y) {
		glWin.warpPointer(x, y);
	}

	public GLRenderControl getRenderControl() {
		return rc;
	}

	public int getWidth() {
		return glWin.getWidth();
	}

	public int getHeight() {
		return glWin.getHeight();
	}

	public int getScreenX() {
		return glWin.getX();
	}

	public int getScreenY() {
		return glWin.getY();
	}

	public GLWindow getNewtWindow() {
		return glWin;
	}

	public void show() {
		if(glWin.isVisible())
			return;
		if (type == Type.FULLSCREEN) {
			if (type.isNativeFullscreen()) {
				glWin.setFullscreen(true);
			}
		}
		glWin.setVisible(true);
		wt = glWin.getWidth();
		ht = glWin.getHeight();
	}

	public void hide() {
		rc.setRenderActive(false);
		glWin.setFullscreen(false);
		glWin.setVisible(false);
	}


	/**
	 * Disposes of this GLDisplay and all of its internally held resources, including
	 * the current GLRenderControl.  This method will create a new disposer thread to
	 * handle the cleanup process and return immediately.  Once the disposer has finished
	 * releasing all resources held by the GL context, the optional GLDisposalCallback will
	 * be notified via GLDisposalCallback.onDispose.
	 */
	public void dispose(GLDisposalCallback onDisposeCallback) {
		Thread disposerThread = new Thread(new Disposer(onDisposeCallback));
		disposerThread.setName("snap2d-disposer_thread");
		disposerThread.start();
	}

	/**
	 * Equivalent to GLDisplay.dispose(null)
	 */
	public void dispose() {
		dispose(null);
	}

	private class Disposer implements Runnable {

		GLDisposalCallback callback;

		Disposer(GLDisposalCallback callback) {
			this.callback = callback;
		}

		@Override
		public void run() {
			if(rc == null || glWin == null)
				return;
			glWin.removeWindowListener(onClose);
			glWin.disposeGLEventListener(rc, true);
			glWin.destroy();
			rc.dispose(); // should block until render-loop threads finish
			rc = null;
			glWin = null;
			if(callback != null)
				callback.onDisposed();
			if(exitOnClose)
				System.exit(0);
		}
	}


	public void setExitOnClose(boolean exitOnWindowClosed) {
		if(exitOnWindowClosed && !exitOnClose) {
			exitOnClose = true;
		} else if(!exitOnWindowClosed && exitOnClose) {
			exitOnClose = false;
		}
	}

	private class ProcessCloseRequest extends WindowAdapter {
		/**
		 *
		 */
		@Override
		public void windowDestroyNotify(WindowEvent arg0) {
			dispose();
		}
	}

	public interface GLDisposalCallback {
		public void onDisposed();
	}
}
