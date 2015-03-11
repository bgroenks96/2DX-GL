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

import com.snap2d.script.ScriptLink;
import com.snap2d.script.Vec2;

/**
 * @author Brian Groenke
 *
 */
public class ScriptUtils {

    @ScriptLink
    public static void print(final String str) {

        System.out.print(str);
    }

    @ScriptLink
    public static void print(final int i) {

        System.out.print(i);
    }

    @ScriptLink
    public static void print(final double d) {

        System.out.print(d);
    }

    @ScriptLink
    public static void print(final boolean b) {

        System.out.print(b);
    }

    @ScriptLink
    public static void print(final Vec2 vec) {

        System.out.print(vec);
    }

    @ScriptLink
    public static void println(final String str) {

        System.out.println(str);
        ;
    }

    @ScriptLink
    public static void println(final int i) {

        System.out.println(i);
        ;
    }

    @ScriptLink
    public static void println(final double d) {

        System.out.println(d);
        ;
    }

    @ScriptLink
    public static void println(final boolean b) {

        System.out.println(b);
        ;
    }

    @ScriptLink
    public static void println(final Vec2 vec) {

        System.out.println(vec.toString());
    }

    @ScriptLink
    public static void println() {

        System.out.println();
    }

    @ScriptLink
    public static int strToInt(final String str) {

        return Integer.parseInt(str);
    }

    @ScriptLink
    public static double strToFloat(final String str) {

        return Double.parseDouble(str);
    }

    @ScriptLink
    public static void sysExit(final int exitStatus) {

        System.exit(exitStatus);
    }

    @ScriptLink
    public static void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ScriptLink
    public static double milliTime() {

        return System.currentTimeMillis();
    }

    @ScriptLink
    public static double nanoTime() {

        return System.nanoTime();
    }
}
