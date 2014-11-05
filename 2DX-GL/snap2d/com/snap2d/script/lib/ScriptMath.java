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
	public static double sqrt(double arg) {
		return Math.sqrt(arg);
	}
	
	@ScriptLink
	public static double cbrt(double arg) {
		return Math.cbrt(arg);
	}
	
	@ScriptLink
	public static double sin(double arg) {
		return Math.sin(arg);
	}
	
	@ScriptLink
	public static double cos(double arg) {
		return Math.cos(arg);
	}
	
	@ScriptLink
	public static double tan(double arg) {
		return Math.tan(arg);
	}
	
	@ScriptLink
	public static double asin(double arg) {
		return Math.asin(arg);
	}
	
	@ScriptLink
	public static double acos(double arg) {
		return Math.acos(arg);
	}
	
	@ScriptLink
	public static double atan(double arg) {
		return Math.atan(arg);
	}
	
	@ScriptLink
	public static double csc(double arg) {
		return 1 / Math.sin(arg);
	}
	
	@ScriptLink
	public static double sec(double arg) {
		return 1 / Math.cos(arg);
	}
	
	@ScriptLink
	public static double cot(double arg) {
		return 1 / Math.tan(arg);
	}
	
	@ScriptLink
	public static double toRads(double degrees) {
		return Math.toRadians(degrees);
	}
	
	@ScriptLink
	public static double toDegs(double rads) {
		return Math.toDegrees(rads);
	}
	
	@ScriptLink
	/**
	 * @param arg0 integer to be shifted
	 * @param arg1 number of binary places to be shifted
	 * @return
	 */
	public static int leftShift(int arg0, int arg1) {
		return arg0 << arg1;
	}
	
	@ScriptLink
	public static int rightShift(int arg0, int arg1) {
		return arg0 >> arg1;
	}
	
	@ScriptLink
	public static int bitXOR(int arg0, int arg1) {
		return arg0 ^ arg1;
	}
	
	@ScriptLink
	public static double absv(double arg0) {
		return Math.abs(arg0);
	}
	
	@ScriptLink
	public static double min(double arg0, double arg1) {
		return Math.min(arg0, arg1);
	}
	
	@ScriptLink
	public static double max(double arg0, double arg1) {
		return Math.max(arg0, arg1);
	}
	
	@ScriptLink
	public static int cast(double arg0) {
		return (int) arg0;
	}
	
	@ScriptLink
	public static int round(double arg0) {
		return (int) Math.round(arg0);
	}
	
	@ScriptLink
	public static double vecMag(Vec2 v) {
		Vector2d vec = v.getValue();
		return vec.getMagnitude();
	}
	
	@ScriptLink
	public static double vecAngle(Vec2 v) {
		Vector2d vec = v.getValue();
		return vec.rads();
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
	public static int randInt(int limit) {
		return ((int) (Math.random() * limit)) * (((int) Math.round(Math.random() * 1) == 0) ? 1:-1);
	}
	
	@ScriptLink
	/**
	 * @return pseudo-random integer between Integer.MIN_VALUE and Integer.MAX_VALUE
	 */
	public static int randInt() {
		return randInt(Integer.MAX_VALUE);
	}
	
	@ScriptLink
	public static int randInt(int min, int max) {
		NumberGenerator<Integer> gen = new NumberGenerator<Integer>(min, max);
		return gen.generate().intValue();
	}
}
