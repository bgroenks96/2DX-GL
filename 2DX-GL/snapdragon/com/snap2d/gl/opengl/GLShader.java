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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class GLShader {

	public static final String DEFAULT_SHADER_PATH = "snap2d_opengl/shaders/",
			DEFAULT_TRANSFORM_UTIL = "transform.vert";

	public static final int TYPE_VERTEX = GL2.GL_VERTEX_SHADER, TYPE_FRAGMENT = GL2.GL_FRAGMENT_SHADER;

	private int sobj;

	public GLShader(int type, String...sources) throws GLShaderException {
		compile(type, sources);
	}

	public GLShader(int type, URL... sources) throws GLShaderException, IOException {
		ArrayList<String> sourceStrs = new ArrayList<String>();
		for(URL source : sources) {
			InputStream in = source.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line=br.readLine()) != null)
				sb.append(line + "\n");
			in.close();
			sourceStrs.add(sb.toString());
		}
		compile(type, sourceStrs.toArray(new String[sourceStrs.size()]));
	}

	private void compile(int type, String... sources) throws GLShaderException {
		final GL2ES2 gl = getGL2();
		sobj = gl.glCreateShader(type);
		StringBuilder sb = new StringBuilder();
		for(String s : sources)
			sb.append(s);
		gl.glShaderSource(sobj, 1, new String[] {sb.toString()}, null);
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
		final GL2ES2 gl = getGL2();
		gl.glDeleteShader(sobj);
	}

	private GL2ES2 getGL2() {
		return GLContext.getCurrentGL().getGL2ES2();
	}

	/**
	 * Loads a Snapdragon2D shader from the library's default shader directory on the class-path.
	 * This method will automatically attach the vertex shader transform source file to the given
	 * file(s).
	 * @param shaderFile
	 * @param type
	 * @return
	 * @throws GLShaderException
	 * @throws IOException
	 */
	public static GLShader loadLibraryShader(int type, String...shaderFiles) throws 
	GLShaderException, IOException {
		ArrayList<URL> urls = new ArrayList<URL>();
		if(type == TYPE_VERTEX)
			urls.add(ClassLoader.getSystemResource(DEFAULT_SHADER_PATH + DEFAULT_TRANSFORM_UTIL));
		for(String s : shaderFiles) {
			URL url = ClassLoader.getSystemResource(DEFAULT_SHADER_PATH + s);
			urls.add(url);
		}
		GLShader shader = new GLShader(type, urls.toArray(new URL[urls.size()]));
		return shader;
	}
}
