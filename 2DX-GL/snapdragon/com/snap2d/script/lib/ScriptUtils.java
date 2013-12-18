/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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

import com.snap2d.script.*;

/**
 * @author Brian Groenke
 *
 */
public class ScriptUtils {
	
	@ScriptLink
	public static void print(String str) {
		System.out.print(str);
	}
	
	@ScriptLink
	public static void print(int i) {
		System.out.print(i);
	}
	
	@ScriptLink
	public static void print(double d) {
		System.out.print(d);
	}
	
	@ScriptLink
	public static void print(boolean b) {
		System.out.print(b);
	}
	
	@ScriptLink
	public static void println(String str) {
		System.out.println(str);;
	}
	
	@ScriptLink
	public static void println(int i) {
		System.out.println(i);;
	}
	
	@ScriptLink
	public static void println(double d) {
		System.out.println(d);;
	}
	
	@ScriptLink
	public static void println(boolean b) {
		System.out.println(b);;
	}
}
