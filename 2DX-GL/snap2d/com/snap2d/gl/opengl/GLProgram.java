/*
 *  Copyright (C) 2012-2014 Brian Groenke
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

import java.lang.reflect.*;
import java.nio.*;
import java.util.ArrayList;

import javax.media.opengl.*;

import com.jogamp.common.nio.Buffers;

/**
 * Class representing a GLSL program object.
 * @author Brian Groenke
 *
 */
public class GLProgram {

	private static GLProgram defaultShaderProg, currentShaderProg;

	private GLHandle handle;
	private int progId;
	private ArrayList<GLShader> shaders = new ArrayList<GLShader>();

	private boolean defaultProg;
	
	private FloatBuffer matBufferFloat;

	public GLProgram(GLHandle handle) {
		final GL2ES2 gl = getGL2();
		progId = gl.glCreateProgram();
	}
	
	public static GLProgram getCurrentProgram() {
		return currentShaderProg;
	}
	
	public static GLProgram getDefaultProgram() {
		return defaultShaderProg;
	}
	
	public static boolean isDefaultProgEnabled() {
		return currentShaderProg == defaultShaderProg;
	}

	public void attachShader(GLShader shader) {
		final GL2ES2 gl = getGL2();
		gl.glAttachShader(progId, shader.getShaderObj());
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
		final GL2ES2 gl = getGL2();
		gl.glDetachShader(progId, sobj);
	}

	public boolean link() {
		final GL2ES2 gl = getGL2();
		gl.glLinkProgram(progId);
		gl.glValidateProgram(progId);
		IntBuffer intBuff = IntBuffer.allocate(1);
		gl.glGetProgramiv(progId, GL2.GL_LINK_STATUS, intBuff);
		return intBuff.get(0) == GL.GL_TRUE;
	}

	public void printLinkLog() {
		final GL2ES2 gl = getGL2();
		IntBuffer intBuff = IntBuffer.allocate(1);
		gl.glGetProgramiv(progId, GL2.GL_INFO_LOG_LENGTH, intBuff);
		int size = intBuff.get(0);
		if (size > 0) {
			System.err.println("GLProgram link error: " + "[log len="+size+"] ");
			ByteBuffer byteBuffer = ByteBuffer.allocate(size);
			gl.glGetProgramInfoLog(progId, size, intBuff, byteBuffer);
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
		final GL2ES2 gl = getGL2();
		gl.glUseProgram(progId);
		currentShaderProg = this;
	}

	/**
	 * Disable the shader program.  When the shader program is disabled, the
	 * library default program will be automatically re-enabled.
	 */
	public void disable() {
		final GL2ES2 gl = getGL2();
		gl.glUseProgram(0);
		if(!defaultProg) {
			defaultShaderProg.enable();
		}
	}
	
	public boolean isEnabled() {
		return currentShaderProg == this;
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

	public void setUniformi(final String name, final int...values) {
		setUniform(name, UniformType.INT, toObjectArray(values));
	}

	public void setUniformf(final String name, final float...values) {
		setUniform(name, UniformType.FLOAT, toObjectArray(values));
	}

	public void setUniformd(final String name, final double...values) {
		setUniform(name, UniformType.DOUBLE, toObjectArray(values));
	}

	public void setUniformiv(final String name, final int componentNum, final int... values) {
		IntBuffer buff = Buffers.newDirectIntBuffer(values);
		setUniformv(name, componentNum, UniformType.INT, buff);
	}

	public void setUniformiv(final String name, final int componentNum, final IntBuffer values) {
		setUniformv(name, componentNum, UniformType.INT, values);
	}

	public void setUniformfv(final String name, final int componentNum, final float... values) {
		FloatBuffer buff = Buffers.newDirectFloatBuffer(values);
		setUniformv(name, componentNum, UniformType.FLOAT, buff);

	}

	public void setUniformfv(final String name, final int componentNum, final FloatBuffer values) {
		setUniformv(name, componentNum, UniformType.FLOAT, values);

	}

	public void setUniformdv(final String name, final int componentNum, final double... values) {
		DoubleBuffer buff = Buffers.newDirectDoubleBuffer(values);
		setUniformv(name, componentNum, UniformType.DOUBLE, buff);

	}

	public void setUniformdv(final String name, final int componentNum, final DoubleBuffer values) {
		setUniformv(name, componentNum, UniformType.DOUBLE, values);
	}

	public void setUniformMatrix(final String name, final int componentNum, final float... values) {
		if(matBufferFloat == null)
			matBufferFloat = Buffers.newDirectFloatBuffer(16);
		matBufferFloat.clear();
		matBufferFloat.put(values);
		matBufferFloat.flip();
		setUniformMatrix(name, componentNum, UniformType.FLOAT, matBufferFloat);
	}

	public void setUniformMatrix(final String name, final int componentNum,
			FloatBuffer values) {
		setUniformMatrix(name, componentNum, UniformType.FLOAT, values);
	}

	private void setUniform(final String name, final UniformType type, final Object...values) {
		int loc = getLocation(name);
		String method = "glUniform"+values.length+type.suffix;
		try {
			switch(values.length) {
			case 1:
				Method m = GL2ES2.class.getMethod(method, int.class, type.value);
				m.setAccessible(true);
				m.invoke(getGL2(), loc, values[0]);
				break;
			case 2:
				m = GL2ES2.class.getMethod(method, int.class, type.value, type.value);
				m.setAccessible(true);
				m.invoke(getGL2(), loc, values[0], values[1]);
				break;
			case 3:
				m = GL2ES2.class.getMethod(method, int.class, type.value, type.value, type.value);
				m.setAccessible(true);
				m.invoke(getGL2(), loc, values[0], values[1], values[2]);
				break;
			case 4:
				m = GL2ES2.class.getMethod(method, int.class, type.value, type.value, type.value, type.value);
				m.setAccessible(true);
				m.invoke(getGL2(), loc, values[0], values[1], values[2], values[3]);
				break;
			default:
				throw(new IllegalArgumentException("illegal number of values supplied to "
						+ "setUniform"+type.suffix));
			}
		} catch (NoSuchMethodException e) {
			throw(new IllegalArgumentException("failed to locate set uniform method: " + method));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void setUniformv(final String name, final int componentNum,
			final UniformType type, final Buffer data) {
		int loc = getLocation(name);
		if(componentNum < 1 || componentNum > 4)
			throw(new IllegalArgumentException("illegal number of compoments for setUniform"+type.suffix+"v"));
		String method = "glUniform"+componentNum+type.suffix+"v";
		try {
			Method m = GL2ES2.class.getMethod(method, int.class, int.class, type.buffer);
			m.setAccessible(true);
			m.invoke(getGL2(), loc, data.limit(), data);
		} catch (NoSuchMethodException e) {
			throw(new IllegalArgumentException("failed to locate set uniform method: " + method));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void setUniformMatrix(final String name, final int componentNum,
			final UniformType type, final Buffer data) {
		int loc = getLocation(name);
		if(componentNum < 2 || componentNum > 4)
			throw(new IllegalArgumentException("illegal number of compoments for setUniformMatrix"));
		String method = "glUniformMatrix"+componentNum+type.suffix+"v";
		try {
			Method m = GL2ES2.class.getMethod(method, int.class, int.class, boolean.class, type.buffer);
			m.setAccessible(true);
			m.invoke(getGL2(), loc, 1, false, data);
		} catch (NoSuchMethodException e) {
			throw(new IllegalArgumentException("failed to locate set uniform method: " + method));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/*
	public void setUniform(String uniform, int num, UniformType type, Object... values) {
		int loc = uniloc(uniform);
		String method = "glUniform"+num+type.suffix;
		try {
			switch(num) {
			case 1:
				Method m = GL2.class.getMethod(method, int.class, type.value);
				m.invoke(handle.gl, loc, values[0]);
				break;
			case 2:
				m = GL2.class.getMethod(method, int.class, type.value, type.value);
				m.invoke(handle.gl, loc, values[0], values[1]);
				break;
			case 3:
				m = GL2.class.getMethod(method, int.class, type.value, type.value, type.value);
				m.invoke(handle.gl, loc, values[0], values[1], values[2]);
				break;
			case 4:
				m = GL2.class.getMethod(method, int.class, type.value, type.value, type.value, type.value);
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
	 */

	public void setAttrib1f(String attrib, float val) {
		final GL2ES2 gl = getGL2();
		gl.glVertexAttrib1f(attloc(attrib), val);
	}

	/**
	 * Disposes this OpenGL program object as well as all attached shaders.
	 */
	public void dispose() {
		final GL2ES2 gl = getGL2();
		for(GLShader gls:shaders) {
			detachShader(gls.getShaderObj());
			gls.dispose();
		}
		gl.glDeleteProgram(progId);
	}
	
	/**
	 * Fetches the internal OpenGL location for the given uniform.
	 * @param uniform
	 * @return the uniform location, or -1 if not found
	 */
	public int getLocation(String uniform) {
		final GL2ES2 gl = getGL2();
		return gl.glGetUniformLocation(progId, uniform);
	}
	
	static void setDefaultProgram(GLProgram newDefaultProg) {
		defaultShaderProg = newDefaultProg;
	}

	private int attloc(String attrib) {
		final GL2ES2 gl = getGL2();
		return gl.glGetAttribLocation(progId, attrib);
	}

	private enum UniformType {
		INT("i", int.class, IntBuffer.class), 
		FLOAT("f", float.class, FloatBuffer.class), 
		DOUBLE("d", double.class, DoubleBuffer.class);

		String suffix;
		Class<?> value, buffer;

		UniformType(String suffix, Class<?> value, Class<?> buffer) {
			this.suffix = suffix;
			this.buffer = buffer;
			this.value = value;
		}
	}

	private Object[] toObjectArray(int[] ints) {
		Object[] intObjs = new Integer[ints.length];
		for(int i=0; i < intObjs.length; i++)
			intObjs[i] = ints[i];
		return intObjs;
	}

	private Object[] toObjectArray(float[] floats) {
		Object[] intObjs = new Float[floats.length];
		for(int i=0; i < intObjs.length; i++)
			intObjs[i] = floats[i];
		return intObjs;
	}

	private Object[] toObjectArray(double[] doubles) {
		Object[] intObjs = new Double[doubles.length];
		for(int i=0; i < intObjs.length; i++)
			intObjs[i] = doubles[i];
		return intObjs;
	}

	private GL2ES2 getGL2() {
		return GLContext.getCurrentGL().getGL2ES2();
	}
}
