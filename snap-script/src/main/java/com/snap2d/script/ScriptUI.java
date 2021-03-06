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

package com.snap2d.script;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import bg.x2d.utils.Utils;

/**
 * A graphical interface utility that allows for live script
 * compilation/execution.
 *
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

    private static final PrintStream STD_OUT = System.out;

    private final OutStream printOut = new OutStream(System.out);

    JFrame parent = this;
    JSplitPane rootPane;
    JTextArea input, output;
    JMenu file, script;
    JMenuItem exit, save, open, compile, run, clear;

    ScriptProgram prog;
    ScriptSource src;
    Function[] funcs = new Function[0];

    Function lastRun;
    Object[] lastRunArgs;

    public ScriptUI(final int exitOp) {

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
        rootPane.setBorder(new EmptyBorder(0, 5, 0, 5));
        rootPane.setOneTouchExpandable(true);
        rootPane.setDividerLocation(WIDTH / 2);
        add(BorderLayout.CENTER, rootPane);
        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(new EmptyBorder(0, 20, 0, 20));
        north.add(BorderLayout.WEST, new JLabel("<html><font size=3> Script: </font></html>"));
        north.add(BorderLayout.EAST, new JLabel("<html><font size=3> Output: </font></html>"));
        add(BorderLayout.NORTH, north);
        setJMenuBar(initMenuBar());
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(exitOp);
        addWindowListener(new WindowMonitor());
        readTempFile();
        validate();
        src = new ScriptSource(input.getText() + "\n");
        prog = new ScriptProgram(true, src);
    }

    public ScriptProgram getScriptProgram() {

        return prog;
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
            public void actionPerformed(final ActionEvent e) {

                System.exit(0);
                ;
            }
        });
        compile = new JMenuItem("Compile");
        compile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                runCompiler();
            }

        });
        run = new JMenuItem("Run");
        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                runDialog();
            }

        });
        clear = new JMenuItem("Clear");
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

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

    public void runCompiler(final Class<?>... javaClasses) {

        src.setSourceFrom(input.getText() + "\n");
        ;
        for (Class<?> c : javaClasses) {
            prog.link(c);
        }
        output.setText("");
        boolean success = prog.compile();
        if (success) {
            try {
                prog.initRuntime(true);
                funcs = prog.getScriptFunctions();
            } catch (ScriptInvocationException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                                              "Error initializing runtime: " + e.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        } else {
            ScriptCompilationException e = prog.getLastCompileError();
            String fname = (e.getFunction() != null) ? e.getFunction().getName() : "";
            output.setText(output.getText() + "compilation error in function '" + fname + "'" + "\n"
                            + prog.getLastCompileError().toString());
        }
    }

    RunDialog diag;

    private void runDialog() {

        if (diag == null || !diag.isDisplayable()) {
            diag = new RunDialog(parent, "Run Script");
        }
        if ( !diag.isVisible()) {
            diag.setVisible(true);
        }
    }

    private static int threadCount = 0;

    private void invokeScriptFunction(final Function f, final Object... args) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                output.setText(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())
                                + "\n<Executing script function: invocation target -> fid=" + f.getID() + ">\n\n");
                f.bytecode.rewind();
                Runnable r = new Runnable() {
                    public void run() {
                        Object ret;
                        try {
                            ret = prog.invoke(f, args);
                            lastRun = f;
                            lastRunArgs = args;
                            if (f.getReturnType() != Keyword.VOID) {
                                System.out.print("return value = " + ret.toString() + "\n");
                            }
                        } catch (ScriptInvocationException e) {
                            System.out.println(e.toString());
                            e.printStackTrace();
                        }
                    }
                };
                Thread scriptThread = new Thread(r);
                scriptThread.setName("scriptui" + threadCount + "-invoke [fid=" + f.getID() + "]");
                scriptThread.start();
            }

        }).start();
    }

    private class KeyEventListener extends KeyAdapter {

        @Override
        public void keyPressed(final KeyEvent ke) {

            if (ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isShiftDown()) {
                runCompiler();
            } else if (ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isControlDown()) {
                runDialog();
            } else if (ke.getKeyCode() == KeyEvent.VK_R && ke.isControlDown()) {
                if (lastRun != null) {
                    invokeScriptFunction(lastRun, lastRunArgs);
                }
            }
        }
    }

    private String lastArgs, lastItem;

    private class RunDialog extends JDialog {

        /**
         *
         */
        private static final long serialVersionUID = -473771619157816946L;

        JComboBox chooseFunc;
        JTextField argField;

        @SuppressWarnings("serial")
        public RunDialog(final Frame parent, final String title) {

            super(parent, title);
            JPanel panel = new JPanel();
            String[] funcStrs = new String[funcs.length];
            for (int i = 0; i < funcStrs.length; i++ ) {
                StringBuilder sb = new StringBuilder();
                Keyword[] params = funcs[i].getParamTypes();
                for (int ii = 0; ii < funcs[i].getParamCount(); ii++ ) {
                    sb.append(params[ii].sym + ( (ii < funcs[i].getParamCount() - 1) ? ", " : ""));
                }
                funcStrs[i] = funcs[i].getName() + " (" + sb.toString() + ")";
            }
            chooseFunc = new JComboBox(funcStrs);
            chooseFunc.setPreferredSize(new Dimension(150, chooseFunc.getPreferredSize().height));
            argField = new JTextField(15);
            if (lastArgs != null && lastItem != null) {
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
                public void actionPerformed(final ActionEvent e) {

                    if (chooseFunc.getSelectedIndex() < 0) {
                        return;
                    }
                    lastItem = (String) chooseFunc.getSelectedItem();
                    lastArgs = argField.getText();
                    Function f = funcs[chooseFunc.getSelectedIndex()];
                    dispose();
                    Keyword[] params = f.getParamTypes();
                    String[] argStrs = argField.getText().split(",");
                    int len = (argStrs[0].isEmpty()) ? 0 : argStrs.length;
                    Object[] args = new Object[argStrs.length];
                    for (int i = 0; i < params.length; i++ ) {
                        String arg = argStrs[i].trim();
                        try {
                            if (len != params.length) {
                                throw (new ScriptInvocationException("invalid argument count", f));
                            }
                            switch (params[i]) {
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
                                if (arg.startsWith("\"") && arg.endsWith("\"")) {
                                    args[i] = arg.substring(1, arg.length() - 1);
                                } else {
                                    throw (new ScriptInvocationException("illegal string argument: " + arg, f));
                                }
                                break;
                            default:
                            }
                        } catch (Exception err) {
                            System.out.println("error parsing function arguments: \n" + err.toString());
                            err.printStackTrace();
                            return;
                        }
                    }

                    invokeScriptFunction(f, args);
                }
            };
            run.addActionListener(invoke);
            panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "invokeOnEnter");
            panel.getActionMap().put("invokeOnEnter", new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {

                    invoke.actionPerformed(e);
                }

            });
            btnPanel.add(run);
            add(BorderLayout.CENTER, panel);
            add(BorderLayout.SOUTH, btnPanel);
            pack();
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            chooseFunc.requestFocus();
        }
    }

    private class WindowMonitor extends WindowAdapter {

        @Override
        public void windowClosing(final WindowEvent e) {

            System.setOut(STD_OUT);
            try {
                Utils.writeToTempStorage(input.getText(), TEMP_FILE, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void windowActivated(final WindowEvent e) {

            System.setOut(printOut);
        }
    }

    private void readTempFile() {

        String text;
        try {
            text = Utils.readText(Utils.getFileURL(new File(System.getProperty("java.io.tmpdir") + File.separator
                            + TEMP_FILE)));
            input.setText(text);
        } catch (IOException e) {
            System.out.println("ScriptUI: no tmp file - initializing new input data...");
        }
    }

    private class OutStream extends PrintStream {

        /**
         * @param out
         */
        public OutStream(final OutputStream out) {

            super(out);
        }

        @Override
        public void println(final String s) {

            output.setText(output.getText() + s.toString() + "\r\n");
        }

        @Override
        public void print(final String s) {

            output.setText(output.getText() + s);
        }

        @Override
        public void println(final int arg) {

            println(String.valueOf(arg));
        }

        @Override
        public void print(final int arg) {

            print(String.valueOf(arg));
        }

        @Override
        public void println(final double arg) {

            println(String.valueOf(arg));
        }

        @Override
        public void print(final double arg) {

            print(String.valueOf(arg));
        }

        @Override
        public void println(final boolean arg) {

            println(String.valueOf(arg));
        }

        @Override
        public void print(final boolean arg) {

            print(String.valueOf(arg));
        }
    }

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(final String[] args) throws FileNotFoundException {

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
        ScriptUI sui = new ScriptUI(EXIT_ON_CLOSE);
        sui.setVisible(true);
    }
}
