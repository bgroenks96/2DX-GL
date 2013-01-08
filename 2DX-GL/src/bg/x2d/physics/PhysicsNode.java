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

package bg.x2d.physics;

import bg.x2d.geo.*;

public interface PhysicsNode {
	
	public Vector2f getVelocity2f();
	public Vector2d getVelocity2d();
	public double getVelocity();
	public void setVelocity(Vector2f vec);
	public void setVelocity(Vector2d vec);
	public void setMass(double kg);
	public Vector2f collideWith2f(PhysicsNode coll);
	public Vector2d collideWith2d(PhysicsNode coll);
	public Vector2f applyForces(float time, Force... f);
	public Vector2d applyForces(double time, Force... f);
}
