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
 * 
 * @author Brian Groenke
 *
 */
public class GeneralForce extends Force {

	/**
	 * 
	 * @param vec
	 */
	public GeneralForce(Vector2f vec) {
		setForceVector(vec);
	}
	
	/**
	 * 
	 * @param vec
	 */
	public GeneralForce(Vector2d vec) {
		setForceVector(vec);
	}

	@Override
	public void setForceVector(Vector2f vec) {
		if(vec != null)
			vecf = vec;
	}

	@Override
	public void setForceVector(Vector2d vec) {
		if(vec != null)
			vecd = vec;
	}

	@Override
	public Vector2f getVec2f() {
		return vecf;
	}

	@Override
	public Vector2d getVec2d() {
		return vecd;
	}

	@Override
	public double getNewtonForce(double mass) {
		return (vecf != null) ? vecf.mag:vecd.mag;
	}
	
	@Override
	/**
	 * Applies this force to the given Vector2f.  The resulting acceleration
	 * amount is determined by the equation a = F/m (such that a is acceleration, 
	 * F is Newton force, and m is mass in grams.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param vec velocity vector to be accelerated by force.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2f applyTo(float time, float mass, Vector2f vec) {
		Vector2f vecf = this.vecf.divNew(mass);
		vec.add(vecf.multNew(time));
		return vec;
	}

	@Override
	/**
	 * Applies this force to the given Vector2d.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param vec velocity vector to be accelerated by force.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2d applyTo(double time, double mass, Vector2d vec) {
		Vector2d vecd = this.vecd.divNew(mass);
		vec.add(vecd.multNew(time));
		return vec;
	}

}
