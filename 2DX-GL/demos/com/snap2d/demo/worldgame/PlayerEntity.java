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

package com.snap2d.demo.worldgame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import bg.x2d.geo.Shape2D;
import bg.x2d.geo.UniformPolygon2D;

import com.snap2d.physics.GamePhysics;
import com.snap2d.world.CollisionModel;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 * 
 */
public class PlayerEntity extends WorldGameEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 9092508303423927199L;

    public static final int SIZE = 50;

    private static CollisionModel model;
    // triangle shape for player
    private static final Shape2D shape = new UniformPolygon2D(3, 0, 0, SIZE, Color.BLUE, true);

    int cwt, cht;

    /**
     * @param worldLoc
     * @param world
     */
    public PlayerEntity(final Point2D worldLoc, final World2D world) {

        super(worldLoc, world);
        initBounds(SIZE, SIZE);
        if (model == null) {
            Polygon p = shape.getShape();
            Point[] pts = new Point[p.npoints];
            for (int i = 0; i < p.npoints; i++ ) {
                pts[i] = new Point(p.xpoints[i], p.ypoints[i]);
            }
            model = new CollisionModel(pts, SIZE, SIZE, world);
        }
    }

    /**
     *
     */
    @Override
    public void render(final Graphics2D g, final float interpolation) {

        shape.draw(g);
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        // System.out.println(worldLoc);
    }

    /**
     *
     */
    @Override
    public void onResize(final Dimension oldSize, final Dimension newSize) {

        cwt = newSize.width;
        cht = newSize.height;
        shape.setLocation(cwt / 2 - SIZE / 2, cht / 2 - SIZE / 2);
    }

    /**
     *
     */
    @Override
    public void setAllowRender(final boolean render) {

        shouldRender = render;
    }

    /**
     *
     */
    @Override
    public GamePhysics getPhysics() {

        return null;
    }

    /**
     *
     */
    @Override
    public CollisionModel getCollisionModel() {

        return model;
    }

}
