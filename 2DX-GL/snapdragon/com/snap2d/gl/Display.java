/*
 *  Copyright © 2011-2012 Brian Groenke
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

/**
 * Represents the underlying frame used to display rendered content on screen.  Display
 * creates a JFrame used to display a RenderControl's BufferStrategy.  This is essentially
 * the entry point for graphics rendering in Snapdragon2D and works tightly in conjunction with
 * RenderControl.
 * @author Brian Groenke
 * @since Snapdragon2D 1.0
 */
public class Display {

	private int wt, ht;
	private Type type;
	private JFrame frame;
	private RenderControl rc;

	/**
	 * Creates the Display with default GLConfig.
	 * @param width
	 * @param height
	 * @param type
	 */
	public Display(int width, int height, Type type) {
		this(width, height, type, new GLConfig());
	}

	/**
	 * Creates the Display and initializes Java2D with the given GLConfig settings.
	 * If <code>config</code> is null, Java's default configuration will be used.
	 * @param width width of the window (irrelevant for Type.FULLSCREEN)
	 * @param height height of the window (irrelevant for Type.FULLSCREEN)
	 * @param type the type of Display to be shown
	 * @param config the graphics configuration to use.
	 */
	public Display(int width, int height, Type type, GLConfig config) {
		this.wt = width;
		this.ht = height;
		this.type = type;
		if(config != null)
			config.apply();
		init();
	}

	/**
	 * Internal method that performs initiation of the Display. Called by the
	 * constructor.
	 */
	protected void init() {
		switch (type) {
		case FULLSCREEN:
			frame = new JFrame();
			frame.setUndecorated(true);
			frame.setLocation(0, 0);
			frame.setSize(getScreenSize());
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

	public void setTitle(String str) {
		frame.setTitle(str);
	}

	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}

	public void setSize(int x, int y) {
		frame.setSize(x, y);
	}

	/**
	 * Convenience method for
	 * <code>Toolkit.getDefaultToolkit().getScreenSize()</code>.
	 * 
	 * @return the local monitor's screen dimensions.
	 */
	public Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public RenderControl getRenderControl(int buffs) {
		return new RenderControl(buffs);
	}

	public void show(RenderControl rc) {
		if(rc == null)
			return;
		this.rc = rc;
		frame.add(rc.canvas);
		frame.setVisible(true);
		if(type == Type.FULLSCREEN) {
			GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if(d.isFullScreenSupported())
				d.setFullScreenWindow(frame);
		}
	}

	public Graphics2D getRawGraphics() {
		return (Graphics2D) frame.getContentPane().getGraphics();
	}

	public void hide() {
		rc.setRenderActive(false);
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		d.setFullScreenWindow(null);
		frame.setVisible(false);
	}

	public void dispose() {
		rc.dispose();
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		d.setFullScreenWindow(null);
		frame.dispose();
	}

	public enum Type {

		WINDOWED, FULLSCREEN;
	}
}
