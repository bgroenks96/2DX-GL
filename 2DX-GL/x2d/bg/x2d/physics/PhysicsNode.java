/*
 *  Copyright © 2012-2013 Brian Groenke
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

public interface PhysicsNode {

	public Vector2f getVelocity2f();

	public Vector2d getVelocity2d();

	public double getVelocity();

	public void setVelocity(Vector2f vec);

	public void setVelocity(Vector2d vec);

	public void setMass(double kg);

	public Vector2f collide(float velFactor, float surfaceAngle, Collision type);

	public Vector2d collide(double velFactor, double surfaceAngle,
			Collision type);

	public Vector2f applyForces(float time, Force... f);

	public Vector2d applyForces(double time, Force... f);

	public enum Collision {
		X, Y, XY, ANGLED;
	}
}
