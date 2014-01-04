/*
 *  Copyright © 2012-2014 Brian Groenke
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

import java.awt.*;
import java.util.*;
import java.util.List;

import com.snap2d.gl.*;
import com.snap2d.world.Entity.DrawableEntity;
import com.snap2d.world.Entity.EntityCollision;
import com.snap2d.world.event.*;

/**
 * Provides a facility for managing registered Entity objects. Adding EntityManager as a Renderable
 * task will allow all render/update calls to be forwarded to Entities registered with
 * EntityManager. EntityManager also checks for collisions between all registered Entities on each
 * update and fires a CollisionEvent when Entity collisions are detected. EntityListeners can also
 * be used to receive events for when a new Entity is registered or removed. <br/>
 * <br/>
 * Note: EntityManager is NOT thread safe. Only one thread should be responsible for modifying its
 * data, or the Object must be synchronized externally.
 * 
 * @author Brian Groenke
 * 
 */
public class EntityManager implements Renderable {

	ArrayList<DrawableEntity> entities = new ArrayList<DrawableEntity>();
	HashMap<Entity, List<EntityListener>> listeners = new HashMap<Entity, List<EntityListener>>();

	/**
	 * @param e
	 * @return true if the Entity was not already registered.
	 */
	public boolean register(DrawableEntity e) {
		boolean added = entities.add(e);
		if (added) {
			fireAddEvent(e);
		}
		return added;
	}

	public void register(DrawableEntity e, EntityListener listener) {
		addEntityListener(listener, e);
		register(e);
	}

	public void unregister(DrawableEntity e) {
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

	public boolean contains(Entity e) {
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
	public void addEntityListener(EntityListener listener, Entity... entities) {
		for (Entity e : entities) {
			List<EntityListener> reg = listeners.get(e);
			if (reg != null) {
				if (!reg.contains(listener)) {
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

	public void removeEntityListener(EntityListener listener) {
		for (Entity e : listeners.keySet()) {
			if (listeners.get(e).contains(listener)) {
				listeners.get(e).remove(listener);
			}
		}
	}

	/**
	 * Dispatches the renderer's draw request to all registered Entity objects.
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		for (DrawableEntity e : entities) {
			e.render(g, interpolation);
		}
	}

	/*
	 * Cache lists used for collision checking. chkCache - Starts with every Entity object and
	 * removes the current iteration's object with each pass (avoids performing the same check
	 * multiple times). collCache - holds each collided Entity during each iteration to be used for
	 * firing the collision event.
	 */
	ArrayList<Entity> chkCache = new ArrayList<Entity>();
	ArrayList<EntityCollision> collCache = new ArrayList<EntityCollision>();

	/**
	 * Dispatches the renderer's update request to all registered Entity objects and checks for
	 * collisions.
	 */
	@Override
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
		chkCache.addAll(entities);
		for (DrawableEntity e : entities) {
			e.update(nanoTimeNow, nanosSinceLastUpdate);
			chkCache.remove(e);
			for (Entity opp : chkCache) {
				EntityCollision coll;
				if ((coll = e.getCollision(opp)) != null) {
					collCache.add(coll);
				}
			}
			if (collCache.size() > 0) {
				fireCollisionEvent(
						e,
						collCache.toArray(new EntityCollision[collCache.size()]));
				collCache.clear();
			}
		}
	}

	/**
	 * Dispatches the renderer's resize request to all registered Entity objects.
	 */
	@Override
	public void onResize(Dimension oldSize, Dimension newSize) {
		for (DrawableEntity e : entities) {
			e.onResize(oldSize, newSize);
		}
	}

	protected void fireCollisionEvent(Entity e, EntityCollision... colls) {
		List<EntityListener> queue = listeners.get(e);
		if (queue == null || queue.size() == 0) {
			return;
		}
		CollisionEvent evt = new CollisionEvent(e, colls);
		for (EntityListener el : queue) {
			el.onCollision(evt);
		}
	}

	protected void fireAddEvent(Entity e) {
		List<EntityListener> queue = listeners.get(e);
		if (queue == null || queue.size() == 0) {
			return;
		}
		AddEvent evt = new AddEvent(e);
		for (EntityListener el : queue) {
			el.onAdd(evt);
		}
	}

	protected void fireRemoveEvent(Entity e) {
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
