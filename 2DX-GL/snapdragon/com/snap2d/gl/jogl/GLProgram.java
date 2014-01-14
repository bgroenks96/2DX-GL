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

import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;

/**
 * Class representing a GLSL program object.
 * @author Brian Groenke
 *
 */
public class GLProgram {
	
	static GLProgram DEFAULT_SHADER_PROG;
	
	private GLHandle handle;
	private int progId;
	private ArrayList<GLShader> shaders = new ArrayList<GLShader>();
	
	private boolean defaultProg;
	
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
	
	/**
	 * Enable the shader program for use in subsequent pipeline calls.
	 */
	public void enable() {
		handle.gl.glUseProgram(progId);
	}
	
	/**
	 * Disable the shader program.  When the shader program is disabled, the
	 * library default program will be automatically re-enabled.
	 */
	public void disable() {
		handle.gl.glUseProgram(0);
		if(!defaultProg)
			DEFAULT_SHADER_PROG.enable();
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
	
	public boolean isDefaultShaderProgram() {
		return defaultProg;
	}
	
	// hide from public API
	void setDefaultShaderProgram(boolean def) {
		defaultProg = def;
	}
	
	public void setUniform(String uniform, int num, UniformType type, Object... values) {
		int loc = uniloc(uniform);
		String method = "glUniform"+num+type.suffix;
		try {
			Method m = GL2.class.getMethod(method, int.class, type.value);
			switch(num) {
			case 1:
				m.invoke(handle.gl, loc, values[0]);
				break;
			case 2:
				m.invoke(handle.gl, loc, values[0], values[1]);
				break;
			case 3:
				m.invoke(handle.gl, loc, values[0], values[1], values[2]);
				break;
			case 4:
				m.invoke(handle.gl, loc, values[0], values[1], values[2], values[3]);
				break;
			}
		} catch (NoSuchMethodException e) {
			System.err.println("failed to locate set uniform method: "+method);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void setUniformv(String uniform, int num, UniformType type, int len, Buffer vals) {
		int loc = uniloc(uniform);
		String method = "glUniform"+num+type.suffix+"v";
		try {
			Method m = GL2.class.getMethod(method, int.class, int.class, type.buffer);
			m.invoke(handle.gl, loc, len, vals);
		} catch (NoSuchMethodException e) {
			System.err.println("failed to locate set uniform method: "+method);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void setAttrib1f(String attrib, float val) {
		handle.gl.glVertexAttrib1f(attloc(attrib), val);
	}
	
	private int uniloc(String uniform) {
		return handle.gl.glGetUniformLocation(progId, uniform);
	}
	
	private int attloc(String attrib) {
		return handle.gl.glGetAttribLocation(progId, attrib);
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
	
	public enum UniformType {
		INT("i", int.class, int[].class, IntBuffer.class), 
		FLOAT("f", float.class, float[].class, FloatBuffer.class), 
		DOUBLE("d", double.class, double[].class, DoubleBuffer.class);
		
		String suffix;
		Class<?> value, array, buffer;
		
		UniformType(String suffix, Class<?> value, Class<?> array, Class<?> buffer) {
			this.suffix = suffix;
			this.array = array;
			this.buffer = buffer;
			this.value = value;
		}
	}
}
