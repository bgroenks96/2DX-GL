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

/**
 * A PhysicsNode that represents standard (real) physical forces acting upon an object.
 * Gravity is built in and will always be called by <code>applyForces</code>.
 * @author Brian Groenke
 *
 */
public class StandardPhysics implements PhysicsNode {
	
	Gravity g = new Gravity();

	public StandardPhysics() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector2f getVelocity2f() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector2d getVelocity2d() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getVelocity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVelocity(Vector2f vec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVelocity(Vector2d vec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMass(double kg) {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector2f collideWith2f(PhysicsNode coll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector2d collideWith2d(PhysicsNode coll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector2f applyForces(float time, Force... f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector2d applyForces(double time, Force... f) {
		// TODO Auto-generated method stub
		return null;
	}

}
