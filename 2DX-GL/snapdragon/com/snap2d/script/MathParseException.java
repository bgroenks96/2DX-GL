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

public class MathParseException extends ScriptCompilationException {

	/**
	 * @param message
	 * @param source
	 * @param charPos
	 */
	public MathParseException(String message) {
		super(message, null, 0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5617820949413283255L;

	public static final int UNSPECIFIED_POS = -1;

	int pos = UNSPECIFIED_POS;

	/*
	 * Value of -1 means not specified.
	 */
	public int getPos() {
		return pos;
	}
}
