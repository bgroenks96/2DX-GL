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

package com.snap2d.script;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * A graphical interface utility that allows for live script compilation/execution.
 * @author Brian Groenke
 *
 */
public class ScriptUI extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7280184562521580572L;

	private static final int WIDTH = 600, HEIGHT = 800;
	
	JFrame parent = this;
	JSplitPane rootPane;
	JEditorPane input, output;
	
	public ScriptUI() {
		input = new JEditorPane();
		input.setMinimumSize(new Dimension(1, 200));
		output = new JEditorPane();
		output.setMinimumSize(new Dimension(1, 200));
		output.setEditable(false);
		rootPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, input, output);
		rootPane.setOneTouchExpandable(true);
		rootPane.setDividerLocation(WIDTH / 2);
		add(BorderLayout.CENTER, rootPane);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		validate();
	}
	
	private class CompileListener extends KeyAdapter {
		
		@Override
		public void keyPressed(KeyEvent ke) {
			if(ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isShiftDown()) {
				ScriptProgram prog = new ScriptProgram(true, new ScriptSource(input.getText()));
				boolean success = prog.compile();
				if(success) {
					try {
						prog.initRuntime(true);
						
					} catch (ScriptInvocationException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(parent, "Execution error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					output.setText(prog.getLastCompileError().toString());
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		ScriptUI sui = new ScriptUI();
		sui.setVisible(true);
	}
}
