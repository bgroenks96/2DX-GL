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

package com.snap2d.gl.jogl;

import javax.media.opengl.*;

import com.jogamp.opengl.util.texture.*;

/**
 * Wrapper for JOGL Texture class.  Not all methods are covered.
 * Direct access can be obtained through {@link #getTextureObject()}.
 * <b>JOGL libraries must be in your build path to use the classes directly.</b>
 * 
 * @author Brian Groenke
 *
 */
public class Texture2D {
	
	Texture tex;

	public Texture2D(Texture tex) {
		this.tex = tex;
	}
	
	public void bind(GL gl) {
		tex.bind(gl);
	}
	
	public void destroy(GL gl) {
		tex.destroy(gl);
	}
	
	public void enable(GL gl) {
		tex.enable(gl);
	}
	
	public void disable(GL gl) {
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
	
	public void setTexParameterf(GL gl, int param, float val) {
		tex.setTexParameterf(gl, param, val);
	}
	
	public void setTexParameterfv(GL gl, int param, float[] params, int offs) {
		tex.setTexParameterfv(gl, param, params, offs);
	}
	
	public void setTexParameteri(GL gl, int param, int val) {
		tex.setTexParameterf(gl, param, val);
	}
	
	public void setTexParameteri(GL gl, int param, int[] params, int offs) {
		tex.setTexParameteriv(gl, param, params, offs);
	}
	
	public boolean isUsingAutoMipmapGeneration() {
		return tex.isUsingAutoMipmapGeneration();
	}
	
	public Texture getTextureObject() {
		return tex;
	}
}
