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
public class PlayerEntity extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9092508303423927199L;

	/**
	 * @param worldLoc
	 * @param world
	 */
	public PlayerEntity(Point2D worldLoc, World2D world) {
		super(worldLoc, world);
	}

	/**
	 *
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		

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
		return null;
	}

}
