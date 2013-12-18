/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.physics;

import bg.x2d.geo.*;
import bg.x2d.physics.*;

/**
 * Extends the PhysicsNode interface to provide a framework for more complex physics calculations
 * suitable for game programming. <br/>
 * <br/>
 * <b>This interface is subject to change! The Snapdragon2D Physics API is still under
 * development.</b>
 * 
 * @author Brian Groenke
 * 
 */
public interface GamePhysics extends PhysicsNode {

	public Vector2f collideWith2f(GamePhysics node);

	public Vector2d collideWith2d(GamePhysics node);
}
