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

import java.awt.*;
import java.nio.*;

import javax.media.opengl.*;

import com.jogamp.common.nio.*;
import com.snap2d.world.*;

/**
 * Provides common utility methods for interfacing with OpenGL libraries.  Some members
 * of this class simply act as forward-wrappers for JOGL/Gluegen utilities.
 * @author Brian Groenke
 *
 */
public class GLUtils {
	
	public static final int SIZEOF_BYTE = Buffers.SIZEOF_BYTE, SIZEOF_INT = Buffers.SIZEOF_INT, 
			SIZEOF_FLOAT = Buffers.SIZEOF_FLOAT, SIZEOF_DOUBLE = Buffers.SIZEOF_DOUBLE, 
			SIZEOF_LONG = Buffers.SIZEOF_LONG, SIZEOF_CHAR = Buffers.SIZEOF_CHAR, 
			SIZEOF_SHORT = Buffers.SIZEOF_SHORT;
	
	private GLUtils() {}
	
	public static final World2D createGLWorldSystem(double minx, double miny, int viewWt, int viewHt, float ppu) {
		return new GLWorld2D(minx, miny, viewWt, viewHt, ppu);
	}
	
	public static final ByteBuffer newDirectByteBuffer(byte...bytes) {
		return Buffers.newDirectByteBuffer(bytes);
	}
	
	public static final IntBuffer newDirectIntBuffer(int...ints) {
		return Buffers.newDirectIntBuffer(ints);
	}
	
	public static final FloatBuffer newDirectFloatBuffer(float...floats) {
		return Buffers.newDirectFloatBuffer(floats);
	}
	
	public static final DoubleBuffer newDirectDoubleBuffer(double...doubles) {
		return Buffers.newDirectDoubleBuffer(doubles);
	}
	
	public static final CharBuffer newDirectCharBuffer(char...chars) {
		return Buffers.newDirectCharBuffer(chars);
	}
	
	public static final ShortBuffer newDirectShortBuffer(short...shorts) {
		return Buffers.newDirectShortBuffer(shorts);
	}
	
	/**
	 * Writes the RGBA components of the given Color to a FloatBuffer as OpenGL color values (0.0f - 1.0f).
	 * @param color the java.awt.Color to convert
	 * @param colorBuff a FloatBuffer with capacity/limit >= 4
	 * @return
	 */
	public static final FloatBuffer writeColorToBuffer(Color color, FloatBuffer colorBuff) {
		float[] rgba = new float[4];
		color.getComponents(rgba);
		colorBuff.put(rgba);
		colorBuff.flip();
		return colorBuff;
	}
	
	/**
	 * Calls {@link #writeColorToBuffer(Color, FloatBuffer)} with a newly allocated FloatBuffer.
	 * @param color
	 * @return
	 */
	public static final FloatBuffer writeColorToNewBuffer(Color color) {
		return writeColorToBuffer(color, newDirectFloatBuffer(4));
	}
	
	/**
	 * Converts an AWT Color (0f - 255f) linearly to GL color space (0.0f - 1.0f).
	 * @param color the AWT color to convert
	 * @return an array of GL color channels
	 */
	public static final float[] convertColorAwtToGL(Color color) {
		float[] rgbaValues = new float[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
		for(int i=0; i < rgbaValues.length; i++)
			rgbaValues[i] = rgbaValues[i] / 255f;
		return rgbaValues;
	}
	
	public static final void printBuffer(FloatBuffer buff) {
		System.out.print("[");
		for(int i=0; i < buff.limit(); i++) {
			System.out.print(buff.get(i));
			if(i < buff.limit() - 1)
				System.out.print(", ");
		}
		System.out.println("]");
	}
	
	public static final float[] readAvailable(FloatBuffer buff, float[] dest) {
		buff.get(dest, 0, Math.min(dest.length, buff.limit() - buff.position()));
		return dest;
	}
	
	static FloatBuffer createOrthoMatrix(float left, float right, float bottom, float top, 
			float zNear, float zFar) {
		float m00=1, m01=0, m02=0, m03=0, m10=0, m11=1, m12=0, m13=0, m20=0, m21=0, 
				m22=1, m23=0, m30=0, m31=0, m32=0, m33=1;
	    m00 = 2 / (right-left);
	    m11 = 2 / (top-bottom);
	    m22 = -2 / (zFar-zNear);
	    m30 = - (right+left) / (right-left);
	    m31 = - (top+bottom) / (top-bottom);
	    m32 = - (zFar+zNear) / (zFar-zNear);
	    m33 = 1;
	    FloatBuffer matBuffer = Buffers.newDirectFloatBuffer(16);
	    matBuffer.put(m00);
	    matBuffer.put(m01);
	    matBuffer.put(m02);
	    matBuffer.put(m03);
	    matBuffer.put(m10);
	    matBuffer.put(m11);
	    matBuffer.put(m12);
	    matBuffer.put(m13);
	    matBuffer.put(m20);
	    matBuffer.put(m21);
	    matBuffer.put(m22);
	    matBuffer.put(m23);
	    matBuffer.put(m30);
	    matBuffer.put(m31);
	    matBuffer.put(m32);
	    matBuffer.put(m33);
	    matBuffer.flip();
		return matBuffer;
	}
	
	static int glGetInteger(GL gl, int pint) {
		int[] val = new int[1];
		gl.glGetIntegerv(pint, val, 0);
		return val[0];
	}
	
	static float glGetFloat(GL gl, int pname) {
		float[] val = new float[1];
		gl.glGetFloatv(pname, val, 0);
		return val[0];
	}
	
	static boolean glGetBoolean(GL gl, int pname) {
		byte[] val = new byte[1];
		gl.glGetBooleanv(pname, val, 0);
		return (val[0] == GL.GL_FALSE) ? false : true;
	}
}
