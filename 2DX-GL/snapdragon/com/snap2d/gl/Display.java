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

package com.snap2d.gl;

import java.awt.*;

import javax.swing.*;

import bg.x2d.utils.*;

/**
 * Represents the underlying frame used to display rendered content on screen. Display creates a
 * JFrame used to display a RenderControl's BufferStrategy. This is essentially the entry point for
 * graphics rendering in Snapdragon2D and works tightly in conjunction with RenderControl.
 * 
 * @author Brian Groenke
 * @since Snapdragon2D 1.0
 */
public class Display {

	private int wt, ht;
	private Type type;
	private JFrame frame;
	private RenderControl rc;
	private GLConfig graphicsConfig;

	/**
	 * Creates the Display with default GLConfig.
	 * 
	 * @param width
	 * @param height
	 * @param type
	 */
	public Display(int width, int height, Type type) {
		this(width, height, type, GLConfig.getDefaultSystemConfig());
	}

	/**
	 * Creates the Display and initializes Java2D with the given GLConfig settings. If
	 * <code>config</code> is null, Java's default configuration will be used.
	 * 
	 * @param width
	 *            width of the window (irrelevant for Type.FULLSCREEN)
	 * @param height
	 *            height of the window (irrelevant for Type.FULLSCREEN)
	 * @param type
	 *            the type of Display to be shown
	 * @param config
	 *            the graphics configuration to use.
	 */
	public Display(int width, int height, Type type, GLConfig config) {
		this.graphicsConfig = config;
		this.wt = width;
		this.ht = height;
		this.type = type;
		if (config != null) {
			config.apply();
		}
		init();
	}

	/**
	 * Internal method that performs initiation of the Display. Called by the constructor.
	 */
	protected void init() {
		switch (type) {
		case FULLSCREEN:
			frame = new JFrame();
			frame.setUndecorated(true);
			frame.setLocation(0, 0);
			frame.setSize(getScreenSize());
			wt = frame.getWidth();
			ht = frame.getHeight();
			break;
		case WINDOWED:
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(new Dimension(wt, ht));
			frame.setLocationRelativeTo(null);
			break;
		}

		frame.setIgnoreRepaint(true);
		frame.getContentPane().setBackground(RenderControl.CANVAS_BACK);
	}

	/**
	 * Sets this Display's window title.
	 * 
	 * @param str
	 */
	public void setTitle(String str) {
		frame.setTitle(str);
	}

	/**
	 * Sets the location on screen. Should only be used for WINDOWED Displays.
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}

	public Component getAsComponent() {
		return frame;
	}

	/**
	 * Sets the size of the frame. Should only be used for WINDOWED Displays.
	 * 
	 * @param x
	 * @param y
	 */
	public void setSize(int x, int y) {
		frame.setSize(x, y);
	}

	/**
	 * Sets whether the frame is decorated.
	 * 
	 * @param undecorated
	 */
	public void setUndecorated(boolean undecorated) {
		frame.setUndecorated(undecorated);
	}

	/**
	 * Only applicable for decorated, WINDOWED Displays.
	 * 
	 * @param onClose
	 *            specified in the JFrame class
	 */
	public void setDefaultCloseOperation(int onClose) {
		frame.setDefaultCloseOperation(onClose);
	}

	/**
	 * @return true if the Display is currently showing, false otherwise
	 */
	public boolean isShowing() {
		return frame.isShowing();
	}

	/**
	 * Sets the Display type. This will force the Display to recreate the JFrame internally and
	 * migrate the rendering handle (if existent).
	 * 
	 * @param dispType
	 */
	public void setType(Type dispType) {
		boolean wasActive = false;
		if (rc != null) {
			wasActive = rc.isActive();
			rc.setRenderActive(false);
		}
		boolean frameVisible = frame.isShowing();
		frame.dispose();
		type = dispType;
		init();
		if (rc != null) {
			frame.add(rc.canvas);
			rc.setRenderActive(wasActive);
		}
		if (frameVisible) {
			show();
		}
	}

	/**
	 * Convenience method for <code>Toolkit.getDefaultToolkit().getScreenSize()</code>.
	 * 
	 * @return the local monitor's screen dimensions.
	 */
	public Dimension getScreenSize() {
		return Utils.getScreenSize();
	}

	/**
	 * @return the current size of this Display regardless of whether or not it is being shown.
	 */
	public Dimension getSize() {
		return new Dimension(wt, ht);
	}

	/**
	 * Creates a new RenderControl to be used as a rendering handle for drawing to this Display.
	 * This method creates the renderer and adds it to the frame. Call <code>show()</code> to show
	 * the Display.
	 * 
	 * @param buffs
	 * @return
	 */
	public RenderControl getRenderControl(int buffs) {
		if (rc != null) {
			rc.setRenderActive(false);
			frame.remove(rc.canvas);
			rc.dispose();
		}
		rc = new RenderControl(buffs, graphicsConfig);
		frame.add(rc.canvas);
		return rc;
	}

	/**
	 * Shows this Display's frame on screen. If this is a fullscreen window, this method will set
	 * the frame to be activated as the system's full screen display view.
	 */
	public void show() {
		if (type == Type.FULLSCREEN) {
			GraphicsDevice d = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if (d.isFullScreenSupported() && type.useNativeFullscreen) {
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

	/**
	 * Hides this Display's frame on screen. If this is a fullscreen window, this method will remove
	 * the frame from the system's full screen display view. For windowed mode, this will minimize
	 * the window on most desktop environments.
	 */
	public void hide() {
		rc.setRenderActive(false);
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		d.setFullScreenWindow(null);
		frame.setVisible(false);
	}

	/**
	 * Disposes this Display and its rendering handle (if any). Once disposed, a Display object
	 * should be discarded. It's a good idea to call this only when the game is exiting.
	 */
	public void dispose() {
		if (rc != null) {
			rc.dispose();
		}
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		d.setFullScreenWindow(null);
		frame.dispose();
	}

	public enum Type {

		WINDOWED, FULLSCREEN;

		boolean useNativeFullscreen = true;

		/**
		 * Only relevant for Type.FULLSCREEN mode. Sets the Display as a native fullscreen window
		 * rather than just displaying a screen-sized frame. This feature is especially useful on
		 * Windows systems. <br/>
		 * <br/>
		 * Note: Using this feature will often cause the native graphics driver to enforce settings
		 * like vertical-sync or triple buffering on the rendering system.
		 * 
		 * @param nativeFullscreen
		 */
		public void setUseNativeFullscreen(boolean nativeFullscreen) {
			this.useNativeFullscreen = nativeFullscreen;
		}

		public boolean isNativeFullscreen() {
			return useNativeFullscreen;
		}
	}

	// ----- DEPRECATED ------- //

	@Deprecated
	/**
	 * This method simply calls <code>show()</code> for backwards compatibility.
		if(rc != null) {
	 * @param rc
	 */
	public void show(RenderControl rc) {
		show();
	}

	@Deprecated
	/**
	 * This method is generally useless because the RenderControl will occupy the frame's drawing
	 * space.  Anything drawn on the content pane graphics probably won't show up.
	 * @return
	 */
	public Graphics2D getRawGraphics() {
		return (Graphics2D) frame.getContentPane().getGraphics();
	}
}
