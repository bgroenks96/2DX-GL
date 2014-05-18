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

package com.snap2d.script;

import java.util.*;

import com.snap2d.script.ScriptCompiler.Variable;

/**
 * Initializer block for constants - executed similarly to a function in Bytecode, but
 * does not have parameters or any explicitly given name.
 * @author Brian Groenke
 *
 */
class ConstantInitializer extends Function {
	
	private static volatile int id=0;
	
	private Variable[] vars;

	/**
	 * @param name
	 * @param returnType
	 * @param params
	 * @param paramNames
	 * @param src
	 * @param srcOffs
	 */
	ConstantInitializer(String src, int srcOffs) {
		super("consts"+id++, Keyword.VOID, new Keyword[0], new String[0], src, srcOffs);
	}
	
	void setConstantVars(Variable...vars) {
		this.vars = vars;
	}
	
	Variable[] getConstantVars() {
		return Arrays.copyOf(this.vars, this.vars.length);
	}
	
	int getConstantVarCount() {
		return this.vars.length;
	}
}
