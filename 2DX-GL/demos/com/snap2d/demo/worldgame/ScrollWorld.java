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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashSet;

import com.snap2d.gl.Renderable;
import com.snap2d.world.Entity;
import com.snap2d.world.EntityManager;
import com.snap2d.world.GameWorld;
import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 * 
 */
public class ScrollWorld extends World2D implements GameWorld, Renderable {

    HashSet <WorldGameEntity> entities = new HashSet <WorldGameEntity>();
    EntityManager manager = new EntityManager();

    /**
     * @param minX
     * @param maxY
     * @param viewWidth
     * @param viewHeight
     * @param ppu
     */
    public ScrollWorld(final double minX, final double maxY, final int viewWidth, final int viewHeight, final float ppu) {

        super(minX, maxY, viewWidth, viewHeight, ppu);
    }

    /**
     * 
     */
    @Override
    public void render(final Graphics2D g, final float interpolation) {

        for (WorldGameEntity e : entities) {
            e.render(g, interpolation);
        }
    }

    /**
     *
     */
    @Override
    public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        manager.update(nanoTimeNow, nanosSinceLastUpdate);
        for (Entity e : entities) {
            if (!viewIntersects(e.getWorldBounds())) {
                if (manager.contains(e)) {
                    manager.unregister(e);
                }
            } else if (!manager.contains(e)) {
                manager.register(e);
            }
        }
    }

    /**
     *
     */
    @Override
    public void onResize(final Dimension oldSize, final Dimension newSize) {

        for (WorldGameEntity e : entities) {
            e.onResize(oldSize, newSize);
        }
    }

    /**
     *
     */
    @Override
    public boolean addEntity(final Entity e) {

        return entities.add((WorldGameEntity) e);
    }

    /**
     *
     */
    @Override
    public boolean removeEntity(final Entity e) {

        return entities.remove(e);
    }

    /**
     *
     */
    @Override
    public boolean hasEntity(final Entity e) {

        return entities.contains(e);
    }

    /**
     *
     */
    @Override
    public boolean isInView(final Entity e) {

        return manager.contains(e);
    }

    /**
     *
     */
    @Override
    public void setViewport(final double x, final double y, final int width, final int height) {

        super.setViewSize(width, height, getPixelsPerUnit());
        super.setLocation(x, y);
    }

    /**
     *
     */
    @Override
    public void moveViewport(final double dx, final double dy) {

        setViewport(getX() + dx, getY() + dy, getViewWidth(), getViewHeight());
    }

    /**
     *
     */
    @Override
    public Entity[] getEntities() {

        return entities.toArray(new Entity[entities.size()]);
    }

    /**
     *
     */
    @Override
    public Entity entityAt(final double x, final double y) {

        for (Entity e : entities) {
            if (e.getCompatibleBounds().contains(x, y)) {
                return e;
            }
        }
        return null;
    }

    public EntityManager getManager() {

        return manager;
    }
}
