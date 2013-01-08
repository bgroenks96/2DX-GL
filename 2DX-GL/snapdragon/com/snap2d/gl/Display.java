/*
 * Copyright � 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.snap2d.gl;

import java.awt.*;

import javax.swing.*;

/**
 * 
 * @author brian
 * @since Snapdragon2D 1.0
 */
public class Display {

	private int wt, ht;
	private Type type;
	private JFrame frame;
	private RenderControl rc;

	public Display(int width, int height, Type type) {
		this.wt = width;
		this.ht = height;
		this.type = type;
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

	/**
	 * Convenience method for
	 * <code>Toolkit.getDefaultToolkit().getScreenSize()</code>.
	 * 
	 * @return the local monitor's screen dimensions.
	 */
	public Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public void show(RenderControl render) {
		rc = render;
		frame.add(rc.canvas);
		frame.setVisible(true);
		GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if(d.isFullScreenSupported())
			d.setFullScreenWindow(frame);
		else
			frame.setSize(getScreenSize());
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
