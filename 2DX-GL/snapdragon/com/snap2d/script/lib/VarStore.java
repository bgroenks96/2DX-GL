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

package com.snap2d.script.lib;

import java.util.*;

import com.snap2d.script.*;

/**
 * @author Brian Groenke
 *
 */
public class VarStore {
	
	private HashMap<String, Object> globals = new HashMap<String, Object>();
	
	boolean useDouble = false;
	
	public void setUseDouble(boolean storeAsDouble) {
		useDouble = storeAsDouble;
	}
	
	@ScriptLink
	public boolean isUsingDouble() {
		return useDouble;
	}
	
	@ScriptLink
	public void storeInt(String name, int value) {
		globals.put(name, value);
	}
	
	@ScriptLink
	public void storeFloat(String name, double value) {
		globals.put(name, (useDouble) ? value:(float)value);
	}
	
	@ScriptLink
	public void storeString(String name, String value) {
		globals.put(name, value);
	}
	
	@ScriptLink
	public void storeBool(String name, boolean value) {
		globals.put(name, value);
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
}
