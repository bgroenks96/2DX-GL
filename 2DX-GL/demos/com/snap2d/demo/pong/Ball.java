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
import java.awt.geom.Point2D;

import bg.x2d.geo.Vector2d;
import bg.x2d.geo.Vector2f;
import bg.x2d.physics.Force;
import bg.x2d.physics.PhysicsNode;
import bg.x2d.physics.StandardPhysics;

import com.snap2d.gl.Renderable;
import com.snap2d.physics.GamePhysics;
import com.snap2d.world.CollisionModel;
import com.snap2d.world.Entity;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 * 
 */
public class Ball extends Entity implements Renderable {

    /**
     * 
     */
    private static final long serialVersionUID = -865736191842447441L;

    public static final int BALL_SIZE = 20;
    public static final Color BALL_COLOR = Color.WHITE;

    private static final int INIT_VEL = 25;

    double lx, ly;
    BallPhysics phys;
    CollisionModel coll;
    ExitBoundsListener listener;

    /**
     * @param worldLoc
     * @param world
     */
    public Ball(final Point2D worldLoc, final World2D world, final ExitBoundsListener listener) {

        super(worldLoc, world);
        initBounds(BALL_SIZE, BALL_SIZE);
        this.listener = listener;
        phys = new BallPhysics(new Vector2f(INIT_VEL, INIT_VEL / 3));
        coll = new CollisionModel(CollisionModel.createCircleBounds(BALL_SIZE, Math.PI / 2), BALL_SIZE, BALL_SIZE,
                world);
    }

    /**
     *
     */
    @Override
    public void render(final Graphics2D g, final float interpolation) {

        if (!shouldRender) {
            return;
        }
        double wx = worldLoc.ux;
        double wy = worldLoc.uy;
        int x, y;
        wy = interpolate(wy, ly, interpolation); // interpolate with the last
        // position
        wx = interpolate(wx, lx, interpolation);
        Point p = world.worldToScreen(wx, wy, worldBounds.getHeight());
        x = p.x;
        y = p.y;
        g.setColor(BALL_COLOR);
        g.fillOval(x, y, BALL_SIZE, BALL_SIZE);
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        lx = getWorldX();
        ly = getWorldY();
        applyVector(phys.getVelocity2f(), 1);
        if (!world.viewContains(worldBounds)) {
            if (worldLoc.ux + worldBounds.getWidth() < world.getX()
                    || worldLoc.ux > world.getWorldWidth() + world.getX()) {
                listener.outOfBounds(this);
            } else if (worldLoc.uy >= world.getY()
                    || worldLoc.uy - worldBounds.getHeight() <= world.getY() - world.getWorldHeight()) {
                phys.collide(1, 0, PhysicsNode.Collision.X);
            }
        }
    }

    /**
     *
     */
    @Override
    public void onResize(final Dimension oldSize, final Dimension newSize) {

    }

    /**
     *
     */
    @Override
    public void setAllowRender(final boolean render) {

        this.shouldRender = render;
    }

    /**
     *
     */
    @Override
    public GamePhysics getPhysics() {

        return phys;
    }

    /**
     *
     */
    @Override
    public CollisionModel getCollisionModel() {

        return coll;
    }

    public static interface ExitBoundsListener {

        public void outOfBounds(Ball b);
    }

    /**
     * Simple physics implementation for the Pong ball based primarily on the
     * 2DX-GL physics library. Note that this implementation does NOT behave
     * like the actual Pong game because standard physics collisions are used.
     * 
     * @author Brian Groenke
     * 
     */
    private class BallPhysics extends StandardPhysics implements GamePhysics {

        /**
         * @param vec
         * @param objMass
         */
        public BallPhysics(final Vector2f vec) {

            super(vec, 1.0);
        }

        /**
         *
         */
        @Override
        public Vector2f getVelocity2f() {

            return velf;
        }

        /**
         *
         */
        @Override
        public Vector2d getVelocity2d() {

            return veld;
        }

        /**
         *
         */
        @Override
        public double getVelocity() {

            return (veld != null) ? veld.getMagnitude() : velf.getMagnitude();
        }

        /**
         *
         */
        @Override
        public void setVelocity(final Vector2f vec) {

            veld = null;
            velf = vec;
        }

        /**
         *
         */
        @Override
        public void setVelocity(final Vector2d vec) {

            velf = null;
            veld = vec;
        }

        /**
         * Not needed
         */
        @Override
        public void setMass(final double kg) {

        }

        /**
         *
         */
        @Override
        public Vector2f applyForces(final float time, final Force... f) {

            return getVelocity2f();
        }

        /**
         * Not needed
         */
        @Override
        public Vector2d applyForces(final double time, final Force... f) {

            return getVelocity2d();
        }

        /**
         * Not needed
         */
        @Override
        public Vector2f collideWith2f(final GamePhysics node) {

            return getVelocity2f();
        }

        /**
         * Not needed
         */
        @Override
        public Vector2d collideWith2d(final GamePhysics node) {

            return getVelocity2d();
        }

        /**
         *
         */
        @Override
        public void attachToPoint(final float x, final float y) {

        }

        /**
         *
         */
        @Override
        public void attachToPoint(final double x, final double y) {

        }

    }

}
