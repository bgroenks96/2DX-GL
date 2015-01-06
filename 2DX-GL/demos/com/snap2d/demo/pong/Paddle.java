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

package com.snap2d.demo.pong;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import bg.x2d.geo.Vector2f;

import com.snap2d.gl.Renderable;
import com.snap2d.input.InputDispatch;
import com.snap2d.input.KeyEventClient;
import com.snap2d.input.MouseEventClient;
import com.snap2d.physics.GamePhysics;
import com.snap2d.world.CollisionModel;
import com.snap2d.world.Entity;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 * 
 */
public class Paddle extends Entity implements Renderable, MouseEventClient, KeyEventClient {

    /**
     * 
     */
    private static final long serialVersionUID = -6604379509452528830L;

    public static final Dimension PADDLE_SIZE = new Dimension(20, 150);
    public static final Color PADDLE_COLOR = Color.WHITE;

    private static final Vector2f MOVE_VECTOR = new Vector2f(0, 30);
    private static CollisionModel coll;

    double lx, ly;
    boolean up, down, useMouse;

    /**
     * @param worldBounds
     * @param world
     */
    public Paddle(final Point2D worldLoc, final World2D world, final InputDispatch input, final boolean useMouse) {

        super(worldLoc, world);
        initBounds(PADDLE_SIZE.width, PADDLE_SIZE.height);

        if (useMouse) {
            input.registerMouseClient(this);
        } else {
            input.registerKeyClient(this);
        }
        this.useMouse = useMouse;

        if (coll == null) {
            Point[] ptarr = new Point[] { new Point(0, 0), new Point(0, screenBounds.height),
                    new Point(screenBounds.width, screenBounds.height), new Point(screenBounds.width, 0) };
            coll = new CollisionModel(ptarr, PADDLE_SIZE.width, PADDLE_SIZE.height, world);
        }
    }

    /**
     *
     */
    @Override
    public void render(final Graphics2D g, final float interpolation) {

        // alternatively, we could use getScreenX() and getScreenY().
        // we can avoid any method call overhead however by simply accessing the
        // protected
        // fields directly.
        double wx = worldLoc.ux;
        double wy = worldLoc.uy;
        int x, y;
        if (useMouse) {
            x = screenLoc.x;
            y = screenLoc.y;
        } else {
            wy = interpolate(wy, ly, interpolation); // interpolate with the
            // last position
            Point p = world.worldToScreen(wx, wy, worldBounds.getHeight());
            x = p.x;
            y = p.y;
        }
        g.setColor(PADDLE_COLOR);
        g.fillRect(x, y, PADDLE_SIZE.width, PADDLE_SIZE.height);
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        lx = worldLoc.getX();
        ly = worldLoc.getY();
        if (up && !down) {

            // this is just a fancy way using Vectors to change the
            // paddle's position. It's essentially equivalent to: x + moveAmount
            MOVE_VECTOR.y = Math.abs(MOVE_VECTOR.y);
            applyVector(MOVE_VECTOR, 1);
        }

        if (down && !up) {
            MOVE_VECTOR.y = -Math.abs(MOVE_VECTOR.y);
            applyVector(MOVE_VECTOR, 1);
        }

        if (worldLoc.getY() < world.getY() - world.getWorldHeight()) {
            setWorldLoc(getWorldX(), world.getY() - world.getWorldHeight());
        } else if (worldLoc.getY() + worldBounds.getHeight() > world.getY()) {
            setWorldLoc(getWorldX(), world.getY() - worldBounds.getHeight());
        }
    }

    /**
     *
     */
    @Override
    public void onResize(final Dimension oldSize, final Dimension newSize) {

        if (useMouse) {
            setScreenLoc(newSize.width - PADDLE_SIZE.width, getScreenY());
        } else {
            setScreenLoc(0, 0);
        }
        lx = getWorldX();
        ly = getWorldY();
    }

    /**
     *
     */
    @Override
    public void setAllowRender(final boolean render) {

    }

    /**
     * 
     */
    @Override
    public GamePhysics getPhysics() {

        // Paddle doesn't require any game physics
        return null;
    }

    /**
     *
     */
    @Override
    public CollisionModel getCollisionModel() {

        return coll;
    }

    /**
     *
     */
    @Override
    public void processKeyEvent(final KeyEvent e) {

        switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                up = true;
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                up = false;
            }
            break;
        case KeyEvent.VK_S:
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                down = true;
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                down = false;
            }
            break;
        }
    }

    /**
     *
     */
    @Override
    public void processMouseEvent(final MouseEvent me) {

        if (me.getID() == MouseEvent.MOUSE_MOVED) {
            // in this case, we want to set by the screen location because we
            // are doing it in relation to the mouse
            if (me.getY() > PADDLE_SIZE.height / 2 && me.getY() < world.getViewHeight() - PADDLE_SIZE.height / 2) {
                setScreenLoc(getScreenX(), me.getY() - PADDLE_SIZE.height / 2);
            }
        }
    }

}
