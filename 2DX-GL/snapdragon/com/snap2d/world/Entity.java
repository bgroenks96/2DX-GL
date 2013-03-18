/*
 *  Copyright © 2011-2013 Brian Groenke
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
import java.io.*;

import bg.x2d.geo.*;

import com.snap2d.gl.*;
import com.snap2d.physics.*;

/**
 * Represents an object in the 2-dimensional world space. Entity provides a base implementation for
 * all objects that exist in the world.
 * 
 * @author Brian Groenke
 * 
 */
public abstract class Entity implements Renderable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1592236500933136785L;

	protected Point screenLoc;
	protected PointLD worldLoc;
	protected Rectangle screenBounds;
	protected Rectangle2D worldBounds;
	protected World2D world;

	protected boolean shouldRender = true;

	/**
	 * Creates this Entity at the given world location in the context of the given World2D. The
	 * screen bounds are calculated using the World2D, converting the world location to a screen
	 * location and setting the size based on pixels-per-unit.
	 * 
	 * @param worldBounds
	 *            the bounding box for the entity in world space
	 * @param world
	 *            the World2D this Entity exists in; used for converting coordinates and determining
	 *            world/screen position.
	 */
	public Entity(Point2D worldLoc, World2D world) {
		this.worldLoc = new PointLD(worldLoc.getX(), worldLoc.getY());
		this.screenLoc = new Point(world.worldToScreen(worldLoc.getX(),
				worldLoc.getY()));
		this.world = world;
	}

	/**
	 * Should be called as soon possible (usually in the constructor) by the Entity implementation
	 * to initialize screen and world bounds. This method creates the screen bounds rectangle with
	 * the current screen location and given dimensions, then creates the world bounds by converting
	 * it to a world rectangle.
	 * 
	 * @param swidth
	 *            width of the Entity on screen
	 * @param sheight
	 *            height of the Entity on screen
	 */
	protected void initBounds(int swidth, int sheight) {
		this.screenBounds = new Rectangle(screenLoc.x, screenLoc.y, swidth,
				sheight);
		this.worldBounds = world.convertScreenRect(screenBounds);
	}

	protected double interpolate(double n, double lastN, float interpolation) {
		return ((n - lastN) * interpolation + lastN);
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
	 * Sets the location of this Entity on the screen coordinate system. Supertype implementation
	 * simply resets the world/screen points and bounds. Overriding subclasses can call through to
	 * super for convenience.
	 * 
	 * @param newLoc
	 */
	public void setScreenLoc(int nx, int ny) {
		screenLoc.setLocation(nx, ny);
		worldLoc.setLocation(world.screenToWorld(nx, ny));
		screenBounds.setLocation(screenLoc);
		worldBounds.setRect(worldLoc.dx, worldLoc.dy, worldBounds.getWidth(),
				worldBounds.getHeight());
	}

	/**
	 * Sets the location of this Entity on the world coordinate system. Supertype implementation
	 * simply resets the world/screen points and bounds. Overriding subclasses can call through to
	 * super for convenience.
	 * 
	 * @param newLoc
	 */
	public void setWorldLoc(double nx, double ny) {
		worldLoc.setLocation(nx, ny);
		screenLoc.setLocation(world.worldToScreen(nx, ny));
		screenBounds.setLocation(screenLoc);
		worldBounds.setRect(worldLoc.dx, worldLoc.dy, worldBounds.getWidth(),
				worldBounds.getHeight());
	}

	public void applyVector(Vector2f vec, float mult) {
		PointLD np = new PointLD(vec.applyTo(worldLoc.getFloatPoint(), mult));
		setWorldLoc(np.getX(), np.getY());
	}

	public void applyVector(Vector2d vec, double mult) {
		PointLD np = new PointLD(vec.applyTo(worldLoc.getDoublePoint(), mult));
		setWorldLoc(np.getX(), np.getY());
	}

	/**
	 * Returns a Rectangle2D representing the Entity's bounds in world space. Note that these bounds
	 * are created against a <b>Cartesian</b> coordinate system <b>NOT the screen</b> coordinate
	 * system, so Java2D geometry functions will not work.
	 * 
	 * @return
	 */
	public Rectangle2D getWorldBounds() {
		return worldBounds;
	}

	/**
	 * This method subtracts height from the Y value to compensate for the inverted Y-axis. Bounds
	 * returned from this method will function correctly with the built-in Java geometry system.
	 * 
	 * @return
	 */
	public Rectangle2D getCompatibleBounds() {
		return new Rectangle2D.Double(worldBounds.getX(), worldBounds.getY()
				- worldBounds.getHeight(), worldBounds.getWidth(),
				worldBounds.getHeight());
	}

	public Rectangle getScreenBounds() {
		return screenBounds;
	}

	public boolean isRendering() {
		return shouldRender;
	}

	/**
	 * Checks to see if this Entity collides with the given Entity. This method uses a low precision
	 * bounds-check followed by a high-precision CollisionModel check if the bounds intersect.
	 * 
	 * @param e
	 * @return true if the Entities are in collision, false otherwise.
	 */
	public boolean collidesWith(Entity e) {
		Rectangle2D coll = world
				.checkCollision(this.worldBounds, e.worldBounds);
		if (coll == null) {
			return false;
		}
		CollisionModel cmodel = getCollisionModel();
		Rectangle collRect = world.convertWorldRect(coll);
		return cmodel.collidesWith(collRect, screenBounds, e.screenBounds,
				e.getCollisionModel());
	}

	/**
	 * This method uses the same process as <code>collidesWith(Entity)</code> but returns the low
	 * precision intersection box if the two entity's CollisionModels are in collision.
	 * 
	 * @param e
	 * @return the computed intersection box between the two Entities in world space or null if no
	 *         collision
	 */
	public EntityCollision getCollision(Entity e) {
		Rectangle2D coll = world
				.checkCollision(this.worldBounds, e.worldBounds);
		if (coll == null) {
			return null;
		}
		CollisionModel cmodel = getCollisionModel();
		Rectangle collRect = world.convertWorldRect(coll);
		if (cmodel.collidesWith(collRect, screenBounds, e.screenBounds,
				e.getCollisionModel())) {
			return new EntityCollision(e, coll);
		} else {
			return null;
		}
	}

	/**
	 * Set whether or not the Entity should be rendered on screen. The behavior of this method is
	 * entirely up to the implementation.
	 * 
	 * @param render
	 */
	public abstract void setAllowRender(boolean render);

	/**
	 * Obtain the GamePhysics node of the Entity, if one exists.
	 * 
	 * @return
	 */
	public abstract GamePhysics getPhysics();

	/**
	 * Obtain the collision model of the Entity. This should always return a valid CollisionModel
	 * for all Entity implementations. Null values will cause errors in collision and bounds
	 * checking.
	 * 
	 * @return the Entity's CollisionModel
	 */
	public abstract CollisionModel getCollisionModel();

	public class EntityCollision {
		Entity e;
		Rectangle2D collisionBox;

		EntityCollision(Entity e, Rectangle2D collisionBox) {
			this.e = e;
			this.collisionBox = collisionBox;
		}
	}
}
