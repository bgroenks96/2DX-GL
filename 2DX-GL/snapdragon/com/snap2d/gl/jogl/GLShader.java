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

import java.io.*;
import java.net.*;

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class GLShader {

	public static final String DEFAULT_SHADER_PATH = "snap2d_opengl/shaders/";

	public static final int TYPE_VERTEX = GL2.GL_VERTEX_SHADER, TYPE_FRAGMENT = GL2.GL_FRAGMENT_SHADER;

	private GLHandle handle;
	private int sobj;

	public GLShader(GLHandle handle, String source, int type) throws GLShaderException {
		this.handle = handle;
		compile(source, type);
	}

	public GLShader(GLHandle handle, URL source, int type) throws GLShaderException, IOException {
		this.handle = handle;
		InputStream in = source.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine()) != null)
			sb.append(line + "\n");
		in.close();
		compile(sb.toString(), type);
	}

	private void compile(String source, int type) throws GLShaderException {
		GL2 gl = handle.gl;
		sobj = gl.glCreateShader(type);
		gl.glShaderSource(sobj, 1, new String[] { source }, (int[]) null, 0);
		gl.glCompileShader(sobj);

		//Check compile status.
		int[] compiled = new int[1];
		gl.glGetShaderiv(sobj, GL2.GL_COMPILE_STATUS, compiled,0);
		
		if(compiled[0] == GL.GL_FALSE) {
			int[] logLength = new int[1];
			gl.glGetShaderiv(sobj, GL2.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(sobj, logLength[0], (int[])null, 0, log, 0);

			throw(new GLShaderException("error compiling shader", this, new String(log), System.currentTimeMillis()));

		}
	}

	public int getShaderObj() {
		return sobj;
	}

	public void dispose() {
		handle.gl.glDeleteShader(sobj);
	}

	public static GLShader loadDefaultShader(GLHandle handle, String shaderFile, int type) throws 
	GLShaderException, IOException {
		URL url = ClassLoader.getSystemResource(DEFAULT_SHADER_PATH + shaderFile);
		GLShader shader = new GLShader(handle, url, type);
		return shader;
	}
}
