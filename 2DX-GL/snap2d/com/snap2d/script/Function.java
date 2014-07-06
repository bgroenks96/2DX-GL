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

import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

/**
 * @author Brian Groenke
 *
 */
public class Function implements Comparable<Function> {

	private static volatile long idTick = 0x860;

	ByteBuffer bytecode;

	private String name, src;
	private String[] paramNames;
	private long id;
	private int srcOffs;
	private Keyword[] paramTypes;
	private Keyword returnType;
	private boolean javaFunc;
	
	private Method javaMethod; // only non-null for linked Java methods

	/**
	 * 
	 * @param name the string identifier of the function as it appears in the script
	 * @param returnType the data type returned by this function
	 * @param params the data type of each parameter in order
	 * @param paramNames the variable name of each parameter in order
	 * @param src the script source code for the function - every character within the function block
	 * @param srcOffs the number of characters preceding the start of the function block - the opening delimiter's
	 * 		character position + 1.
	 */
	Function(String name, Keyword returnType, Keyword[] params, String[] paramNames, String src, int srcOffs) {
		this.name = name;
		this.src = src;
		this.srcOffs = srcOffs;
		this.paramNames = paramNames;
		this.paramTypes = params;
		this.returnType = returnType;
		id = idTick++;
	}

	Function(String name, Class<?> cl, Class<?>... params) throws SecurityException, NoSuchMethodException {
		Method method = cl.getDeclaredMethod(name, params);
		this.name = name;
		this.paramTypes = new Keyword[params.length];
		this.paramNames = new String[params.length];
		this.javaMethod = method;
		for(int i=0;i<params.length;i++) {
			Class<?> c = params[i];
			if(isString(c))
				paramTypes[i] = Keyword.STRING;
			else if(isInt(c) || c.equals(Long.class))
				paramTypes[i] = Keyword.INT;
			else if(isFloat(c))
				paramTypes[i] = Keyword.FLOAT;
			else if(isBool(c))
				paramTypes[i] = Keyword.BOOL;
			else if(isVector(c))
				paramTypes[i] = Keyword.VEC2;
			else
				throw(new IllegalArgumentException("unsupported data type: " + c.getName()));
			paramNames[i] = "arg"+i;
		}
		Class<?> c = method.getReturnType();
		if(isString(c))
			returnType = Keyword.STRING;
		else if(isInt(c))
			returnType = Keyword.INT;
		else if(isFloat(c))
			returnType = Keyword.FLOAT;
		else if(isBool(c))
			returnType = Keyword.BOOL;
		else if(isVoid(c))
			returnType = Keyword.VOID;
		else if(isVector(c))
			returnType = Keyword.VEC2;
		else
			throw(new IllegalArgumentException("unsupported script data type " + c.getName()));
		javaFunc = true;
		id = idTick++;
	}
	
	public Method getJavaMethod() {
		return javaMethod;
	}

	public String getName() {
		return name;
	}

	public long getID() {
		return id;
	}

	/**
	 * @return the function source; null for java functions
	 */
	public String getSource() {
		return src;
	}

	/**
	 * @return the offset of the function source in the file; -1 for java functions
	 */
	public int getSourceCharOffs() {
		return (javaFunc) ? -1:srcOffs;
	}

	public Keyword[] getParamTypes() {
		return Arrays.copyOf(paramTypes, paramTypes.length);
	}

	/**
	 * @return parameter string names for the function; null for java functions
	 */
	public String[] getParamNames() {
		return Arrays.copyOf(paramNames, paramNames.length);
	}

	public int getParamCount() {
		return (paramTypes != null) ? paramTypes.length: 0;
	}

	public Keyword getReturnType() {
		return returnType;
	}

	public boolean isJavaFunction() {
		return javaFunc;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[" + returnType.getSymbol() + " " + name);
		sb.append(Keyword.PARAM_BEGIN.getSymbol());
		if(paramTypes.length > 0) {
			int i = 0;
			for(Keyword k:paramTypes)
				sb.append(k.getSymbol() + " " + paramNames[i++] + Keyword.SEPARATOR.getSymbol() + " ");
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(Keyword.PARAM_END.getSymbol() + "]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Function) {
			Function f = (Function) obj;
			return name.equals(f.name) && Arrays.equals(paramTypes, f.paramTypes);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int compareTo(Function f) {
		if(this.equals(f))
			return 0;
		else {
			int nc = name.compareTo(f.name);
			if(nc != 0)
				return nc;
			if(paramTypes.length > f.paramTypes.length)
				return 1;
			else if(paramTypes.length < f.paramTypes.length)
				return -1;
			return paramTypes[0].compareTo(f.paramTypes[0]);
		}
	}
	
	static boolean isInt(Class<?> c) {
		if(c.equals(Integer.class) || c.equals(int.class))
			return true;
		else 
			return false;
	}
	
	static boolean isFloat(Class<?> c) {
		if(c.equals(Float.class) || c.equals(float.class) || c.equals(Double.class) || c.equals(double.class))
			return true;
		else
			return false;
	}
	
	static boolean isBool(Class<?> c) {
		if(c.equals(Boolean.class) || c.equals(boolean.class))
			return true;
		else
			return false;
	}
	
	static boolean isString(Class<?> c) {
		if(c.equals(String.class))
			return true;
		else
			return false;
	}
	
	static boolean isVector(Class<?> c) {
		if(c.equals(Vec.class))
			return true;
		else
			return false;
	}
	
	static boolean isVoid(Class<?> c) {
		if(c.equals(Void.class) || c.equals(void.class))
			return true;
		else
			return false;
	}
}
