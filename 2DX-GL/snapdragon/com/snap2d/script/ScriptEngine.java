/*
 *  Copyright � 2012-2013 Brian Groenke
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
import java.math.*;
import java.nio.*;
import java.util.*;

import com.snap2d.script.lib.*;

/**
 * Class responsible for interpreting and executing script function bytecode.
 * @author Brian Groenke
 *
 */
class ScriptEngine {

	HashMap<Long, Function> funcMap = new HashMap<Long, Function>();
	HashMap<Function, Object> javaObjs = new HashMap<Function, Object>();
	VarStore vars = new VarStore();
	
	boolean useDouble;

	/**
	 * Creates a new ScriptEngine with the given compiled Functions
	 * @param functions the array of Functions returned and fully compiled by ScriptCompiler
	 * @param useDouble true if VarStore should use double precision values for storage, false
	 *             if floating point should be used instead.
	 * @throws ScriptInvocationException if VarStore functions cannot be attached to the local object
	 */
	ScriptEngine(Function[] functions, boolean useDouble) throws ScriptInvocationException {
		vars.setUseDouble(useDouble);
		this.useDouble = useDouble;
		List<Method> varFuncs = Arrays.asList(VarStore.class.getMethods());

		for(Function f:functions) {
			funcMap.put(f.getID(), f);

			if(varFuncs.contains(f.getJavaMethod())) // attach VarStore object to linked methods
				attachObjectToFunction(f.getID(), vars);
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

	// >>>>>> SCRIPT EXECUTION ENGINE >>>>>> //

	/*
	 * As a development note, all methods that interpret and execute bytecode
	 * commands should begin with the phrase "exec" followed by the specific action
	 * i.e. "execExampleTask"
	 */

	Object ret;
	ByteBuffer buff;
	Function curr;
	LinkedList<VarStack> stacks;
	MathParser math = new MathParser();
	
	private boolean inLoop = false;

	private void putVar(int id, int type, Object value) {
		Variable prev = fetchVar(id);
		if(prev != null) {
			prev.value = value;
		} else
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
	
	private Object invokeJavaFunction(Function f, Object javaObj, Object... args) throws ScriptInvocationException {
		if(!f.isJavaFunction())
			throw(new ScriptInvocationException("cannot invoke Java execution on a script function", f));
		Method m = f.getJavaMethod();
		try {
			Object ret = m.invoke(javaObj, args);
			ret = checkFuncReturnValue(ret, f.getReturnType());
			return ret;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * this method is exempt from the 'exec<Operation>' name convention despite its evaluation of bytecode because
	 * it's a sibling method of invokeJavaFunction.  Both simply serve to provide the final means of executing the called function code.
	 */
	private Object invokeFunction(Function f, Object... args) throws ScriptInvocationException {
		buff = f.bytecode;
		curr = f;
		ret = null;
		stacks = new LinkedList<VarStack>();
		stacks.add(new VarStack());

		byte init = buff.get();
		switch(init) {
		case INIT_PARAMS:
			for(int i=0;i<f.getParamCount();i++) {
				if(buff.get() != Bytecodes.PARAM_VAR)
					throw(new ScriptInvocationException("found unexpected bytecode instruction: " + Integer.toHexString(init), f));
				int id = buff.getInt();
				putVar(id, Keyword.typeKeyToFlag(f.getParamTypes()[i]) /* Get flag value for type */, args[i]);
			}
		case NO_PARAMS:
			execMain(buff.position());
			break;
		default:
			throw(new ScriptInvocationException("found unexpected bytecode instruction: " + Integer.toHexString(init), f));
		}

		buff.rewind();
		
		ret = checkFuncReturnValue(ret, f.getReturnType());

		if(f.getReturnType() == Keyword.INT)
			ret = ((Double)ret).intValue();

		return (f.getReturnType() == Keyword.VOID) ? null:ret;
	}
	
	/*
	 * if the return value is null for a non-void function (script or Java based), imply a return value
	 */
	private Object checkFuncReturnValue(Object ret, Keyword expectedType) {
		if(ret == null && expectedType != Keyword.VOID) {
			switch(expectedType) {
			case INT:
			case FLOAT:
				ret = new Double(0.0f);
				break;
			case STRING:
				ret = "";
				break;
			case BOOL:
				ret = false;
			}
		}
		
		return ret;
	}

	private int execMain(int st) throws ScriptInvocationException {
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
				execStoreVar();
				break;
			case IF:
				execConditional();
				break;
			case FOR_VAR:
				execForLoop();
				break;
			case RETURN:
				this.ret = execExpression();
			case CONTINUE:
				if(!inLoop && next == CONTINUE)
					throw(new ScriptInvocationException("found continue instruction outside of loop execution", curr));
				return Flags.RETURN;
			case BREAK:
				if(!inLoop)
					throw(new ScriptInvocationException("found break instruction outside of loop execution", curr));
				return Flags.BREAK;
			default:
				throw(new ScriptInvocationException("found unexpected bytecode instruction: " + Integer.toHexString(next), curr));
			}
		}
		
		return Flags.RETURN;
	}
	
	private void execStoreVar() throws ScriptInvocationException {
		byte next = buff.get();
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

		if((next=buff.get()) != END_CMD)
			throw(new ScriptInvocationException("expected END_CMD for STORE_VAR: found="+Integer.toHexString(next), curr));
	}

	private Object execExpression() throws ScriptInvocationException {
		byte next = buff.get();
		Object ret = null;
		switch(next) {
		case EVAL:
			ret = execEvaluation();
			break;
		case READ_STR:
			ret = execStringLiteral();
			break;
		case REF_VAR:
			Variable var = execRefVar();
			ret = var.value;
			break;
		case INVOKE_JAVA_FUNC:
			ret = execJavaCall();
			break;
		case INVOKE_FUNC:
			ret = execFuncCall();
			break;
		}

		return ret;
	}

	/*
	 * Returns Double.NaN if calculation fails.  This should, however, be irrelevant since
	 * a ScriptInvocationException will be thrown.
	 */
	private double execEvaluation() throws ScriptInvocationException {
		byte next = -1;
		StringBuilder sb = new StringBuilder();
		while((next=buff.get()) != Bytecodes.END_CMD) {
			switch(next) {
			case READ_OP:
				byte op = buff.get();
				char opchar = MathRef.matchBytecode(op);
				sb.append(opchar);
				break;
			case READ_FLOAT:
				sb.append(BigDecimal.valueOf(buff.getDouble()).toPlainString());
				break;
			case READ_INT:
				sb.append(buff.getInt());
				break;
			case TRUE:
				sb.append(1.0);
				break;
			case FALSE:
				sb.append(0.0);
				break;
			case REF_VAR:
				Variable var = execRefVar();
				if(var.type == Flags.TYPE_STRING)
					throw(new ScriptInvocationException("found type 'string' in mathematical expression", curr));
				double val = checkNumberObject(var.value);
				String strval = BigDecimal.valueOf(val).toPlainString();
				sb.append(strval);
				break;
			case INVOKE_FUNC:
				Object ret = execFuncCall();
				val = checkNumberObject(ret);
				sb.append(BigDecimal.valueOf(val).toPlainString());
				break;
			case INVOKE_JAVA_FUNC:
				ret = execJavaCall();
				val = checkNumberObject(ret);
				sb.append(BigDecimal.valueOf(val).toPlainString());
				break;
			}
			sb.append(MathParser.SEP);
		}
		sb.deleteCharAt(sb.length() - 1);
		double result = Double.NaN;
		try {
			result = math.calculate(sb.toString());
		} catch (MathParseException e) {
			ScriptInvocationException scriptError = new ScriptInvocationException("error parsing math command", curr);
			scriptError.initCause(e);
			throw(scriptError);
		}
		return result;
	}

	private String execStringLiteral() throws ScriptInvocationException {
		byte next = buff.get();
		HashMap<Integer, Variable> inVars = new HashMap<Integer, Variable>();
		while(next == Bytecodes.STR_VAR) {
			if(buff.get() != Bytecodes.REF_VAR)
				throw(new ScriptInvocationException("expected REF_VAR after STR_VAR: found=" + Integer.toHexString(next), curr));
			Variable var = execRefVar();
			int pos = buff.getInt();
			inVars.put(pos, var);
			next = buff.get();
		}

		if(next != Bytecodes.STR_START)
			throw(new ScriptInvocationException("found unexpected bytecode instruction in READ_STR: 0x" + Integer.toHexString(next), curr));
		int len = buff.getInt();
		byte[] bytes = new byte[len];
		buff.get(bytes);
		if((next=buff.get()) != Bytecodes.END_CMD)
			throw(new ScriptInvocationException("expected END_CMD for READ_STR: found="+Integer.toHexString(next), curr));
		StringBuilder s = new StringBuilder(new String(bytes));
		
		int offs = 0;
		String orig = s.toString();
		for(int i:inVars.keySet()) {
			String val = inVars.get(i).value.toString();
			
			// if the preceding character is whitespace, subtract 1 off the insert position
			// (something weird with how 'insert' handles whitespace)
			if(i != 0 && i != orig.length()) {
				if(Character.isWhitespace(orig.charAt(i)) && Character.isWhitespace(orig.charAt(i - 1)))
					i--;
			}
			s.insert(i + offs, val);
			offs += val.length();
		}
		return s.toString();
	}

	private Object execJavaCall() throws ScriptInvocationException {
		long fid = buff.getLong();
		Function f = funcMap.get(fid);
		if(!f.isJavaFunction())
			throw(new ScriptInvocationException("invalid command for non-Java function", curr));

		Object[] args = readArgs(f);

		byte next;
		if((next=buff.get()) != Bytecodes.END_CMD)
			throw(new ScriptInvocationException("expected END_CMD for INVOKE_JAVA_FUNC: found="+Integer.toHexString(next), curr));

		return invokeJavaFunction(f, javaObjs.get(f), args);
	}

	private Object execFuncCall() throws ScriptInvocationException {
		long fid = buff.getLong();
		Function f = funcMap.get(fid);
		if(f.isJavaFunction())
			throw(new ScriptInvocationException("invalid command for Java based function", curr));

		Object[] args = readArgs(f);

		byte next;
		if((next=buff.get()) != Bytecodes.END_CMD)
			throw(new ScriptInvocationException("expected END_CMD for INVOKE_FUNC: found="+Integer.toHexString(next), curr));

		Object ret = this.ret;
		ByteBuffer buff = this.buff;
		Function curr = this.curr;
		LinkedList<VarStack> stacks = this.stacks;
		Object robj = invokeFunction(f, args);
		this.ret = ret;
		this.buff = buff;
		this.curr = curr;
		this.stacks = stacks;
		return robj;
	}

	private Object[] readArgs(Function f) throws ScriptInvocationException {
		Object[] args = new Object[f.getParamCount()];
		for(int i=0;i < f.getParamCount();i++) {
			args[i] = execExpression();

			Keyword type = f.getParamTypes()[i];
			if(type == Keyword.INT)
				args[i] = ((Double)args[i]).intValue();
			else if(type == Keyword.BOOL)
				args[i] = ((Double)args[i] == 0) ? false:true;
		}
		return args;
	}

	private void execConditional() throws ScriptInvocationException {
		int iflen = buff.getInt();
		int init = buff.position();

		byte next = buff.get();
		if(next != EVAL)
			throw(new ScriptInvocationException("found unexpected bytecode instruction in IF cond: 0x" + Integer.toHexString(next), curr));
		while(true) {
			boolean cond = (execEvaluation() != 0) ? true:false;
			next = buff.get();
			if(next != END_COND)
				throw(new ScriptInvocationException("found unexpected bytecode instruction in IF cond: 0x" + Integer.toHexString(next), curr));
			int blockLen = buff.getInt();
			if(cond) {
				execMain(buff.position());
				break;
			} else {
				buff.position(buff.position() + blockLen + 1);
				next = buff.get();
				if(next == ELSE_IF)
					continue;
				else if(next == ELSE) {
					blockLen = buff.getInt();
					execMain(buff.position());
					break;
				} else if(next == END_CMD)
					return;
			}
		}

		if(buff.position() < init + iflen)
			buff.position(init + iflen);

		if((next=buff.get()) != END_CMD)
			throw(new ScriptInvocationException("expected END_CMD for IF: found=" + Integer.toHexString(next), curr));
	}

	private void execForLoop() throws ScriptInvocationException {
		// for loop variable declaration
		byte next = buff.get();
		if(next != STORE_VAR)
			throw(new ScriptInvocationException("expected loop variable evaluation: found="+Integer.toHexString(next), curr));
		execStoreVar();
		// for loop condition evaluation
		next = buff.get();
		if(next != FOR_COND)
			throw(new ScriptInvocationException("expected loop condition evaluation: found="+Integer.toHexString(next), curr));
		int cst = buff.position();
		boolean chk = ((Double) execExpression() != 0) ? true:false;  // we need to parse first to find the command's proper endpoint
		if(!chk)
			return;
		int cen = buff.position();
		ByteBuffer cond = ByteBuffer.allocate(cen - cst); // allocate a separate ByteBuffer for just the condition checking instructions
		buff.position(cst);  // reset the main buffer to the start of the condition evaluation so we can re-read the insruction set
		while(buff.position() < cen)
			cond.put(buff.get());
		cond.flip();
		// for loop iteration command
		next = buff.get();
		if(next != FOR_OP)
			throw(new ScriptInvocationException("expected loop iteration instruction: found="+Integer.toHexString(next), curr));
		next = buff.get();
		if(next != REF_VAR)
			throw(new ScriptInvocationException("expected loop variable reference instruction: found="+Integer.toHexString(next), curr));
		Variable opvar = execRefVar();
		if(opvar.type != Flags.TYPE_FLOAT && opvar.type != Flags.TYPE_INT)
			throw(new ScriptInvocationException("illegal variable type in loop reference", curr));
		next = buff.get();
		double mod = 1;
		int modOp = ADD_MOD;
		switch(next) {
		case DECREM:
			mod = -1;
		case INCREM:
			break;
		case MINUS_MOD:
			mod = -1;
		case ADD_MOD:
			mod = mod * (Double) execExpression(); // if MINUS_DEC, the result of the evaluation will be negated
			break;
		case MULT_MOD:
			mod = (Double) execExpression();
			modOp = MULT_MOD;
			break;
		case DIV_MOD:
			mod = (Double) execExpression();
			modOp = DIV_MOD;
		}
		
		next = buff.get();
		if(next != FOR_START)
			throw(new ScriptInvocationException("expected loop body declaration: found="+Integer.toHexString(next), curr));
		int st = buff.position();
		while(checkLoopCondition(cond)) {
			inLoop = true;
			int stat = execMain(st);
			if(stat == Flags.BREAK)
				break;
			
			double val = ((Number) opvar.value).doubleValue();
			if(modOp == ADD_MOD)
				val += mod;
			else if(modOp == MULT_MOD)
				val *= mod;
			else if(modOp == DIV_MOD)
				val /= mod;
			opvar.value = val;
		}
		inLoop = false;
		
		next = buff.get();
		if(next != END_CMD)
			throw(new ScriptInvocationException("expected END_CMD in loop evaluation: found="+Integer.toHexString(next), curr));
	}
	
	/*
	 * Checks the loop condition using the buffer containing the boolean expression evaluation instructions.
	 * When evaluation completes, the condition buffer is reset for the next call (ByteBuffer.rewind).
	 */
	private boolean checkLoopCondition(ByteBuffer condBuff) throws ScriptInvocationException {
		boolean cont;
		ByteBuffer sto = this.buff;
		this.buff = condBuff;
		cont = ((Double) execExpression() != 0) ? true:false;
		this.buff = sto;
		condBuff.rewind(); // reset condition bytecode buffer
		return cont;
	}
	
	private Variable execRefVar() throws ScriptInvocationException {
		int varid = buff.getInt();
		Variable var = fetchVar(varid);
		if(var == null)
			throw(new ScriptInvocationException("failed to locate var_id="+varid, curr));
		return var;
	}

	private double checkNumberObject(Object o) throws ScriptInvocationException {
		double val;
		if(o instanceof Double)
			val = (Double) o;
		else if(o instanceof Integer)
			val = ((Integer) o).doubleValue();
		else if(o instanceof Boolean)
			val = ((Boolean)o) ? 1:0;
		else
			throw(new ScriptInvocationException("function return type does not match expression", curr));
		return val;

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

			// for int variables, we have to make sure the value is stored as a true Integer,
			// not Double (which the engine returns as an evaluation result regardless).
			switch(type) {
			case Flags.TYPE_INT:
				if(value instanceof Double)
					this.value = ((Double)value).intValue();
				else
					this.value = value;
				break;
			// for float variables, we have to check whether or not to store them as a Java Float or Double.
			case Flags.TYPE_FLOAT:
				if(!useDouble) {
					this.value = ((Double)value).floatValue();
					break;
				}
			case Flags.TYPE_BOOL:
			case Flags.TYPE_STRING:
				this.value = value;
			}
		}
	}
	
	private static <T> T castVariable(Variable var, Function context, Class<T> type) {
		if(Keyword.isValidDataType(type))
			return type.cast(var.value);
		else
			return null;
	}
}
