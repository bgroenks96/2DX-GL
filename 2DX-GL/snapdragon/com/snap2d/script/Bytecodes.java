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

package com.snap2d.script;

/**
 * @author Brian Groenke
 *
 */
final class Bytecodes {
	
	private Bytecodes() {}

	public static final byte END_CMD = 0x0, ALLOC_INT = 0x1, ALLOC_FLOAT = 0x2, ALLOC_STRING = 0x3, ALLOC_BOOL = 0x4, STORE_VAR = 0x5, 
			REF_VAR = 0x6, INVOKE_FUNC = 0x7, INVOKE_JAVA_FUNC = 0x8, TYPE_INT = 0x9, TYPE_FLOAT = 0xA, TYPE_STRING = 0xB, TYPE_BOOL = 0xC,
			IF = 0xD, TRUE = 0xE, FALSE = 0xF, SKIP = 0x10;
}
