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

package com.snap2d.physics.testing;

import bg.x2d.geo.Vector2d;
import bg.x2d.geo.Vector2f;
import bg.x2d.physics.Force;
import bg.x2d.physics.Gravity;
import bg.x2d.physics.StandardPhysics;

import com.snap2d.physics.GamePhysics;
import com.snap2d.script.ScriptInvocationException;
import com.snap2d.script.ScriptLink;
import com.snap2d.script.ScriptProgram;
import com.snap2d.script.lib.VarStore;

/**
 * @author Brian Groenke
 *
 */
public class TestNode extends StandardPhysics implements GamePhysics {

    public static final Gravity ANTI_GRAV = new Gravity(-Gravity.STANDARD);
    public static final String COLL_FUNC_NAME = "OnCollide";

    private static String[] varNames;
    private static final int VEL_X1 = 0, VEL_Y1 = 1, VEL_X2 = 2, VEL_Y2 = 3, MASS1 = 4, MASS2 = 5;

    private static ScriptProgram script = PhysicsUI.scriptUI.getScriptProgram();

    protected boolean noGravity;

    public TestNode(final Vector2d vel, final double mass, final boolean noGravity) {

        super(vel, mass);
        this.noGravity = noGravity;
        String[] consts = script.getConstants();
        varNames = new String[consts.length];
        for (int i = 0; i < consts.length; i++ ) {
            varNames[i] = script.getConstantValue(consts[i]).toString();
        }
    }

    public boolean hasNoGravity() {

        return noGravity;
    }

    /**
     * @return a Vector2f copy of this node's velocity vector
     */
    @Override
    public Vector2f getVelocity2f() {

        return veld.toFloatVec();
    }

    /**
     * @return this node's velocity Vector2d
     */
    @Override
    public Vector2d getVelocity2d() {

        return veld;
    }

    /**
     *
     */
    @Override
    public double getVelocity() {

        return veld.getMagnitude();
    }

    /**
     *
     */
    @Override
    public void setVelocity(final Vector2f vec) {

        veld.x = vec.x;
        veld.y = vec.y;
    }

    /**
     *
     */
    @Override
    public void setVelocity(final Vector2d vec) {

        veld.x = vec.x;
        veld.y = vec.y;
    }

    /**
     *
     */
    @Override
    public void setMass(final double kg) {

        this.mass = kg;
    }

    /**
     *
     */
    @Override
    public Vector2f collide(final float velFactor, final float surfaceAngle, final Collision type) {

        return collide(velFactor, surfaceAngle, type);
    }

    /**
     *
     */
    @Override
    public Vector2d collide(final double velFactor, final double surfaceAngle, final Collision type) {

        return super.collide(velFactor, surfaceAngle, type);
    }

    /**
     *
     */
    @Override
    public Vector2f applyForces(final float time, final Force... f) {

        if (noGravity) {
            ANTI_GRAV.applyTo(time, mass, ANTI_GRAV.getVec2d(), veld);
        }
        return applyForces((double) time, f).toFloatVec();
    }

    /**
     *
     */
    @Override
    public Vector2d applyForces(final double time, final Force... f) {

        if (noGravity) {
            ANTI_GRAV.applyTo(time, mass, ANTI_GRAV.getVec2d(), veld);
        }
        return super.applyForces(time, f);
    }

    /**
     *
     */
    @Override
    public Vector2f collideWith2f(final GamePhysics node) {

        return null;
    }

    /**
     *
     */
    @Override
    public Vector2d collideWith2d(final GamePhysics node) {

        VarStore vars = script.getVarStore();
        vars.putFloat(varNames[VEL_X1], this.veld.x);
        vars.putFloat(varNames[VEL_Y1], this.veld.y);
        vars.putFloat(varNames[VEL_X2], node.getVelocity2d().x);
        vars.putFloat(varNames[VEL_Y2], node.getVelocity2d().y);
        vars.putFloat(varNames[MASS1], this.mass);
        vars.putFloat(varNames[MASS2], node.getMass());
        try {
            script.invoke(script.findFunction(COLL_FUNC_NAME));
        } catch (ScriptInvocationException e) {
            e.printStackTrace();
        }
        this.veld.setXY(vars.getFloat(varNames[VEL_X1]), vars.getFloat(varNames[VEL_Y1]));
        node.getVelocity2d().setXY(vars.getFloat(varNames[VEL_X2]), vars.getFloat(varNames[VEL_Y2]));
        this.setMass(vars.getFloat(varNames[MASS1]));
        node.setMass(vars.getFloat(varNames[MASS2]));
        return null;
    }

    /**
     *
     */
    @Override
    public void attachToPoint(final float dx, final float dy) {

    }

    /**
     *
     */
    @Override
    public void attachToPoint(final double dx, final double dy) {

    }

    static class CollisionData {

        GamePhysics node1, node2;

        CollisionData(final GamePhysics node1, final GamePhysics node2) {

            this.node1 = node1;
            this.node2 = node2;
        }

        @ScriptLink
        public double getNode1XVel() {

            if (node1 == null) {
                return 0;
            }
            return node1.getVelocity2d().x;
        }

        @ScriptLink
        public double getNode1YVel() {

            if (node1 == null) {
                return 0;
            }
            return node1.getVelocity2d().y;
        }

        @ScriptLink
        public double getNode2XVel() {

            if (node2 == null) {
                return 0;
            }
            return node2.getVelocity2d().x;
        }

        @ScriptLink
        public double getNode2YVel() {

            if (node2 == null) {
                return 0;
            }
            return node2.getVelocity2d().y;
        }

        @ScriptLink
        public double getNode1Mass() {

            if (node1 == null) {
                return 0;
            }
            return node1.getMass();
        }

        @ScriptLink
        public double getNode2Mass() {

            if (node2 == null) {
                return 0;
            }
            return node2.getMass();
        }
    }

}
