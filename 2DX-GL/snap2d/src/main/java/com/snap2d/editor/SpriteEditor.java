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

package com.snap2d.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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

    JFrame frame = this;
    EditPanel canvas;
    JScrollPane scroller;

    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenuItem open, save, exit;

    public SpriteEditor() {

        canvas = new EditPanel(this);
        scroller = new JScrollPane(canvas);
        add(scroller);
        this.setSize(DEFAULT_SIZE, DEFAULT_SIZE);
        this.setLocationRelativeTo(null);
        this.setTitle(TITLE);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent evt) {

                exit();
            }
        });

        menuBar.add(file);
        open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                canvas.load();
            }

        });
        file.add(open);
        save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                canvas.save();
            }
        });
        file.add(save);
        exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                exit();
            }
        });
        file.add(exit);
        this.setJMenuBar(menuBar);
    }

    public void updateScrollPane() {

        scroller.revalidate();
        scroller.repaint();
    }

    private final void exit() {

        int resp = JOptionPane.YES_OPTION;
        if (canvas.fileStatus == EditPanel.CHANGED) {
            resp = JOptionPane.showConfirmDialog(frame,
                                                 "Exit without saving?",
                                                 "Confirm Exit",
                                                 JOptionPane.YES_NO_CANCEL_OPTION);
            switch (resp) {
            case JOptionPane.NO_OPTION:
                boolean chk = canvas.save();
                if ( !chk) {
                    return;
                }
            case JOptionPane.YES_OPTION:
                frame.dispose();
                System.exit(0);
                break;
            default:
            }
        } else {
            System.exit(0);
        }
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor = new SpriteEditor();
        editor.setVisible(true);
    }

}
