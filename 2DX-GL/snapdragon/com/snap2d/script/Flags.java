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
 * Flags used by compiler and engine internally to represent types using primitives.
 * @author Brian Groenke
 */
final class Flags {
	
	private Flags() {}
	
	/*
	 * Keyword flags - used by compiler and Keyword enum
	 */
	// 4 keyword classifications - type definitions, operators, statements, and delimiters.
	public static final int TYPE = 0xFF00000, OP = 0xFF00001, STATEMENT = 0xFF00002, DELIMITER = 0xFF00003;
	
	// Define keyword arguments:
	// ARG_BOOL = boolean argument - expects a variable of type bool as arg
	// ARG_SPEC = special argument - reserved for statements like FOR loops that take statements or operations as arguments
	// ARG_NONE = no argument
	// ARG_INT = integer argument - expects variable of type int as arg
	// ARG_FLOAT = float argument - expects variable of floating point type as arg
	// ARG_MATCH = operations that take 2 or more arguments that all must be of matching types
	// ARG_NUM = argument of any numerical type (int or float)
	// ARG_ASSIGN = reserved specifically for assignment operators
	// ARG_DEF = defined argument - expected argument type is operationally defined by some previous declaration
	public static final int ARG_BOOL = 0xFA00000, ARG_SPEC = 0xFA00001, ARG_NONE = 0xFA00002, ARG_INT = 0xFA00003,
			ARG_FLOAT = 0xFA00004, ARG_MATCH = 0xFA00005, ARG_NUM = 0xFA00006, ARG_ASSIGN = 0xFA00007, ARG_DEF = 0xFA00008;
	
	// Define return types:
	// RETURN_BOOL = returns a boolean value (bool)
	// RETURN_INT = returns an integer value (int)
	// RETURN_FLOAT = returns a floating point value (float)
	// RETURN_NONE = returns void - operation does not return any value
	// RETURN_MATCH_ARG = the operation will return a value of its argument(s) type
	public static final int RETURN_BOOL = 0xFB00000, RETURN_INT = 0xFB00001, RETURN_FLOAT = 0xFB00002,
			RETURN_NONE = 0xFB00003, RETURN_MATCH_ARG = 0xFB00004;
	
	// keyword type flags
	public static final int TYPE_INT = 0xFC00000, TYPE_FLOAT = 0xFC00001, TYPE_STRING = 0xFC00002, 
			TYPE_BOOL = 0xFC00003;
	
	// engine main execution completion status signals
	public static final int RETURN = 0xFD000000, BREAK = 0xFD000001;
	
	/*
	 * Pre-compiler signals
	 */
	public static final int W_FLUSH = 0xC000000, DELIM_FLUSH = 0xC000001, PC_RETURN = 0xC000002,
			PC_FUNC = 0xC000003;
}
