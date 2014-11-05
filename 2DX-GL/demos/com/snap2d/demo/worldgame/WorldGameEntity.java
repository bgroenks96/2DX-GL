/*
 *  Copyright (C) 2011-2013 Brian Groenke
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

import java.awt.geom.Point2D;

import com.snap2d.gl.Renderable;
import com.snap2d.world.*;

/**
 * @author brian
 *
 */
public abstract class WorldGameEntity extends Entity implements Renderable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2903152253423920417L;

	/**
	 * @param worldLoc
	 * @param world
	 */
	protected WorldGameEntity(Point2D worldLoc, World2D world) {
		super(worldLoc, world);
	}

}
