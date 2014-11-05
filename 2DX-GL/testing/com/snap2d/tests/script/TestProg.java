/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.tests.script;

import org.junit.Test;

import com.snap2d.script.Function;
import com.snap2d.script.ScriptInvocationException;
import com.snap2d.script.ScriptProgram;
import com.snap2d.script.ScriptSource;

/**
 * @author brian
 *
 */
public class TestProg {
	
	private static final String SRC1 =
			"void Init() { putInt(\"tick\",0);}\n"
			+ "void Tick() { int tick = getInt(\"tick\"); println(tick); sleep(1000);}";
	
	@Test
	public void testLoop() {
		ScriptProgram prog = new ScriptProgram(true, new ScriptSource(SRC1));
		if (!prog.compile())
			throw new RuntimeException("compilation failed",prog.getLastCompileError());
		try {
			prog.initRuntime(true);
			Function init = prog.findFunction("Init");
			Function tick = prog.findFunction("Tick");
			prog.invoke(init);
			for (int i=0; i < 10; i++) {
				prog.invoke(tick);
			}
		} catch (ScriptInvocationException e) {
			e.printStackTrace();
		}
	}
}
