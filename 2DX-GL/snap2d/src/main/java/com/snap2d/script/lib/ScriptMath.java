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

package com.snap2d.script.lib;

import bg.x2d.gen.NumberGenerator;
import bg.x2d.geo.Vector2d;

import com.snap2d.script.ScriptLink;
import com.snap2d.script.Vec2;

/**
 * @author Brian Groenke
 *
 */
public class ScriptMath {

    @ScriptLink
    public static double sqrt(final double arg) {

        return Math.sqrt(arg);
    }

    @ScriptLink
    public static double cbrt(final double arg) {

        return Math.cbrt(arg);
    }

    @ScriptLink
    public static double sin(final double arg) {

        return Math.sin(arg);
    }

    @ScriptLink
    public static double cos(final double arg) {

        return Math.cos(arg);
    }

    @ScriptLink
    public static double tan(final double arg) {

        return Math.tan(arg);
    }

    @ScriptLink
    public static double asin(final double arg) {

        return Math.asin(arg);
    }

    @ScriptLink
    public static double acos(final double arg) {

        return Math.acos(arg);
    }

    @ScriptLink
    public static double atan(final double arg) {

        return Math.atan(arg);
    }

    @ScriptLink
    public static double csc(final double arg) {

        return 1 / Math.sin(arg);
    }

    @ScriptLink
    public static double sec(final double arg) {

        return 1 / Math.cos(arg);
    }

    @ScriptLink
    public static double cot(final double arg) {

        return 1 / Math.tan(arg);
    }

    @ScriptLink
    public static double toRads(final double degrees) {

        return Math.toRadians(degrees);
    }

    @ScriptLink
    public static double toDegs(final double rads) {

        return Math.toDegrees(rads);
    }

    @ScriptLink
    /**
     * @param arg0 integer to be shifted
     * @param arg1 number of binary places to be shifted
     * @return
     */
    public static int leftShift(final int arg0, final int arg1) {

        return arg0 << arg1;
    }

    @ScriptLink
    public static int rightShift(final int arg0, final int arg1) {

        return arg0 >> arg1;
    }

    @ScriptLink
    public static int bitXOR(final int arg0, final int arg1) {

        return arg0 ^ arg1;
    }

    @ScriptLink
    public static double absv(final double arg0) {

        return Math.abs(arg0);
    }

    @ScriptLink
    public static double min(final double arg0, final double arg1) {

        return Math.min(arg0, arg1);
    }

    @ScriptLink
    public static double max(final double arg0, final double arg1) {

        return Math.max(arg0, arg1);
    }

    @ScriptLink
    public static int cast(final double arg0) {

        return (int) arg0;
    }

    @ScriptLink
    public static int round(final double arg0) {

        return (int) Math.round(arg0);
    }

    @ScriptLink
    public static int fromHex(final String hexStr) {

        return Integer.parseInt(hexStr, 16);
    }

    @ScriptLink
    public static String toHex(final int dec) {

        return Integer.toHexString(dec);
    }

    @ScriptLink
    public static double vecMag(final Vec2 v) {

        Vector2d vec = v.getValue();
        return vec.getMagnitude();
    }

    @ScriptLink
    public static double vecAngle(final Vec2 v) {

        Vector2d vec = v.getValue();
        return vec.rads();
    }

    @ScriptLink
    public static double angleBetween(final Vec2 v1, final Vec2 v2) {

        Vector2d vec1 = v1.getValue();
        Vector2d vec2 = v2.getValue();
        return vec1.angleBetween(vec2);
    }

    @ScriptLink
    public static double rand() {

        return Math.random();
    }

    @ScriptLink
    /**
     * @param +/- bound for the randomly generated integer
     * @return pseudo-random integer between -limit and limit
     */
    public static int randInt(final int limit) {

        return ((int) (Math.random() * limit)) * ( ((int) Math.round(Math.random() * 1) == 0) ? 1 : -1);
    }

    @ScriptLink
    /**
     * @return pseudo-random integer between Integer.MIN_VALUE and Integer.MAX_VALUE
     */
    public static int randInt() {

        return randInt(Integer.MAX_VALUE);
    }

    @ScriptLink
    public static int randInt(final int min, final int max) {

        NumberGenerator<Integer> gen = new NumberGenerator<Integer>(min, max);
        return gen.generate().intValue();
    }
}
