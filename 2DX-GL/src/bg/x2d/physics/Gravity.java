/*
 * Copyright � 2011-2012 Brian Groenke
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

public class Gravity extends Force {

	public static final float STANDARD = -9.807f;

	private float ga;

	public Gravity(float mpss) {
		ga = mpss;
		initVectors();
	}
	
	public Gravity() {
		this(STANDARD);
	}


	private void initVectors() {
		vecf = new Vector2f(0, ga);
		vecd = new Vector2d(0, ga);
	}
	
	@Override
	/**
	 * Gravity implementation does nothing.
	 */
	public void setForceVector(Vector2f vec) {
		//
	}

	@Override
	/**
	 * Gravity implementation does nothing.
	 */
	public void setForceVector(Vector2d vec) {
		//
	}

	@Override
	public Vector2f getVec2f() {
		return vecf;
	}

	@Override
	public Vector2d getVec2d() {
		return vecd;
	}

	/**
	 * F = ma
	 * The newton force exerted by gravity depends on the mass of the object.
	 * Therefore, the force (in N) is determined by multiplying the given
	 * mass value by the value of gravity's acceleration.
	 * @param mass mass of the object in Kg (required).
	 * @return the force exerted on the object with the given mass in Newtons (N).
	 */
	@Override
	public double getNewtonForce(double mass) {
		return ga * mass;
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2f.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg.  Irrelevant for gravitational force.
	 * @param vec velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	@Override
	public Vector2f applyTo(float time, float mass, Vector2f forceSum, Vector2f vec) {
		return vec.add(vecf.multNew(time));
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2d.
	 * @param time seconds to accelerate the given vector (assumed to be
	 * velocity).
	 * @param mass of the object in Kg.  Irrelevant for gravitational force.
	 * @param vec velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2d object (allows for chain calls).
	 */
	@Override
	public Vector2d applyTo(double time, double mass, Vector2d forceSum, Vector2d vec) {
		return vec.add(vecd.multNew(time));
	}
}
