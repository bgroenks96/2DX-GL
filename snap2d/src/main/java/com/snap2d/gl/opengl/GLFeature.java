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

package com.snap2d.gl.opengl;

import com.jogamp.opengl.GL;

/**
 * Mappings to selected OpenGL features that may be enabled/disabled by the
 * caller.
 *
 * @author Brian Groenke
 *
 */
public enum GLFeature {

    BLENDING(GL.GL_BLEND);

    private int mapping;

    GLFeature(final int glMapping) {

        this.mapping = glMapping;
    }

    public int getGLCommand() {

        return mapping;
    }
}
