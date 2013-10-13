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
public class ScriptInvocationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2687331964728629451L;
	
	String msg;
	Function func;
	
	public ScriptInvocationException(String message, Function f) {
		super(message);
		this.func = f;
		msg = "error executing function '"+f.getName()+"' [fid="+f.getID()+"]";
	}
	
	public Function getTargetFunction() {
		return func;
	}
	
	@Override
	public void printStackTrace() {
		System.err.println(msg);
		super.printStackTrace();
	}
}
