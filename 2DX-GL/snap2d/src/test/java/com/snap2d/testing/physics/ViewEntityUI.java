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

package com.snap2d.testing.physics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bg.x2d.geo.Vector2d;

import com.snap2d.testing.physics.PhysicsUI.PUICallback;

/**
 * @author Brian Groenke
 *
 */
public class ViewEntityUI extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1110753895193903414L;

    private static final Dimension DIALOG_SIZE = new Dimension(360, 250);

    private PUICallback uiCallback;
    private EntitySelected selListener;
    private final JDialog parent = this;

    JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    JList list = new JList();
    Box box;
    HashMap<EntityProperty, JComponent> propFields = new HashMap<EntityProperty, JComponent>();
    ArrayList<PhysicsEntity> entityList;

    public ViewEntityUI(final PUICallback callback, final ArrayList<PhysicsEntity> entities) {

        this.uiCallback = callback;
        this.entityList = entities;
        String[] data = new String[entities.size()];
        for (int i = 0; i < entities.size(); i++ ) {
            data[i] = "entity" + entityList.get(i).getID();
        }
        list.setListData(data);
        selListener = new EntitySelected();
        list.addListSelectionListener(selListener);
        root.add(list);
        buildPropertyContainer();
        root.add(box);
        root.setDividerLocation(DIALOG_SIZE.width / 3);

        JPanel btnPanel = new JPanel();
        JButton ok = new JButton("OK");
        JButton del = new JButton("Remove");
        btnPanel.add(ok);
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (loaded == null) {
                    uiCallback.onClose(parent);
                    return;
                }

                boolean chk = parseInputValues();
                if (chk) {
                    uiCallback.onClose(parent);
                } else {
                    JOptionPane.showMessageDialog(parent, "Error parsing properties: one or more inputs is invalid");
                }
            }

        });
        del.addActionListener(new ActionListener() {

            // remove entity and re-initialize relevant UI components
            @Override
            public void actionPerformed(final ActionEvent e) {

                uiCallback.remove(loaded);
                list = new JList();
                String[] data = new String[entityList.size()];
                for (int i = 0; i < entityList.size(); i++ ) {
                    data[i] = "entity" + entityList.get(i).getID();
                }
                list.setListData(data);
                remove(root);
                root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
                root.add(list);
                buildPropertyContainer();
                root.add(box);
                root.setDividerLocation(DIALOG_SIZE.width / 3);
                add(BorderLayout.CENTER, root);
                loaded = null;
                list.addListSelectionListener(selListener); // restore listener
                validate();
            }
        });
        btnPanel.add(del);
        ok.setPreferredSize(del.getPreferredSize());
        add(BorderLayout.CENTER, root);
        add(BorderLayout.SOUTH, btnPanel);
        setSize(DIALOG_SIZE);
        setLocationRelativeTo(null);
        addWindowListener(new OnWinClose());
    }

    private void buildPropertyContainer() {

        box = Box.createVerticalBox();
        for (EntityProperty prop : EntityProperty.values()) {
            JLabel label = new JLabel(prop.label);
            JComponent opt = null;
            switch (prop.type) {
            case PhysicsUI.FLOAT:
            case PhysicsUI.VECTOR:
            case PhysicsUI.STRING:
                opt = new JTextField(7);
                break;
            case PhysicsUI.BOOLEAN:
                opt = new JCheckBox();
            }
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(BorderLayout.WEST, label);
            JPanel optPanel = new JPanel();
            optPanel.add(opt);
            panel.add(BorderLayout.EAST, optPanel);
            box.add(panel);
            propFields.put(prop, opt);
        }
        box.setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private PhysicsEntity loaded;

    /*
     * this method runs each time the selection is changed or when 'OK' is
     * pressed to parse all of the current input values and apply them to the
     * selected PhysicsEntity object
     */
    private boolean parseInputValues() {

        PhysicsEntity pe = loaded;
        double mass = 0, coll = 0;
        Vector2d vel = null, size = null;
        boolean nograv = false;
        for (EntityProperty prop : EntityProperty.values()) {
            JComponent comp = propFields.get(prop);
            try {
                switch (prop) {
                case MASS:
                    mass = EntityProperty.parseDouble( ((JTextField) comp).getText());
                    break;
                case VELOCITY:
                    vel = EntityProperty.parseVector( ((JTextField) comp).getText());
                    break;
                case COLL_FACTOR:
                    coll = EntityProperty.parseDouble( ((JTextField) comp).getText());
                    break;
                case SIZE:
                    size = EntityProperty.parseVector( ((JTextField) comp).getText());
                    break;
                case NO_GRAVITY:
                    nograv = ((JCheckBox) comp).isSelected();
                }
            } catch (PropertyFormatException e) {
                e.printStackTrace();
                return false;
            }
        }
        pe.node.setMass(mass);
        pe.node.setVelocity(vel);
        pe.setSize(size);
        pe.collFactor = coll;
        pe.node.noGravity = nograv;
        return true;
    }

    private class EntitySelected implements ListSelectionListener {

        /**
         * Load EntityProperty values from corresponding PhysicsEntity object
         * into input fields.
         */
        @Override
        public void valueChanged(final ListSelectionEvent e) {

            if (e.getValueIsAdjusting()) {
                return;
            }
            if (loaded != null) {
                parseInputValues();
            }
            PhysicsEntity pe = entityList.get(list.getSelectedIndex());
            loaded = pe;
            for (EntityProperty prop : EntityProperty.values()) {
                JComponent comp = propFields.get(prop);
                switch (prop) {
                case MASS:
                    ((JTextField) comp).setText("" + pe.node.getMass());
                    break;
                case VELOCITY:
                    Vector2d vel = pe.node.getVelocity2d();
                    ((JTextField) comp).setText(vel.x + "i" + ( (vel.y >= 0) ? "+" : "") + vel.y + "j");
                    break;
                case COLL_FACTOR:
                    ((JTextField) comp).setText("" + pe.collFactor);
                    break;
                case SIZE:
                    ((JTextField) comp).setText(pe.wt + "i" + ( (pe.ht >= 0) ? "+" : "") + pe.ht + "j");
                    break;
                case NO_GRAVITY:
                    ((JCheckBox) comp).setSelected(pe.node.hasNoGravity());
                }
            }
        }
    }

    private class OnWinClose extends WindowAdapter {

        @Override
        public void windowClosing(final WindowEvent we) {

            uiCallback.onClose(parent);
        }
    }
}
