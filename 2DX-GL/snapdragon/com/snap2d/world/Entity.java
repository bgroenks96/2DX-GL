/*
 *  Copyright © 2011-2012 Brian Groenke
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
import java.awt.geom.*;

import bg.x2d.geo.*;

import com.snap2d.gl.*;
import com.snap2d.physics.*;

/**
 * Represents an object in the 2-dimensional world space.  Entity provides a base implementation
 * for all objects that exist in the world.
 * @author Brian Groenke
 *
 */
public abstract class Entity implements Renderable {

	protected Point screenLoc;
	protected PointLD worldLoc;
	protected Rectangle screenBounds;
	protected Rectangle2D worldBounds;
	protected World2D world;

	protected boolean shouldRender;

	/**
	 * Creates this Entity at the given world location in the context of the given World2D.
	 * The screen bounds are calculated using the World2D, converting the world location to a screen
	 * location and setting the size based on pixels-per-unit.
	 * @param worldBounds the bounding box for the entity in world space
	 * @param world the World2D this Entity exists in; used for converting coordinates and determining
	 *     world/screen position.
	 */
	public Entity(Rectangle2D worldBounds, World2D world) {
		this.worldLoc = new PointLD(worldBounds.getX(), worldBounds.getY());
		this.worldBounds = (Rectangle2D.Double) worldBounds.clone();
		this.screenBounds = world.convertWorldRect(worldBounds);
		this.screenLoc = screenBounds.getLocation();
		this.world = world;
	}
	
	public double getWorldX() {
		return worldLoc.dx;
	}
	
	public double getWorldY() {
		return worldLoc.dy;
	}
	
	public int getScreenX() {
		return screenLoc.x;
	}
	
	public int getScreenY() {
		return screenLoc.y;
	}
	
	/**
	 * Sets the location of this Entity on the screen coordinate system.
	 * Supertype implementation simply resets the world/screen points and bounds.
	 * Overriding subclasses can call through to super for convenience.
	 * @param newLoc
	 */
	public void setScreenLoc(int nx, int ny) {
		screenLoc.setLocation(nx, ny);
		worldLoc.setLocation(world.screenToWorld(nx, ny));
		screenBounds.setLocation(screenLoc);
		worldBounds.setRect(worldLoc.dx, worldLoc.dy, worldBounds.getWidth(), worldBounds.getHeight());
	}
	
	/**
	 * Sets the location of this Entity on the world coordinate system.
	 * Supertype implementation simply resets the world/screen points and bounds.
	 * Overriding subclasses can call through to super for convenience.
	 * @param newLoc
	 */
	public void setWorldLoc(double nx, double ny) {
		worldLoc.setLocation(nx, ny);
		screenLoc.setLocation(world.worldToScreen(nx, ny));
		screenBounds.setLocation(screenLoc);
		worldBounds.setRect(worldLoc.dx, worldLoc.dy, worldBounds.getWidth(), worldBounds.getHeight());
	}

	public Rectangle2D getWorldBounds() {
		return worldBounds;
	}

	public Rectangle getScreenBounds() {
		return screenBounds;
	}

	public boolean isRendering() {
		return shouldRender;
	}
	
	public boolean collidesWith(Entity e) {
		Rectangle2D coll = world.checkCollision(this.worldBounds, e.worldBounds);
		if(coll == null)
			return false;
		CollisionModel cmodel = getCollisionModel();
		Rectangle collRect = world.convertWorldRect(coll);
		int x1 = collRect.x - screenLoc.x;
		int y1 = collRect.y - screenLoc.y;
		int x2 = collRect.x - e.screenLoc.x;
		int y2 = collRect.y - e.screenLoc.y;
		return cmodel.collidesWith(collRect, new Rectangle(x1, y1, screenBounds.width, screenBounds.height), 
				new Rectangle(x2, y2, e.screenBounds.width, e.screenBounds.height), cmodel);
	}
	
	/**
	 * Set whether or not the Entity should be rendered on screen.  The behavior of this
	 * method is entirely up to the implementation.
	 * @param render
	 */
	public abstract void setAllowRender(boolean render);
	
	/**
	 * Obtain the GamePhysics node of the Entity, if one exists.
	 * @return
	 */
	public abstract GamePhysics getPhysics();
	
	/**
	 * Obtain the collision model of the Entity.  This should always return a valid CollisionModel
	 * for all Entity implementations.  Null values will cause errors in collision and bounds checking.
	 * @return the Entity's CollisionModel
	 */
	public abstract CollisionModel getCollisionModel();
}
