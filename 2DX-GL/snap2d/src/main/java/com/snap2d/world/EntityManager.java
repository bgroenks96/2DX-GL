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

package com.snap2d.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.snap2d.gl.spi.RenderableSpi;
import com.snap2d.world.event.AddEvent;
import com.snap2d.world.event.CollisionEvent;
import com.snap2d.world.event.EntityCollision;
import com.snap2d.world.event.RemoveEvent;

/**
 * Provides a facility for managing registered Entity objects. Adding
 * EntityManager as a Renderable or GLRenderable task to the renderer will
 * forward all render/update calls to Entities registered with EntityManager.
 * EntityManager also checks for collisions between all registered Entities on
 * each update and fires a CollisionEvent when Entity collisions are detected.
 * EntityListeners can also be used to receive events for when a new Entity is
 * registered or removed. <br/>
 * <br/>
 * Note: EntityManager is NOT thread safe. Only one thread should be responsible
 * for modifying its data, or the Object must be synchronized externally.
 * 
 * @author Brian Groenke
 * 
 */
public class EntityManager implements RenderableSpi {

    ArrayList<Entity> entities = new ArrayList<Entity>();
    HashMap<Entity, List<EntityListener>> listeners = new HashMap<Entity, List<EntityListener>>();

    /**
     * @param e
     * @return true if the Entity was not already registered.
     */
    public boolean register(final Entity e) {

        boolean added = entities.add(e);
        if (added) {
            fireAddEvent(e);
        }
        return added;
    }

    public void register(final Entity e, final EntityListener listener) {

        addEntityListener(listener, e);
        register(e);
    }

    public void unregister(final Entity e) {

        if (entities.remove(e)) {
            fireRemoveEvent(e);
        }
        listeners.remove(e);
    }

    public void unregisterAll() {

        for (Entity e : entities) {
            fireRemoveEvent(e);
        }
        entities.clear();
        listeners.clear();
    }

    public boolean contains(final Entity e) {

        return entities.contains(e);
    }

    public Entity[] getEntities() {

        return entities.toArray(new Entity[entities.size()]);
    }

    /**
     * 
     * @param listener
     *            the EntityListener to receive Entity events.
     * @param entities
     *            the entities this listener should receive events for.
     */
    public void addEntityListener(final EntityListener listener, final Entity... entities) {

        for (Entity e : entities) {
            List<EntityListener> reg = listeners.get(e);
            if (reg != null) {
                if ( !reg.contains(listener)) {
                    reg.add(listener);
                }
                listeners.put(e, reg);
            } else {
                List<EntityListener> els = new ArrayList<EntityListener>();
                els.add(listener);
                listeners.put(e, els);
            }
        }
    }

    public void removeEntityListenersFor(final Entity e) {

        listeners.remove(e);
    }

    public void removeEntityListener(final EntityListener listener) {

        for (Entity e : listeners.keySet()) {
            if (listeners.get(e).contains(listener)) {
                listeners.get(e).remove(listener);
            }
        }
    }

    /*
     * Cache lists used for collision checking. chkCache - Starts with every
     * Entity object and removes the current iteration's object with each pass
     * (avoids performing the same check multiple times). collCache - holds each
     * collided Entity during each iteration to be used for firing the collision
     * event.
     */
    ArrayList<Entity> chkCache = new ArrayList<Entity>();
    ArrayList<EntityCollision> collCache = new ArrayList<EntityCollision>();

    /**
     * Dispatches the renderer's update request to all registered Entity objects
     * and checks for collisions.
     */
    @Override
    public void update(final long nanoTimeNow, final long nanosSinceLastUpdate) {

        chkCache.addAll(entities);
        for (Entity e : entities) {
            e.update(nanoTimeNow, nanosSinceLastUpdate);
        }

        for (Entity e : entities) {
            chkCache.remove(e);
            for (Entity opp : chkCache) {
                EntityCollision coll;
                if ( (coll = e.getCollision(opp)) != null) {
                    collCache.add(coll);
                }
            }
            if (collCache.size() > 0) {
                fireCollisionEvent(e, collCache.toArray(new EntityCollision[collCache.size()]));
                collCache.clear();
            }
        }
    }

    protected void fireCollisionEvent(final Entity e, final EntityCollision... colls) {

        List<EntityListener> queue = listeners.get(e);
        if (queue == null || queue.size() == 0) {
            return;
        }
        CollisionEvent evt = new CollisionEvent(e, colls);
        for (EntityListener el : queue) {
            el.onCollision(evt);
        }
    }

    protected void fireAddEvent(final Entity e) {

        List<EntityListener> queue = listeners.get(e);
        if (queue == null || queue.size() == 0) {
            return;
        }
        AddEvent evt = new AddEvent(e);
        for (EntityListener el : queue) {
            el.onAdd(evt);
        }
    }

    protected void fireRemoveEvent(final Entity e) {

        List<EntityListener> queue = listeners.get(e);
        if (queue == null || queue.size() == 0) {
            return;
        }
        RemoveEvent evt = new RemoveEvent(e);
        for (EntityListener el : queue) {
            el.onRemove(evt);
        }
    }
}
