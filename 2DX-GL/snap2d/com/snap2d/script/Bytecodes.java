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

/**
 * Contains static byte values used in the script's compiled, bytecode
 * instruction set.
 * 
 * @author Brian Groenke
 *
 */
public final class Bytecodes {

    private Bytecodes() {

    }

    public static final byte END_CMD = 0x0, ALLOC_INT = 0x1, ALLOC_FLOAT = 0x2, ALLOC_STRING = 0x3, ALLOC_BOOL = 0x4,
            STORE_VAR = 0x5, REF_VAR = 0x6, INVOKE_FUNC = 0x7, INVOKE_JAVA_FUNC = 0x8, TYPE_INT = 0x9,
            TYPE_FLOAT = 0xA, TYPE_STRING = 0xB, TYPE_BOOL = 0xC, IF = 0xD, TRUE = 0xE, FALSE = 0xF, SKIP = 0x10,
            FOR_VAR = 0x11, FOR_COND = 0x12, FOR_OP = 0x13, ADD = 0x14, SUBTRACT = 0x15, MULTIPLY = 0x16,
            DIVIDE = 0x17, POW = 0x18, SHIFT_LEFT = 0x19, SHIFT_RIGHT = 0x1A, EVAL = 0x1B, READ_INT = 0x1C,
            READ_FLOAT = 0x1D, READ_OP = 0x1F, READ_STR = 0x20, STR_VAR = 0x21, STR_START = 0x22, REALLOC = 0x23,
            AND = 0x24, OR = 0x25, RETURN = 0x26, END_COND = 0x27, ELSE_IF = 0x28, ELSE = 0x29, EQUALS = 0x2A,
            GREATER = 0x2B, LESSER = 0x2C, NOT_EQUALS = 0x2D, PARAM_VAR = 0x2E, FOR_START = 0x30, INCREM = 0x31,
            DECREM = 0x32, ADD_MOD = 0x33, MINUS_MOD = 0x34, NO_PARAMS = 0x35, INIT_PARAMS = 0x36, NEW_STACK = 0x37,
            CLEAR_STACK = 0x38, BITOR = 0x39, BITAND = 0x3A, BITXOR = 0x3B, MODULO = 0x3C, MULT_MOD = 0x3D,
            DIV_MOD = 0x3E, BREAK = 0x3F, CONTINUE = 0x40, LESS_EQUALS = 0x41, GREAT_EQUALS = 0x42, STORE_CONST = 0x43,
            ALLOC_VEC2 = 0x44, READ_VEC2 = 0x45;

    @Deprecated
    // unnecessary loop instruction
    public static final byte FOR_SEP = 0x2F;
}
