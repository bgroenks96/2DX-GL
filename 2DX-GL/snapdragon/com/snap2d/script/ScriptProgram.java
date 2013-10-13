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

import java.lang.reflect.*;
import java.util.*;

import bg.x2d.utils.*;

import com.snap2d.script.lib.*;

/**
 * @author Brian Groenke
 *
 */
public class ScriptProgram {

	ArrayList<ScriptSource> scripts = new ArrayList<ScriptSource>();
	ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	Multimap<String, Function> funcs;
	ScriptEngine engine;

	/**
	 * Create a new ScriptProgram with the given sources.  The program is not compiled or executable
	 * until the <code>compile</code> method is called.
	 * @param linkLibs whether or not the standard SnapScript libraries should be automatically linked (usually should be true)
	 * @param scriptSources the sources to compile
	 */
	public ScriptProgram(boolean linkLibs, ScriptSource...scriptSources) {
		for(ScriptSource s:scriptSources)
			scripts.add(s);
		if(linkLibs) {
			link(ScriptMath.class);
			link(VarStore.class);
		}
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

	public boolean compile() {
		ScriptCompiler compiler = new ScriptCompiler();
		boolean chk = false;
		try {
			String[] srcs = new String[scripts.size()];
			for(int i = 0; i < scripts.size(); i++) {
				srcs[i] = scripts.get(i).getSource();
			}
			funcs = compiler.precompile(srcs);
			System.out.println(funcs);
			// register methods from linked classes
			for(Class<?> c:classes) {
				Method[] methods = c.getDeclaredMethods();
				for(Method m:methods) {
					ScriptLink link = m.getAnnotation(ScriptLink.class);
					if(link == null || !link.value())
						continue;
					Function func = new Function(m.getName(), c, m.getParameterTypes());
					Function[] other = funcs.getAll(func.getName());
					if(other != null) {
						for(Function f:other) {
							if(Arrays.equals(func.getParamTypes(), f.getParamTypes()))
								throw(new ScriptCompilationException("found duplicate linked method '"+f.getName()+"' in class " + c.getName()));
						}
					}
					funcs.put(m.getName(), func);
				}
			}

			chk = compiler.compile(funcs);
			if(!chk) {  // if the compilation failed, clear the function map reference so the runtime cannot be initialized
				funcs.clear();
				funcs = null;
			}
		} catch (ScriptCompilationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return chk;
	}
	
	/**
	 * Initializes the script engine runtime after successful compilation.
	 * @param useDoubleStore if true, the script engine will store 'float' data types as Java doubles, otherwise they will be stored
	 * 		as Java floats
	 */
	public void initRuntime(boolean useDoubleStore) {
		if(funcs == null)
			throw(new IllegalStateException("cannot initialize runtime before compilation"));
		engine = new ScriptEngine(funcs.values().toArray(new Function[funcs.size()]), useDoubleStore);
	}

	public Function findFunction(String name, Class<?>... params) {
		Function[] matches = funcs.getAll(name);
		if(matches == null || matches.length == 0)
			return null;

		funcLoop:
			for(Function f:matches) {
				Keyword[] ks = f.getParamTypes();
				if(ks.length != params.length)
					continue;
				for(int i=0;i<ks.length;i++) {
					Keyword pkw = getKeyword(params[i]);
					if(!ks[i].equals(pkw))
						continue funcLoop;
				}
				
				return f;
			}
		return null;
	}

	public void invoke(Function f, Object...args) throws ScriptInvocationException {
		if(engine == null)
			throw(new IllegalStateException("script engine not initialized"));
		engine.invoke(f.getID(), args);
	}

	private Keyword getKeyword(Class<?> param) {
		if(Function.isInt(param))
			return Keyword.INT;
		else if(Function.isFloat(param))
			return Keyword.FLOAT;
		else if(Function.isBool(param))
			return Keyword.BOOL;
		else if(Function.isString(param))
			return Keyword.STRING;
		else if(Function.isVoid(param))
			return Keyword.VOID;
		else
			return null;
	}

	public static void main(String[] args) {
		ScriptProgram prog = new ScriptProgram(true, new ScriptSource(Utils.readText(
				ClassLoader.getSystemResource("com/snap2d/script/test_script.txt"))));
		prog.compile();
		prog.initRuntime(true);
		Function f = prog.findFunction("CheckCondition", Boolean.class);
		try {
			prog.invoke(f, true);
		} catch (ScriptInvocationException e) {
			e.printStackTrace();
		}
	}
}
