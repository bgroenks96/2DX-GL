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

package com.snap2d.world.event;

import com.snap2d.world.*;
import com.snap2d.world.Entity.EntityCollision;

public class CollisionEvent extends EntityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2631149230119417861L;

	private EntityCollision[] colls;

	/**
	 * 
	 * @param source
	 */
	public CollisionEvent(Entity source, EntityCollision... colls) {
		super(source);
		this.colls = colls;
	}

	public EntityCollision[] getCollisions() {
		return colls;
	}
}
