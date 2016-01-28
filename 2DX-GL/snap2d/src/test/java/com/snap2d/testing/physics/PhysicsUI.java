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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import bg.x2d.geo.PointUD;
import bg.x2d.geo.Vector2d;

import com.snap2d.gl.Display;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.GraphicsConfig;
import com.snap2d.gl.GraphicsConfig.Property;
import com.snap2d.gl.RenderControl;
import com.snap2d.gl.Renderable;
import com.snap2d.input.InputDispatch;
import com.snap2d.input.KeyEventClient;
import com.snap2d.input.MouseEventClient;
import com.snap2d.physics.GamePhysics;
import com.snap2d.script.ScriptUI;
import com.snap2d.world.Entity;
import com.snap2d.world.EntityListener;
import com.snap2d.world.EntityManager;
import com.snap2d.world.World2D;
import com.snap2d.world.event.AddEvent;
import com.snap2d.world.event.CollisionEvent;
import com.snap2d.world.event.EntityCollision;
import com.snap2d.world.event.RemoveEvent;

/**
 * @author Brian Groenke
 *
 */
public class PhysicsUI {

    public static final int FLOAT = 0xFFF001, BOOLEAN = 0xFFF002, STRING = 0xFFF003, VECTOR = 0xFFF004,
                    OPTION = 0xFFF005;

    static ScriptUI scriptUI = new ScriptUI(ScriptUI.DISPOSE_ON_CLOSE);

    Display disp;
    RenderControl rc;
    InputDispatch input = new InputDispatch(false);

    ArrayList<PhysicsEntity> entities = new ArrayList<PhysicsEntity>();
    EntityManager manager = new EntityManager();
    PhysicsEntityEventListener collListener = new PhysicsEntityEventListener();

    JMenuBar menuBar = new JMenuBar();
    JMenu control = new JMenu("Control"), worldMenu = new JMenu("World");
    JMenuItem addEntity = new JMenuItem("Add Entity"), viewEntity = new JMenuItem("View Entities"),
                    addForce = new JMenuItem("Add Forces"), modifyView = new JMenuItem("Set viewport"),
                    editScript = new JMenuItem("Edit Script");

    boolean isDialogShowing;

    World2D world;

    PhysicsUI() {

        GraphicsConfig config = GraphicsConfig.getDefaultSystemConfig();
        config.set(Property.USE_OPENGL, "true");
        config.set(Property.USE_LINUX_XRENDER, "false");
        config.set(Property.SNAP2D_PRINT_RENDER_STAT, "false");
        disp = new Display(800, 600, Type.WINDOWED, config);
        rc = disp.getRenderControl(3);
        rc.addRenderable(new Background(), 0);
        rc.addRenderable(new InfoDisplay(), 1);
        registerKeyClient(new KeyEventClient() {

            @Override
            public void processKeyEvent(final KeyEvent e) {

                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    rc.setDisableUpdates(rc.isUpdating());
                }
            }

        });

        registerMouseClient(new EntityClickMovement());
    }

    public void init() {

        scriptUI.runCompiler(TestNode.CollisionData.class);
        setUpControlMenu();
        disp.getJFrame().setJMenuBar(menuBar);
        disp.setTitle("Snap2D PhysicsUI - alpha v.0.2");
        disp.show();
        initWorld();
        rc.setTargetFPS(30);
        rc.setTargetTPS(30);
        rc.setRenderOp(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        rc.startRenderLoop();
    }

    public void initWorld() {

        world = new World2D(0, disp.getSize().height, disp.getSize().width, disp.getSize().height, 1);
    }

    public void showEntityUI() {

        if (scriptUI.getScriptProgram().findFunction(TestNode.COLL_FUNC_NAME) == null) {
            JOptionPane.showMessageDialog(null, "Can't find script function: " + TestNode.COLL_FUNC_NAME);
            return;
        }

        if ( !isDialogShowing) {
            rc.setRenderActive(false);
            new AddEntityUI().setVisible(true);
            isDialogShowing = true;
        }
    }

    public void showViewUI() {

        if ( !isDialogShowing) {
            rc.setRenderActive(false);
            new ViewEntityUI(new PUICallback(), entities).setVisible(true);
            isDialogShowing = true;
        }
    }

    public void addEntity(final PhysicsEntity pe, final int pos) {

        // rc.addRenderable(pe, pos);
        manager.register(pe, collListener);
        entities.add(pe);
        rc.addRenderable(pe, 1);
    }

    public void removeEntity(final PhysicsEntity pe) {

        // rc.removeRenderable(pe);
        manager.unregister(pe);
        entities.remove(pe);
    }

    public void registerKeyClient(final KeyEventClient keyc) {

        input.registerKeyClient(keyc);
    }

    public void registerMouseClient(final MouseEventClient mousec) {

        input.registerMouseClient(mousec);
    }

    private void setUpControlMenu() {

        addEntity.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                showEntityUI();
            }
        });
        viewEntity.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                showViewUI();
            }
        });
        modifyView.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                new ViewportUI().setVisible(true);
                isDialogShowing = true;
            }

        });
        editScript.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                scriptUI.setVisible(true);
            }
        });
        control.add(addEntity);
        control.add(viewEntity);
        control.add(addForce);
        control.add(editScript);
        worldMenu.add(modifyView);
        menuBar.add(control);
        menuBar.add(worldMenu);
    }

    void close(final JDialog dialog) {

        dialog.setVisible(false);
        dialog.dispose();
        isDialogShowing = false;
    }

    /**
     * Obtains user input for adding a new Entity to the physics world
     * 
     * @author Brian Groenke
     *
     */
    private class AddEntityUI extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = 5776929083064004930L;

        private static final String DEF_MASS = "1", DEF_VEL = "0i+0j", DEF_CF = "1.0", DEF_SIZE = "50i+50j";

        private final int PROP_LEN = EntityProperty.values().length;
        private final int FIELD_SIZE = 6;
        private final EmptyBorder PANE_BORDER = new EmptyBorder(0, 10, 0, 5);

        Component[] options = new Component[PROP_LEN];

        JDialog parent = this;

        AddEntityUI() {

            super(disp.getJFrame(), "Add Physics Entity");
            Box box = Box.createVerticalBox();
            box.setBorder(PANE_BORDER);
            int i = 0;
            for (EntityProperty prop : EntityProperty.values()) {
                JLabel label = new JLabel(prop.label);
                Component option = null;
                if (prop.type == FLOAT || prop.type == STRING || prop.type == VECTOR) {
                    option = new JTextField(FIELD_SIZE);
                    switch (prop) {
                    case MASS:
                        ((JTextField) option).setText(DEF_MASS);
                        break;
                    case VELOCITY:
                        ((JTextField) option).setText(DEF_VEL);
                        break;
                    case COLL_FACTOR:
                        ((JTextField) option).setText(DEF_CF);
                        break;
                    case SIZE:
                        ((JTextField) option).setText(DEF_SIZE);
                    }
                } else if (prop.type == BOOLEAN) {
                    option = new JCheckBox();
                }
                options[i++ ] = option;

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(BorderLayout.WEST, label);
                JPanel optPan = new JPanel();
                optPan.add(option);
                panel.add(BorderLayout.EAST, optPan);
                box.add(panel);
            }
            add(BorderLayout.CENTER, box);
            JPanel btnPanel = new JPanel();
            JButton add = new JButton("Confirm");
            add.addActionListener(new ProcessAddEntity());
            JButton cancel = new JButton("Cancel");
            OnCloseListener closeListener = new OnCloseListener();
            cancel.addActionListener(closeListener);
            cancel.setPreferredSize(add.getPreferredSize());
            btnPanel.add(add);
            btnPanel.add(cancel);
            add(BorderLayout.SOUTH, btnPanel);
            pack();
            setLocationRelativeTo(null);
            addWindowListener(closeListener);
        }

        private class ProcessAddEntity implements ActionListener {

            /**
             *
             */
            @Override
            public void actionPerformed(final ActionEvent e) {

                EntityProperty[] props = EntityProperty.values();
                double mass = 0, coll = 0;
                boolean nograv = false;
                Vector2d vel = null, size = null;
                for (int i = 0; i < PROP_LEN; i++ ) {
                    EntityProperty prop = props[i];
                    try {
                        switch (prop) {
                        case MASS:
                            mass = EntityProperty.parseDouble( ((JTextField) options[i]).getText());
                            break;
                        case VELOCITY:
                            vel = EntityProperty.parseVector( ((JTextField) options[i]).getText());
                            break;
                        case SIZE:
                            size = EntityProperty.parseVector( ((JTextField) options[i]).getText());
                            break;
                        case COLL_FACTOR:
                            coll = EntityProperty.parseDouble( ((JTextField) options[i]).getText());
                            break;
                        case NO_GRAVITY:
                            nograv = ((JCheckBox) options[i]).isSelected();
                            break;
                        }
                    } catch (PropertyFormatException err) {
                        JOptionPane.showMessageDialog(parent,
                                                      "Parsing error: invalid format or characters in one or more property inputs\n"
                                                                      + err.toString());
                        return;
                    }
                }

                TestNode physNode = new TestNode(vel, mass, nograv);
                PhysicsEntity entity = new PhysicsEntity(0, 0, size.x, size.y, computeVertices(0, 0, size), physNode,
                                world);
                entity.collFactor = coll;
                addEntity(entity, RenderControl.POSITION_LAST);
                close(parent);
            }

            private PointUD[] computeVertices(final double x, final double y, final Vector2d size) {

                PointUD[] verts = new PointUD[4];
                verts[0] = new PointUD(x, y);
                verts[1] = new PointUD(x, y + size.y);
                verts[2] = new PointUD(x + size.x, y + size.y);
                verts[3] = new PointUD(x + size.x, y);
                return verts;
            }
        }

        private class OnCloseListener extends WindowAdapter implements ActionListener {

            @Override
            public void actionPerformed(final ActionEvent e) {

                close(parent);
            }

            @Override
            public void windowClosing(final WindowEvent e) {

                close(parent);
            }
        }

    }

    private class ViewportUI extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = -4793854080415174464L;

        private static final int FIELD_SIZE = 5, FIELD_NUM = 3;

        JDialog parent = this;

        ViewportUI() {

            JPanel panel = new JPanel();
            for (int i = 0; i < FIELD_NUM; i++ ) {
                String label = null;
                JTextField jtf = new JTextField(FIELD_SIZE);
                switch (i) {
                case 0:
                    label = "Min X ";
                    jtf.setText(String.valueOf(world.getX()));
                    break;
                case 1:
                    label = "Max Y ";
                    jtf.setText(String.valueOf(world.getMaxY()));
                    break;
                case 2:
                    label = "PPU ";
                    jtf.setText(String.valueOf(world.getPixelsPerUnit()));
                }
                JLabel txtLabel = new JLabel(label);
                panel.add(txtLabel);
                panel.add(jtf);
                panel.add(Box.createHorizontalStrut(5));
            }
            add(BorderLayout.CENTER, panel);
            JButton btn = new JButton("OK");
            JPanel bpan = new JPanel();
            bpan.add(btn);
            add(BorderLayout.SOUTH, bpan);
            pack();
            setLocationRelativeTo(null);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent evt) {

                    close(parent);
                }
            });
        }
    }

    private class Background implements Renderable {

        int wt, ht;

        /**
         *
         */
        @Override
        public void render(final Graphics2D g, final float interpolation) {

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, wt, ht);
            g.setColor(Color.RED);
        }

        /**
         *
         */
        @Override
        public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        }

        /**
         *
         */
        @Override
        public void onResize(final Dimension oldSize, final Dimension newSize) {

            this.wt = newSize.width;
            this.ht = newSize.height;
        }

    }

    private class InfoDisplay implements Renderable {

        final int PAD = 10;
        int wt, ht;

        /**
         *
         */
        @Override
        public void render(final Graphics2D g, final float interpolation) {

            g.setFont(new Font("Courier", Font.PLAIN, 13));
            FontMetrics fm = g.getFontMetrics();
            String renderOut = rc.getCurrentFPS() + " fps / " + rc.getCurrentTPS() + " ticks";
            Rectangle2D r = fm.getStringBounds(renderOut, g);
            g.setColor(Color.RED);
            g.drawString(renderOut, wt - (float) r.getWidth() - PAD, ht - PAD);
        }

        /**
         *
         */
        @Override
        public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        }

        /**
         *
         */
        @Override
        public void onResize(final Dimension oldSize, final Dimension newSize) {

            this.wt = newSize.width;
            this.ht = newSize.height;
            world.setViewSize(wt, ht, 1);
            world.setLocation(0, ht);
        }

    }

    private class EntityClickMovement implements MouseEventClient {

        private PointUD last;
        private PhysicsEntity held;

        /**
         *
         */
        @Override
        public void processMouseEvent(final MouseEvent me) {

            if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON1) {
                PointUD pt = world.screenToWorld(me.getX(), me.getY());
                if ( !rc.isUpdating()) {
                    for (PhysicsEntity pe : entities) {
                        if (pe.getWorldBounds().contains(pt.ux, pt.uy)) {
                            last = pt;
                            held = pe;
                        }
                    }
                }
            } else if (me.getID() == MouseEvent.MOUSE_DRAGGED && last != null) {
                PointUD npt = world.screenToWorld(me.getX(), me.getY());
                held.move(npt.ux - last.ux, npt.uy - last.uy);
                last = npt;
            } else if (me.getID() == MouseEvent.MOUSE_RELEASED && me.getButton() == MouseEvent.BUTTON1) {
                last = null;
            }
        }

    }

    class PUICallback {

        void onClose(final JDialog dialog) {

            close(dialog);
        }

        void remove(final PhysicsEntity pe) {

            removeEntity(pe);
        }
    }

    private class PhysicsEntityEventListener implements EntityListener {

        /**
         *
         */
        @Override
        public void onCollision(final CollisionEvent collEvt) {

            EntityCollision coll = collEvt.getCollisions()[0];
            Entity e0 = coll.getEntity();
            Entity e1 = coll.getCollidingEntity();
            GamePhysics phys0 = e0.getPhysics();
            GamePhysics phys1 = e1.getPhysics();
            PointUD e0loc = new PointUD(e0.getWorldX(), e0.getWorldY()), e1loc = new PointUD(e1.getWorldX(),
                            e1.getWorldY());
            e0.getCollisionModel().resolve(e0loc,
                                           e1loc,
                                           e1.getCollisionModel(),
                                           phys0.getVelocity2d(),
                                           phys1.getVelocity2d(),
                                           1,
                                           0.5);
            e0.setWorldLoc(e0loc.ux, e0loc.uy);
            e1.setWorldLoc(e1loc.ux, e1loc.uy);
            phys0.collideWith2d(phys1);
            // rc.setDisableUpdates(true);
        }

        /**
         *
         */
        @Override
        public void onAdd(final AddEvent addEvt) {

        }

        /**
         *
         */
        @Override
        public void onRemove(final RemoveEvent remEvt) {

        }
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {

        try {
            Properties props = new Properties();
            props.put("logoString", "");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        PhysicsUI pui = new PhysicsUI();
        pui.init();
    }
}
