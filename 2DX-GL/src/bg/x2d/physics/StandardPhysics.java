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
	
	Vector2f vecf;
	Vector2d vecd;
	
	double mass;

	/**
	 * 
	 * @param vec
	 * @param objMass
	 */
	public StandardPhysics(Vector2f vec, double objMass) {
		this.mass = objMass;
		vecf = vec;
	}
	
	public StandardPhysics(Vector2d vec, double objMass) {
		this.mass = objMass;
		vecd = vec;
	}

	@Override
	public Vector2f getVelocity2f() {
		return vecf;
	}

	@Override
	public Vector2d getVelocity2d() {
		return vecd;
	}

	@Override
	public double getVelocity() {
		return (vecd != null) ? vecd.getMagnitude():vecf.getMagnitude();
	}

	@Override
	public void setVelocity(Vector2f vec) {
		vecd = null;
		vecf = vec;
	}

	@Override
	public void setVelocity(Vector2d vec) {
		vecf = null;
		vecd = vec;
	}

	@Override
	public void setMass(double kg) {
		mass = kg;
	}

	@Override
	public Vector2f applyForces(float time, Force... f) {
		g.applyTo(time, (float) mass, null, vecf);
		
		Vector2f vecSum = new Vector2f(g.getVec2f());
		for(Force force:f) {
			force.applyTo(time, (float)mass, vecSum, vecf);
			vecSum.add(force.getVec2f());
		}
		return vecf;
	}

	@Override
	public Vector2d applyForces(double time, Force... f) {
		g.applyTo(time, mass, null, vecd);
		
		Vector2d vecSum = new Vector2d(g.getVec2d());
		for(Force force:f) {
			force.applyTo(time, mass, vecSum, vecd);
			vecSum.add(force.getVec2d());
		}
		return vecd;
	}

	@Override
	/**
	 * Applies a basic collision to this node's vector.
	 * @param velFactor the factor applied to velocity on collision
	 * @param surfaceAngle terminal angle of the colliding surface (should be between 0-PI); 
	 *     only required for Collision.ANGLED
	 * @param type indicates whether the collision with the X bound, Y bound, XY corner or angled surface.
	 *     Pass a type value from enum Collision.
	 * @return
	 */
	public Vector2f collide(float velFactor, float surfaceAngle, Collision type) {
		//int q = GeoUtils.quadrant(vecf.x, vecf.y);
		
		if(type == Collision.X) {
			vecf.negateY().mult(velFactor);
		} else if(type == Collision.XY) {
			vecf.negate().mult(velFactor);
		} else {
			vecf.negateX();
			if(type == Collision.ANGLED) {
				vecf.rotate(surfaceAngle - ((float)Math.PI / 2));
			}
			vecf.mult(velFactor);
		}
		
		return vecf;
	}

	@Override
	public Vector2d collide(double velFactor, double surfaceAngle, Collision type) {
		//int q = GeoUtils.quadrant(vecd.x, vecd.y);
		
		if(type == Collision.X) {
			vecd.negateY().mult(velFactor);
		} else if(type == Collision.XY) {
			vecd.negate().mult(velFactor);
		} else {
			vecd.negateX();
			if(type == Collision.ANGLED) {
				vecd.rotate(surfaceAngle - (Math.PI / 2));
			}
			vecd.mult(velFactor);
		}
		
		return vecd;
	}

}
