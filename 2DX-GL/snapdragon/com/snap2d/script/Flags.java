/*
 *  Copyright � 2011-2013 Brian Groenke
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
 * @author Brian Groenke
 *
 */
final class Flags {
	
	private Flags() {}
	
	/*
	 * Keyword flags - used by compiler and Keyword enum
	 */
	public static final int TYPE = 0xFF00000, OP = 0xFF00001, STATEMENT = 0xFF00002, DELIMITER = 0xFF00003;
	public static final int ARG_BOOL = 0xFA00000, ARG_SPEC = 0xFA00001, ARG_NONE = 0xFA00002, ARG_INT = 0xFA00003,
			ARG_FLOAT = 0xFA00004, ARG_MATCH = 0xFA00005, ARG_NUM = 0xFA00006, ARG_ASSIGN = 0xFA00007;
	public static final int RETURN_BOOL = 0xFB00000, RETURN_INT = 0xFB00001, RETURN_FLOAT = 0xFB00002,
			RETURN_NONE = 0xFB00003, RETURN_MATCH_ARG = 0xFB00004;
	public static final int TYPE_INT = 0xFC00000, TYPE_FLOAT = 0xFC00001, TYPE_STRING = 0xFC00002, 
			TYPE_BOOL = 0xFC00003;
	
	/*
	 * Compiler signals
	 */
	public static final int W_FLUSH = 0xC000000, DELIM_FLUSH = 0xC000001, PC_RETURN = 0xC000002,
			PC_FUNC = 0xC000003;
}
