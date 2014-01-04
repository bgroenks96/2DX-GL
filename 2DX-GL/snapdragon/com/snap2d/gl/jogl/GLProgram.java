/*
 *  Copyright © 2012-2014 Brian Groenke
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

import java.nio.*;
import java.util.*;

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class GLProgram {
	
	private GLHandle handle;
	private int progId;
	private ArrayList<GLShader> shaders = new ArrayList<GLShader>();
	
	public GLProgram(GLHandle handle) {
		this.handle = handle;
		GL2 gl = handle.gl;
		progId = gl.glCreateProgram();
	}
	
	public void attachShader(GLShader shader) {
		handle.gl.glAttachShader(progId, shader.getShaderObj());
		shaders.add(shader);
	}
	
	public void detachShader(GLShader shader) {
		detachShader(shader.getShaderObj());
		shaders.remove(shader);
	}
	
	/*
	 * Internal method - performs the actual detachment of the shader.
	 * Primary purpose is to separate detachment of shaders from Collection
	 * removal.
	 */
	private final void detachShader(int sobj) {
		handle.gl.glDetachShader(progId, sobj);
	}
	
	public boolean link() {
		handle.gl.glLinkProgram(progId);
		handle.gl.glValidateProgram(progId);
		IntBuffer intBuff = IntBuffer.allocate(1);
		handle.gl.glGetProgramiv(progId, GL2.GL_LINK_STATUS, intBuff);
		return intBuff.get(0) == GL.GL_TRUE;
	}
	
	public void printLinkLog() {
		IntBuffer intBuff = IntBuffer.allocate(1);
        handle.gl.glGetProgramiv(progId, GL2.GL_INFO_LOG_LENGTH, intBuff);
        int size = intBuff.get(0);
        if (size > 0) {
            System.err.println("GLProgram link error: " + "[log len="+size+"] ");
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            handle.gl.glGetProgramInfoLog(progId, size, intBuff, byteBuffer);
            for (byte b : byteBuffer.array()) {
                System.err.print((char) b);
            }
        } else {
            System.err.println("Info log is unavailable");
        }
	}
	
	public void enable() {
		handle.gl.glUseProgram(progId);
	}
	
	public void disable() {
		handle.gl.glUseProgram(0);
	}
	
	public void setHandle(GLHandle handle) {
		this.handle = handle;
	}
	
	public GLHandle getHandle() {
		return handle;
	}
	
	public int getProgramObject() {
		return progId;
	}
	
	public void setUniform1i(String uniform, int val) {
		handle.gl.glUniform1i(getUniformFieldLoc(uniform), val);
	}
	
	public int getUniform1i(String uniform) {
		int[] out = new int[1];
		handle.gl.glUniform1iv(getUniformFieldLoc(uniform), 1, out, 0);
		return out[0];
	}
	
	public void setUniform1f(String uniform, float val) {
		handle.gl.glUniform1f(getUniformFieldLoc(uniform), val);
	}
	
	public float getUniform1f(String uniform) {
		float[] out = new float[1];
		handle.gl.glUniform1fv(getUniformFieldLoc(uniform), 1, out, 0);
		return out[0];
	}
	
	private int getUniformFieldLoc(String uniform) {
		return handle.gl.glGetUniformLocation(progId, uniform);
	}
	
	/**
	 * Disposes this OpenGL program object as well as all attached shaders.
	 */
	public void dispose() {
		for(GLShader gls:shaders) {
			detachShader(gls.getShaderObj());
			gls.dispose();
		}
		handle.gl.glDeleteProgram(progId);
	}
}
