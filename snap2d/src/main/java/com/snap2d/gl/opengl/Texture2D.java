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

package com.snap2d.gl.opengl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * Wrapper for JOGL Texture class. Not all methods are covered. Direct access
 * can be obtained through {@link #getTextureObject()}. <b>JOGL libraries must
 * be in your build path to use the classes directly.</b>
 *
 * @author Brian Groenke
 *
 */
public class Texture2D {

    Texture tex;
    TextureCoords coords;

    public Texture2D(final Texture tex) {

        if (tex == null) {
            throw (new GLException("texture cannot be null"));
        }
        this.tex = tex;
        coords = tex.getImageTexCoords();
    }

    void bind(final GL gl) {

        tex.bind(gl);
    }

    void destroy(final GL gl) {

        tex.destroy(gl);
    }

    void enable(final GL gl) {

        tex.enable(gl);
    }

    void disable(final GL gl) {

        tex.disable(gl);
    }

    public int getWidth() {

        return tex.getWidth();
    }

    public int getHeight() {

        return tex.getHeight();
    }

    public int getEstimatedMemorySize() {

        return tex.getEstimatedMemorySize();
    }

    public float getAspectRatio() {

        return tex.getAspectRatio();
    }

    public int getTarget() {

        return tex.getTarget();
    }

    public boolean isVerticallyFlipped() {

        return tex.getMustFlipVertically();
    }

    public float getBottomCoord() {

        return coords.bottom();
    }

    public float getLeftCoord() {

        return coords.left();
    }

    public float getRightCoord() {

        return coords.right();
    }

    public float getTopCoord() {

        return coords.top();
    }

    void setTexParameterf(final GL gl, final int param, final float val) {

        tex.setTexParameterf(gl, param, val);
    }

    void setTexParameterfv(final GL gl, final int param, final float[] params, final int offs) {

        tex.setTexParameterfv(gl, param, params, offs);
    }

    void setTexParameteri(final GL gl, final int param, final int val) {

        tex.setTexParameterf(gl, param, val);
    }

    void setTexParameteriv(final GL gl, final int param, final int[] params, final int offs) {

        tex.setTexParameteriv(gl, param, params, offs);
    }

    public boolean isUsingAutoMipmapGeneration() {

        return tex.isUsingAutoMipmapGeneration();
    }

    public Texture getTextureObject() {

        return tex;
    }
}
