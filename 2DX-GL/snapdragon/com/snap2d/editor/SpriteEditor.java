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

package com.snap2d.editor;

import javax.swing.*;

/**
 * @author Brian Groenke
 *
 */
public class SpriteEditor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7267162673618479594L;
	
	public static final int DEFAULT_SIZE = 750;
	public static final String TITLE = "Snapdragon2D Sprite Editor";
	
	private static SpriteEditor editor;
	
	EditPanel canvas;
	JScrollPane scroller;
	
	public SpriteEditor() {
		canvas = new EditPanel(this);
		scroller = new JScrollPane(canvas);
		add(scroller);
		this.setSize(DEFAULT_SIZE, DEFAULT_SIZE);
		this.setLocationRelativeTo(null);
		this.setTitle(TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void updateScrollPane() {
		scroller.revalidate();
		scroller.repaint();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		editor = new SpriteEditor();
		editor.setVisible(true);
	}

}
