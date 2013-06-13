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
enum Keyword {
	
	IF("if", Flags.STATEMENT, Flags.ARG_BOOL, 1), ELSE("else", Flags.STATEMENT, Flags.ARG_BOOL, 1), FOR("for", Flags.STATEMENT, Flags.ARG_SPEC, 3), BREAK("break", Flags.STATEMENT),
	CONTINUE("continue", Flags.STATEMENT), END(";", Flags.DELIMITER), ARG_BEGIN("(",Flags.DELIMITER), ARG_END(")", Flags.DELIMITER),
	BLOCK_BEGIN("{", Flags.DELIMITER), BLOCK_END("}", Flags.DELIMITER), SEPARATOR(",", Flags.DELIMITER), INT("int", Flags.TYPE), FLOAT("float", Flags.TYPE),
	BOOL("bool", Flags.TYPE), STRING("string", Flags.TYPE), VOID("void", Flags.TYPE), ADD("+", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_MATCH_ARG),
	SUBTRACT("-", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_MATCH_ARG), MULTIPLY("*", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_MATCH_ARG), 
	DIVIDE("/", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_MATCH_ARG), POW("^", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_MATCH_ARG),
	SHIFT_RIGHT(">>", Flags.OP, Flags.ARG_INT, 2, Flags.RETURN_INT), SHIFT_LEFT("<<", Flags.OP, Flags.ARG_INT, 2, Flags.RETURN_INT), 
	ASSIGN("=", Flags.OP, Flags.ARG_ASSIGN, 2, Flags.RETURN_MATCH_ARG),EQUALS("==", Flags.OP, Flags.ARG_MATCH, 2, Flags.RETURN_BOOL), 
	NOT_EQUALS("!=", Flags.OP, Flags.ARG_MATCH, 2, Flags.RETURN_BOOL), NOT("!", Flags.OP), GREATER(">", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_BOOL),
	LESSER("<", Flags.OP, Flags.ARG_NUM, 2, Flags.RETURN_BOOL), STR_MARK("\"", Flags.DELIMITER), OR("|", Flags.OP, Flags.ARG_BOOL, 2, Flags.RETURN_BOOL),
	AND("&", Flags.OP, Flags.ARG_BOOL, 2, Flags.RETURN_BOOL), TRUE("true", Flags.STATEMENT), FALSE("false", Flags.STATEMENT);
	
	private static final int TYPE_POS = 0, ARG_TYPE_POS = 1, ARG_COUNT_POS = 2, RETURN_POS = 3;
	
	final String sym;
	int[] specs;

	/**
	 * Specification flag positions:
	 * 0 = keyword type
	 * 1 = argument type
	 * 2 = argument count
	 * 3 = return type
	 * @param symbol keyword symbol/identifier
	 * @param specs keyword specification flags
	 */
	Keyword(String symbol, int...specs) {
		this.sym = symbol;
		this.specs = specs;
	}
	
	public int getType() {
		return specs[TYPE_POS];
	}
	
	public int getArgType() {
		if(specs.length > ARG_TYPE_POS)
			return specs[ARG_TYPE_POS];
		else
			return Flags.ARG_NONE;
	}
	
	public int getArgCount() {
		if(specs.length > ARG_COUNT_POS)
			return specs[ARG_COUNT_POS];
		else
			return 0;
	}
	
	public int getReturnType() {
		if(specs.length > RETURN_POS)
			return specs[RETURN_POS];
		else
			return Flags.RETURN_NONE;
	}
	
	public String getSymbol() {
		return sym;
	}
	
	public static Keyword getFromSymbol(String sym) {
		for(Keyword k:Keyword.values())
			if(k.sym.equals(sym))
				return k;
		return null;
	}
	
	public static boolean isDelimiter(String s) {
		Keyword k = getFromSymbol(s);
		if(k == null || k.getType() != Flags.DELIMITER)
			return false;
		else
			return true;
	}
	
	public static boolean isOperator(String s) {
		Keyword k = getFromSymbol(s);
		if(k == null || k.getType() != Flags.OP)
			return false;
		else
			return true;
	}
}
