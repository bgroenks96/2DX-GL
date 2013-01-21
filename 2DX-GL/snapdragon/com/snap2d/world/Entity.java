/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
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
		this.screenLoc = world.worldToScreen(worldLoc.dx, worldLoc.dy);
		this.screenBounds = new Rectangle(screenLoc.x, screenLoc.y, 
				(int) Math.round(worldBounds.getWidth() * world.getPixelsPerUnit()),
				(int) Math.round(worldBounds.getHeight() * world.getPixelsPerUnit()));
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
		return cmodel.collidesWith(coll, world, this, e);
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
