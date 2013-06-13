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

import java.util.*;

import bg.x2d.utils.*;

/**
 * @author Brian Groenke
 *
 */
public class ScriptProgram {

	ArrayList<ScriptSource> scripts = new ArrayList<ScriptSource>();
	ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	Multimap<String, Function> funcs;

	public ScriptProgram(ScriptSource...scriptSources) {
		for(ScriptSource s:scriptSources)
			scripts.add(s);
	}

	public void link(ScriptSource script) {
		scripts.add(script);
	}

	public void link(Class<?> javaClass) {
		classes.add(javaClass);
	}

	public void unlink(ScriptSource script) {
		scripts.remove(script);
	}

	public void unlink(Class<?> javaClass) {
		classes.remove(javaClass);
	}

	public void compile() {
		ScriptCompiler compiler = new ScriptCompiler();
		try {
			String[] srcs = new String[scripts.size()];
			for(int i = 0; i < scripts.size(); i++) {
				srcs[i] = scripts.get(i).getSource();
			}
			funcs = compiler.precompile(srcs);
			System.out.println(funcs);
			compiler.compile(funcs);
		} catch (ScriptCompilationException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ScriptProgram prog = new ScriptProgram(new ScriptSource(Utils.readText(
				ClassLoader.getSystemResource("com/snap2d/script/test_script.txt"))));
		prog.compile();
	}
}
