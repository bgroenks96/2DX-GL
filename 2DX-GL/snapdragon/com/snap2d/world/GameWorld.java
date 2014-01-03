/*
 *  Copyright � 2012-2014 Brian Groenke
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

import com.snap2d.world.Entity.DrawableEntity;

/**
 * @author Brian Groenke
 * 
 */
public interface GameWorld {

	public boolean addEntity(DrawableEntity e);

	public boolean removeEntity(DrawableEntity e);

	public boolean hasEntity(DrawableEntity e);

	public boolean isInView(DrawableEntity e);

	public DrawableEntity entityAt(double x, double y);

	public void setViewport(double x, double y, int width, int height);

	public void moveViewport(double dx, double dy);

	public DrawableEntity[] getEntities();
}
