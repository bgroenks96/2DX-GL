/*
 *  Copyright © 2012-2014 Brian Groenke
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
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import bg.x2d.utils.*;

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

	private static final int WIDTH = 1100, HEIGHT = 750;
	private static final String TEMP_FILE = "snap2d_scriptui_tmpstr.txt";

	JFrame parent = this;
	JSplitPane rootPane;
	JTextArea input, output;
	JMenu file, script;
	JMenuItem exit, save, open, compile, run, clear;

	ScriptProgram prog;
	Function[] funcs = new Function[0];

	public ScriptUI() {
		super("SnapScript Editor Interface");
		input = new JTextArea();
		input.setMinimumSize(new Dimension(1, 200));
		input.addKeyListener(new KeyEventListener());
		input.setFont(new Font("Verdana", Font.PLAIN, 13));
		output = new JTextArea();
		output.setMinimumSize(new Dimension(1, 200));
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setFont(new Font("Courier", Font.PLAIN, 13));
		rootPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(input), new JScrollPane(output));
		rootPane.setOneTouchExpandable(true);
		rootPane.setDividerLocation(WIDTH / 2);
		add(BorderLayout.CENTER, rootPane);
		JPanel north = new JPanel(new BorderLayout());
		north.setBorder(new EmptyBorder(0,20,0,20));
		north.add(BorderLayout.WEST, new JLabel("<html><font size=3> Script: </font></html>"));
		north.add(BorderLayout.EAST, new JLabel("<html><font size=3> Output: </font></html>"));
		add(BorderLayout.NORTH, north);
		setJMenuBar(initMenuBar());
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new OnWindowClose());
		System.setOut(new OutStream(System.out));
		readTempFile();
		validate();
	}

	private JMenuBar initMenuBar() {
		JMenuBar jmb = new JMenuBar();
		file = new JMenu("File");
		script = new JMenu("Script");
		jmb.add(file);
		jmb.add(script);
		exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);;
			}
		});
		compile = new JMenuItem("Compile");
		compile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runCompiler();
			}

		});
		run = new JMenuItem("Run");
		run.addActionListener(new ActionListener() {


			@Override
			public void actionPerformed(ActionEvent e) {
				runDialog();
			}

		});
		clear = new JMenuItem("Clear");
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				input.setText("");
				output.setText("");
			}
		});
		file.add(exit);
		script.add(compile);
		script.add(run);
		script.add(clear);

		return jmb;
	}

	private void runCompiler() {
		prog = new ScriptProgram(true, new ScriptSource(input.getText() + "\n"));
		output.setText("");
		boolean success = prog.compile();
		if(success) {
			try {
				prog.initRuntime(true);
				funcs = prog.getScriptFunctions();
			} catch (ScriptInvocationException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "Error initializing runtime: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			output.setText(output.getText() + prog.getLastCompileError().toString());
		}
	}
	
	RunDialog diag;
	
	private void runDialog() {
		if(diag == null || !diag.isDisplayable())
			diag = new RunDialog(parent, "Run Script");
		if(!diag.isVisible())
			diag.setVisible(true);
	}

	private class KeyEventListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent ke) {
			if(ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isShiftDown()) {
				runCompiler();
			} else if(ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isControlDown())
				runDialog();
		}
	}
	
	private String lastArgs, lastItem;

	private class RunDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = -473771619157816946L;

		JComboBox<String> chooseFunc;
		JTextField argField;

		@SuppressWarnings("serial")
		public RunDialog(Frame parent, String title) {
			super(parent, title);
			JPanel panel = new JPanel();
			String[] funcStrs = new String[funcs.length];
			for(int i=0; i < funcStrs.length; i++) {
				StringBuilder sb = new StringBuilder();
				Keyword[] params = funcs[i].getParamTypes();
				for(int ii=0; ii < funcs[i].getParamCount(); ii++) {
					sb.append(params[ii].sym + ((ii < funcs[i].getParamCount() - 1) ? ", ":""));
				}
				funcStrs[i] = funcs[i].getName() + " ("+sb.toString()+")";
			}
			chooseFunc = new JComboBox<String>(funcStrs);
			chooseFunc.setPreferredSize(new Dimension(150, chooseFunc.getPreferredSize().height));
			argField = new JTextField(15);
			if(lastArgs != null && lastItem != null) {
				argField.setText(lastArgs);
				chooseFunc.setSelectedItem(lastItem);
			}
			panel.add(new JLabel("Choose function: "));
			panel.add(Box.createHorizontalStrut(10));
			panel.add(chooseFunc);
			panel.add(argField);
			JPanel btnPanel = new JPanel();
			JButton run = new JButton("Invoke");
			final ActionListener invoke = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(chooseFunc.getSelectedIndex() < 0)
						return;
					lastItem = (String) chooseFunc.getSelectedItem();
					lastArgs = argField.getText();
					Function f = funcs[chooseFunc.getSelectedIndex()];
					output.setText(new SimpleDateFormat("dd MMM HH:mm:ss").format(Calendar.getInstance().getTime())
							+"\n<Executing script function: invocation target -> fid="+f.getID()+">\n\n");
					dispose();
					Keyword[] params = f.getParamTypes();
					String[] argStrs = argField.getText().split(",");
					int len = (argStrs[0].isEmpty()) ? 0:argStrs.length;
					Object[] args = new Object[argStrs.length];
					for(int i=0; i < params.length; i++) {
						String arg = argStrs[i].trim();
						try {
							if(len != params.length)
								throw(new ScriptInvocationException("invalid argument count", f));
							switch(params[i]) {
							case INT:
								args[i] = Integer.parseInt(arg);
								break;
							case FLOAT:
								args[i] = Double.parseDouble(arg);
								break;
							case BOOL:
								args[i] = Boolean.parseBoolean(arg);
								break;
							case STRING:
								if(arg.startsWith("\"") && arg.endsWith("\"")) {
									args[i] = arg.substring(1, arg.length() - 1);
								} else
									throw(new ScriptInvocationException("illegal string argument: " + arg, f));
								break;
							default:
							}
						} catch (Exception err) {
							System.out.println("error parsing function arguments: \n" + err.toString());
							err.printStackTrace();
							return;
						}
					}
					
					try {
						Object ret = prog.invoke(f, args);
						if(f.getReturnType() != Keyword.VOID)
							System.out.print(ret.toString() + "\n");
					} catch (ScriptInvocationException e1) {
						e1.printStackTrace();
					}
				}
			};
			run.addActionListener(invoke);
			panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "invokeOnEnter");
			panel.getActionMap().put("invokeOnEnter", new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					invoke.actionPerformed(e);
				}
				
			});
			btnPanel.add(run);
			add(BorderLayout.CENTER, panel);
			add(BorderLayout.SOUTH, btnPanel);
			pack();
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			chooseFunc.requestFocus();
		}
	}
	
	private class OnWindowClose extends WindowAdapter {
		
		@Override
		public void windowClosing(WindowEvent e) {
			try {
				Utils.writeToTempStorage(input.getText(), TEMP_FILE, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	private void readTempFile() {
		String text;
		try {
			text = Utils.readText(Utils.getFileURL(new File(System.getProperty("java.io.tmpdir") + File.separator + TEMP_FILE)));
			input.setText(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class OutStream extends PrintStream {

		/**
		 * @param out
		 */
		public OutStream(OutputStream out) {
			super(out);
		}

		@Override
		public void println(String s) {
			output.setText(output.getText() + s.toString() + "\r\n");
		}

		@Override
		public void print(String s) {
			output.setText(output.getText() + s);
		}
		
		@Override
		public void println(int arg) {
			println(String.valueOf(arg));
		}
		
		@Override
		public void print(int arg) {
			print(String.valueOf(arg));
		}
		
		@Override
		public void println(double arg) {
			println(String.valueOf(arg));
		}
		@Override
		public void print(double arg) {
			print(String.valueOf(arg));
		}
		@Override
		public void println(boolean arg) {
			println(String.valueOf(arg));
		}
		@Override
		public void print(boolean arg) {
			print(String.valueOf(arg));
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
