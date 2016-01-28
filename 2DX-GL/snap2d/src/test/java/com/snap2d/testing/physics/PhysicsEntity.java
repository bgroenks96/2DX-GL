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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import bg.x2d.gen.ColorGenerator;
import bg.x2d.geo.PointUD;
import bg.x2d.geo.Vector2d;
import bg.x2d.physics.PhysicsNode.Collision;

import com.snap2d.gl.Renderable;
import com.snap2d.physics.GamePhysics;
import com.snap2d.world.CollisionModel;
import com.snap2d.world.Entity;
import com.snap2d.world.Rect2D;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 *
 */
public class PhysicsEntity extends Entity implements Renderable {

    /**
     * 
     */
    private static final long serialVersionUID = 210327291526337773L;

    private static volatile int ID = 0;

    private static final ColorGenerator colorgen = ColorGenerator.createRGB();
    private static final Font LABEL_FONT = new Font("Verdana", Font.BOLD, 14);

    Color color = colorgen.generate(),
                    antiColor = (color.getRed() > 128 || color.getGreen() > 128 || color.getBlue() > 128) ? Color.BLACK
                                    : Color.WHITE;

    TestNode node;
    PointUD[] wverts;

    CollisionModel model;

    private final int id = ID++ ;

    public double collFactor = 1.0, wt, ht;

    public PhysicsEntity(final double wx,
                         final double wy,
                         final double wt,
                         final double ht,
                         final PointUD[] vertices,
                         final TestNode physics,
                         final World2D world) {

        super(new PointUD(wx, wy), world);
        this.wt = wt;
        this.ht = ht;
        this.worldLoc = new PointUD(wx, wy);
        this.screenLoc = world.worldToScreen(wx, wy);
        this.node = physics;
        this.wverts = vertices;
        this.world = world;

        model = new CollisionModel(vertices, (int) wt, (int) ht, world);

        initBounds(wt, ht);
    }

    public int getID() {

        return id;
    }

    /**
     *
     */
    @Override
    public void render(final Graphics2D g, final float interpolation) {

        Polygon p = new Polygon();
        float avgx = 0, avgy = 0;
        for (PointUD pt : wverts) {
            Point sp = world.worldToScreen(pt.getX() + worldLoc.getX(), pt.getY() + worldLoc.getY());
            p.addPoint(sp.x, sp.y);
            avgx += sp.x;
            avgy += sp.y;
        }
        avgx = avgx / p.npoints;
        avgy = avgy / p.npoints;
        g.setPaint(color);
        g.fill(p);
        g.setColor(antiColor);
        g.setFont(LABEL_FONT);
        Rectangle2D strBounds = g.getFontMetrics().getStringBounds(String.valueOf(id), g);
        g.drawString(String.valueOf(id), avgx - (float) strBounds.getWidth() / 2, avgy + (float) strBounds.getHeight()
                        / 4);
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long lastUpdate) {

        boolean ground = worldLoc.uy == world.getY() && node.getVelocity2d().y == 0;
        if (ground && !node.hasNoGravity()) {
            node.applyForces(1 / 30.0, TestNode.ANTI_GRAV);
        } else {
            node.applyForces(1 / 30.0);
        }

        node.getVelocity2d().applyTo(worldLoc, 1);
        worldBounds.setLocation(worldLoc.ux, worldLoc.uy);
        if ( !world.viewContains(worldBounds)) {
            Rect2D viewBounds = world.getBounds();
            Rect2D cbounds = world.checkCollision(worldBounds, viewBounds);
            if (cbounds == null) {
                return;
            }
            if (cbounds.getHeight() < worldBounds.getHeight() && cbounds.getWidth() < worldBounds.getWidth()) {
                node.collide(collFactor, 0, Collision.XY);
            } else if (cbounds.getHeight() < worldBounds.getHeight()) {
                node.collide(collFactor, 0, Collision.X);
            } else if (cbounds.getWidth() < worldBounds.getWidth()) {
                node.collide(collFactor, 0, Collision.Y);
            }

            if (worldLoc.ux + worldBounds.getWidth() > world.getMaxX()) {
                worldLoc.setLocation(world.getMaxX() - worldBounds.getWidth(), worldLoc.uy);
            } else if (worldLoc.ux < world.getX()) {
                worldLoc.setLocation(world.getX(), worldLoc.uy);
            }
            if (worldLoc.uy + worldBounds.getHeight() > world.getMaxY()) {
                worldLoc.setLocation(worldLoc.ux, world.getMaxY() - worldBounds.getHeight());
            } else if (worldLoc.uy < world.getY()) {
                worldLoc.setLocation(worldLoc.ux, world.getY());
            }
        }
    }

    /**
     *
     */
    @Override
    public void onResize(final Dimension oldSize, final Dimension newSize) {

    }

    public void setSize(final Vector2d newSize) {

        wverts[0].setLocation(0, 0);
        wverts[1].setLocation(0, newSize.y);
        wverts[2].setLocation(newSize.x, newSize.y);
        wverts[3].setLocation(newSize.x, 0);
        wt = newSize.x;
        ht = newSize.y;
        this.worldBounds = new Rect2D(worldLoc.ux, worldLoc.uy, wt, ht);
    }

    public void move(final double dx, final double dy) {

        worldLoc.setLocation(worldLoc.ux + dx, worldLoc.uy + dy);
        worldBounds.setLocation(worldLoc.ux, worldLoc.uy);
    }

    public void setColor(final Color p) {

        this.color = p;
    }

    /**
     *
     */
    @Override
    public GamePhysics getPhysics() {

        return node;
    }

    /**
     *
     */
    @Override
    public CollisionModel getCollisionModel() {

        return model;
    }
}
