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
import bg.x2d.math.*;

/**
 * 
 * @author Brian Groenke
 * 
 */
public class Friction extends Force {

	public float fsc, fkc, fg;
	public double dsc, dkc, dg;

	/**
	 * 
	 * @param staticCoeff
	 *            static friction
	 * @param kineticCoeff
	 *            kinetic friction
	 * @param g
	 */
	public Friction(float staticCoeff, float kineticCoeff, Gravity g) {
		fsc = staticCoeff;
		fkc = kineticCoeff;
		fg = g.getVec2f().getMagnitude();
		vecf = new Vector2f(0, 0);
	}

	/**
	 * 
	 * @param staticCoeff
	 * @param kineticCoeff
	 * @param g
	 */
	public Friction(double staticCoeff, double kineticCoeff, Gravity g) {
		dsc = staticCoeff;
		dkc = kineticCoeff;
		dg = g.getVec2d().getMagnitude();
		vecd = new Vector2d(0, 0);
	}

	@Override
	/**
	 * Friction implementation does nothing.
	 */
	public void setForceVector(Vector2f vec) {
		//
	}

	@Override
	/**
	 * Friction implementation does nothing.
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

	@Override
	public double getNewtonForce(double mass) {
		return (vecd != null) ? vecd.getMagnitude() : vecf.getMagnitude();
	}

	@Override
	/**
	 * 
	 * @param time
	 * @param mass
	 * @param forceSum used in calculating static friction. null indicates an infinite force vector, in
	 *     which case static friction will never be applied.
	 * @param vec velocity vector being accelerated
	 */
	public Vector2f applyTo(float time, float mass, Vector2f forceSum,
			Vector2f vec) {

		boolean noForceSum = (forceSum == null);
		if (noForceSum) {
			forceSum = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
		}

		float sx = fsc * mass * fg;
		if (forceSum.getMagnitude() > sx
				|| !FloatMath.equals(vec.getMagnitude(), 0)) {

			float f = fkc * mass * fg;

			// set the force vector's magnitude according to kinetic friction and angle according to
			// the current
			// velocity. Adding or subtracting pi doesn't technically matter, but for cleanliness we
			// should try
			// to prevent the value from being outside of 0-2pi.
			float vecAngle = vec.rads();
			vecf.setFromPolar(f, (vecAngle >= Math.PI) ? vecAngle
					- (float) Math.PI : vecAngle + (float) Math.PI);
		} else {
			vecf.setFromPolar(forceSum.getMagnitude(), forceSum.rads());
		}

		float signX = Math.signum(vec.x);
		float signY = Math.signum(vec.y);
		vec = super.applyTo(time, mass, (noForceSum) ? null : forceSum, vec);
		if (Math.signum(vec.x) != signX) {
			vec.x = 0;
			vecf.x = 0;
		}
		if (Math.signum(vec.y) != signY) {
			vec.y = 0;
			vecf.y = 0;
		}
		return vec;

		/*
		 * boolean noForceSum = (forceSum == null); if(noForceSum) forceSum = new
		 * Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
		 * 
		 * Vector2f fneg = forceSum.negateNew();
		 * 
		 * float sx = Math.abs(fsx * mass * fg); if(Math.abs(forceSum.x) > sx ||
		 * !FloatMath.equals(vec.x, 0)) { vecf.x = fkx * mass * fg * -Math.signum(vec.x); } else
		 * vecf.x = fneg.x;
		 * 
		 * float sy = Math.abs(fsy * mass * fg); if(Math.abs(forceSum.y) > sy ||
		 * !FloatMath.equals(vec.y, 0)) vecf.y = fky * mass * fg * -Math.signum(vec.y); else vecf.y
		 * = fneg.y;
		 * 
		 * float signX = Math.signum(vec.x); float signY = Math.signum(vec.y); vec =
		 * super.applyTo(time, mass, (noForceSum) ? null:forceSum, vec); if(Math.signum(vec.x) !=
		 * signX) vec.x = 0; if(Math.signum(vec.y) != signY) vec.y = 0; return vec;
		 */
	}

	@Override
	/**
	 * 
	 */
	public Vector2d applyTo(double time, double mass, Vector2d forceSum,
			Vector2d vec) {
		boolean noForceSum = (forceSum == null);
		if (noForceSum) {
			forceSum = new Vector2d(Double.POSITIVE_INFINITY,
					Double.POSITIVE_INFINITY);
		}

		double sx = dsc * mass * dg;
		if (forceSum.getMagnitude() > sx
				|| !DoubleMath.equals(vec.getMagnitude(), 0)) {

			double f = dkc * mass * dg;

			// set the force vector's magnitude according to kinetic friction and angle according to
			// the current
			// velocity. Adding or subtracting pi doesn't technically matter, but for cleanliness we
			// should try
			// to prevent the value from being outside of 0-2pi.
			double vecAngle = vec.rads();
			vecd.setFromPolar(f, (vecAngle >= Math.PI) ? vecAngle
					- (float) Math.PI : vecAngle + (float) Math.PI);
		} else {
			vecd.setFromPolar(forceSum.getMagnitude(), forceSum.rads());
		}

		double signX = Math.signum(vec.x);
		double signY = Math.signum(vec.y);
		vec = super.applyTo(time, mass, (noForceSum) ? null : forceSum, vec);
		if (Math.signum(vec.x) != signX) {
			vec.x = 0;
			vecd.x = 0;
		}
		if (Math.signum(vec.y) != signY) {
			vec.y = 0;
			vecd.y = 0;
		}
		return vec;
	}

	/**
	 *
	 */
	@Override
	public double getAcceleration(double mass) {
		return (vecd != null) ? -dkc * dg : -fkc * fg;
	}
}
