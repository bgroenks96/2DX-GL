/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
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
		if (vec != null) {
			vecd = null;
			vecf = vec;
		}
	}

	@Override
	public void setForceVector(Vector2d vec) {
		if (vec != null) {
			vecf = null;
			vecd = vec;
		}
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
		return (vecf != null) ? vecf.getMagnitude() : vecd.getMagnitude();
	}

	@Override
	/**
	 * Applies this force to the given Vector2f.  The resulting acceleration
	 * amount is determined by the equation a = F/m (such that a is acceleration, 
	 * F is Newton force, and m is mass in grams.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param forceSum unused in general force
	 * @param vec velocity vector to be accelerated by force.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2f applyTo(float time, float mass, Vector2f forceSum,
			Vector2f vec) {
		Vector2f vecf = this.vecf.divNew(mass);
		vec.add(vecf.multNew(time));
		return vec;
	}

	@Override
	/**
	 * Applies this force to the given Vector2d.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg
	 * @param forceSum unused in GeneralForce
	 * @param vec velocity vector to be accelerated by force.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	public Vector2d applyTo(double time, double mass, Vector2d forceSum,
			Vector2d vec) {
		Vector2d vecd = this.vecd.divNew(mass);
		vec.add(vecd.multNew(time));
		return vec;
	}

	/**
	 *
	 */
	@Override
	public double getAcceleration(double mass) {
		return getNewtonForce(mass) / mass;
	}

}
