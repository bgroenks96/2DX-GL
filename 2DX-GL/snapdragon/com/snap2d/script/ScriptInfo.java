/*
 *  Copyright © 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.script;

/**
 * SnapScript language info constants.  All members have a human-readable string
 * identifier and an associated code.  The implementation and use of the code
 * depends on the use of the info member.
 * @author Brian Groenke
 *
 */
public enum ScriptInfo {
	
	/**
	 * The language specification version.
	 */
	SCRIPT_VERSION("1.1", 0x0002),
	/**
	 * The bytecode specification version.
	 */
	BYTECODE_SPEC("0.2", 0x1002);
	
	String str;
	int code;
	
	ScriptInfo(String str, int code) {
		this.str = str;
		this.code = code;
	}
	
	public String getString() {
		return str;
	}
	
	public int getCode() {
		return code;
	}
}
