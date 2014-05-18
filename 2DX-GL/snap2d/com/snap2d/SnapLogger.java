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

package com.snap2d;

import java.io.*;
import java.util.*;

import bg.x2d.utils.*;

/**
 * Used by Snap2D library classes to print messages to standard streams and
 * dump logs.  Not intended for outside use (except to enable/disable messages).
 * @author Brian Groenke
 *
 */
public final class SnapLogger {
	
	public static final File LOG_DIR = new File(Utils.TEMP_DIR + File.separator + "logs");
	
	private static final String LINE_PREFIX = "[Snap2D] ";
	
	private static final File LOG_MAIN = new File(LOG_DIR + File.separator + "snap2d_log-0.log");
	
	private static boolean enabled = true;
	
	static {
		try {
			LOG_DIR.mkdir();
			LOG_MAIN.createNewFile();
		} catch (IOException e) {
			System.err.println("[Snap2D] failed to create log file: " + e.toString());
		}
	}
	
	private SnapLogger() {}

	public static final void print(String message, boolean noprefix) {
		appendToLog(message, false);
		if(!enabled)
			return;
		System.out.print((noprefix) ? message:LINE_PREFIX + message);
	}
	
	public static final void print(String message) {
		print(message, false);
	}
	
	public static final void println(String message, boolean noprefix) {
		appendToLog(message, true);
		if(!enabled)
			return;
		System.out.println((noprefix) ? message:LINE_PREFIX + message);
	}
	
	public static final void println(String message) {
		println(message, false);
	}
	
	public static final void printErr(String message, boolean newLine) {
		String errMsg = LINE_PREFIX + message + ((newLine) ? "\n":"");
		appendToLog(errMsg, newLine);
		System.err.print(errMsg);
	}
	
	/**
	 * Quietly prints this message to the log file only.
	 * @param message
	 */
	public static final void log(String message) {
		appendToLog(message, true);
	}
	
	private static final void appendToLog(String message, boolean newLine) {
		if(newLine)
			Utils.writeToLogFile(LOG_MAIN, "[" + Calendar.getInstance().getTime() + "] " + message, true, newLine);
		else
			Utils.writeToLogFile(LOG_MAIN, message, true, newLine);
	}
	
	/**
	 * Enables/disables log messages from Snap2D libraries.  If the
	 * SnapLogger class is disabled, no messages will be printed to stdout or stderr.
	 * Received log messages will still be written to the primary log file in the Snap2D temp-dir.
	 * @param enable
	 */
	public static final void setEnabled(boolean enable) {
		enabled = enable;
	}
}
