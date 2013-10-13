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

import static com.snap2d.script.Bytecodes.*;

import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

/**
 * Class responsible for interpreting and executing script function bytecode.
 * @author Brian Groenke
 *
 */
class ScriptEngine {

	HashMap<Long, Function> funcMap = new HashMap<Long, Function>();
	HashMap<Function, Object> javaObjs = new HashMap<Function, Object>();

	private final boolean useDouble;

	ScriptEngine(Function[] functions, boolean useDouble) {
		this.useDouble = useDouble;
		for(Function f:functions) {
			funcMap.put(f.getID(), f);
		}
	}

	public void attachObjectToFunction(long fid, Object obj) throws ScriptInvocationException {
		Function f = funcMap.get(fid);
		if(f == null)
			return;
		if(!f.isJavaFunction())
			throw(new ScriptInvocationException("cannot attach Object to non-Java function", f));
		javaObjs.put(f, obj);
	}

	public Object invoke(long id, Object... args) throws ScriptInvocationException {
		Function f = funcMap.get(id);
		if(f.isJavaFunction())
			return invokeJavaFunction(f, javaObjs.get(f), args);
		else
			return invokeFunction(f, args);
	}

	private Object invokeJavaFunction(Function f, Object javaObj, Object... args) throws ScriptInvocationException {
		if(!f.isJavaFunction())
			throw(new ScriptInvocationException("cannot invoke Java execution on a script function", f));
		Method m = f.getJavaMethod();
		try {
			return m.invoke(javaObj, args);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	// >>>>>> SCRIPT EXECUTION ENGINE >>>>>> //

	/*
	 * As a development note, all methods that interpret and execute bytecode
	 * commands should begin with the phrase "exec" followed by the specific action
	 * i.e. "execExampleTask"
	 */

	Object ret;
	ByteBuffer buff;
	Function curr;
	LinkedList<VarStack> stacks = new LinkedList<VarStack>();

	private void putVar(int id, int type, Object value) {
		Variable prev = fetchVar(id);
		if(prev != null)
			findStack(prev).put(id, new Variable(id, type, value));
		else
			stacks.peekFirst().put(id, new Variable(id, type, value));
	}

	private Variable fetchVar(int id) {
		for(VarStack vs:stacks) {
			Variable var = vs.get(id);
			if(var != null)
				return var;
		}
		return null;
	}

	private VarStack findStack(Variable var) {
		for(VarStack vs:stacks) {
			if(vs.get(var.id) != null)
				return vs;
		}
		return null;
	}

	private Object invokeFunction(Function f, Object... args) throws ScriptInvocationException {
		buff = f.bytecode;
		curr = f;
		stacks.clear();
		stacks.add(new VarStack());

		byte init = buff.get();
		switch(init) {
		case INIT_PARAMS:
			for(int i=0;i<f.getParamCount();i++) {
				if(buff.get() != Bytecodes.PARAM_VAR)
					throw(new ScriptInvocationException("found unexpected bytecode instruction: 0x" + Integer.toHexString(init), f));
				int id = buff.getInt();
				putVar(id, Keyword.typeKeyToFlag(f.getParamTypes()[i]) /* Get flag value for type */, args[i]);
			}
		case NO_PARAMS:
			execMain(buff.position());
			break;
		default:
			throw(new ScriptInvocationException("found unexpected bytecode instruction: 0x" + Integer.toHexString(init), f));
		}

		return (f.getReturnType().equals(Keyword.VOID)) ? null:ret;
	}

	private void execMain(int st) throws ScriptInvocationException {
		buff.position(st);
		while(buff.position() < buff.capacity()) {
			byte next = buff.get();
			switch(next) {
			case NEW_STACK:
				stacks.push(new VarStack());
				break;
			case CLEAR_STACK:
				stacks.pop();
				break;
			case INVOKE_FUNC:
				execFuncCall();
				break;
			case INVOKE_JAVA_FUNC:
				execJavaCall();
				break;
			case STORE_VAR:
				next = buff.get();
				int id = buff.getInt();
				Object ret = execExpression();
				switch(next) {
				case REALLOC:
					Variable curr = fetchVar(id);
					putVar(id, curr.type, ret);
					break;
				case ALLOC_INT:
					putVar(id, Flags.TYPE_INT, ret);
					break;
				case ALLOC_FLOAT:
					putVar(id, Flags.TYPE_FLOAT, ret);
					break;
				case ALLOC_BOOL:
					putVar(id, Flags.TYPE_BOOL, ret);
					break;
				case ALLOC_STRING:
					putVar(id, Flags.TYPE_STRING, ret);
					break;
				}
				System.exit(0);
				break;
			case IF:
				break;
			case FOR_VAR:
				break;
			default:
				throw(new ScriptInvocationException("found unexpected bytecode instruction: 0x" + Integer.toHexString(next), curr));
			}
		}
	}

	private Object execExpression() throws ScriptInvocationException {
		byte next = 0;
		while(next != Bytecodes.END_CMD) {
			next = buff.get();
			switch(next) {
			case EVAL:
				return execEvaluation();
			case READ_STR:
				break;
			case REF_VAR:
				break;
			case INVOKE_JAVA_FUNC:
				return execJavaCall();
			case INVOKE_FUNC:
				return execFuncCall();
			}
		}
		return null;
	}
	
	private double execEvaluation() throws ScriptInvocationException {
		byte next = 0;
		Vector<Double> operands = new Vector<Double>();
		while(next != Bytecodes.END_CMD) {
			next = buff.get();
			switch(next) {
			case READ_OP:
				if(operands.size() == 0)
					throw(new ScriptInvocationException("too few operands", curr));
				break;
			case READ_FLOAT:
				operands.add((double) buff.getFloat());
				break;
			case READ_INT:
				operands.add((double) buff.getInt());
				break;
			case TRUE:
				break;
			case FALSE:
				break;
			case REF_VAR:
				break;
			}
		}
		return 0.0;
	}

	private Object execJavaCall() throws ScriptInvocationException {
		long fid = buff.getLong();
		Function f = funcMap.get(fid);
		if(!f.isJavaFunction())
			throw(new ScriptInvocationException("invalid command for non-Java function", curr));
		Object[] args = new Object[f.getParamCount()];
		for(int i=0;i<f.getParamCount();i++) {
			args[i] = execExpression();
		}
		return invokeJavaFunction(f, javaObjs.get(f), args);
	}

	private Object execFuncCall() {
		return null;
	}

	private class VarStack {

		HashMap<Integer, Variable> varmap = new HashMap<Integer, Variable>();

		public void put(int id, Variable var) {
			varmap.put(id, var);
		}

		public Variable get(int id) {
			return varmap.get(id);
		}

		public void remove(int id) {
			varmap.remove(id);
		}
	}

	private class Variable {

		final int id, type;
		Object value;

		Variable(int id, int type, Object value) {
			this.id = id;
			this.type = type;
			this.value = value;
		}

		int castInt() {
			return (Integer) value;
		}

		double castFloat() {
			return (Double) value;
		}

		String castString() {
			return (String) value;
		}

		boolean castBool() {
			return (Boolean) value;
		}
	}
}
