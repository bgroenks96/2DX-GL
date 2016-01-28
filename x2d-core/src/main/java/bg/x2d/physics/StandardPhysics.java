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

import bg.x2d.geo.Vector2d;
import bg.x2d.geo.Vector2f;

/**
 * A PhysicsNode that represents standard (real) physical forces acting upon an
 * object. Gravity is built in and will always be called by
 * <code>applyForces</code>.
 * 
 * @author Brian Groenke
 * 
 */
public class StandardPhysics implements PhysicsNode {

    Gravity g = new Gravity();

    protected Vector2f velf, accelf;
    protected Vector2d veld, acceld;

    protected double mass;
    /**
     * configurable only by subclasses to toggle gravity - gravity will always
     * be applied within a StandardPhysics object
     */
    protected boolean applyGravity = true;

    /**
     * 
     * @param vec
     * @param objMass
     */
    public StandardPhysics(final Vector2f vec, final double objMass) {

        this.mass = objMass;
        velf = vec;
    }

    public StandardPhysics(final Vector2d vec, final double objMass) {

        this.mass = objMass;
        veld = vec;
    }

    @Override
    public Vector2f getVelocity2f() {

        return velf;
    }

    @Override
    public Vector2d getVelocity2d() {

        return veld;
    }

    @Override
    public double getVelocity() {

        return (veld != null) ? veld.getMagnitude() : velf.getMagnitude();
    }

    @Override
    public void setVelocity(final Vector2f vec) {

        veld = null;
        velf = vec;
    }

    @Override
    public void setVelocity(final Vector2d vec) {

        velf = null;
        veld = vec;
    }

    @Override
    public void setMass(final double kg) {

        mass = kg;
    }

    @Override
    public double getMass() {

        return mass;
    }

    @Override
    public Vector2f applyForces(final float time, final Force... f) {

        if (applyGravity) {
            g.applyTo(time, (float) mass, null, velf);
        }

        Vector2f vecSum = new Vector2f(g.getVec2f());
        for (Force force : f) {
            force.applyTo(time, (float) mass, vecSum, velf);
            vecSum.add(force.getVec2f());
        }
        accelf = vecSum.div((float) mass);
        return velf;
    }

    @Override
    public Vector2d applyForces(final double time, final Force... f) {

        if (applyGravity) {
            g.applyTo(time, mass, null, veld);
        }

        Vector2d vecSum = new Vector2d(g.getVec2d());
        for (Force force : f) {
            force.applyTo(time, mass, vecSum, veld);
            vecSum.add(force.getVec2d());
        }
        acceld = vecSum.div(mass);
        return veld;
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
    public Vector2f collide(final float velFactor, final float surfaceAngle, final Collision type) {

        // int q = GeoUtils.quadrant(vecf.x, vecf.y);

        if (type == Collision.X) {
            velf.negateY().mult(velFactor);
        } else if (type == Collision.XY) {
            velf.negate().mult(velFactor);
        } else {
            velf.negateX();
            if (type == Collision.ANGLED) {
                velf.rotate(surfaceAngle - ((float) Math.PI / 2));
            }
            velf.mult(velFactor);
        }

        return velf;
    }

    @Override
    public Vector2d collide(final double velFactor, final double surfaceAngle, final Collision type) {

        // int q = GeoUtils.quadrant(vecd.x, vecd.y);

        if (type == Collision.X) {
            veld.negateY().mult(velFactor);
        } else if (type == Collision.XY) {
            veld.negate().mult(velFactor);
        } else {
            veld.negateX();
            if (type == Collision.ANGLED) {
                veld.rotate(surfaceAngle - (Math.PI / 2));
            }
            veld.mult(velFactor);
        }

        return veld;
    }

    /**
     *
     */
    @Override
    public Vector2f getAcceleration2f() {

        return accelf;
    }

    /**
     *
     */
    @Override
    public Vector2d getAcceleration2d() {

        return acceld;
    }
}
