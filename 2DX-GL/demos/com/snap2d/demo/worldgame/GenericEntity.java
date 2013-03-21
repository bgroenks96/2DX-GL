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

package com.snap2d.demo.worldgame;

import java.awt.*;
import java.awt.geom.*;

import com.snap2d.physics.*;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 * 
 */
public class GenericEntity extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 949575200050379961L;

	private static final CollisionModel model;
	private static final Color COLOR = Color.GREEN;
	private static final int SIZE = 50;

	static {
		model = new CollisionModel(new Rectangle2D.Double(0, 0, SIZE, SIZE),
				COLOR, new AffineTransform(), true);
	}

	/**
	 * @param worldLoc
	 * @param world
	 */
	public GenericEntity(Point2D worldLoc, World2D world) {
		super(worldLoc, world);
		super.initBounds(SIZE, SIZE);
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
		Point p = world.worldToScreen(wx, wy);
		int x, y;
		x = p.x;
		y = p.y;
		g.setPaint(COLOR);
		g.fillRect(x, y, SIZE, SIZE);
	}

	/**
	 *
	 */
	@Override
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {

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
		shouldRender = render;
	}

	/**
	 *
	 */
	@Override
	public GamePhysics getPhysics() {
		return null;
	}

	/**
	 *
	 */
	@Override
	public CollisionModel getCollisionModel() {
		return model;
	}

}