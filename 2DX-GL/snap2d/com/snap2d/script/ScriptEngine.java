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

import static com.snap2d.script.Bytecodes.ADD_MOD;
import static com.snap2d.script.Bytecodes.ALLOC_BOOL;
import static com.snap2d.script.Bytecodes.ALLOC_FLOAT;
import static com.snap2d.script.Bytecodes.ALLOC_INT;
import static com.snap2d.script.Bytecodes.ALLOC_STRING;
import static com.snap2d.script.Bytecodes.ALLOC_VEC2;
import static com.snap2d.script.Bytecodes.BREAK;
import static com.snap2d.script.Bytecodes.CLEAR_STACK;
import static com.snap2d.script.Bytecodes.CONTINUE;
import static com.snap2d.script.Bytecodes.DECREM;
import static com.snap2d.script.Bytecodes.DIV_MOD;
import static com.snap2d.script.Bytecodes.ELSE;
import static com.snap2d.script.Bytecodes.ELSE_IF;
import static com.snap2d.script.Bytecodes.END_CMD;
import static com.snap2d.script.Bytecodes.END_COND;
import static com.snap2d.script.Bytecodes.EVAL;
import static com.snap2d.script.Bytecodes.FALSE;
import static com.snap2d.script.Bytecodes.FOR_COND;
import static com.snap2d.script.Bytecodes.FOR_OP;
import static com.snap2d.script.Bytecodes.FOR_START;
import static com.snap2d.script.Bytecodes.FOR_VAR;
import static com.snap2d.script.Bytecodes.IF;
import static com.snap2d.script.Bytecodes.INCREM;
import static com.snap2d.script.Bytecodes.INIT_PARAMS;
import static com.snap2d.script.Bytecodes.INVOKE_FUNC;
import static com.snap2d.script.Bytecodes.INVOKE_JAVA_FUNC;
import static com.snap2d.script.Bytecodes.MINUS_MOD;
import static com.snap2d.script.Bytecodes.MULT_MOD;
import static com.snap2d.script.Bytecodes.NEW_STACK;
import static com.snap2d.script.Bytecodes.NO_PARAMS;
import static com.snap2d.script.Bytecodes.READ_FLOAT;
import static com.snap2d.script.Bytecodes.READ_INT;
import static com.snap2d.script.Bytecodes.READ_OP;
import static com.snap2d.script.Bytecodes.READ_STR;
import static com.snap2d.script.Bytecodes.READ_VEC2;
import static com.snap2d.script.Bytecodes.REALLOC;
import static com.snap2d.script.Bytecodes.REF_VAR;
import static com.snap2d.script.Bytecodes.RETURN;
import static com.snap2d.script.Bytecodes.STORE_CONST;
import static com.snap2d.script.Bytecodes.STORE_VAR;
import static com.snap2d.script.Bytecodes.TRUE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import bg.x2d.geo.Vector2d;
import bg.x2d.utils.Multimap;
import bg.x2d.utils.Utils;

import com.snap2d.script.lib.ScriptTimer;
import com.snap2d.script.lib.VarStore;

/**
 * Class responsible for interpreting and executing script function bytecode.
 * 
 * @author Brian Groenke
 *
 */
class ScriptEngine {

    private static final Logger log = Logger.getLogger(ScriptEngine.class.getCanonicalName());

    HashMap <Long, Function> funcMap = new HashMap <Long, Function>();
    HashMap <Function, Object> javaObjs = new HashMap <Function, Object>();
    VarStore vars = new VarStore();
    ScriptTimer timers;

    boolean useDouble;

    /**
     * Creates a new ScriptEngine with the given compiled Functions
     * 
     * @param functions
     *            the array of Functions returned and fully compiled by
     *            ScriptCompiler
     * @param useDouble
     *            true if VarStore should use double precision values for
     *            storage, false if floating point should be used instead.
     * @throws ScriptInvocationException
     *             if VarStore functions cannot be attached to the local object
     */
    ScriptEngine(final ScriptProgram prog, final Function[] functions, final ConstantInitializer[] constInits,
            final boolean useDouble) throws ScriptInvocationException {

        vars.setUseDouble(useDouble);
        this.useDouble = useDouble;
        this.timers = new ScriptTimer(prog);
        List <Method> varFuncs = Arrays.asList(VarStore.class.getMethods());
        List <Method> timerFuncs = Arrays.asList(ScriptTimer.class.getMethods());

        for (Function f : functions) {
            funcMap.put(f.getID(), f);

            if (varFuncs.contains(f.getJavaMethod())) {
                // to linked methods
                attachObjectToFunction(f.getID(), vars);
            }
            if (timerFuncs.contains(f.getJavaMethod())) {
                // object to linked
                // methods
                attachObjectToFunction(f.getID(), timers);
            }
        }

        initConstantVars(constInits);
    }

    public void attachObjectToFunction(final long fid, final Object obj) throws ScriptInvocationException {

        Function f = funcMap.get(fid);
        if (f == null) {
            return;
        }
        if (!f.isJavaFunction()) {
            throw (new ScriptInvocationException("cannot attach Object to non-Java function", f));
        }
        javaObjs.put(f, obj);
    }

    public Object invoke(final long id, final Object... args) throws ScriptInvocationException {

        Function f = funcMap.get(id);
        Object ret = null;
        if (f.isJavaFunction()) {
            ret = invokeJavaFunction(f, javaObjs.get(f), args);
        } else {
            ret = invokeFunction(f, args);
        }
        return ret;
    }

    public Object fetchConstValue(final int id) {

        Variable var = consts.get(id);
        if (var == null) {
            return null;
        }
        return var.getValue();
    }

    // >>>>>> SCRIPT EXECUTION ENGINE >>>>>> //

    /*
     * As a development note, all methods that interpret and execute bytecode
     * commands should begin with the phrase "exec" followed by the specific
     * action i.e. "execExampleTask"
     */

    private static final int RELEASE_GC = 100000;

    Object ret;
    ByteBuffer buff;
    Function curr;
    LinkedList <VarStack> stacks;
    VarStack consts;

    int varGC = 0; // used to track released Variable objects
    int mathGC = 0; // used to track released MathParser objects

    private boolean inLoop = false;

    private void putVar(final int id, final int type, final Object value) throws ScriptInvocationException {

        Variable prev = fetchVar(id);
        if (prev != null) {
            prev.setValue(value); // setValue runs type verification
        } else {
            stacks.peekFirst().put(id, new Variable(id, type, value));
        }
    }

    private void putConst(final int id, final int type, final Object value) throws ScriptInvocationException {

        Variable prev = consts.get(id);
        if (prev != null) {
            throw (new ScriptInvocationException("constant already exists in immutable storage: " + prev.id, curr));
        } else {
            consts.put(id, new Variable(id, type, value));
        }
    }

    private Variable fetchVar(final int id) {

        Variable var = consts.get(id); // check in constant store
        if (var != null) {
            return var;
        }
        for (VarStack vs : stacks) { // then check standard var-stacks
            var = vs.get(id);
            if (var != null) {
                return var;
            }
        }
        return null;
    }

    private VarStack findStack(final Variable var) {

        for (VarStack vs : stacks) {
            if (vs.get(var.id) != null) {
                return vs;
            }
        }
        return null;
    }

    private void initConstantVars(final ConstantInitializer[] initArr) throws ScriptInvocationException {

        consts = new VarStack();
        for (ConstantInitializer cfunc : initArr) {
            buff = cfunc.bytecode;
            execMain(0);
        }
        buff = null;
    }

    private Object invokeJavaFunction(final Function f, final Object javaObj, final Object... args)
            throws ScriptInvocationException {

        if (!f.isJavaFunction()) {
            throw (new ScriptInvocationException("cannot invoke Java execution on a script function", f));
        }
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
            String msg = (e.getCause() != null) ? e.getCause().toString() : e.toString();
            ScriptInvocationException e1 = new ScriptInvocationException("error in Java function call: " + msg, curr);
            throw (e1);
        }
        return null;
    }

    /*
     * this method is exempt from the 'exec<Operation>' name convention despite
     * its evaluation of bytecode because it's a sibling method of
     * invokeJavaFunction. Both simply serve to provide the final means of
     * executing the called function code.
     */
    private Object invokeFunction(final Function f, final Object... args) throws ScriptInvocationException {

        buff = f.bytecode;
        curr = f;
        ret = null;
        stacks = new LinkedList <VarStack>();
        stacks.add(new VarStack());

        byte init = buff.get();
        switch (init) {
        case INIT_PARAMS:
            for (int i = 0; i < f.getParamCount(); i++ ) {
                if (buff.get() != Bytecodes.PARAM_VAR) {
                    throw (new ScriptInvocationException("found unexpected bytecode instruction: "
                            + Integer.toHexString(init), f));
                }
                int id = buff.getInt();
                putVar(id, Keyword.typeKeyToFlag(f.getParamTypes()[i]) /*
                 * Get
                 * flag
                 * value
                 * for
                 * type
                 */, args[i]);
            }
        case NO_PARAMS:
            execMain(buff.position());
            break;
        default:
            throw (new ScriptInvocationException("found unexpected bytecode instruction: " + Integer.toHexString(init),
                    f));
        }

        for (VarStack stack : stacks) {
            stack.clear();
        }
        stacks.clear();

        buff.rewind();

        ret = checkFuncReturnValue(ret, f.getReturnType());

        return (f.getReturnType() == Keyword.VOID) ? null : ret;
    }

    /*
     * if the return value is null for a non-void function (script or Java
     * based), imply a return value
     */
    private Object checkFuncReturnValue(Object ret, final Keyword expectedType) {

        if (ret == null && expectedType != Keyword.VOID) {
            switch (expectedType) {
            case INT:
            case FLOAT:
                ret = new Double(0.0f);
                break;
            case STRING:
                ret = "";
                break;
            case BOOL:
                ret = false;
            default:
                break;
            }
        }

        return ret;
    }

    private int execMain(final int st) throws ScriptInvocationException {

        buff.position(st);
        while (buff.position() < buff.capacity()) {
            byte next = buff.get();
            switch (next) {
            case NEW_STACK:
                stacks.push(new VarStack());
                break;
            case CLEAR_STACK:
                stacks.pop().clear();
                if (varGC > RELEASE_GC || mathGC > RELEASE_GC) {
                    log.fine("SnapScript: GC requested [varGC=" + varGC + " mathGC=" + mathGC + "]");
                    System.runFinalization();
                    System.gc();
                    if (varGC > RELEASE_GC) {
                        varGC = 0;
                    } else if (mathGC > RELEASE_GC) {
                        mathGC = 0;
                    }
                }
                break;
            case INVOKE_FUNC:
                execFuncCall();
                break;
            case INVOKE_JAVA_FUNC:
                execJavaCall();
                break;
            case STORE_VAR:
                execStoreVar(false);
                break;
            case STORE_CONST:
                execStoreVar(true);
                break;
            case IF:
                execConditional();
                break;
            case FOR_VAR:
                execForLoop();
                break;
            case RETURN:
                this.ret = execExpression();
                if (curr.getReturnType() == Keyword.INT && !Function.isInt(ret.getClass())) {
                    ret = ((Scalar) ret).getValue().intValue();
                } else if (curr.getReturnType() == Keyword.BOOL && !Function.isBool(ret.getClass())) {
                    ret = ( ((Double) ret).byteValue() == 1) ? true : false;
                }
            case CONTINUE:
                if (!inLoop && next == CONTINUE) {
                    throw (new ScriptInvocationException("found continue instruction outside of loop execution", curr));
                }
                return Flags.RETURN;
            case BREAK:
                // if(!inLoop)
                // throw(new
                // ScriptInvocationException("found break instruction outside of loop execution",
                // curr));
                return Flags.BREAK;
            default:
                throw (new ScriptInvocationException("found unexpected bytecode instruction: "
                        + Integer.toHexString(next), curr));
            }
        }

        return Flags.RETURN;
    }

    private void execStoreVar(final boolean constant) throws ScriptInvocationException {

        byte next = buff.get();
        int id = buff.getInt();
        Object ret = execExpression();
        switch (next) {
        case REALLOC:
            Variable currVar = fetchVar(id);
            if (constant || consts.get(id) != null) {
                throw (new ScriptInvocationException("cannot reallocate constant variable", curr));
            }
            if (currVar.type != Flags.TYPE_STRING) {
                ret = ((Operand) ret).getValue();
            }
            putVar(id, currVar.type, ret);
            break;
        case ALLOC_INT:
            ret = ((Operand) ret).getValue();
            if (constant) {
                putConst(id, Flags.TYPE_INT, ret);
            } else {
                putVar(id, Flags.TYPE_INT, ret);
            }
            break;
        case ALLOC_FLOAT:
            ret = ((Operand) ret).getValue();
            if (constant) {
                putConst(id, Flags.TYPE_FLOAT, ret);
            } else {
                putVar(id, Flags.TYPE_FLOAT, ret);
            }
            break;
        case ALLOC_BOOL:
            ret = ((Operand) ret).getValue();
            if (constant) {
                putConst(id, Flags.TYPE_BOOL, ret);
            } else {
                putVar(id, Flags.TYPE_BOOL, ret);
            }
            break;
        case ALLOC_STRING:
            if (constant) {
                putConst(id, Flags.TYPE_STRING, ret);
            } else {
                putVar(id, Flags.TYPE_STRING, ret);
            }
            break;
        case ALLOC_VEC2:
            if (constant) {
                putConst(id, Flags.TYPE_VEC2, ret);
            } else {
                putVar(id, Flags.TYPE_VEC2, ret);
            }
        }

        if ( (next = buff.get()) != END_CMD) {
            throw (new ScriptInvocationException("expected END_CMD for STORE_VAR: found=" + Integer.toHexString(next),
                    curr));
        }
    }

    private Object execExpression() throws ScriptInvocationException {

        byte next = buff.get();
        Object ret = null;
        switch (next) {
        case EVAL:
            ret = execEvaluation();
            break;
        case READ_STR:
            ret = execStringLiteral();
            break;
        case REF_VAR:
            Variable var = execRefVar();
            ret = var.getValue();
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
     * Returns Double.NaN if calculation fails. This should, however, be
     * irrelevant since a ScriptInvocationException will be thrown.
     */
    private Operand execEvaluation() throws ScriptInvocationException {

        byte next = -1;
        StringBuilder sb = new StringBuilder();
        while ( (next = buff.get()) != Bytecodes.END_CMD) {
            switch (next) {
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
            case READ_VEC2:
                double x = (Double) execEvaluation().getValue();
                double y = (Double) execEvaluation().getValue();
                sb.append("[" + x + "," + y + "]");
                break;
            case TRUE:
                sb.append(1.0);
                break;
            case FALSE:
                sb.append(0.0);
                break;
            case REF_VAR:
                Variable var = execRefVar();
                if (var.type == Flags.TYPE_STRING) {
                    throw (new ScriptInvocationException("found type 'string' in mathematical expression", curr));
                }
                double val = 0;
                if (var.type == Flags.TYPE_VEC2) {
                    Vector2d vec = (Vector2d) var.getValue();
                    sb.append("[" + vec.x + "," + vec.y + "]");
                } else {
                    val = checkNumberObject(var.getValue());
                    String strval = BigDecimal.valueOf(val).toPlainString();
                    sb.append(strval);
                }
                break;
            case INVOKE_FUNC:
                Object ret = execFuncCall();
                if (ret == null) {
                    ret = 0;
                }
                val = checkNumberObject(ret);
                sb.append(BigDecimal.valueOf(val).toPlainString());
                break;
            case INVOKE_JAVA_FUNC:
                ret = execJavaCall();
                if (ret == null) {
                    ret = 0;
                }
                val = checkNumberObject(ret);
                sb.append(BigDecimal.valueOf(val).toPlainString());
                break;
            }
            sb.append(MathParser.SEP);
        }
        sb.deleteCharAt(sb.length() - 1);
        Operand result = null;
        try {
            MathParser math = new MathParser();
            result = math.calculate(sb.toString());
            math = null;
            mathGC++ ;
        } catch (MathParseException e) {
            ScriptInvocationException scriptError = new ScriptInvocationException("error parsing math command", curr);
            scriptError.initCause(e);
            throw (scriptError);
        }
        return result;
    }

    private String execStringLiteral() throws ScriptInvocationException {

        byte next = buff.get();
        Multimap <Integer, Variable> inVars = new Multimap <Integer, Variable>();
        while (next == Bytecodes.STR_VAR) {
            if (buff.get() != Bytecodes.REF_VAR) {
                throw (new ScriptInvocationException("expected REF_VAR after STR_VAR: found="
                        + Integer.toHexString(next), curr));
            }
            Variable var = execRefVar();
            int pos = buff.getInt();
            inVars.put(pos, var);
            next = buff.get();
        }

        if (next != Bytecodes.STR_START) {
            throw (new ScriptInvocationException("found unexpected bytecode instruction in READ_STR: 0x"
                    + Integer.toHexString(next), curr));
        }
        int len = buff.getInt();
        byte[] bytes = new byte[len];
        buff.get(bytes);
        if ( (next = buff.get()) != Bytecodes.END_CMD) {
            throw (new ScriptInvocationException("expected END_CMD for READ_STR: found=" + Integer.toHexString(next),
                    curr));
        }
        StringBuilder s = new StringBuilder(new String(bytes));

        int offs = 0;
        for (int i : inVars.keySet()) {
            for (Variable var : inVars.getAll(i)) {
                String val = var.getValue().toString();
                s.insert(i + offs, val);
                offs += val.length();
            }
        }
        return s.toString();
    }

    private Object execJavaCall() throws ScriptInvocationException {

        long fid = buff.getLong();
        Function f = funcMap.get(fid);
        if (!f.isJavaFunction()) {
            throw (new ScriptInvocationException("invalid command for non-Java function", curr));
        }

        Object[] args = readArgs(f);

        byte next;
        if ( (next = buff.get()) != Bytecodes.END_CMD) {
            throw (new ScriptInvocationException("expected END_CMD for INVOKE_JAVA_FUNC: found="
                    + Integer.toHexString(next), curr));
        }

        return invokeJavaFunction(f, javaObjs.get(f), args);
    }

    private Object execFuncCall() throws ScriptInvocationException {

        long fid = buff.getLong();
        Function f = funcMap.get(fid);
        if (f.isJavaFunction()) {
            throw (new ScriptInvocationException("invalid command for Java based function", curr));
        }

        Object[] args = readArgs(f);

        byte next;
        if ( (next = buff.get()) != Bytecodes.END_CMD) {
            throw (new ScriptInvocationException(
                    "expected END_CMD for INVOKE_FUNC: found=" + Integer.toHexString(next), curr));
        }

        Object ret = this.ret;
        ByteBuffer buff = this.buff;
        Function curr = this.curr;
        LinkedList <VarStack> stacks = this.stacks;
        Object robj = invokeFunction(f, args);
        if (robj instanceof Operand) {
            robj = ((Operand) robj).getValue();
        }
        this.ret = ret;
        this.buff = buff;
        this.curr = curr;
        this.stacks = stacks;
        return robj;
    }

    private Object[] readArgs(final Function f) throws ScriptInvocationException {

        Object[] args = new Object[f.getParamCount()];
        for (int i = 0; i < f.getParamCount(); i++ ) {
            args[i] = execExpression();

            Keyword type = f.getParamTypes()[i];
            if (type == Keyword.INT) {
                args[i] = ((Scalar) args[i]).getValue().intValue();
            } else if (type == Keyword.FLOAT) {
                args[i] = ((Scalar) args[i]).getValue();
            } else if (type == Keyword.BOOL) {
                args[i] = ( ((Scalar) args[i]).val == 0) ? false : true;
            }
        }
        return args;
    }

    private void execConditional() throws ScriptInvocationException {

        int iflen = buff.getInt();
        int init = buff.position();

        byte next = buff.get();
        if (next != EVAL) {
            throw (new ScriptInvocationException("found unexpected bytecode instruction in IF cond: 0x"
                    + Integer.toHexString(next), curr));
        }
        while (true) {
            Operand val = execEvaluation();
            if (val.isVector()) {
                throw (new ScriptInvocationException("conditional expression must return boolean value", curr));
            }
            boolean cond = ((Double) val.getValue() != 0) ? true : false;
            next = buff.get();
            if (next != END_COND) {
                throw (new ScriptInvocationException("found unexpected bytecode instruction in IF cond: 0x"
                        + Integer.toHexString(next), curr));
            }
            int blockLen = buff.getInt();
            if (cond) {
                execMain(buff.position());
                break;
            } else {
                buff.position(buff.position() + blockLen + 1);
                next = buff.get();
                if (next == ELSE_IF) {
                    continue;
                } else if (next == ELSE) {
                    blockLen = buff.getInt();
                    execMain(buff.position());
                    break;
                } else if (next == END_CMD) {
                    return;
                }
            }
        }

        if (buff.position() < init + iflen) {
            buff.position(init + iflen);
        }

        if ( (next = buff.get()) != END_CMD) {
            throw (new ScriptInvocationException("expected END_CMD for IF: found=" + Integer.toHexString(next), curr));
        }
    }

    private void execForLoop() throws ScriptInvocationException {

        // for loop variable declaration
        byte next = buff.get();
        if (next != STORE_VAR) {
            throw (new ScriptInvocationException("expected loop variable evaluation: found="
                    + Integer.toHexString(next), curr));
        }
        execStoreVar(false);
        // for loop condition evaluation
        next = buff.get();
        if (next != FOR_COND) {
            throw (new ScriptInvocationException("expected loop condition evaluation: found="
                    + Integer.toHexString(next), curr));
        }
        int cst = buff.position();

        // we need to parse first to find the command's proper endpoint
        boolean chk = ( ((Scalar) execExpression()).getValue() != 0) ? true : false;
        if (!chk) {
            return;
        }
        int cen = buff.position();
        ByteBuffer cond = ByteBuffer.allocate(cen - cst); // allocate a separate
        // ByteBuffer for just
        // the condition
        // checking
        // instructions
        buff.position(cst);  // reset the main buffer to the start of the
        // condition evaluation so we can re-read the
        // instruction set
        while (buff.position() < cen) {
            cond.put(buff.get());
        }
        cond.flip();
        // for loop iteration command
        next = buff.get();
        if (next != FOR_OP) {
            throw (new ScriptInvocationException("expected loop iteration instruction: found="
                    + Integer.toHexString(next), curr));
        }
        next = buff.get();
        if (next != REF_VAR) {
            throw (new ScriptInvocationException("expected loop variable reference instruction: found="
                    + Integer.toHexString(next), curr));
        }
        Variable opvar = execRefVar();
        if (opvar.type != Flags.TYPE_FLOAT && opvar.type != Flags.TYPE_INT) {
            throw (new ScriptInvocationException("illegal variable type in loop reference", curr));
        }
        next = buff.get();
        double mod = 1;
        int modOp = ADD_MOD;
        switch (next) {
        case DECREM:
            mod = -1;
        case INCREM:
            break;
        case MINUS_MOD:
            mod = -1;
        case ADD_MOD:
            mod = mod * (Double) execExpression(); // if MINUS_DEC, the result
            // of the evaluation will be
            // negated
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
        if (next != FOR_START) {
            throw (new ScriptInvocationException("expected loop body declaration: found=" + Integer.toHexString(next),
                    curr));
        }
        int st = buff.position();
        while (checkLoopCondition(cond)) {
            inLoop = true;
            int stat = execMain(st);
            if (stat == Flags.BREAK) {
                break;
            }

            double val = ((Number) opvar.getValue()).doubleValue();
            if (modOp == ADD_MOD) {
                val += mod;
            } else if (modOp == MULT_MOD) {
                val *= mod;
            } else if (modOp == DIV_MOD) {
                val /= mod;
            }
            opvar.setValue(val);
        }
        inLoop = false;

        next = buff.get();
        if (next != END_CMD) {
            throw (new ScriptInvocationException("expected END_CMD in loop evaluation: found="
                    + Integer.toHexString(next), curr));
        }
    }

    /*
     * Checks the loop condition using the buffer containing the boolean
     * expression evaluation instructions. When evaluation completes, the
     * condition buffer is reset for the next call (ByteBuffer.rewind).
     */
    private boolean checkLoopCondition(final ByteBuffer condBuff) throws ScriptInvocationException {

        boolean cont;
        ByteBuffer sto = this.buff;
        this.buff = condBuff;
        cont = ( ((Scalar) execExpression()).getValue() != 0) ? true : false;
        this.buff = sto;
        condBuff.rewind(); // reset condition bytecode buffer
        return cont;
    }

    private Variable execRefVar() throws ScriptInvocationException {

        int varid = buff.getInt();
        Variable var = fetchVar(varid);
        if (var == null) {
            throw (new ScriptInvocationException("failed to locate var_id=" + varid, curr));
        }
        return var;
    }

    private double checkNumberObject(final Object o) throws ScriptInvocationException {

        double val;
        if (o instanceof Double) {
            val = (Double) o;
        } else if (o instanceof Integer) {
            val = ((Integer) o).doubleValue();
        } else if (o instanceof Boolean) {
            val = ((Boolean) o) ? 1 : 0;
        } else {
            throw (new ScriptInvocationException("function return type does not match expression", curr));
        }
        return val;

    }

    private class VarStack {

        HashMap <Integer, Variable> varmap = new HashMap <Integer, Variable>();

        public void put(final int id, final Variable var) {

            varmap.put(id, var);
        }

        public Variable get(final int id) {

            return varmap.get(id);
        }

        public void remove(final int id) {

            varmap.remove(id);
        }

        public void clear() {

            varGC += varmap.size();
            varmap.clear();
        }
    }

    private class Variable {

        final int id, type;
        boolean doubleStore;
        ByteBuffer val;

        Variable(final int id, final int type, final Object value) throws ScriptInvocationException {

            this.id = id;
            this.type = type;

            setValue(value);
        }

        /*
         * This method should be called each time a Variable's value is reset to
         * ensure proper type handling. DO NOT (in most cases) directly assign
         * the 'value' field to the new Object.
         */
        void setValue(final Object value) throws ScriptInvocationException {

            // if useDouble has changed, float values must be re-allocated
            if (val != null && type == Flags.TYPE_FLOAT && useDouble != this.doubleStore) {
                val.clear();
                val = null;
            }

            this.doubleStore = useDouble; // assign this variable's current use
            // of double-store to the engine
            // setting

            // for int variables, we have to make sure the value is stored as a
            // true Integer,
            // not Double (which the engine returns as an evaluation result
            // regardless).
            switch (type) {
            case Flags.TYPE_INT:
                if (val == null) {
                    val = ByteBuffer.allocateDirect((int) Utils.INT_SIZE);
                }
                if (value instanceof Double) {
                    val.putInt( ((Double) value).intValue());
                } else {
                    val.putInt((Integer) value);
                }
                break;
                // for float variables, we have to check whether or not to store
                // them as a Java Float or Double.
            case Flags.TYPE_FLOAT:
                if (!doubleStore) {
                    if (val == null) {
                        val = ByteBuffer.allocateDirect((int) Utils.FLOAT_SIZE);
                    }
                    val.putFloat( ((Double) value).floatValue());
                } else {
                    if (val == null) {
                        val = ByteBuffer.allocateDirect((int) Utils.DOUBLE_SIZE);
                    }
                    val.putDouble((Double) value);
                }
                break;
            case Flags.TYPE_BOOL:
                if (val == null) {
                    val = ByteBuffer.allocateDirect(1);
                }
                if (value instanceof Double) {
                    val.put( ((Double) value).byteValue());
                } else {
                    val.put( ((Boolean) value) ? (byte) 1 : (byte) 0);
                }
                break;
            case Flags.TYPE_STRING:
                String sval = (String) value;
                byte[] sbytes = sval.getBytes();
                if (val == null || val.capacity() < sbytes.length) {
                    val = ByteBuffer.allocateDirect(sbytes.length);
                }
                val.put(sbytes);
                break;
            case Flags.TYPE_VEC2:
                Vector2d vec;
                if (value instanceof Operand) {
                    Operand opn = (Operand) value;
                    if (!opn.isVector()) {
                        throw (new ScriptInvocationException("cannot assign non-vector value to vector type", curr));
                    }
                    vec = (Vector2d) opn.getValue();
                } else if (value instanceof Vector2d) {
                    vec = (Vector2d) value;
                } else {
                    throw new ScriptInvocationException("illegal type for vec2 value: " + value.getClass(), curr);
                }

                if (val == null) {
                    val = ByteBuffer.allocateDirect((int) Utils.DOUBLE_SIZE * 2);
                }
                val.putDouble(vec.x);
                val.putDouble(vec.y);
                break;
            default:
                throw (new ScriptInvocationException("unrecognized variable type: " + type, curr));
            }
            val.flip();
        }

        Object getValue() {

            Object value = null;
            switch (type) {
            case Flags.TYPE_INT:
                value = val.getInt();
                break;
            case Flags.TYPE_FLOAT:
                if (!doubleStore) {
                    value = val.getFloat();
                } else {
                    value = val.getDouble();
                }
                break;
            case Flags.TYPE_BOOL:
                value = (val.get() == 1) ? true : false;
                break;
            case Flags.TYPE_STRING:
                byte[] bytes = new byte[val.limit()];
                val.get(bytes);
                value = new String(bytes);
                break;
            case Flags.TYPE_VEC2:
                double x = val.getDouble();
                double y = val.getDouble();
                value = new Vector2d(x, y);
            }
            val.rewind();
            return value;
        }
    }

    private static <T> T castVariable(final Variable var, final Function context, final Class <T> type) {

        if (Keyword.isValidDataType(type)) {
            return type.cast(var.getValue());
        } else {
            return null;
        }
    }
}
