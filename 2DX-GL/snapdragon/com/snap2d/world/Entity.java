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
import java.awt.geom.*;
import java.awt.image.*;
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
public abstract class Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1592236500933136785L;

	protected Point screenLoc;
	protected PointLD worldLoc;
	protected Rectangle screenBounds;
	protected Rect2D worldBounds;
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
		screenBounds.setLocation(screenLoc);
		worldBounds = world.convertScreenRect(screenBounds);
		worldLoc.setLocation(worldBounds.getX(), worldBounds.getY());
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
		worldBounds.setRect(nx, ny, worldBounds.getWidth(), worldBounds.getHeight());
		screenBounds = world.convertWorldRect(worldBounds);
		screenLoc.setLocation(screenBounds.x, screenBounds.y);
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
	 * @return a Rect2D representing the Entity's bounds in world space.
	 */
	public Rect2D getWorldBounds() {
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
		Rect2D coll = world
				.checkCollision(this.worldBounds, e.worldBounds);
		if (coll == null) {
			return false;
		}
		CollisionModel cmodel = getCollisionModel();
		return cmodel.collidesWith(0, 0, 0, 0, e.getCollisionModel());
	}

	/**
	 * This method uses the same process as <code>collidesWith(Entity)</code> but returns an EntityCollision
	 * object that contains the collided Entity and low precision collision box.
	 * @param e
	 * @return the computed intersection box between the two Entities in world space or null if no
	 *         collision
	 */
	public EntityCollision getCollision(Entity e) {
		Rect2D coll = world
				.checkCollision(this.worldBounds, e.worldBounds);
		if (coll == null) {
			return null;
		}
		CollisionModel cmodel = getCollisionModel();
		if(cmodel.collidesWith(getWorldX(), getWorldY(), e.getWorldX(), e.getWorldY(), e.getCollisionModel())) {
			return new EntityCollision(e, coll);
		} else {
			return null;
		}
	}
	
	/**
	 * Convenience method provided for drawing the BufferedImage of an Entity in world space.
	 * The coordinates are assumed to have been converted from world space, so the image height
	 * is subtracted from the Y value to compensate for the change in position representation
	 * (World2D Cartesian system recognizes point locations as bottom left - Java2D uses top left).
	 * @param g graphics object to render on
	 * @param sx x coordinate of the Entity in screen space
	 * @param sy y coordinate of the Entity in screen space (without height subtracted)
	 * @param img the sprite image to draw
	 */
	public static void renderWorldSprite(Graphics2D g, int sx, int sy, BufferedImage img) {
		g.drawImage(img, sx, sy - img.getHeight(), null);
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
		Rect2D collisionBox;

		EntityCollision(Entity e, Rect2D collisionBox) {
			this.e = e;
			this.collisionBox = collisionBox;
		}
	}
	
	public static abstract class DrawableEntity extends Entity implements Renderable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param worldLoc
		 * @param world
		 */
		public DrawableEntity(Point2D worldLoc, World2D world) {
			super(worldLoc, world);
		}
	}
	
	public static abstract class GLDrawableEntity extends Entity implements Renderable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param worldLoc
		 * @param world
		 */
		public GLDrawableEntity(Point2D worldLoc, World2D world) {
			super(worldLoc, world);
		}
		
	}
}
