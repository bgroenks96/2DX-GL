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
 * @author Brian Groenke
 *
 */
public class ScriptCompilationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6770378760915410306L;

    private static final char lineSep; // used for finding the line of a
    // compilation error

    static {
        String ls = System.getProperty("line.separator");
        lineSep = ls.charAt(ls.length() - 1);
    }

    String message;
    Function inFunc;

    public ScriptCompilationException(final Function func, final String message, final String source, final int charPos) {

        super();
        this.inFunc = func;
        if (source != null) {
            this.message = appendErrorInfo(message, source, charPos);
        } else {
            this.message = message;
        }
    }

    public ScriptCompilationException(final String message, final String source, final int charPos) {

        this(null, message, source, charPos);
    }

    public ScriptCompilationException(final String message) {

        super(message);
        this.message = message;
    }

    public Function getFunction() {

        return inFunc;
    }

    @Override
    public String getMessage() {

        return message;
    }

    private String appendErrorInfo(final String msg, final String src, final int cpos) {

        char[] chars = src.toCharArray();
        int n = 0;
        for (int i = 0; i < cpos; i++ ) {
            if (chars[i] == lineSep) {
                n++ ;
            }
        }

        return msg + " [line=" + n + " charIndex=" + cpos + "]";
    }
}
