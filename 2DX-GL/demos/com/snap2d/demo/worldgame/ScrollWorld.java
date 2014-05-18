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

import java.awt.*;
import java.util.HashSet;

import com.snap2d.gl.Renderable;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 * 
 */
public class ScrollWorld extends World2D implements GameWorld, Renderable {

	HashSet<Entity> entities = new HashSet<Entity>();
	EntityManager manager = new EntityManager();

	/**
	 * @param minX
	 * @param maxY
	 * @param viewWidth
	 * @param viewHeight
	 * @param ppu
	 */
	public ScrollWorld(double minX, double maxY, int viewWidth, int viewHeight,
			float ppu) {
		super(minX, maxY, viewWidth, viewHeight, ppu);
	}

	/**
	 * 
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		manager.render(g, interpolation);
	}

	/**
	 *
	 */
	@Override
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
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
	public void onResize(Dimension oldSize, Dimension newSize) {
		for (Entity e : entities) {
			e.onResize(oldSize, newSize);
		}
	}

	/**
	 *
	 */
	@Override
	public boolean addEntity(Entity e) {
		return entities.add(e);
	}

	/**
	 *
	 */
	@Override
	public boolean removeEntity(Entity e) {
		return entities.remove(e);
	}

	/**
	 *
	 */
	@Override
	public boolean hasEntity(Entity e) {
		return entities.contains(e);
	}

	/**
	 *
	 */
	@Override
	public boolean isInView(Entity e) {
		return manager.contains(e);
	}

	/**
	 *
	 */
	@Override
	public void setViewport(double x, double y, int width, int height) {
		super.setViewSize(width, height, getPixelsPerUnit());
		super.setLocation(x, y);
	}

	/**
	 *
	 */
	@Override
	public void moveViewport(double dx, double dy) {
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
	public Entity entityAt(double x, double y) {
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
