/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.script.refactor;

import java.nio.ByteBuffer;

/**
 * @author brian
 *
 */
public interface Parseable {

    /**
     * @param buff
     *            the bytecode source buffer
     * @param src
     *            the full source string being parsed
     * @param pos
     *            the current relative position in the source
     * @return the new position after parsing this Parseable statement
     */
    int parse(ByteBuffer buff, String src, int pos);
}
