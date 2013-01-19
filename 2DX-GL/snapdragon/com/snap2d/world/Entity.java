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
 * 
 * Not recommended for implementation yet.  This class is a work in progress and subject to change.
 * @author Brian Groenke
 *
 */
public abstract class Entity implements Renderable {
	
	protected Point screenLoc;
	protected PointLD worldLoc;
	protected boolean shouldRender;
	protected Rectangle screenBounds;
	protected Rectangle2D worldBounds;
	protected GamePhysics phys;
	
	public Point getScreenLoc() {
		return screenLoc;
	}
	
	public PointLD getWorldLoc() {
		return worldLoc;
	}
	
	public Rectangle2D getWorldBounds() {
		return worldBounds;
	}
	
	public Rectangle getScreenBounds() {
		return screenBounds;
	}
	
	public GamePhysics getPhysics() {
		return phys;
	}
	
	public boolean isOnScreen() {
		return shouldRender;
	}
	
	public abstract void setAllowRender(boolean render);
}
