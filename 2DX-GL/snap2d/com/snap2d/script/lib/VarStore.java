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

import java.util.*;

import com.snap2d.script.*;

/**
 * @author Brian Groenke
 *
 */
public class VarStore {
	
	private static final int INT = 0x10, FLOAT = 0x11, BOOL = 0x12, STRING = 0x13, VEC2 = 0x14;
	
	private HashMap<String, Object> globals = new HashMap<String, Object>();
	private HashMap<String, Array<?>> arrays = new HashMap<String, Array<?>>();
	
	boolean useDouble = false;
	
	public void setUseDouble(boolean storeAsDouble) {
		useDouble = storeAsDouble;
	}
	
	@ScriptLink
	public boolean isUsingDouble() {
		return useDouble;
	}
	
	@ScriptLink
	public void putInt(String name, int value) {
		globals.put(name, value);
	}
	
	@ScriptLink
	public void putFloat(String name, double value) {
		globals.put(name, (useDouble) ? value:(float)value);
	}
	
	@ScriptLink
	public void putString(String name, String value) {
		globals.put(name, value);
	}
	
	@ScriptLink
	public void putBool(String name, boolean value) {
		globals.put(name, value);
	}
	
	@ScriptLink
	public void putVec(String name, Vec2 v) {
		globals.put(name, v);
	}
	
	@ScriptLink
	public int getInt(String name) {
		return (Integer) globals.get(name);
	}
	
	@ScriptLink
	public double getFloat(String name) {
		return (Double) globals.get(name);
	}
	
	@ScriptLink
	public boolean getBool(String name) {
		return (Boolean) globals.get(name);
	}
	
	@ScriptLink
	public String getString(String name) {
		return (String) globals.get(name);
	}
	
	@ScriptLink
	public Vec2 getVec(String name) {
		return (Vec2) globals.get(name);
	}
	
	@ScriptLink
	public void newIntArray(String name, int length) {
		arrays.put(name, new IntArray(length));
	}
	
	@ScriptLink
	public void newFloatArray(String name, int length) {
		arrays.put(name, new FloatArray(length));
	}
	
	@ScriptLink
	public void newBoolArray(String name, int length) {
		arrays.put(name, new BoolArray(length));
	}
	
	@ScriptLink
	public void newStringArray(String name, int length) {
		arrays.put(name, new StringArray(length));
	}
	
	@ScriptLink
	public void deleteArray(String name) {
		arrays.remove(name);
	}
	
	@ScriptLink
	public int accessInt(String name, int pos) {
		Array<?> arr = arrays.get(name);
		if(arr == null)
			throw(new NullPointerException("unable to locate array referenced by " + name));
		return arr.intArray().array[pos];
	}
	
	@ScriptLink
	public float accessFloat(String name, int pos) {
		Array<?> arr = arrays.get(name);
		if(arr == null)
			throw(new NullPointerException("unable to locate array referenced by " + name));
		return arr.floatArray().array[pos];
	}
	
	@ScriptLink
	public boolean accessBool(String name, int pos) {
		Array<?> arr = arrays.get(name);
		if(arr == null)
			throw(new NullPointerException("unable to locate array referenced by " + name));
		return arr.boolArray().array[pos];
	}
	
	@ScriptLink
	public String accessStr(String name, int pos) {
		Array<?> arr = arrays.get(name);
		if(arr == null)
			throw(new NullPointerException("unable to locate array referenced by " + name));
		return arr.strArray().array[pos];
	}
	
	private class IntArray extends Array<Integer> {
		int[] array;
		
		IntArray(int length) {
			super(INT);
			array = new int[length];
		}
	}
	
	private class FloatArray extends Array<Float> {
		float[] array;
		
		FloatArray(int length) {
			super(FLOAT);
			array = new float[length];
		}
	}
	private class BoolArray extends Array<Boolean> {
		boolean[] array;
		
		BoolArray(int length) {
			super(BOOL);
			array = new boolean[length];
		}
	}
	private class StringArray extends Array<String> {
		String[] array;
		
		StringArray(int length) {
			super(STRING);
			array = new String[length];
		}
	}
	
	private class Array<T> {
		
		private int type;
		
		Array(int type) {
			this.type = type;
		}
		
		IntArray intArray() {
			return (IntArray) this;
		}
		
		FloatArray floatArray() {
			return (FloatArray) this;
		}
		
		BoolArray boolArray() {
			return (BoolArray) this;
		}
		
		StringArray strArray() {
			return (StringArray) this;
		}
		
		int getType() {
			return type;
		}
	}
}
