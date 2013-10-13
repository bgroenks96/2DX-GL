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

	private int findArgEnd(char[] src, int start) {
		start++;
		int offs = 0;
		for(int i = start; i < src.length; i++) {
			char c = src[i];
			if(String.valueOf(c).equals(Keyword.ARG_BEGIN.getSymbol()))
				offs++;
			else if(String.valueOf(c).equals(Keyword.ARG_END.getSymbol())) {
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
	private HashMap<String, Variable> paramVars = new HashMap<String, Variable>();
	private HashMap<String, Variable> stackVars = new HashMap<String, Variable>();

	private Function func;

	public boolean compile(Multimap<String, Function> functions) {
		this.functions = functions;
		stackVars.clear();
		boolean success = true;
		for(Function func:functions.values()) {
			if(func.isJavaFunction())
				continue;

			String src = func.getSource();
			this.func = func;
			this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);

			Keyword[] params = func.getParamTypes();
			String[] names = func.getParamNames();
			
			if(params.length == 0)
				buff.put(Bytecodes.NO_PARAMS);
			else
				buff.put(Bytecodes.INIT_PARAMS);
			for(int i=0; i < params.length; i++) {
				Variable var = new Variable(names[i], getVarTypeFromKeyword(params[i]));
				stackVars.put(names[i], var);
				paramVars.put(names[i], var);
				buff.put(Bytecodes.PARAM_VAR);
				buff.putInt(var.id);
			}

			try {
				parseMain(src, 0);
			} catch(ScriptCompilationException e) {
				System.err.println("compilation problem in function '" + func.getName() + "'");
				e.printStackTrace();
				success = false;
			}
			
			ByteBuffer finalBuff = ByteBuffer.allocateDirect(buff.position());
			buff.flip();
			finalBuff.put(buff).flip();
			func.bytecode = finalBuff;
			
			buff.clear();
			buff = null;
			
			stackVars.clear();
			paramVars.clear();
		}
		
		return success;
	}

	private ByteBuffer buff;

	private void parseMain(String src, int pos) throws ScriptCompilationException {
		char[] chars = src.toCharArray();
		buff.put(Bytecodes.NEW_STACK);
		StringBuilder strbuff = new StringBuilder();
		List<Keyword> nextDelims = new ArrayList<Keyword>();
		nextDelims.add(Keyword.ARG_BEGIN); nextDelims.add(Keyword.END);
		for(int i=pos; i < chars.length; i++) {
			char c = chars[i];
			boolean delim = false, voidEndCmd = false;
			if(Character.isWhitespace(c) && strbuff.length() == 0)
				continue;
			else if(Character.isWhitespace(c) || (delim=Keyword.isDelimiter(String.valueOf(c)))
					|| Keyword.isOperator(String.valueOf(c))) {
				if(delim && (nextDelims.size() == 0 || !nextDelims.contains(Keyword.getFromSymbol(String.valueOf(c)))))
					throw(new ScriptCompilationException("found unexpected delimeter: " + c, src, i));
				String str = strbuff.toString();
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
						i = parseConditional(src, src.indexOf(Keyword.ARG_BEGIN.sym, i));
						voidEndCmd = true;
						break;
					case FOR:
						i = parseForLoop(src, src.indexOf(Keyword.ARG_BEGIN.sym, i));
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
					case RETURN:
						i = parseReturn(src, func.getReturnType(), i);
						break;
					default:
						throw(new ScriptCompilationException("found unexpected symbol: " + str, src, i));
					}
				}

				if(!String.valueOf(chars[i]).equals(Keyword.END.sym) && !voidEndCmd)
					throw(new ScriptCompilationException("expected '" + Keyword.END.sym + "' to end statement", src, i));
				i++;

				clear(strbuff);
			} else {
				strbuff.append(c);
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
		buff.put(Bytecodes.CLEAR_STACK);
		stackVars.clear();
		stackVars.putAll(paramVars);
	}

	/*
	 * Expects the given char position to be the opening delimiter for the conditional.
	 */
	private int parseConditional(String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.ARG_BEGIN.sym))
			throw(new ScriptCompilationException("illegal conditional delimeter: " + src.charAt(pos), src, pos));
		int argEnd = findArgEnd(src.toCharArray(), pos);
		buff.put(Bytecodes.IF);
		boolean cont = true;
		int endPos = 0;

		while(cont) {
			String bool = src.substring(pos + 1, argEnd).trim();
			parseBoolean(bool, src, pos + 1);
			pos = argEnd + 1;
			buff.put(Bytecodes.END_COND); // signal end of if condition - NOT END OF BLOCK
			int bst = src.indexOf(Keyword.BLOCK_BEGIN.sym, pos);
			int end = src.indexOf(Keyword.END.sym, pos);
			String blockSrc;
			if(bst >= 0 && (bst < end || end < 0)) {
				endPos = findBlockEnd(src.toCharArray(), bst);
				blockSrc = src.substring(bst + 1, endPos);
			} else {
				endPos = src.indexOf(Keyword.END.sym, pos) + 1;
				if(endPos < 0)
					throw(new ScriptCompilationException("reached end of block without closing delimeter", src, pos));
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
			StringBuilder sb = new StringBuilder();
			cont = false;
			for(int i=endPos+1;i<chars.length;i++) {
				char c = chars[i];
				if(Character.isWhitespace(c) && sb.length() == 0)
					continue;
				else if(Character.isWhitespace(c) || Keyword.isDelimiter(String.valueOf(c))) {
					if(sb.toString().equals(Keyword.ELSE.sym)) {
						int stBlock = src.indexOf(Keyword.BLOCK_BEGIN.sym, i);
						int nend = src.indexOf(Keyword.END.sym, i);
						int argBegin = src.indexOf(Keyword.ARG_BEGIN.sym, i);
						if(argBegin < 0 || !src.substring(i, argBegin).trim().equals(Keyword.IF.sym)) {
							if(stBlock < end && stBlock > 0) {
								endPos = findBlockEnd(chars, stBlock);
							} else {
								endPos = nend + 1;
								stBlock = i;
							}

							if(endPos < 0)
								throw(new ScriptCompilationException("reached end of block without closing delimeter", src, pos));
							String elseBlock = src.substring(stBlock + 1, endPos);
							buff.put(Bytecodes.ELSE);
							prev = this.buff;
							this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);
							parseMain(elseBlock, 0);
							prev.putInt(buff.position());
							this.buff.flip();
							prev.put(buff);
							this.buff = prev;
						} else {
							String expif = src.substring(i, src.indexOf(Keyword.ARG_BEGIN.sym, i)).trim();
							if(!expif.equals(Keyword.IF.sym))
								throw(new ScriptCompilationException("illegal conditional delimeter: " + src.charAt(i), src, pos));
							pos = src.indexOf(Keyword.ARG_BEGIN.sym, i);
							buff.put(Bytecodes.ELSE_IF);
							cont = true;
						}
					}
					break;
				} else
					sb.append(c);
			}
		}
		return endPos + 1;
	}

	private int parseForLoop(String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.ARG_BEGIN.sym))
			throw(new ScriptCompilationException("illegal for loop delimiter: " + src.charAt(pos), src, pos));
		int argEnd = findArgEnd(src.toCharArray(), pos);
		String argstr = src.substring(pos + 1, argEnd);
		String[] forpts = argstr.split(Keyword.END.sym);
		if(forpts.length != 3)
			throw(new ScriptCompilationException("invalid for loop arguments", src, pos));

		buff.put(Bytecodes.FOR_VAR);
		String arg = forpts[0];
		StringBuilder sb = new StringBuilder();
		char[] chars = arg.toCharArray();
		for(int i=0;i<chars.length;i++) {
			char c = chars[i];
			if(Character.isWhitespace(c) && sb.length() == 0)
				continue;
			else if(Character.isWhitespace(c) || Keyword.getFromSymbol(String.valueOf(c)) != null) {
				Keyword keyw = Keyword.getFromSymbol(sb.toString());
				if(keyw != null)
					parseType(keyw, src, pos + sb.length() + 1);
				else {
					Variable var = stackVars.get(sb.toString());
					parseVarAssign(var.varType, sb.toString(), src, false, pos + 1);
				}
				break;	
			} else
				sb.append(c);
		}
		buff.put(Bytecodes.FOR_SEP);

		buff.put(Bytecodes.FOR_COND);
		int npos = pos + forpts[0].length() + 2;
		parseBoolean(forpts[1], src, npos);
		buff.put(Bytecodes.FOR_SEP);

		buff.put(Bytecodes.FOR_OP);
		String varop = forpts[2].trim();
		chars = varop.toCharArray();
		sb = new StringBuilder();
		for(int i=0;i<chars.length - 1;i++) {
			char c = chars[i];
			String nx = String.valueOf(c) + chars[i+1];
			Keyword kw = Keyword.getFromSymbol(nx);
			if(kw != null && isForOperator(kw)) {
				boolean code = true;
				switch(kw) {
				case INCREM:
					buff.put(Bytecodes.INCREM);
					code = false;
				case DECREM:
					if(code)
						buff.put(Bytecodes.DECREM);
					parseExpression(sb.toString(), src, npos + forpts[1].length(), Flags.TYPE_FLOAT);
					break;
				case ADD_INCREM:
					buff.put(Bytecodes.ADD_INC);
					code = false;
				case MINUS_DECREM:
					if(code)
						buff.put(Bytecodes.MINUS_INC);
					npos = npos + forpts[1].length() + 1;
					parseExpression(sb.toString(), src, npos, Flags.TYPE_FLOAT);
					System.out.println(varop.substring(varop.indexOf(nx) + 2));
					parseExpression(varop.substring(varop.indexOf(nx) + 2), src, npos + sb.length() + 3, Flags.TYPE_FLOAT);
				}
			} else
				sb.append(c);
		}
		buff.put(Bytecodes.FOR_START);
		int stblock = src.indexOf(Keyword.BLOCK_BEGIN.sym, argEnd);
		int nend = src.indexOf(Keyword.END.sym, argEnd);
		int endPos = -1;
		if(stblock < 0 || nend < stblock && nend >= 0)
			endPos = nend;
		else
			endPos = findBlockEnd(src.toCharArray(), stblock);
		buff.put(Bytecodes.END_CMD);
		return endPos + 1;
	}

	private int parseType(Keyword keyw, String src, int pos) throws ScriptCompilationException {
		int varType = getVarTypeFromKeyword(keyw);
		String name = src.substring(pos, src.indexOf(Keyword.ASSIGN.getSymbol(), pos));
		name = name.trim();
		if(name.split("\\s+").length > 1)
			throw(new ScriptCompilationException("illegal variable name: " + name, src, pos));
		if(stackVars.containsKey(name))
			throw(new ScriptCompilationException("variable '"+name+"' is already declared in scope", src, pos));
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
			parseExpression(str, src, st, Flags.RETURN_INT);
			break;
		case Flags.TYPE_FLOAT:
			parseExpression(str, src, st, Flags.RETURN_FLOAT);
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

	private int parseReturn(String src, Keyword type, int pos) throws ScriptCompilationException {
		buff.put(Bytecodes.RETURN);
		int endPos = src.indexOf(Keyword.END.sym, pos);
		int typeflag = getVarTypeFromKeyword(type);
		String arg = src.substring(pos, endPos).trim();
		switch(type) {
		case INT:
		case FLOAT:
			parseExpression(arg, src, pos, typeflag);
			break;
		case STRING:
			parseString(arg, src, pos);
			break;
		case BOOL:
			parseBoolean(arg, src, pos);
			break;
		case VOID:
			if(arg.length() != 0)
				throw(new ScriptCompilationException("function must return void", src, pos));
			break;
		default:
			throw(new ScriptCompilationException("invalid return type identifier: " + type, src, pos));
		}
		buff.put(Bytecodes.END_CMD);
		return endPos;
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
				parseFunctionInvocation(f, src, pos + argPos + 1);
			} else
				putVarRef(str, buff, src, pos);
		}
		buff.put(Bytecodes.END_CMD); // end READ_STR
	}

	private void parseExpression(String str, String src, int pos, int returnFlag) throws ScriptCompilationException {
		buff.put(Bytecodes.EVAL);
		str = str.replaceAll("\\s+", "");
		StringBuilder nstr = new StringBuilder(str);
		StringBuilder sb = new StringBuilder();
		char[] chars = str.toCharArray();
		for(int i=0;i<chars.length;i++) {
			String s = String.valueOf(chars[i]);
			if(Keyword.isOperator(s) || isBooleanOperator(s))
				clear(sb);
			else if(s.equals(Keyword.ARG_BEGIN.sym) && sb.length() > 0) {
				nstr.insert(i - sb.length(), "{");
				nstr.insert(nstr.indexOf(Keyword.ARG_END.sym, i) + 1, "}");
			} else
				sb.append(s);
		}
		str = nstr.toString();
		String exp = parser.shuntingYard(str);
		String[] pts = exp.split(MathParser.SEP);
		boolean hasbool = false;
		for(int i = 0; i < pts.length; i++) {
			String s = pts[i].trim();
			Keyword keyw = Keyword.getFromSymbol(s); // for use later
			if(Keyword.isOperator(s) || isBooleanOperator(s)) {
				if(isBooleanOperator(s)) {
					if(s.equals(String.valueOf(MathRef.EQUALS))) // convert back to language Keyword boolean operators
						s = Keyword.EQUALS.sym;
					else if(s.equals(String.valueOf(MathRef.NOT_EQUALS)))
						s = Keyword.NOT_EQUALS.sym;
					else if(s.equals(String.valueOf(MathRef.AND_BOOL)))
						s = Keyword.AND.sym;
					else if(s.equals(String.valueOf(MathRef.OR_BOOL)))
						s = Keyword.OR.sym;
				}
				keyw = Keyword.getFromSymbol(s); // reload 'keyw' for any converted operators
				if(keyw.getReturnType() == Flags.RETURN_BOOL)
					hasbool = true;
				buff.put(Bytecodes.READ_OP);
				switch(keyw) {
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
				case EQUALS:
					buff.put(Bytecodes.EQUALS);
					break;
				case NOT_EQUALS:
					buff.put(Bytecodes.NOT_EQUALS);
					break;
				case GREATER:
					buff.put(Bytecodes.GREATER);
					break;
				case LESSER:
					buff.put(Bytecodes.LESSER);
					break;
				case OR:
					buff.put(Bytecodes.OR);
					break;
				case AND:
					buff.put(Bytecodes.AND);
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
			} else if(keyw != null) {
				if(keyw == Keyword.TRUE) {
					if(returnFlag != Flags.TYPE_BOOL)
						throw(new ScriptCompilationException("found incompatible boolean type: " + s, src, pos));
					buff.put(Bytecodes.TRUE);
					hasbool = true;
				} else if(keyw == Keyword.FALSE) {
					if(returnFlag != Flags.TYPE_BOOL)
						throw(new ScriptCompilationException("found incompatible boolean type: " + s, src, pos));
					buff.put(Bytecodes.FALSE);
					hasbool = true;
				}
			} else if(s.startsWith(strval(MathParser.BRACE_OPEN)) && s.endsWith(strval(MathParser.BRACE_CLOSE))) {
				String func = s.substring(1, s.length() - 1);
				int argStart = func.indexOf(Keyword.ARG_BEGIN.sym);
				String fname = func.substring(0, argStart);
				Function f = functions.get(fname);
				if(f.getReturnType().equals(Keyword.BOOL))
					hasbool = true;
				parseFunctionInvocation(f, src, pos + i + argStart + 1);
			} else {
				Variable v = stackVars.get(s);
				if(v != null && v.varType != Flags.TYPE_INT && v.varType != Flags.TYPE_FLOAT && v.varType != returnFlag)
					throw(new ScriptCompilationException("variable type does not match expression: " + s, src, pos));
				if(v != null && v.varType == Flags.TYPE_BOOL)
					hasbool = true;
				putVarRef(s, buff, src, pos);
			}
		}

		if(returnFlag == Flags.TYPE_BOOL && !hasbool)
			throw(new ScriptCompilationException("expression does not match boolean type", src, pos));
		else if(returnFlag != Flags.TYPE_BOOL && hasbool)
			throw(new ScriptCompilationException("boolean expression does not match assigned type", src, pos));

		buff.put(Bytecodes.END_CMD); // end EVAL
	}

	private static final String TMP_CHK = "`";
	private void parseBoolean(String str, String src, int pos) throws ScriptCompilationException {
		str = str.replaceAll(Keyword.NOT_EQUALS.sym, String.valueOf(MathRef.NOT_EQUALS));
		str = str.replaceAll(Keyword.EQUALS.sym, TMP_CHK);
		if(str.contains(Keyword.ASSIGN.sym))
			throw(new ScriptCompilationException("found invalid assignment operator", src, pos + str.indexOf(Keyword.ASSIGN.sym)));
		str = str.replaceAll(TMP_CHK, String.valueOf(MathRef.EQUALS));
		str = str.replaceAll(Keyword.OR.getEscapedSym(), String.valueOf(MathRef.OR_BOOL));
		str = str.replaceAll(Keyword.AND.sym, String.valueOf(MathRef.AND_BOOL));
		parseExpression(str, src, pos, Flags.TYPE_BOOL);
	}

	private boolean isBooleanOperator(String s) {
		String and = String.valueOf(MathRef.AND_BOOL);
		String or = String.valueOf(MathRef.OR_BOOL);
		String eq = String.valueOf(MathRef.EQUALS);
		String neq = String.valueOf(MathRef.NOT_EQUALS);
		if(s.equals(and) || s.equals(or) || s.equals(eq) || s.equals(neq))
			return true;
		else
			return false;
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
						parseExpression(pt, src, pos, Flags.TYPE_INT);
					else if(ptypes[ii] == Keyword.FLOAT)
						parseExpression(pt, src, pos, Flags.TYPE_FLOAT);
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

	private boolean isForOperator(Keyword k) {
		switch(k) {
		case INCREM:
		case DECREM:
		case ADD_INCREM:
		case MINUS_DECREM:
			return true;
		default:
			return false;
		}
	}

	private void clear(StringBuilder sb) {
		sb.delete(0, sb.length());
	}

	private String strval(char c) {
		return String.valueOf(c);
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
