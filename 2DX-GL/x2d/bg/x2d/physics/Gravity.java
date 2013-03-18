/*
 *  Copyright © 2011-2013 Brian Groenke
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
	 * F = ma The newton force exerted by gravity depends on the mass of the object. Therefore, the
	 * force (in N) is determined by multiplying the given mass value by the value of gravity's
	 * acceleration.
	 * 
	 * @param mass
	 *            mass of the object in Kg (required).
	 * @return the force exerted on the object with the given mass in Newtons (N).
	 */
	@Override
	public double getNewtonForce(double mass) {
		return ga * mass;
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2f.
	 * 
	 * @param time
	 *            seconds to accelerate the given vector
	 * @param mass
	 *            of the object in Kg. Irrelevant for gravitational force.
	 * @param vec
	 *            velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	@Override
	public Vector2f applyTo(float time, float mass, Vector2f forceSum,
			Vector2f vec) {
		return vec.add(vecf.multNew(time));
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2d.
	 * 
	 * @param time
	 *            seconds to accelerate the given vector (assumed to be velocity).
	 * @param mass
	 *            of the object in Kg. Irrelevant for gravitational force.
	 * @param vec
	 *            velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2d object (allows for chain calls).
	 */
	@Override
	public Vector2d applyTo(double time, double mass, Vector2d forceSum,
			Vector2d vec) {
		return vec.add(vecd.multNew(time));
	}

	/**
	 *
	 */
	@Override
	public double getAcceleration(double mass) {
		return ga;
	}
}
