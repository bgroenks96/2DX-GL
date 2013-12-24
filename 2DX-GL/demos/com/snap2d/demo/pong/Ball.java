/*
 *  Copyright © 2012-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.demo.pong;

import java.awt.*;
import java.awt.geom.*;

import bg.x2d.geo.*;
import bg.x2d.physics.*;

import com.snap2d.physics.*;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 * 
 */
public class Ball extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -865736191842447441L;

	public static final int BALL_SIZE = 20;
	public static final Color BALL_COLOR = Color.WHITE;

	private static final int INIT_VEL = 25;

	double lx, ly;
	BallPhysics phys;
	CollisionModel coll;
	ExitBoundsListener listener;

	/**
	 * @param worldLoc
	 * @param world
	 */
	public Ball(Point2D worldLoc, World2D world, ExitBoundsListener listener) {
		super(worldLoc, world);
		initBounds(BALL_SIZE, BALL_SIZE);
		this.listener = listener;
		phys = new BallPhysics(new Vector2f(INIT_VEL, INIT_VEL / 3));
		coll = new CollisionModel(CollisionModel.createCircleBounds(BALL_SIZE, Math.PI / 2), BALL_SIZE, BALL_SIZE, world);
	}

	/**
	 *
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		if (!shouldRender) {
			return;
		}
		double wx = worldLoc.dx;
		double wy = worldLoc.dy;
		int x, y;
		wy = interpolate(wy, ly, interpolation); // interpolate with the last position
		wx = interpolate(wx, lx, interpolation);
		Point p = world.worldToScreen(wx, wy, worldBounds.getHeight());
		x = p.x;
		y = p.y;
		g.setColor(BALL_COLOR);
		g.fillOval(x, y, BALL_SIZE, BALL_SIZE);
	}

	/**
	 *
	 */
	@Override
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
		lx = getWorldX();
		ly = getWorldY();
		applyVector(phys.getVelocity2f(), 1);
		if (!world.viewContains(worldBounds)) {
			if (worldLoc.dx + worldBounds.getWidth() < world.getX()
					|| worldLoc.dx > world.getWorldWidth() + world.getX()) {
				listener.outOfBounds(this);
			} else if (worldLoc.dy >= world.getY()
					|| worldLoc.dy - worldBounds.getHeight() <= world.getY()
							- world.getWorldHeight()) {
				phys.collide(1, 0, PhysicsNode.Collision.X);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void onResize(Dimension oldSize, Dimension newSize) {

	}

	/**
	 *
	 */
	@Override
	public void setAllowRender(boolean render) {
		this.shouldRender = render;
	}

	/**
	 *
	 */
	@Override
	public GamePhysics getPhysics() {
		return phys;
	}

	/**
	 *
	 */
	@Override
	public CollisionModel getCollisionModel() {
		return coll;
	}

	public static interface ExitBoundsListener {
		public void outOfBounds(Ball b);
	}

	/**
	 * Simple physics implementation for the Pong ball based primarily on the 2DX-GL physics
	 * library. Note that this implementation does NOT behave like the actual Pong game because
	 * standard physics collisions are used.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private class BallPhysics extends StandardPhysics implements GamePhysics {

		/**
		 * @param vec
		 * @param objMass
		 */
		public BallPhysics(Vector2f vec) {
			super(vec, 1.0);
		}

		/**
		 *
		 */
		@Override
		public Vector2f getVelocity2f() {
			return vecf;
		}

		/**
		 *
		 */
		@Override
		public Vector2d getVelocity2d() {
			return vecd;
		}

		/**
		 *
		 */
		@Override
		public double getVelocity() {
			return (vecd != null) ? vecd.getMagnitude() : vecf.getMagnitude();
		}

		/**
		 *
		 */
		@Override
		public void setVelocity(Vector2f vec) {
			vecd = null;
			vecf = vec;
		}

		/**
		 *
		 */
		@Override
		public void setVelocity(Vector2d vec) {
			vecf = null;
			vecd = vec;
		}

		/**
		 * Not needed
		 */
		@Override
		public void setMass(double kg) {

		}

		/**
		 *
		 */
		@Override
		public Vector2f applyForces(float time, Force... f) {
			return getVelocity2f();
		}

		/**
		 * Not needed
		 */
		@Override
		public Vector2d applyForces(double time, Force... f) {
			return getVelocity2d();
		}

		/**
		 * Not needed
		 */
		@Override
		public Vector2f collideWith2f(GamePhysics node) {
			return getVelocity2f();
		}

		/**
		 * Not needed
		 */
		@Override
		public Vector2d collideWith2d(GamePhysics node) {
			return getVelocity2d();
		}

	}

}
