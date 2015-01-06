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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import bg.x2d.geo.PointUD;
import bg.x2d.geo.Vector2d;
import bg.x2d.geo.Vector2f;

import com.snap2d.gl.spi.RenderableSpi;
import com.snap2d.physics.GamePhysics;
import com.snap2d.world.event.EntityCollision;

/**
 * Represents an object in the 2-dimensional world space. Entity provides a base
 * implementation for all objects that exist in the world.<br/>
 * <br/>
 * Entity implements the rendering interfaces for both the Java2D and OpenGL
 * renderers and overrides all their methods with blank implementations except
 * for <code>update</code>. All subclasses regardless of the target renderer
 * must implement this method. It is still highly recommended that subclasses
 * override the methods defined by the appropriate rendering interface to avoid
 * unexpected behavior due to lack of implementation.
 * 
 * @author Brian Groenke
 * 
 */
public abstract class Entity implements RenderableSpi, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1592236500933136785L;

    protected Entity self = this;

    protected Point screenLoc;
    protected PointUD worldLoc;
    protected Rectangle screenBounds;
    protected Rect2D worldBounds;
    protected World2D world;

    protected boolean shouldRender = true;

    /**
     * Creates this Entity at the given world location in the context of the
     * given World2D. The dimensions of the Entity are not required in this
     * constructor to allow subclasses to perform any necessary size
     * calculations in their respective constructors.<br/>
     * <br/>
     * {@link #initBounds(int, int)} should be called at the end of the
     * constructor to initialize the world/screen bounds. Otherwise, the
     * default-initialized bounds will be used, assuming a width/height of 1
     * world unit.
     * 
     * @param worldLoc
     *            the location of the Entity in world space.
     * @param world
     *            the World2D this Entity exists in; used for converting
     *            coordinates and determining world/screen position.
     */
    protected Entity(final Point2D worldLoc, final World2D world) {

        this.worldLoc = new PointUD(worldLoc.getX(), worldLoc.getY());
        this.screenLoc = new Point(world.worldToScreen(worldLoc.getX(), worldLoc.getY()));
        this.world = world;

        this.worldBounds = new Rect2D(this.worldLoc, 1.0, 1.0);
        this.screenBounds = world.convertWorldRect(worldBounds);
    }

    /**
     * Should be called as soon possible (usually in the constructor) by the
     * Entity implementation to initialize screen and world bounds. This method
     * creates the screen bounds rectangle with the current screen location and
     * given dimensions, then creates the world bounds by converting its
     * dimensions to world dimensions.
     * 
     * @param swidth
     *            width of the Entity on screen
     * @param sheight
     *            height of the Entity on screen
     */
    protected void initBounds(final int screenWt, final int screenHt) {

        this.screenBounds = new Rectangle(screenLoc.x, screenLoc.y, screenWt, screenHt);
        Rect2D srtow = world.convertScreenRect(screenBounds);
        worldBounds = new Rect2D(worldLoc.ux, worldLoc.uy, srtow.getWidth(), srtow.getHeight());
    }

    /**
     * Initialize the Entity's bounds using the world dimensions instead of
     * screen dimensions.
     * 
     * @see #initBounds(int, int)
     * @param worldWt
     * @param worldHt
     */
    protected void initBounds(final double worldWt, final double worldHt) {

        this.worldBounds = new Rect2D(worldLoc.ux, worldLoc.uy, worldWt, worldHt);
        this.screenBounds = world.convertWorldRect(worldBounds);
    }

    protected double interpolate(final double n, final double lastN, final float interpolation) {

        return ( (n - lastN) * interpolation + lastN);
    }

    public double getWorldX() {

        return worldLoc.ux;
    }

    public double getWorldY() {

        return worldLoc.uy;
    }

    public int getScreenX() {

        return screenLoc.x;
    }

    public int getScreenY() {

        return screenLoc.y;
    }

    /**
     * Sets the location of this Entity on the screen coordinate system.
     * Supertype implementation simply resets the world/screen points and
     * bounds. Overriding subclasses can call through to super for convenience.
     * 
     * @param newLoc
     */
    public void setScreenLoc(final int nx, final int ny) {

        screenLoc.setLocation(nx, ny);
        screenBounds.setLocation(screenLoc);
        worldBounds = world.convertScreenRect(screenBounds);
        worldLoc.setLocation(worldBounds.getX(), worldBounds.getY());
    }

    /**
     * Sets the location of this Entity on the world coordinate system.
     * Supertype implementation simply resets the world/screen points and
     * bounds. Overriding subclasses can call through to super for convenience.
     * 
     * @param newLoc
     */
    public void setWorldLoc(final double nx, final double ny) {

        worldLoc.setLocation(nx, ny);
        worldBounds.setRect(nx, ny, worldBounds.getWidth(), worldBounds.getHeight());
        screenBounds = world.convertWorldRect(worldBounds);
        screenLoc.setLocation(screenBounds.x, screenBounds.y);
    }

    public void applyVector(final Vector2f vec, final float mult) {

        PointUD np = new PointUD(vec.applyTo(worldLoc.getFloatPoint(), mult));
        setWorldLoc(np.getX(), np.getY());
    }

    public void applyVector(final Vector2d vec, final double mult) {

        PointUD np = new PointUD(vec.applyTo(worldLoc.getDoublePoint(), mult));
        setWorldLoc(np.getX(), np.getY());
    }

    /**
     * @return a Rect2D representing the Entity's bounds in world space.
     */
    public Rect2D getWorldBounds() {

        return worldBounds;
    }

    /**
     * This method subtracts height from the Y value to compensate for the
     * inverted Y-axis. Bounds returned from this method will function correctly
     * with the built-in Java geometry system.
     * 
     * @return
     */
    public Rectangle2D getCompatibleBounds() {

        return new Rectangle2D.Double(worldBounds.getX(), worldBounds.getY() - worldBounds.getHeight(),
                worldBounds.getWidth(), worldBounds.getHeight());
    }

    public Rectangle getScreenBounds() {

        return screenBounds;
    }

    public boolean isRendering() {

        return shouldRender;
    }

    /**
     * Checks to see if this Entity collides with the given Entity. This method
     * uses a low precision bounds-check followed by a high-precision
     * CollisionModel check if the bounds intersect.
     * 
     * @param e
     * @return true if the Entities are in collision, false otherwise.
     */
    public boolean collidesWith(final Entity e) {

        Rect2D coll = world.checkCollision(this.worldBounds, e.worldBounds);
        if (coll == null) {
            return false;
        }
        CollisionModel cmodel = getCollisionModel();
        return cmodel.collidesWith(0, 0, 0, 0, e.getCollisionModel());
    }

    /**
     * This method uses the same process as <code>collidesWith(Entity)</code>
     * but returns an EntityCollision object that contains the collided Entity
     * and low precision collision box.
     * 
     * @param e
     * @return the computed intersection box between the two Entities in world
     *         space or null if no collision
     */
    public EntityCollision getCollision(final Entity e) {

        Rect2D coll = world.checkCollision(this.worldBounds, e.worldBounds);
        if (coll == null) {
            return null;
        }
        CollisionModel cmodel = getCollisionModel();
        if (cmodel.collidesWith(getWorldX(), getWorldY(), e.getWorldX(), e.getWorldY(), e.getCollisionModel())) {
            return new EntityCollisionImpl(e, coll);
        } else {
            return null;
        }
    }

    /**
     * Convenience method provided for drawing the BufferedImage of an Entity in
     * world space. The coordinates are assumed to have been converted from
     * world space, so the image height is subtracted from the Y value to
     * compensate for the change in position representation (World2D Cartesian
     * system recognizes point locations as bottom left - Java2D uses top left).
     * 
     * @param g
     *            graphics object to render on
     * @param sx
     *            x coordinate of the Entity in screen space
     * @param sy
     *            y coordinate of the Entity in screen space (without height
     *            subtracted)
     * @param img
     *            the sprite image to draw
     */
    public static void renderWorldSprite(final Graphics2D g, final int sx, final int sy, final BufferedImage img) {

        g.drawImage(img, sx, sy - img.getHeight(), null);
    }

    /**
     * Set whether or not the Entity should be rendered on screen. The default
     * implementation will set the <code>shouldRender</code> variable of Entity.
     * 
     * @param render
     */
    public void setAllowRender(final boolean render) {

        shouldRender = render;
    }

    /**
     * Obtain the GamePhysics node of the Entity, if one exists.
     * 
     * @return
     */
    public abstract GamePhysics getPhysics();

    /**
     * Obtain the collision model of the Entity. This should always return a
     * valid CollisionModel for all Entity implementations. Null values will
     * cause errors in collision and bounds checking.
     * 
     * @return the Entity's CollisionModel
     */
    public abstract CollisionModel getCollisionModel();

    /*
     * @Override public void render(Graphics2D g, float interpolation) {}
     * 
     * @Override public void onResize(Dimension newSize, Dimension oldSize) {}
     * 
     * @Override public void init(GLHandle handle) {}
     * 
     * @Override public void render(GLHandle handle, float interpolation) {}
     * 
     * @Override public void resize(GLHandle handle, int wt, int ht) {}
     * 
     * @Override public void dispose(GLHandle handle) {}
     */

    public class EntityCollisionImpl implements EntityCollision {

        Entity e;
        Rect2D collisionBox;

        EntityCollisionImpl(final Entity e, final Rect2D collisionBox) {

            this.e = e;
            this.collisionBox = collisionBox;
        }

        @Override
        public Entity getEntity() {

            return self;
        }

        @Override
        public Entity getCollidingEntity() {

            return e;
        }

        @Override
        public Rect2D getCollisionBounds() {

            return collisionBox;
        }
    }
}
