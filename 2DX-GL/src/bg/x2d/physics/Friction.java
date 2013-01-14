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
 * @author Brian Groenke
 *
 */
public class Friction extends Force {
	
	public float fsx, fsy, fkx, fky, fg;
	public double dsx, dsy, dkx, dky, dg;

	/**
	 * 
	 * @param staticX
	 * @param staticY
	 * @param kineticX
	 * @param kineticY
	 * @param g
	 */
	public Friction(float staticX, float staticY, float kineticX, float kineticY, Gravity g) {
		fsx = staticX;
		fsy = staticY;
		fkx = kineticX;
		fky = kineticY;
		fg = Math.abs(g.getVec2f().y);
		vecf = new Vector2f(0,0);
	}
	
	/**
	 * 
	 * @param staticX
	 * @param staticY
	 * @param kineticX
	 * @param kineticY
	 * @param g
	 */
	public Friction(double staticX, double staticY, double kineticX, double kineticY, Gravity g) {
		dsx = staticX;
		dsy = staticY;
		dkx = kineticX;
		dky = kineticY;
		dg = Math.abs(g.getVec2d().y);
		vecd = new Vector2d(0,0);
	}
	
	public Friction(float staticX, float kineticX, Gravity g) {
		this(staticX, 0, kineticX, 0, g);
	}
	
	public Friction(double staticX, double kineticX, Gravity g) {
		this(staticX, 0, kineticX, 0, g);
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
		return (vecd != null) ? vecd.mag:vecf.mag;
	}
	
	public Vector2f applyTo(float time, float mass, Vector2f vec) {
		Vector2f neg = vec.negateNew();
		
		float sx = Math.abs(fsx * mass * fg);
		if(Math.abs(vec.x) > sx)
			vecf.x = fkx * mass * fg * Math.signum(neg.x);
		else
			vecf.x = neg.x;
		
		float sy = Math.abs(fsy * mass * fg);
		if(Math.abs(vec.y) > sy)
			vecf.y = fky * mass * fg * Math.signum(neg.y);
		else
			vecf.y = neg.y;
		
		return super.applyTo(time, mass, vec);
	}

	public Vector2d applyTo(double time, double mass, Vector2d vec) {
		Vector2d neg = vec.negateNew();
		
		double sx = Math.abs(dsx * mass * dg);
		if(Math.abs(vec.x) > sx)
			vecd.x = dkx * mass * dg * Math.signum(neg.x);
		else
			vecd.x = neg.x;
		
		double sy = Math.abs(dsy * mass * dg);
		if(Math.abs(vec.y) > sy)
			vecd.y = dky * mass * dg * Math.signum(neg.y);
		else
			vecd.y = neg.y;
		
		return super.applyTo(time, mass, vec);
	}
}
