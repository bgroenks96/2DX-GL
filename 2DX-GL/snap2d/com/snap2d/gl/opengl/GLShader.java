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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.media.opengl.*;

/**
 * Represents an individual OpenGL shader object.  GLShader loads and compiles
 * GLSL source files for use in a GLProgram.  All vertex shaders loaded via this
 * class will have the Snap2D transform vertex shader appended to the head of the
 * source string.  This means any vertex shaders passed into this class should NOT
 * declare a version string in the shader source file, rather the desired version
 * should be specified in the constructor.  The default GLSL version is 150 (GL 3.2).
 * The minimum required is 130 (GL 3.0), although this is not recommended.
 * @author Brian Groenke
 */
public class GLShader {

	public static final String DEFAULT_SHADER_PATH = "com/snap2d/gl/opengl/res/",
			DEFAULT_TRANSFORM_UTIL = "snap2d-transform.vert", ATTRIB_VERT_COORD = "vert_coord",
			ATTRIB_VERT_COLOR = "vert_color", ATTRIB_TEX_COORD = "tex_coord", DEFAULT_GLSL_VERSION = "150";

	public static final int TYPE_VERTEX = GL2.GL_VERTEX_SHADER, TYPE_FRAGMENT = GL2.GL_FRAGMENT_SHADER;

	private int sobj;
	
	/**
	 * Convenience constructor using the default GLSL version.  Equivalent to calling
	 * <code>new GLShader(type, GLShader.DEFAULT_GLSL_VERSION, sources)</code>
	 * @param type
	 * @param sources
	 * @throws GLShaderException
	 * @throws IOException
	 * @see #GLShader(int, String, String...)
	 */
	public GLShader(int type, String...sources) throws GLShaderException, IOException {
		this(type, DEFAULT_GLSL_VERSION, sources);
	}
	
	/**
	 * Convenience constructor using the default GLSL version.  Equivalent to calling
	 * <code>new GLShader(type, GLShader.DEFAULT_GLSL_VERSION, sources)</code>
	 * @param type
	 * @param sources
	 * @throws GLShaderException
	 * @throws IOException
	 * @see #GLShader(int, String, URL...)
	 */
	public GLShader(int type, URL...sources) throws GLShaderException, IOException {
		this(type, DEFAULT_GLSL_VERSION, sources);
	}

	/**
	 * Initialize and compile a new GLShader of the given type and version from the given sources.
	 * @param type
	 * @param version a valid GLSL version string; e.g. for shader version 330 (GL 3.3), use "330"
	 * @param sources
	 * @throws GLShaderException
	 * @throws IOException
	 */
	public GLShader(int type, String version, String...sources) throws GLShaderException, IOException {
		compile(type, version, sources);
	}

	/**
	 * Create a new GLShader of the given type and version.  Sources will be loaded and compiled from
	 * the given URLs.
	 * @param type
	 * @param version a valid GLSL version string; e.g. for shader version 330 (GL 3.3), use "330"
	 * @param sources
	 * @throws GLShaderException
	 * @throws IOException
	 */
	public GLShader(int type, String version, URL... sources) throws GLShaderException, IOException {
		ArrayList<String> sourceStrs = new ArrayList<String>();
		for(URL source : sources) {
			if(source == null)
				continue;
			InputStream in = source.openStream();
			sourceStrs.add(readFromStream(in));
		}
		compile(type, version, sourceStrs.toArray(new String[sourceStrs.size()]));
	}
	
	public int getShaderObj() {
		return sobj;
	}

	public void dispose() {
		final GL2ES2 gl = getGL();
		gl.glDeleteShader(sobj);
	}

	private void compile(int type, String version, String... sources) throws GLShaderException, IOException {
		final GL2ES2 gl = getGL();
		sobj = gl.glCreateShader(type);
		StringBuilder sb = new StringBuilder();
		sb.append("#version " + version);
		if(type == TYPE_VERTEX)
			sb.append(loadTransformShaderSource()); // append transform vertex shader source
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

	private String loadTransformShaderSource() throws IOException {
		InputStream in = GLShader.class.getResourceAsStream("res/snap2d-transform.vert");
		return readFromStream(in);
	}

	private String readFromStream(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine()) != null)
			sb.append(line + "\n");
		in.close();
		return sb.toString();
	}

	private GL2ES2 getGL() {
		return GLContext.getCurrentGL().getGL2GL3();
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
		for(String s : shaderFiles) {
			URL url = ClassLoader.getSystemClassLoader().getResource(DEFAULT_SHADER_PATH + s);
			if(url == null)
				throw(new IOException("failed to load library shader: " + s));
			urls.add(url);
		}
		GLShader shader = new GLShader(type, urls.toArray(new URL[urls.size()]));
		return shader;
	}
}
