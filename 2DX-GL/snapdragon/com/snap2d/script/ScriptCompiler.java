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

import java.nio.*;
import java.util.*;

import bg.x2d.utils.*;

/**
 * Parses and compiles script source code to bytecode form.
 * @author Brian Groenke
 *
 */
class ScriptCompiler {

	final MathParser parser = new MathParser();

	public float test(String math) throws MathParseException {
		return parser.parse(math);
	}

	// ------------------ PRECOMPILER ------------------ //

	public Multimap<String, Function> precompile(String...scripts) throws ScriptCompilationException {
		Multimap<String, Function> fmap = new Multimap<String, Function>();
		for(String script:scripts) {
			Function[] farr = parseScript(script);
			for(Function f:farr) {
				fmap.put(f.getName(), f);
			}
		}
		return fmap;
	}

	private Function[] parseScript(String src) throws ScriptCompilationException {
		char[] chars = src.toCharArray();
		ArrayList<Function> flist = new ArrayList<Function>();
		StringBuilder buff = new StringBuilder();

		int ekey = Flags.PC_RETURN; // expected Keyword type
		int nextFlush = Flags.W_FLUSH; // next buffer flush point
		boolean allowDigits = false;
		String fname = null; Keyword rtype = null; Keyword[] params = null; String[] paramNames;
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if(Character.isWhitespace(c) && buff.length() == 0)
				continue;
			else if(Character.isWhitespace(c) && nextFlush == Flags.W_FLUSH) {
				switch(ekey) {
				case Flags.PC_RETURN:
					String type = buff.toString();
					rtype = Keyword.getFromSymbol(type);
					if(rtype == null)
						throw(new ScriptCompilationException("unrecognized return type", src, i));
					buff.delete(0, buff.length());
					ekey = Flags.PC_FUNC;
					nextFlush = Flags.DELIM_FLUSH;
					allowDigits = true;
				}
			} else if(Keyword.isDelimiter(String.valueOf(c)) && nextFlush == Flags.DELIM_FLUSH) {
				if(!String.valueOf(c).equals(Keyword.ARG_BEGIN.getSymbol()))
					throw(new ScriptCompilationException("expected " + Keyword.ARG_BEGIN.getSymbol() +
							" in function declaration", src, i));
				fname = buff.toString();
				buff.delete(0, buff.length());
				ekey = Flags.PC_RETURN;
				nextFlush = Flags.W_FLUSH;
				allowDigits = false;
				int close = src.indexOf(Keyword.ARG_END.getSymbol(), i);
				if(close < 0)
					throw(new ScriptCompilationException("missing function delimeter", src, i));
				String paramString = src.substring(i + 1, close);
				int paramCount = (paramString.isEmpty()) ? 0:paramString.split(Keyword.SEPARATOR.getSymbol()).length;
				params = new Keyword[paramCount];
				paramNames = new String[paramCount];
				parseParams(params, paramNames, paramString, src, i + 1);

				int funcStart = src.indexOf(Keyword.BLOCK_BEGIN.getSymbol(), close);
				if(src.substring(close + 1, funcStart + 1).trim().length() > 1)
					throw(new ScriptCompilationException("expected " + Keyword.BLOCK_BEGIN.getSymbol() +
							" in function declaration", src, i));
				int funcEnd = findBlockEnd(chars, funcStart);
				if(funcEnd < 0)
					throw(new ScriptCompilationException("reached end of script with unterminated function", src, funcStart));
				i = funcEnd;

				Function func = new Function(fname, rtype, params, paramNames, src.substring(funcStart + 1, funcEnd), funcStart + 1);
				if(flist.contains(func))
					throw(new ScriptCompilationException("found duplicate function: " + func, src, funcStart));
				flist.add(func);
			} else if(Character.isLetter(c) || Character.isDigit(c)) {
				if(Character.isDigit(c) && !isDigitValid(buff.length(), allowDigits))
					throw(new ScriptCompilationException("illegal numeric character", src, i));
				buff.append(c);
			}
		}
		return flist.toArray(new Function[flist.size()]);
	}

	private void parseParams(Keyword[] storeParams, String[] storeNames, String paramStr, String src, int pos) throws ScriptCompilationException {
		if(storeParams.length == 0)
			return;
		String sep = Keyword.SEPARATOR.getSymbol();
		String[] pts = paramStr.split(sep);
		for(int i = 0; i < pts.length; i++) {
			String s = pts[i].trim();
			String[] param = s.split("\\s+");
			if(param.length != 2)
				throw(new ScriptCompilationException("invalid parameter declaration", src, pos + i));
			Keyword keyw = Keyword.getFromSymbol(param[0]);
			if(keyw == null)
				throw(new ScriptCompilationException("unrecognized parameter type", src, pos + i));
			storeParams[i] = keyw;
			storeNames[i] = param[1];
		}
	}

	// start value is incremented, so the given start position should be the position of the block's starting character
	private int findBlockEnd(char[] src, int start) {
		start++;
		int offs = 0;
		for(int i = start; i < src.length; i++) {
			char c = src[i];
			if(String.valueOf(c).equals(Keyword.BLOCK_BEGIN.getSymbol()))
				offs++;
			else if(String.valueOf(c).equals(Keyword.BLOCK_END.getSymbol())) {
				if(offs > 0)
					offs--;
				else
					return i;
			}
		}
		return -1;
	}

	// ------------------ COMPILER ------------------ //

	private static final int INIT_BUFFER_ALLOC = 0xF00, THRESHOLD = 0x100, REALLOC = 0x400;

	private Multimap<String, Function> functions;
	private HashMap<String, Variable> stackVars = new HashMap<String, Variable>();

	private Function func;

	public void compile(Multimap<String, Function> functions) {
		this.functions = functions;
		stackVars.clear();
		for(Function func:functions.values()) {
			func.bytecode = ByteBuffer.allocateDirect(INIT_BUFFER_ALLOC);

			Keyword[] params = func.getParamTypes();
			String[] names = func.getParamNames();
			for(int i=0; i < params.length; i++) {
				stackVars.put(names[i], new Variable(names[i], getVarTypeFromKeyword(params[i])));
			}

			String src = func.getSource();
			this.func = func;
			this.buff = func.bytecode;
			try {
				parseMain(src, 0);
			} catch(ScriptCompilationException e) {
				System.err.println("compilation problem in function '" + func.getName() + "'");
				e.printStackTrace();
			}
		}
	}

	private ByteBuffer buff;

	private void parseMain(String src, int pos) throws ScriptCompilationException {
		char[] chars = src.toCharArray();
		StringBuilder buff = new StringBuilder();
		Keyword nextDelim = Keyword.ARG_BEGIN;
		for(int i=pos; i < chars.length; i++) {
			char c = chars[i];
			boolean delim = false, voidEndCmd = false;
			if(Character.isWhitespace(c) && buff.length() == 0)
				continue;
			else if(Character.isWhitespace(c) || (delim=Keyword.isDelimiter(String.valueOf(c)))
					|| Keyword.isOperator(String.valueOf(c))) {
				if(delim && (nextDelim == null || !Keyword.getFromSymbol(String.valueOf(c)).equals(nextDelim)))
					throw(new ScriptCompilationException("found unexpected delimeter: " + c, src, i));
				String str = buff.toString();
				Keyword keyw = Keyword.getFromSymbol(str);
				if(keyw == null) {
					Variable var = stackVars.get(str);
					if(var != null) {
						i = parseVarAssign(var.varType, str, src, false, i);
					} else {
						Function f = functions.get(str);
						if(f == null)
							throw(new ScriptCompilationException("unrecognized variable or function: " + str, src, i));
						i = parseFunctionInvocation(f, src, i);
					}
				} else {
					switch(keyw) {
					case IF:
						i = parseConditional(src, i);
						voidEndCmd = true;
						break;
					case FOR:
						i = parseForLoop(src, i);
						voidEndCmd = true;
						break;
					case INT:
						i = parseType(keyw, src, i);
						break;
					case FLOAT:
						i = parseType(keyw, src, i);
						break;
					case STRING:
						i = parseType(keyw, src, i);
						break;
					case BOOL:
						i = parseType(keyw, src, i);
						break;
					default:
						throw(new ScriptCompilationException("found unexpected symbol: " + str, src, i));
					}
				}

				if(!String.valueOf(chars[i]).equals(Keyword.END.sym) && !voidEndCmd)
					throw(new ScriptCompilationException("expected '" + Keyword.END.sym + "' to end statement", src, i));
				i++;

				clear(buff);
			} else {
				buff.append(c);
			}

			// Check if the bytecode buffer needs to be reallocated and expanded
			ByteBuffer data = this.buff;
			if(data.capacity() - data.position() < THRESHOLD) {
				ByteBuffer newBuff = ByteBuffer.allocateDirect(data.capacity() + REALLOC);
				data.flip();
				newBuff.put(data);
				this.buff = newBuff;
				data.clear();
			}
			// ------------------------------------------------------------------
		}
	}

	private int parseConditional(String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.ARG_BEGIN.sym))
			throw(new ScriptCompilationException("illegal conditional delimeter: " + src.charAt(pos), src, pos));
		buff.put(Bytecodes.IF);
		boolean cont = true;
		int endPos = 0;
		while(cont) {
			String bool = src.substring(pos + 1, src.indexOf(Keyword.ARG_END.sym, pos)).trim();
			parseBoolean(bool, src, pos + 1);
			pos = src.indexOf(Keyword.ARG_END.sym, pos) + 1;
			buff.put(Bytecodes.END_COND);
			int bst = src.indexOf(Keyword.BLOCK_BEGIN.sym, pos);
			int end = src.indexOf(Keyword.END.sym, pos);
			String blockSrc;
			if(bst < end) {
				endPos = findBlockEnd(src.toCharArray(), bst);
				blockSrc = src.substring(bst + 1, endPos);
			} else {
				endPos = src.indexOf(Keyword.END.sym, pos);
				blockSrc = src.substring(pos, endPos);
			}

			ByteBuffer prev = this.buff;
			this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);
			parseMain(blockSrc, 0);
			prev.putInt(buff.position());
			this.buff.flip();
			prev.put(buff);
			this.buff = prev;
			
			char[] chars = src.toCharArray();
			
		}
		return endPos + 1;
	}

	private int parseForLoop(String src, int pos) {
		return 0;
	}

	private int parseType(Keyword keyw, String src, int pos) throws ScriptCompilationException {
		int varType = getVarTypeFromKeyword(keyw);
		String name = src.substring(pos, src.indexOf(Keyword.ASSIGN.getSymbol(), pos));
		name = name.trim();
		if(name.split("\\s+").length > 1)
			throw(new ScriptCompilationException("illegal variable name: " + name, src, pos));
		return parseVarAssign(varType, name, src, true, pos + name.length());
	}

	private int parseVarAssign(int varType, String name, String src, boolean alloc, int pos) throws ScriptCompilationException {
		if(Keyword.getFromSymbol(name) != null)
			throw(new ScriptCompilationException("variable names cannot use language keywords: " + name, src, pos));
		buff.put(Bytecodes.STORE_VAR); // STORE
		if(alloc) {
			switch(varType) { // ALLOC
			case Flags.TYPE_INT:
				buff.put(Bytecodes.ALLOC_INT);
				break;
			case Flags.TYPE_FLOAT:
				buff.put(Bytecodes.ALLOC_FLOAT);
				break;
			case Flags.TYPE_STRING:
				buff.put(Bytecodes.ALLOC_STRING);
				break;
			case Flags.TYPE_BOOL:
				buff.put(Bytecodes.ALLOC_BOOL);
			}
			Variable var = new Variable(name, varType);
			stackVars.put(name, var);
			buff.putInt(var.getID());
		} else {
			buff.put(Bytecodes.REALLOC);
			buff.putInt(stackVars.get(name).getID());
		}

		int st = src.indexOf(Keyword.ASSIGN.sym, pos) + 1;
		int end = src.indexOf(Keyword.END.sym, pos);
		if(st > end)
			throw(new ScriptCompilationException("expected variable assignment: " + Keyword.ASSIGN.sym, src, end));
		String str = src.substring(st, end).trim(); // get the assignment statement and trim whitespace

		switch(varType) { // split up type parsing
		case Flags.TYPE_INT:
			parseMathEval(str, src, st, Flags.RETURN_INT);
			break;
		case Flags.TYPE_FLOAT:
			parseMathEval(str, src, st, Flags.RETURN_FLOAT);
			break;
		case Flags.TYPE_STRING:
			parseString(str, src, st);
			break;
		case Flags.TYPE_BOOL:
			parseBoolean(str, src, st);
		}

		buff.put(Bytecodes.END_CMD); // end var assign
		return end;
	}

	private static final char REF = '&', ESCAPE = '\\';

	private void parseString(String str, String src, int pos) throws ScriptCompilationException {
		if(str.contains(Keyword.STR_MARK.sym)) {
			if(!str.startsWith(Keyword.STR_MARK.sym) || !str.endsWith(Keyword.STR_MARK.sym))
				throw(new ScriptCompilationException("mismatched string expression", src, pos));

			buff.put(Bytecodes.READ_STR); // read string command
			StringBuilder s = new StringBuilder(str.substring(1, str.length() - 1));
			StringBuilder sbuff = new StringBuilder();
			boolean invar = false;
			int last = 0;
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if((Character.isWhitespace(c) || i == s.length() - 1) && invar) {
					if(i == s.length() - 1)
						sbuff.append(c);

					String var = sbuff.toString();
					buff.put(Bytecodes.STR_VAR); // init mid-string variable reference
					putVarRef(var, buff, src, pos); // standard ref-var command
					s.delete(last, last + sbuff.length() + 1);
					i -= sbuff.length();
					buff.putInt(i - 1); // position to insert the referenced variable value
					clear(sbuff);
					invar = false;
				} else if(invar) {
					sbuff.append(c);
				}

				if(c == REF && s.charAt(i - 1) == ESCAPE) {
					s.deleteCharAt(i - 1);
					i--;
				} else if(c == REF) {
					invar = true;
					last = i;
				}
			}
			buff.put(Bytecodes.STR_START); // actual string start read
			byte[] sbytes = s.toString().getBytes();
			buff.putInt(sbytes.length); // number of bytes to read for string
			for(byte b:sbytes)
				buff.put(b);
		} else {
			if(str.contains(Keyword.ARG_BEGIN.sym) && str.contains(Keyword.ARG_END.sym)) {
				int argPos = 0;
				String funcName = str.substring(0, (argPos=str.indexOf(Keyword.ARG_BEGIN.sym)));
				Function f = functions.get(funcName);
				if(f == null)
					throw(new ScriptCompilationException("unrecongized function: " + funcName, src, pos));
				parseFunctionInvocation(f, src, pos + argPos);
			} else
				putVarRef(str, buff, src, pos);
		}
		buff.put(Bytecodes.END_CMD); // end READ_STR
	}

	private void parseMathEval(String str, String src, int pos, int returnFlag) throws ScriptCompilationException {
		buff.put(Bytecodes.EVAL);
		String exp = parser.shuntingYard(str);
		System.out.println(exp);
		String[] pts = exp.split(MathParser.SEP);
		for(int i = 1; i < pts.length; i++) {
			String s = pts[i];
			if(Keyword.isOperator(s)) {
				Keyword k = Keyword.getFromSymbol(s);
				if(k.getReturnType() != Flags.RETURN_MATCH_ARG && k.getReturnType() != returnFlag)
					throw(new ScriptCompilationException("incompatible operator return type for: " + s, src, pos));
				buff.put(Bytecodes.READ_OP);
				switch(k) {
				case ADD:
					buff.put(Bytecodes.ADD);
					break;
				case SUBTRACT:
					buff.put(Bytecodes.SUBTRACT);
					break;
				case MULTIPLY:
					buff.put(Bytecodes.MULTIPLY);
					break;
				case DIVIDE:
					buff.put(Bytecodes.DIVIDE);
					break;
				case POW:
					buff.put(Bytecodes.POW);
					break;
				default:
					throw(new ScriptCompilationException("unrecognized mathematical operator: " + s, src, pos));
				}
			} else if(isNumber(s)) {
				if(s.contains(".")) {
					if(returnFlag == Flags.RETURN_INT)
						throw(new ScriptCompilationException("type int cannot be assigned to floating point value", src, pos));
					float f;
					try {
						f = Float.parseFloat(s);
					} catch (NumberFormatException e) {
						throw(new ScriptCompilationException("number formatting error: " + s, src, pos));
					}
					buff.put(Bytecodes.READ_FLOAT);
					buff.putFloat(f);
				} else {
					int a;
					try {
						a = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						throw(new ScriptCompilationException("number formatting error: " + s, src, pos));
					}
					buff.put(Bytecodes.READ_INT);
					buff.putInt(a);
				}
			} else {
				Variable v = stackVars.get(s);
				if(v != null && v.varType != Flags.TYPE_INT && v.varType != Flags.TYPE_FLOAT)
					throw(new ScriptCompilationException("found non-numeric variable reference in math expression: " + s, src, pos));
				putVarRef(s, buff, src, pos);
			}
		}

		buff.put(Bytecodes.END_CMD); // end EVAL
	}

	private void parseBoolean(String str, String src, int pos) throws ScriptCompilationException {
		char[] chars = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		LinkedList<String> stack = new LinkedList<String>();
		for(int i=0;i < chars.length; i++) {
			char c = chars[i];
			String s = String.valueOf(c);
			if(Character.isWhitespace(c) && sb.length() == 0)
				continue;

			if(s.equals(Keyword.ARG_BEGIN.sym) && sb.length() != 0) {
				Function f = functions.get(sb.toString());
				if(f == null)
					throw(new ScriptCompilationException("unrecognized function: " + sb.toString(), src, pos + i));
				if(f.getReturnType() != Keyword.BOOL)
					throw(new ScriptCompilationException("function must return type bool", src, pos + i));
				i = parseFunctionInvocation(f, src, pos + i) - pos;
				clear(sb);
			} else if(s.equals(Keyword.ARG_BEGIN.sym)) {
				if(sb.length() != 0)
					throw(new ScriptCompilationException("expected operator before delimeter", src, pos + i));
				stack.push(Keyword.ARG_BEGIN.sym);
			} else if(Keyword.isOperator(s)) {
				if(sb.length() == 0)
					throw(new ScriptCompilationException("found operator with no operand: " + s, src, pos + i));
				putVarRef(sb.toString(), buff, src, pos + i, Flags.TYPE_BOOL);
				clear(sb);
				if(stack.peek() != null && !stack.peek().equals(Keyword.ARG_BEGIN.sym)) {
					Keyword op = Keyword.getFromSymbol(stack.pop());
					if(op == Keyword.OR)
						buff.put(Bytecodes.OR);
					else if(op == Keyword.AND)
						buff.put(Bytecodes.AND);
				} else {
					Keyword op = Keyword.getFromSymbol(s);
					if(op == null || (op != Keyword.AND && op != Keyword.OR))
						throw(new ScriptCompilationException("unrecognized boolean operator: " + s, src, pos + i));
					stack.push(s);
				}
			} else if(!Character.isWhitespace(c))
				sb.append(c);

			boolean flushed = false;
			if(sb.toString().equals(Keyword.TRUE.sym)) {
				buff.put(Bytecodes.TRUE);
				clear(sb);
				flushed = true;
			} else if(sb.toString().equals(Keyword.FALSE.sym)) {
				buff.put(Bytecodes.FALSE);
				clear(sb);
				flushed = true;
			}

			if(s.equals(Keyword.ARG_END.sym) || i == chars.length - 1) {
				if(sb.length() == 0 && !flushed)
					throw(new ScriptCompilationException("reached end of statement or block with " +
							"mismatched operators: " + s, src, pos + i));
				else if(!flushed) {
					putVarRef(sb.toString(), buff, src, pos + i, Flags.TYPE_BOOL);
					clear(sb);
				}

				String n = null;
				while(stack.size() > 0 && !n.equals(Keyword.ARG_BEGIN)) {
					n = stack.pop();
					Keyword op = Keyword.getFromSymbol(n);
					if(op == Keyword.OR)
						buff.put(Bytecodes.OR);
					else if(op == Keyword.AND)
						buff.put(Bytecodes.AND);
				}
			}
		}
	}

	private int parseFunctionInvocation(Function f, String src, int pos) throws ScriptCompilationException {
		if(f.isJavaFunction())
			buff.put(Bytecodes.INVOKE_JAVA_FUNC);
		else
			buff.put(Bytecodes.INVOKE_FUNC);
		buff.putLong(f.getID());  // INVOKE CMD -> followed by long type ID -> arguments
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.ARG_BEGIN.sym))
			throw(new ScriptCompilationException("function invocation - expected argument delimeter: " + src.charAt(pos), src, pos));
		String s = src.substring(pos + 1, src.indexOf(Keyword.ARG_END.sym, pos)).trim();
		String[] params = s.split(",");
		if(params[0].isEmpty())
			params = new String[0];
		Function[] matching = functions.getAll(f.getName());
		for(int i=0;i<matching.length;i++) {
			Keyword[] ptypes = f.getParamTypes();
			f = matching[i];

			boolean validCount = true;
			if(params.length != f.getParamCount())
				validCount = false;
			if(!validCount && i == matching.length - 1)
				throw(new ScriptCompilationException("arguments do not match parameters for function " + f, src, pos));
			else if(!validCount)
				continue;

			for(int ii=0;ii<ptypes.length;ii++) {
				String pt = params[ii];
				try {
					if(ptypes[ii] == Keyword.BOOL)
						parseBoolean(pt, src, pos);
					else if(ptypes[ii] == Keyword.INT)
						parseMathEval(pt, src, pos, Flags.TYPE_INT);
					else if(ptypes[ii] == Keyword.FLOAT)
						parseMathEval(pt, src, pos, Flags.TYPE_FLOAT);
					else if(ptypes[ii] == Keyword.STRING)
						parseString(pt, src, pos);
				} catch(ScriptCompilationException e) {
					if(i == matching.length - 1) {
						System.err.println("error in parsing arguments expected for function " + f + ":");
						throw(e);
					}
					break;
				}
			}
		}
		buff.put(Bytecodes.END_CMD);
		return src.indexOf(Keyword.ARG_END.sym, pos) + 1;
	}

	private Variable putVarRef(String s, ByteBuffer buff, String src, int pos) throws ScriptCompilationException {
		Variable var = stackVars.get(s);
		if(var == null)
			throw(new ScriptCompilationException("unrecognized variable: " + s, src, pos));
		buff.put(Bytecodes.REF_VAR);
		buff.putInt(var.getID());
		return var;
	}

	private Variable putVarRef(String s, ByteBuffer buff, String src, int pos, int type) throws ScriptCompilationException {
		Variable var = stackVars.get(s);
		if(var == null)
			throw(new ScriptCompilationException("unrecognized variable: " + s, src, pos));
		if(var.varType != type)
			throw(new ScriptCompilationException("illegal variable type for '"+s+"'", src, pos));
		buff.put(Bytecodes.REF_VAR);
		buff.putInt(var.getID());
		return var;
	}

	private boolean isDigitValid(int len, boolean shouldPermit) {
		return len > 0 && shouldPermit;
	}

	private boolean isNumber(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	private int getVarTypeFromKeyword(Keyword keyw) {
		int varType = 0;
		switch(keyw) {
		case INT:
			varType = Flags.TYPE_INT;
			break;
		case FLOAT:
			varType = Flags.TYPE_FLOAT;
			break;
		case STRING:
			varType = Flags.TYPE_STRING;
			break;
		case BOOL:
			varType = Flags.TYPE_BOOL;
		default:
			break;
		}
		return varType;
	}

	private void clear(StringBuilder sb) {
		sb.delete(0, sb.length());
	}

	static volatile int globalId = Integer.MIN_VALUE;

	private class Variable {
		public String name;
		public int varType;
		private int id;

		Variable(String name, int varType) {
			this.name = name;
			this.varType = varType;
			this.id = globalId++;
		}

		public int getID() {
			return id;
		}
	}
}
