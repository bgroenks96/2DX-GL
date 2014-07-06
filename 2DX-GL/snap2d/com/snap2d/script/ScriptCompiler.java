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

import java.nio.ByteBuffer;
import java.util.*;

import bg.x2d.utils.*;

/**
 * Parses and compiles script source code to bytecode form.
 * @author Brian Groenke
 *
 */
class ScriptCompiler {

	final MathParser parser = new MathParser();

	// ------------------ PRECOMPILER ------------------ //

	public Multimap<String, Function> precompile(ArrayList<ConstantInitializer> constList, String...scripts) throws ScriptCompilationException {
		Multimap<String, Function> fmap = new Multimap<String, Function>();
		for(String script:scripts) {
			script = removeComments(script);
			Function[] farr = parseScript(script, constList);
			for(Function f:farr) {
				fmap.put(f.getName(), f);
			}
		}
		return fmap;
	}

	private Function[] parseScript(String src, ArrayList<ConstantInitializer> clist) throws ScriptCompilationException {
		char[] chars = src.toCharArray();
		ArrayList<Function> flist = new ArrayList<Function>();
		StringBuilder buff = new StringBuilder();

		int ekey = Flags.PC_RETURN; // expected Keyword type
		int nextFlush = Flags.W_FLUSH; // next buffer flush point
		boolean allowDigits = true;
		String fname = null; Keyword rtype = null; Keyword[] params = null; String[] paramNames;
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];

			if(buff.toString().equals(Keyword.CONST.sym)) {
				ekey = Flags.PC_CONST;
				nextFlush = Flags.DELIM_FLUSH;
				clear(buff);
			}

			if(Character.isWhitespace(c) && buff.length() == 0)
				continue;
			else if(Character.isWhitespace(c) && nextFlush == Flags.W_FLUSH) {
				switch(ekey) {
				case Flags.PC_RETURN:
					String type = buff.toString();
					rtype = Keyword.getFromSymbol(type);
					if(rtype == null)
						throw(new ScriptCompilationException("unrecognized return type: " + type, src, i));
					buff.delete(0, buff.length());
					ekey = Flags.PC_FUNC;
					nextFlush = Flags.DELIM_FLUSH;
					allowDigits = true;
				}
		    // nextFlush rule exception for PC_CONST flag (single line const initializers):
		    // allow whitespace flush for non-empty buffer
			} else if(Character.isWhitespace(c) && buff.length() != 0 && ekey == Flags.PC_CONST) {
				int st = i - buff.length();
				int en = src.indexOf(Keyword.END.sym, st);
				String csrc = src.substring(st, en + 1).trim();
				ConstantInitializer cfunc = new ConstantInitializer(csrc, st);
				clist.add(cfunc);
				i = en + 1; // set new char pos
				ekey = Flags.PC_RETURN; // reset expected Keyword type
				nextFlush = Flags.W_FLUSH; // reset next buffer flush point
				clear(buff);
			} else if(Keyword.isDelimiter(String.valueOf(c)) && nextFlush == Flags.DELIM_FLUSH) {
				if(ekey == Flags.PC_FUNC) {
					if(!strval(c).equals(Keyword.PARAM_BEGIN.getSymbol()))
						throw(new ScriptCompilationException("expected " + Keyword.PARAM_BEGIN.getSymbol() +
								" in function declaration", src, i));
					fname = buff.toString();
					checkValidName(fname);
					buff.delete(0, buff.length());
					ekey = Flags.PC_RETURN;
					nextFlush = Flags.W_FLUSH;
					allowDigits = false;
					int close = src.indexOf(Keyword.PARAM_END.getSymbol(), i);
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
				} else if(ekey == Flags.PC_CONST) {
					int st = i;
					int en = findBlockEnd(chars, st);
					if(en < 0)
						throw(new ScriptCompilationException("reached end of script with unterminated constant initializer", src, i+1));
					String csrc = src.substring(st+1, en).trim();
					ConstantInitializer cfunc = new ConstantInitializer(csrc, st+1);
					clist.add(cfunc);
					i = en + 1; // set new char pos
					ekey = Flags.PC_RETURN; // reset expected Keyword type
					nextFlush = Flags.W_FLUSH; // reset next buffer flush point
				}
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
		Keyword lastType = null;
		for(int i = 0; i < pts.length; i++) {
			String s = pts[i].trim();
			String[] param = s.split("\\s+");
			boolean defineType = param.length == 2;
			if(!defineType && lastType == null)
				throw(new ScriptCompilationException("invalid parameter declaration: no type definition", src, pos + i));
			else if(defineType)
				lastType = Keyword.getFromSymbol(param[0]);
			if(lastType == null)
				throw(new ScriptCompilationException("unrecognized parameter type: " + param[0], src, pos + i));
			storeParams[i] = lastType;
			storeNames[i] = param[(defineType) ? 1:0];
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
			if(String.valueOf(c).equals(Keyword.PARAM_BEGIN.getSymbol()))
				offs++;
			else if(String.valueOf(c).equals(Keyword.PARAM_END.getSymbol())) {
				if(offs > 0)
					offs--;
				else
					return i;
			}
		}
		return -1;
	}

	private String removeComments(String src) {
		StringBuilder sb = new StringBuilder(src);
		int ind = 0;
		while(ind >= 0) {
			int lrpos = sb.indexOf(Keyword.REM_LINE.sym, ind);
			int brpos = sb.indexOf(Keyword.REM_START.sym, ind);
			if((lrpos < brpos || brpos < 0) && lrpos >= 0) {
				int newline = sb.indexOf("\n", lrpos);
				sb.delete(lrpos, newline);
				ind = lrpos;
			} else if(brpos >= 0) {
				int end = sb.indexOf(Keyword.REM_END.sym, brpos);
				sb.delete(brpos, end + Keyword.REM_END.sym.length());
			} else
				ind = -1;
		}

		return sb.toString();
	}

	// ------------------ COMPILER ------------------ //

	protected static final int INIT_BUFFER_ALLOC = 0xF00, THRESHOLD = 0x100, REALLOC = 0x400;

	private Multimap<String, Function> functions;
	private HashMap<String, Variable> paramVars = new HashMap<String, Variable>();
	private HashMap<String, Variable> stackVars = new HashMap<String, Variable>();
	private HashMap<String, Variable> constVars = new HashMap<String, Variable>();

	private Function func;

	/**
	 * Compiles the Functions in the given Multimap obtained from precompile method.  The method
	 * will return normally if successful, otherwise an exception will be thrown.
	 * @param functions
	 * @param constList a list of constant initializers to compile and initialize before function compilation
	 * @throws ScriptCompilationException if an error occurs during compilation
	 */
	public void compile(Multimap<String, Function> functions, ArrayList<ConstantInitializer> constList) throws ScriptCompilationException {
		this.functions = functions;
		stackVars.clear();

		// --- compile constant expressions --- //
		try {
			for(ConstantInitializer cfunc : constList) {
				this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);
				this.func = cfunc;
				String csrc = cfunc.getSource();
				int count = 0;
				for(int i=0; i < csrc.length(); i++)
					if(strval(csrc.charAt(i)).equals(Keyword.END.sym))
						count++;
				int pos = 0;
				for(int ii=0; ii < count; ii++) {
					int en = csrc.indexOf(Keyword.END.sym, pos);
					String dec = csrc.substring(pos, en + 1);
					int assign = dec.indexOf(Keyword.ASSIGN.sym);
					if(assign < 0)
						throw(new ScriptCompilationException("expected assignment operator '=' in constant declaration", csrc, pos));
					String left = dec.substring(0, assign).trim();
					String[] pts = left.split("\\s+");
					Keyword type = Keyword.getFromSymbol(pts[0]);
					if(pts.length < 2)
						throw(new ScriptCompilationException("expected identifier after constant type declaration", csrc, pos));
					parseVarFromType(type, true, csrc, pos + pts[0].length());

					pos = en + 1;
					for(;pos < csrc.length() && Character.isWhitespace(csrc.charAt(pos)); pos++); // skip whitespace
				}
				Variable[] consts = stackVars.values().toArray(new Variable[stackVars.size()]);
				Arrays.sort(consts, new VariableIdComparator());
				cfunc.setConstantVars(consts);
				constVars.putAll(stackVars);
				stackVars.clear();

				ByteBuffer finalBuff = ByteBuffer.allocateDirect(buff.position());
				buff.flip();
				finalBuff.put(buff).flip();
				func.bytecode = finalBuff;
				buff.clear();
				buff = null;
			}
		} catch (ScriptCompilationException e) {
			System.err.println("compilation problem in constant initializer");
			e.inFunc = func;
			throw(e);
		}

		for(Function func:functions.values()) {
			if(func.isJavaFunction())
				continue;

			String src = func.getSource();
			this.func = func;
			this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);

			stackVars.putAll(constVars); // add constants to variable stack

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
				e.inFunc = func;
				throw(e);
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
	}

	private ByteBuffer buff;

	private boolean inLoop = false;

	int parseMainStack = 0;
	private void parseMain(String src, int pos) throws ScriptCompilationException {
		parseMainStack++;
		char[] chars = src.toCharArray();
		HashMap<String, Variable> snapshot = new HashMap<String, Variable>(stackVars);
		buff.put(Bytecodes.NEW_STACK);
		StringBuilder strbuff = new StringBuilder();
		List<Keyword> nextDelims = new ArrayList<Keyword>();
		nextDelims.add(Keyword.PARAM_BEGIN); nextDelims.add(Keyword.END);
		boolean returnCall = false;
		for(int i=pos; i < chars.length; i++) {
			char c = chars[i];
			boolean delim = false, voidEndCmd = false, forceReturn = false;
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
						i = parseVarAssign(var.varType, str, src, false, false, i);
					} else {
						Function[] f = functions.getAll(str);
						if(f == null)
							throw(new ScriptCompilationException("unrecognized variable or function: " + str, src, i));
						i = parseFunctionInvocation(f, src, i)[0]; // first item in returned array is new position mark
					}
				} else {
					switch(keyw) {
					case IF:
						i = parseConditional(src, src.indexOf(Keyword.PARAM_BEGIN.sym, i));
						voidEndCmd = true;
						break;
					case FOR:
						i = parseForLoop(src, src.indexOf(Keyword.PARAM_BEGIN.sym, i));
						voidEndCmd = true;
						break;
					case INT:
						i = parseVarFromType(keyw, src, i);
						break;
					case FLOAT:
						i = parseVarFromType(keyw, src, i);
						break;
					case STRING:
						i = parseVarFromType(keyw, src, i);
						break;
					case BOOL:
						i = parseVarFromType(keyw, src, i);
						break;
					case VEC2:
						i = parseVarFromType(keyw, src, i);
						break;
					case BREAK:
						if(!inLoop)
							throw(new ScriptCompilationException("break cannot be used outside of loop", src, i));
						buff.put(Bytecodes.BREAK);
						forceReturn = true;
						break;
					case CONTINUE:
						if(!inLoop)
							throw(new ScriptCompilationException("continue cannot be used outside of loop", src, i));
						buff.put(Bytecodes.CONTINUE);
						forceReturn = true;
						break;
					case RETURN:
						i = parseReturn(src, func.getReturnType(), i);
						returnCall = true;
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

			if(forceReturn)
				break;
		}

		if(parseMainStack == 1 && func.getReturnType() != Keyword.VOID && !returnCall)
			printWarning(func, "function declares non-void return type but does not explicitly return a value");
		
		buff.put(Bytecodes.CLEAR_STACK);
		stackVars.clear();
		stackVars = snapshot;
		parseMainStack--;
	}

	/*
	 * Expects the given char position to be the opening delimiter for the conditional.
	 */
	private int parseConditional(String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.PARAM_BEGIN.sym))
			throw(new ScriptCompilationException("illegal conditional delimeter: " + src.charAt(pos), src, pos));
		buff.put(Bytecodes.IF);

		// write the whole IF statement compilation into a new buffer so we can record the total length
		// the statement takes up in bytecode
		ByteBuffer curr = buff;
		this.buff = ByteBuffer.allocate(INIT_BUFFER_ALLOC);

		boolean cont = true;
		int endPos = 0;

		while(cont) {
			int argEnd = findArgEnd(src.toCharArray(), pos);
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

			buff.put(Bytecodes.BREAK);

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
						int argBegin = src.indexOf(Keyword.PARAM_BEGIN.sym, i);
						if(argBegin < 0 || !src.substring(i, argBegin).trim().equals(Keyword.IF.sym)) {
							if(stBlock < nend && stBlock > 0) {
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

							buff.put(Bytecodes.BREAK);
						} else {
							String expif = src.substring(i, src.indexOf(Keyword.PARAM_BEGIN.sym, i)).trim();
							if(!expif.equals(Keyword.IF.sym))
								throw(new ScriptCompilationException("illegal conditional delimeter: " + src.charAt(i), src, pos));
							pos = src.indexOf(Keyword.PARAM_BEGIN.sym, i);
							buff.put(Bytecodes.ELSE_IF);
							cont = true;
						}
					}
					break;
				} else
					sb.append(c);
			}
		}

		int len = buff.position();
		curr.putInt(len);  // this is the main buffer
		buff.flip();  // prepare temp buffer for reading
		curr.put(buff);
		this.buff = curr; // re-assign primary pointer to the main buffer object

		buff.put(Bytecodes.END_CMD);
		return endPos;
	}

	private int parseForLoop(String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.PARAM_BEGIN.sym))
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
					parseVarFromType(keyw, src, pos + sb.length() + 1);
				else {
					Variable var = stackVars.get(sb.toString());
					parseVarAssign(var.varType, sb.toString(), src, false, false, pos + 1);
				}
				break;	
			} else
				sb.append(c);
		}

		buff.put(Bytecodes.FOR_COND);
		int npos = pos + forpts[0].length() + 2;
		parseBoolean(forpts[1], src, npos);

		buff.put(Bytecodes.FOR_OP);
		String varop = forpts[2].replaceAll("\\s+", "");
		chars = varop.toCharArray();
		sb = new StringBuilder();
		for(int i=0;i<chars.length - 1;i++) {
			char c = chars[i];
			String nx = String.valueOf(c) + chars[i+1];
			Keyword kw = Keyword.getFromSymbol(nx);
			if(kw != null && isForOperator(kw)) {

				putVarRef(sb.toString(), buff, src, npos + forpts[1].length(), Flags.TYPE_FLOAT | Flags.TYPE_INT);

				boolean code = true;
				switch(kw) {
				case INCREM:
					buff.put(Bytecodes.INCREM);
					code = false;
				case DECREM:
					if(code)
						buff.put(Bytecodes.DECREM);
					//parseEvaluation(sb.toString(), src, npos + forpts[1].length(), Flags.TYPE_FLOAT);
					break;
				case ADD_MOD:
					buff.put(Bytecodes.ADD_MOD);
					code = false;
				case MINUS_MOD:
					if(code) {
						buff.put(Bytecodes.MINUS_MOD);
						code = false;
					}
				case MULT_MOD:
					if(code) {
						buff.put(Bytecodes.MULTIPLY);
						code = false;
					}
				case DIV_MOD:
					if(code)
						buff.put(Bytecodes.DIVIDE);
					npos = npos + forpts[1].length() + 1;
					//System.out.println(varop.substring(varop.indexOf(nx) + 2));
					parseEvaluation(varop.substring(varop.indexOf(nx) + 2), src, npos + sb.length() + 3, Flags.TYPE_FLOAT);
					break;
				default:
					throw(new ScriptCompilationException("found unexpected symbol as for operator: " + nx, src, pos));
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
		String blockSrc = src.substring(stblock + 1, endPos);
		inLoop = true;
		parseMain(blockSrc, 0);
		inLoop = false;
		buff.put(Bytecodes.CONTINUE);

		buff.put(Bytecodes.END_CMD);
		return endPos + 1;
	}


	private int parseVarFromType(Keyword keyw, String src, int pos) throws ScriptCompilationException {
		return parseVarFromType(keyw, false, src, pos);
	}

	private int parseVarFromType(Keyword keyw, boolean constant, String src, int pos) throws ScriptCompilationException {
		int varType = getVarTypeFromKeyword(keyw);
		String name = src.substring(pos, src.indexOf(Keyword.ASSIGN.getSymbol(), pos));
		name = name.trim();
		if(name.split("\\s+").length > 1)
			throw(new ScriptCompilationException("illegal variable name: " + name, src, pos));
		if(stackVars.containsKey(name))
			throw(new ScriptCompilationException("variable '"+name+"' is already declared in scope", src, pos));
		checkValidName(name);
		return parseVarAssign(varType, name, src, true, constant, pos + name.length());
	}


	private int parseVarAssign(int varType, String name, String src, boolean alloc, boolean constant, int pos) throws ScriptCompilationException {
		if(Keyword.getFromSymbol(name) != null)
			throw(new ScriptCompilationException("variable names cannot use language keywords: " + name, src, pos));

		buff.put((constant) ? Bytecodes.STORE_CONST : Bytecodes.STORE_VAR); // STORE

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
				break;
			case Flags.TYPE_VEC2:
				buff.put(Bytecodes.ALLOC_VEC2);
			}
			Variable var = new Variable(name, varType);
			stackVars.put(name, var);
			buff.putInt(var.getID());
		} else {
			if(constant || constVars.get(name) != null)
				throw(new ScriptCompilationException("constant fields cannot be reassigned", src, pos));
			buff.put(Bytecodes.REALLOC);
			buff.putInt(stackVars.get(name).getID());
		}

		int st = src.indexOf(Keyword.ASSIGN.sym, pos) + 1;
		int end = src.indexOf(Keyword.END.sym, pos);
		if(st > end || st < 0)
			throw(new ScriptCompilationException("expected variable assignment: " + Keyword.ASSIGN.sym, src, end));
		String str = src.substring(st, end).trim(); // get the assignment statement and trim whitespace

		switch(varType) { // split up type parsing
		case Flags.TYPE_INT:
			parseEvaluation(str, src, st, Flags.TYPE_INT);
			break;
		case Flags.TYPE_FLOAT:
			parseEvaluation(str, src, st, Flags.TYPE_FLOAT);
			break;
		case Flags.TYPE_STRING:
			parseString(str, src, st);
			break;
		case Flags.TYPE_BOOL:
			parseBoolean(str, src, st);
			break;
		case Flags.TYPE_VEC2:
			parseEvaluation(str, src, st, Flags.TYPE_VEC2);
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
		case VEC2:
		case INT:
		case FLOAT:
			parseEvaluation(arg, src, pos, typeflag);
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

	private static final char REF = '|', ESCAPE = '\\';

	private void parseString(String str, String src, int pos) throws ScriptCompilationException {
		if(str.startsWith(Keyword.STR_MARK.sym)) {
			if(!str.endsWith(Keyword.STR_MARK.sym))
				throw(new ScriptCompilationException("mismatched string expression", src, pos));

			buff.put(Bytecodes.READ_STR); // read string command
			StringBuilder s = new StringBuilder(str.substring(1, str.length() - 1));
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if(c == REF && i != 0 && s.charAt(i - 1) == ESCAPE) {
					s.deleteCharAt(i - 1);
					i--;
				} else if(c == REF) {
					int closeInd = s.indexOf(strval(REF), i + 1);
					if(closeInd < 0)
						throw(new ScriptCompilationException("mismatched variable reference in string literal", src, pos));
					String var = s.substring(i + 1, closeInd);
					buff.put(Bytecodes.STR_VAR);
					putVarRef(var, buff, src, pos);
					s.delete(i, closeInd + 1);
					buff.putInt(i--);
				}
			}
			buff.put(Bytecodes.STR_START); // actual string start read
			byte[] sbytes = s.toString().getBytes();
			buff.putInt(sbytes.length); // number of bytes to read for string
			for(byte b:sbytes)
				buff.put(b);
			buff.put(Bytecodes.END_CMD); // end READ_STR
		} else {
			if(str.contains(Keyword.PARAM_BEGIN.sym) && str.contains(Keyword.PARAM_END.sym)) {
				int argPos = 0;
				String funcName = str.substring(0, (argPos=str.indexOf(Keyword.PARAM_BEGIN.sym)));
				Function[] f = functions.getAll(funcName);
				if(f == null)
					throw(new ScriptCompilationException("unrecongized function: " + funcName, src, pos));
				int[] fdata = parseFunctionInvocation(f, Flags.TYPE_STRING, src, pos + argPos + 1);
				int ind = fdata[1];  // get index of Function in f array
				System.err.println(f[ind].getReturnType().sym);
				if(f[ind].getReturnType() != Keyword.STRING)
					throw(new ScriptCompilationException("function in string expression must return type string", src, pos));
			} else {
				Variable ref = stackVars.get(str);
				if(ref == null)
					throw(new ScriptCompilationException("unrecognized variable: " + str, src, pos));
				if(ref.varType == Flags.TYPE_STRING)
					putVarRef(str, buff, src, pos);
				else
					throw(new ScriptCompilationException("cannot use a non-string variable as string expression", src, pos));
			}
		}
	}


	/**
	 * @param str
	 * @param src
	 * @param pos
	 * @param returnFlag flag for return type - should be any TYPE_[vartype] flags OR RETURN_FLOAT_STRICT
	 *     for disallowing int upcasting
	 * @throws ScriptCompilationException
	 */
	private void parseEvaluation(String str, String src, int pos, int returnFlag) throws ScriptCompilationException {
		buff.put(Bytecodes.EVAL);
		str = str.replaceAll("\\s+", "");
		if(str.startsWith(Keyword.STR_MARK.sym))
			throw(new ScriptCompilationException("found syntax invalid for numerical types: " + Keyword.STR_MARK.sym));
		StringBuilder nstr = new StringBuilder(str);
		StringBuilder sb = new StringBuilder();

		TreeMap<Integer, Boolean> marks = new TreeMap<Integer, Boolean>();
		char[] chars = str.toCharArray();
		for(int i=0;i<chars.length;i++) {
			String s = String.valueOf(chars[i]);
			if(Keyword.isOperator(s) || isBooleanOperator(s))
				clear(sb);
			else if(s.equals(Keyword.PARAM_BEGIN.sym) && sb.length() > 0) {
				marks.put(i - sb.length(), true);
				marks.put(findParamEnd(str, i) + 1, false);
				clear(sb);
				//nstr.insert(i - sb.length(), "{");
				//nstr.insert(findParamEnd(nstr.toString(), i + 1) + 1, "}");
			} else
				sb.append(s);
		}

		Integer[] keys = marks.keySet().toArray(new Integer[marks.size()]);
		keys = Utils.flipArray(keys);
		for(int i:keys) {
			if(marks.get(i) && !strval(nstr.charAt(i)).equals(Keyword.BLOCK_BEGIN.sym))
				nstr.insert(i, Keyword.BLOCK_BEGIN.sym);
			else if (!marks.get(i) && !strval(nstr.charAt((i==nstr.length()) ? i-1:i)).equals(Keyword.BLOCK_END.sym))
				nstr.insert(i, Keyword.BLOCK_END.sym);
		}

		parser.format(nstr);
		str = nstr.toString();
		String exp = parser.shuntingYard(str);
		String[] pts = exp.split(MathParser.SEP);
		boolean hasbool = false;
		for(int i = 0; i < pts.length; i++) {
			String s = pts[i];
			Keyword keyw = Keyword.getFromSymbol(s); // for use later
			if(Keyword.isOperator(s) || isBooleanOperator(s)) {
				if(isBooleanOperator(s)) {
					if(s.equals(String.valueOf(MathRef.EQUALS))) // convert back to language Keyword boolean operators
						s = Keyword.EQUALS.sym;
					else if(s.equals(strval(MathRef.NOT_EQUALS)))
						s = Keyword.NOT_EQUALS.sym;
					else if(s.equals(strval(MathRef.AND_BOOL)))
						s = Keyword.AND.sym;
					else if(s.equals(strval(MathRef.OR_BOOL)))
						s = Keyword.OR.sym;
					else if(s.equals(strval(MathRef.LESS_EQUALS)))
						s = Keyword.LESS_EQUALS.sym;
					else if(s.equals(strval(MathRef.GREAT_EQUALS)))
						s = Keyword.GREAT_EQUALS.sym;
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
				case BITOR:
					buff.put(Bytecodes.BITOR);
					break;
				case BITAND:
					buff.put(Bytecodes.BITAND);
					break;
				case BITXOR:
					buff.put(Bytecodes.BITXOR);
					break;
				case MODULO:
					buff.put(Bytecodes.MODULO);
					break;
				case EQUALS:
					buff.put(Bytecodes.EQUALS);
					hasbool=true;
					break;
				case NOT_EQUALS:
					buff.put(Bytecodes.NOT_EQUALS);
					hasbool=true;
					break;
				case GREATER:
					buff.put(Bytecodes.GREATER);
					hasbool=true;
					break;
				case LESSER:
					buff.put(Bytecodes.LESSER);
					hasbool=true;
					break;
				case LESS_EQUALS:
					buff.put(Bytecodes.LESS_EQUALS);
					hasbool=true;
					break;
				case GREAT_EQUALS:
					buff.put(Bytecodes.GREAT_EQUALS);
					hasbool=true;
					break;
				case OR:
					buff.put(Bytecodes.OR);
					hasbool=true;
					break;
				case AND:
					buff.put(Bytecodes.AND);
					hasbool=true;
					break;
				default:
					throw(new ScriptCompilationException("unrecognized mathematical operator: " + s, src, pos));
				}
			} else if(isNumber(s)) {
				if(s.contains(".")) {
					if(returnFlag == Flags.TYPE_INT)
						throw(new ScriptCompilationException("type int cannot be assigned to floating point value", src, pos));
					double d;
					try {
						d = Double.parseDouble(s);
					} catch (NumberFormatException e) {
						throw(new ScriptCompilationException("number formatting error: " + s, src, pos));
					}
					buff.put(Bytecodes.READ_FLOAT);
					buff.putDouble(d);
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
			} else if(isVector(s)) {
				String[] vecPts = s.substring(1, s.length() - 1).split(",");
				buff.put(Bytecodes.READ_VEC2);
				parseEvaluation(vecPts[0], src, pos, returnFlag);
				parseEvaluation(vecPts[1], src, pos, returnFlag);
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
				int argStart = func.indexOf(Keyword.PARAM_BEGIN.sym);
				String fname = func.substring(0, argStart);
				Function[] funcs = functions.getAll(fname);
				if(funcs == null || funcs.length == 0)
					throw(new ScriptCompilationException("function '"+fname+"' is undefined", src, pos));
				int mpos = parseFunctionInvocation(funcs, func, argStart)[1]; // second position in array is matched function
				Function matched = funcs[mpos];
				Keyword rtype = matched.getReturnType();
				int rtypeFlag = Keyword.typeKeyToFlag(rtype);
				if(Keyword.typeKeyToFlag(rtype) != returnFlag && !isTypeCompatible(returnFlag, rtypeFlag))
					throw(new ScriptCompilationException("function return type does not match expression", src, pos));
				if(rtype == Keyword.BOOL)
					hasbool = true;
				
			} else {
				Variable v = stackVars.get(s);
				if(v != null && v.varType != Flags.TYPE_INT && v.varType != Flags.TYPE_FLOAT && v.varType != returnFlag)
					throw(new ScriptCompilationException("variable type does not match expression: " + s, src, pos));
				if(v != null && returnFlag == Flags.TYPE_INT && v.varType == Flags.TYPE_FLOAT)
					throw(new ScriptCompilationException("float variable does not match expected integer expression: " + s, src, pos));
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

		// replace all two characters or special boolean operators with internal stand-ins that work with the bounds of the parser
		str = str.replaceAll(Keyword.NOT_EQUALS.sym, String.valueOf(MathRef.NOT_EQUALS));
		str = str.replaceAll(Keyword.OR.getEscapedSym(), String.valueOf(MathRef.OR_BOOL));
		str = str.replaceAll(Keyword.AND.sym, String.valueOf(MathRef.AND_BOOL));
		str = str.replaceAll(Keyword.LESS_EQUALS.sym, String.valueOf(MathRef.LESS_EQUALS));
		str = str.replaceAll(Keyword.GREAT_EQUALS.sym, String.valueOf(MathRef.GREAT_EQUALS));
		str = str.replaceAll(Keyword.EQUALS.sym, TMP_CHK);
		if(str.contains(Keyword.ASSIGN.sym))
			throw(new ScriptCompilationException("found invalid assignment operator", src, pos + str.indexOf(Keyword.ASSIGN.sym)));
		str = str.replaceAll(TMP_CHK, String.valueOf(MathRef.EQUALS));
		parseEvaluation(str, src, pos, Flags.TYPE_BOOL);
	}
	
	// valid boolean operator replacements from MathRef class
	private final String[] validOps = new String[] {strval(MathRef.AND_BOOL), strval(MathRef.OR_BOOL), strval(MathRef.EQUALS),
			strval(MathRef.NOT_EQUALS), strval(MathRef.LESS_EQUALS), strval(MathRef.LESS_EQUALS), strval(MathRef.GREAT_EQUALS)};
	// ------------------------------------------------------

	private boolean isBooleanOperator(String s) {
		Arrays.sort(validOps);
		if(Arrays.binarySearch(validOps, s) >= 0)
			return true;
		else
			return false;
	}


	private int[] parseFunctionInvocation(Function[] matching, String src, int pos) throws ScriptCompilationException {
		return parseFunctionInvocation(matching, 0, src, pos);
	}

	/*
	 * returns int[] (UGH!) so new src position marker AND the matching function's position in the given array can be returned.
	 * Curse you Java for not having multiple return value support! </3
	 */
	// INVOKE cmd -> long type id -> arguments
	private int[] parseFunctionInvocation(Function[] matching, int type, String src, int pos) throws ScriptCompilationException {
		if(!String.valueOf(src.charAt(pos)).equals(Keyword.PARAM_BEGIN.sym))
			throw(new ScriptCompilationException("function invocation - expected argument delimeter: " + src.charAt(pos), src, pos));
		String s = src.substring(pos + 1, findParamEnd(src, pos)).trim();
		String[] params = splitArgs(s, Keyword.SEPARATOR.sym);
		if(params[0].isEmpty())
			params = new String[0];

		Function f = null;
		int fpos = -1;

		Function[] origArr = matching;
		matching = Arrays.copyOf(matching, matching.length);
		Arrays.sort(matching);
		funcLoop:
			for(int i=0;i<matching.length;i++) {
				f = matching[i];
				Keyword[] ptypes = f.getParamTypes();

				boolean validCount = true;
				if(params.length != f.getParamCount())
					validCount = false;
				if(!validCount && i == matching.length - 1)
					throw(new ScriptCompilationException("arguments do not match parameters for function " + f +" " + params.length, src, pos));
				else if(!validCount)
					continue;

				ByteBuffer temp = ByteBuffer.allocate(INIT_BUFFER_ALLOC);
				ByteBuffer curr = buff;
				buff = temp;
				for(int ii=0;ii<ptypes.length;ii++) {
					String pt = params[ii].trim();
					try {
						if(ptypes[ii] == Keyword.BOOL)
							parseBoolean(pt, src, pos);
						else if(ptypes[ii] == Keyword.INT)
							parseEvaluation(pt, src, pos, Flags.TYPE_INT);
						else if(ptypes[ii] == Keyword.FLOAT)
							parseEvaluation(pt, src, pos, Flags.TYPE_FLOAT);
						else if(ptypes[ii] == Keyword.VEC2) {
							parseEvaluation(pt, src, pos, Flags.TYPE_VEC2);
						} else if(ptypes[ii] == Keyword.STRING)
							parseString(pt, src, pos);
					} catch(ScriptCompilationException e) {
						if(i == matching.length - 1) {
							System.out.println("error in parsing arguments expected for function " + f + ":");
							throw(e);
							//throw(new ScriptCompilationException("arguments do not match function parameters for "+
							//		f, src, pos));
						}
						buff = curr;
						continue funcLoop;
					}
				}

				int rtype = getVarTypeFromKeyword(f.getReturnType());
				if(type != 0 && rtype != type && (rtype != Flags.TYPE_INT || type != Flags.TYPE_FLOAT)) /* exception for returning int to float type */
					throw(new ScriptCompilationException("function return type does not match expression", src, pos));

				if(f.isJavaFunction())
					curr.put(Bytecodes.INVOKE_JAVA_FUNC);
				else
					curr.put(Bytecodes.INVOKE_FUNC);
				curr.putLong(f.getID());
				temp.flip();
				curr.put(temp);
				buff = curr;
				break;
			}
		
		buff.put(Bytecodes.END_CMD);
		
		for(int i=0; i < origArr.length; i++)
			if(origArr[i].equals(f))
				fpos = i;
		return new int[] {findParamEnd(src, pos) + 1, fpos};
	}

	private String[] splitArgs(String argStr, String ex) {
		ArrayList<String> splitPts = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		int paramDelims = 0;
		boolean instr = false;
		for(int i=0; i < argStr.length(); i++) {
			String s = strval(argStr.charAt(i));
			if(s.equals(Keyword.PARAM_BEGIN.sym))
				paramDelims++;
			else if(s.equals(Keyword.PARAM_END.sym))
				paramDelims--;
			else if(s.equals(Keyword.STR_MARK.sym) && argStr.charAt(Math.max(0, i-1)) != '\\')
				instr = !instr;

			if(s.equals(ex) && paramDelims == 0 && !instr) {
				splitPts.add(sb.toString());
				clear(sb);
			} else {
				sb.append(s);
				if(i == argStr.length() - 1)
					splitPts.add(sb.toString().trim());
			}
		}
		if(splitPts.size() == 0)
			splitPts.add(argStr);

		return splitPts.toArray(new String[splitPts.size()]);
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
		if((type & var.varType) != var.varType)
			throw(new ScriptCompilationException("illegal variable type for '"+s+"'", src, pos));
		buff.put(Bytecodes.REF_VAR);
		buff.putInt(var.getID());
		return var;
	}

	private boolean isDigitValid(int len, boolean shouldPermit) {
		return len > 0 && shouldPermit;
	}

	private void checkValidName(String name) throws ScriptCompilationException {
		for(Keyword k : Keyword.values()) {
			if(k.sym.equals(name))
				throw(new ScriptCompilationException("illegal member string identifier: " + name + " is a reserved keyword"));
			else if(name.startsWith("consts"))
				throw(new ScriptCompilationException("illegal member string identifier: cannot start with 'consts'"));
		}
	}
	
	private boolean isTypeCompatible(int type0, int type1) {
		if(type0 == type1)
			return true;
		if(type1 == Flags.TYPE_INT && type0 == Flags.TYPE_FLOAT)
			return true;
		else
			return false;
	}
	
	private boolean isNumber(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	private boolean isVector(String s) {
		if(s.startsWith("[") && s.endsWith("]") && s.split(",").length == 2)
			return true;
		else
			return false;
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
			break;
		case VEC2:
			varType = Flags.TYPE_VEC2;
		default:
			break;
		}
		return varType;
	}

	private boolean isForOperator(Keyword k) {
		switch(k) {
		case INCREM:
		case DECREM:
		case ADD_MOD:
		case MINUS_MOD:
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

	private int findParamEnd(String src, int pos) throws ScriptCompilationException {
		if(!strval(src.charAt(pos)).equals(Keyword.PARAM_BEGIN.sym))
			throw(new ScriptCompilationException("invalid starting position for end param: found="+src.charAt(pos), src, pos));
		int i, init=0, end=0;
		for(i=pos; i < src.length(); i++) {
			String s = strval(src.charAt(i));
			if(s.equals(Keyword.PARAM_BEGIN.sym))
				init++;
			else if(s.equals(Keyword.PARAM_END.sym)) {
				end++;
				if(init == end)
					return i;
			}
		}

		return -1;
	}

	private void printWarning(Function context, String msg) {
		System.err.println("[SnapScript] WARNING - function '"+context.getName()+"': " + msg);
	}

	static volatile int globalId = Integer.MIN_VALUE;

	/*
	 * Represents a compiler variable by its name, type, and internal ID.
	 */
	final class Variable {
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

		@Override
		public String toString() {
			return name + "@id="+id;
		}
	}
	
	static class VariableNameComparator implements Comparator<Variable> {

		/**
		 *
		 */
		@Override
		public int compare(Variable o1, Variable o2) {
			return o1.name.compareTo(o2.name);
		}
	}
	
	static class VariableIdComparator implements Comparator<Variable> {

		/**
		 *
		 */
		@Override
		public int compare(Variable o1, Variable o2) {
			return (int) Math.signum(o1.id - o2.id);
		}
	}
}
