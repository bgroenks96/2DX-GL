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

public abstract class Force {

	protected Vector2f vecf;
	protected Vector2d vecd;

	/**
	 * Sets the vector for this force.  A vector may hold different
	 * meanings depending on the force; i.e a force vector might be
	 * acceleration, energy, etc. or just straight force in Newtons.
	 * @param vec the new force direction and magnitude.
	 */
	public abstract void setForceVector(Vector2f vec);
	public abstract void setForceVector(Vector2d vec);
	public abstract Vector2f getVec2f();
	public abstract Vector2d getVec2d();
	
	/**
	 * Implementation dependent.  Standard implementation for a Newton force
	 * will simply return the Force's value regardless of the value of <code>mass</code>.
	 * Some forces like Gravity may compute this value using F = ma.
	 * @param mass mass of object force is being applied to.
	 * @return the amount of force exerted on the given object.
	 */
	public abstract double getNewtonForce(double mass);

	/**
	 * Applies this force to the given Vector2f.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param forceSum some Forces may require a sum of other forces also being applied at
	 *     at the current time interval.  This argument is optional, thus all implementations should
	 *     account for possible null values.
	 * @param vec velocity vector to be accelerated by force.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2f applyTo(float time, float mass, Vector2f forceSum, Vector2f vec) {
		vec.add(vecf.divNew(mass).mult(time));
		return vec;
	}

	/**
	 * Applies this force to the given Vector2d.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param vec velocity vector to be accelerated by force.
	 * @param forceSum some Forces may require a sum of other forces also being applied at
	 *     at the current time interval.  This argument is optional, thus all implementations should
	 *     account for possible null values.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2d applyTo(double time, double mass, Vector2d forceSum, Vector2d vec) {
		vec.add(vecd.divNew(mass).mult(time));
		return vec;
	}
}
