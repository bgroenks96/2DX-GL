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

package com.snap2d.testing.physics;

import static com.snap2d.testing.physics.PhysicsUI.BOOLEAN;
import static com.snap2d.testing.physics.PhysicsUI.FLOAT;
import static com.snap2d.testing.physics.PhysicsUI.VECTOR;
import bg.x2d.geo.Vector2d;

/**
 * @author Brian Groenke
 *
 */
public enum EntityProperty {

    MASS("Mass", FLOAT), VELOCITY("Velocity", VECTOR), COLL_FACTOR("Collision Factor", FLOAT), SIZE("Size", VECTOR), NO_GRAVITY(
            "Disable Gravity", BOOLEAN);

    final String label;
    final int type;
    final Object objs;

    private EntityProperty(final String label, final int type, final Object... objs) {

        this.label = label;
        this.type = type;
        this.objs = objs;
    }

    public static Vector2d parseVector(String vecStr) throws PropertyFormatException {

        vecStr = vecStr.replaceAll("\\s+", "");
        int xind = vecStr.indexOf("i"), yind = vecStr.indexOf("j");
        if (xind < 0 || yind < 0 || yind <= xind || Character.isDigit(vecStr.charAt(xind + 1))) {
            throw (new PropertyFormatException("invalid vector notation in string: " + vecStr));
        }
        String xs = vecStr.substring(0, xind);
        String ys = vecStr.substring(xind + 1, yind);
        try {
            double x = Double.parseDouble(xs);
            double y = Double.parseDouble(ys);
            return new Vector2d(x, y);
        } catch (NumberFormatException e) {
            throw (new PropertyFormatException("illegal value in vector string: " + vecStr, e));
        }
    }

    public static double parseDouble(final String doubleStr) throws PropertyFormatException {

        double result = 0;
        try {
            result = Double.parseDouble(doubleStr);
            return result;
        } catch (NumberFormatException e) {
            throw (new PropertyFormatException("invalid numeric input: " + doubleStr, e));
        }
    }
}
