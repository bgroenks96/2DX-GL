/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d;

/**
 * Used by Snapdragon2D library classes to print messages to standard streams and
 * dump logs.  Not intended for outside use.
 * @author Brian Groenke
 *
 */
public final class Logger {
	
	private static final String LINE_PREFIX = "[Snap2D] ";
	
	private Logger() {}

	public static final void print(String message, boolean noprefix) {
		System.out.print((noprefix) ? message:LINE_PREFIX + message);
	}
	
	public static final void print(String message) {
		print(message, false);
	}
	
	public static final void println(String message, boolean noprefix) {
		System.out.println((noprefix) ? message:LINE_PREFIX + message);
	}
	
	public static final void println(String message) {
		println(message, false);
	}
}
