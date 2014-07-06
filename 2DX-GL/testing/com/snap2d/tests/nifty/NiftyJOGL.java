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

package com.snap2d.tests.nifty;

import java.io.*;
import java.net.URL;
import java.nio.*;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.*;

import org.junit.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.*;
import com.snap2d.niftygui.NewtInputSystem;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

/**
 * @author Brian Groenke
 *
 */
public class NiftyJOGL {

	final static String DEFAULT_VS = "stupid-simple.vert", TRANSFORM_VS = "snap2d-transform.vert",
			DEFAULT_FS = "stupid-simple.frag";

	Display newtDisp;
	Screen newtScreen;
	GLWindow glWin;

	Nifty nifty;
	NewtInputSystem newtInput;
	
	FPSAnimator anim;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NiftyJOGL().beginTest();
	}

	@Test
	public void beginTest() {
		init();
	}

	private void init() {
		GLProfile.initSingleton();
		GLProfile glp = GLProfile.getMaximum(true);
		GLCapabilities glc = new GLCapabilities(glp);
		newtDisp = NewtFactory.createDisplay(null);
		newtScreen = NewtFactory.createScreen(newtDisp, 0);
		glWin = GLWindow.create(newtScreen, glc);
		anim = new FPSAnimator(glWin, 30);
		glWin.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
		glWin.setSize(1100, 825);
		glWin.addGLEventListener(new RenderTest(glWin));
		glWin.setVisible(true);
		anim.start();
	}
	
	private void printInfo() {
		System.out.println("|-------------OpenGL System Info-------------|");
		GLContext context = glWin.getContext();
		System.out.println("JOGL profile: " + glWin.getGLProfile().getName());
		System.out.println("Vendor version: " + context.getGLVendorVersionNumber());
		System.out.println("OpenGL-version: " + context.getGLVersion() + " # " + context.getGLVersionNumber());
		System.out.println("GLSL-version: " + context.getGLSLVersionString());
		System.out.println("|--------------------------------------------|");
	}

	class RenderTest implements GLEventListener {

		int wt, ht, prog;

		int quadSize = 300;

		int[] vboInd = new int[1];
		int[] vaoInd = new int[1];

		final float[] coords, coords2;

		RenderTest(GLWindow win) {
			wt = win.getWidth();
			ht = win.getHeight();
			coords = new float[] {
					wt-wt / 4 - quadSize / 2, ht-ht / 4 - quadSize / 2,  // bottom left
					wt-wt / 4 - quadSize / 2, ht-ht / 4 + quadSize / 2,  // top left
					wt-wt / 4 + quadSize / 2, ht-ht / 4 - quadSize / 2,  // bottom right
					wt-wt / 4 + quadSize / 2, ht-ht / 4 + quadSize / 2 };// top right
			coords2 = new float[] {
					wt-wt / 4 - quadSize / 2, ht / 4 - quadSize / 2,  // bottom left
					wt-wt / 4 - quadSize / 2, ht / 4 + quadSize / 2,  // top left
					wt-wt / 4 + quadSize / 2, ht / 4 - quadSize / 2,  // bottom right
					wt-wt / 4 + quadSize / 2, ht / 4 + quadSize / 2 };// top right
		}

		/**
		 *
		 */
		@Override
		public void display(GLAutoDrawable arg0) {
			final GL2GL3 gl = arg0.getGL().getGL2GL3();
			
			gl.glClearColor(0,0,0,1);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glBindVertexArray(vaoInd[0]);
			
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glUseProgram(prog);
			gl.glMultiDrawArrays(GL2.GL_TRIANGLE_STRIP, new int[] {0,4}, 0, new int[]{4,4}, 0, 2);
			gl.glUseProgram(0);
			
			gl.glBindVertexArray(0);
			
		    nifty.update();
			nifty.render(false);
		}

		/**
		 *
		 */
		@Override
		public void dispose(GLAutoDrawable arg0) {
			arg0.getGL().glDeleteBuffers(1, vboInd, 0);
			anim.stop();
		}

		@Override
		public void init(GLAutoDrawable arg0) {
			final GL2GL3 gl = arg0.getGL().getGL2GL3();
			printInfo();
			loadShaders(gl);
			gl.glGenVertexArrays(1, vaoInd, 0);
			gl.glBindVertexArray(vaoInd[0]);
			
			gl.glGenBuffers(1, vboInd, 0);
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboInd[0]);
			gl.glEnableVertexAttribArray(0);
			gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glVertexAttribPointer(1, 4, GL.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 2 * Buffers.SIZEOF_FLOAT);
			gl.glBufferData(GL.GL_ARRAY_BUFFER, 48 * Buffers.SIZEOF_FLOAT, null, GL2.GL_STATIC_DRAW);
			ByteBuffer byteBuff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
			FloatBuffer floatBuff = byteBuff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			
			floatBuff.put(coords[0]); floatBuff.put(coords[1]);
			floatBuff.put(new float[] {1,0,0,1});
			floatBuff.put(coords[2]); floatBuff.put(coords[3]);
			floatBuff.put(new float[] {0,1,0,1});
			floatBuff.put(coords[4]); floatBuff.put(coords[5]);
			floatBuff.put(new float[] {0,0,1,1});
			floatBuff.put(coords[6]); floatBuff.put(coords[7]);
			floatBuff.put(new float[] {1,0,1,1});
			
			floatBuff.put(coords2[0]); floatBuff.put(coords2[1]);
			floatBuff.put(new float[] {1,0,1,1});
			floatBuff.put(coords2[2]); floatBuff.put(coords2[3]);
			floatBuff.put(new float[] {1,1,0,1});
			floatBuff.put(coords2[4]); floatBuff.put(coords2[5]);
			floatBuff.put(new float[] {0,1,0,1});
			floatBuff.put(coords2[6]); floatBuff.put(coords2[7]);
			floatBuff.put(new float[] {0,1,1,1});
			
			gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
			gl.glBindVertexArray(0);
			
			// upload transform to vertex shader
			gl.glUseProgram(prog);
			final FloatBuffer buff = GLUtils.createOrthoMatrix(0, wt, 0, ht, -1, 1);
			gl.glUniformMatrix4fv(getLocation("mOrtho"), 1, false, buff);
			gl.glUniform2f(getLocation("vTranslate"), 0, 0);
			gl.glUniform2f(getLocation("vScale"), 1, 1);
			gl.glUniform2f(getLocation("vPivot"), 0, 0);
			gl.glUniform1f(getLocation("fRotate"), 0);
			
			// set gamma
			gl.glUniform1f(getLocation("gamma"), 1);
			gl.glUseProgram(0);
			
			newtInput = new NewtInputSystem(glWin);
			nifty = new Nifty(new BatchRenderDevice(JoglBatchRenderBackendCoreProfileFactory.create()), new NullSoundDevice(), 
					newtInput, new AccurateTimeProvider());
			nifty.fromXml("nifty_text_test_screen.xml", "screen0");
		}

		/**
		 *
		 */
		@Override
		public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
			this.wt = width;
			this.ht = height;
		}
		
		public int getLocation(String uniform) {
			final GL2GL3 gl = GLContext.getCurrentGL().getGL2GL3();
			return gl.glGetUniformLocation(prog, uniform);
		}

		private void loadShaders(final GL2GL3 gl) {
			try {
				String src = read(ClassLoader.getSystemClassLoader().getResource(DEFAULT_VS));
				src = read(ClassLoader.getSystemClassLoader().getResource(TRANSFORM_VS)).concat(src);
				int vs = compileShader(gl, GL2.GL_VERTEX_SHADER, src);
				src = read(ClassLoader.getSystemClassLoader().getResource(DEFAULT_FS));
				int fs = compileShader(gl, GL2.GL_FRAGMENT_SHADER, src);
				prog = gl.glCreateProgram();
				gl.glAttachShader(prog, vs);
				gl.glAttachShader(prog, fs);
				linkProgram(gl);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private int compileShader(final GL2GL3 gl, int type, String src) throws Exception {
			int sobj = gl.glCreateShader(type);
			gl.glShaderSource(sobj, 1, new String[] {src}, null);
			gl.glCompileShader(sobj);

			//Check compile status.
			int[] compiled = new int[1];
			gl.glGetShaderiv(sobj, GL2.GL_COMPILE_STATUS, compiled,0);

			if(compiled[0] == GL.GL_FALSE) {
				int[] logLength = new int[1];
				gl.glGetShaderiv(sobj, GL2.GL_INFO_LOG_LENGTH, logLength, 0);

				byte[] log = new byte[logLength[0]];
				gl.glGetShaderInfoLog(sobj, logLength[0], (int[])null, 0, log, 0);

				throw(new Exception("error compiling shader: " + new String(log)));
			}
			return sobj;
		}

		private boolean linkProgram(final GL2GL3 gl) {
			gl.glLinkProgram(prog);
			gl.glValidateProgram(prog);
			IntBuffer intBuff = IntBuffer.allocate(1);
			gl.glGetProgramiv(prog, GL2.GL_LINK_STATUS, intBuff);
			return intBuff.get(0) == GL.GL_TRUE;
		}
	}

	static String read(URL url) throws IOException {
		Assert.assertNotNull(url);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = null;
		while((line=br.readLine()) != null) {
			sb.append(line+"\n");
		}
		br.close();
		return sb.toString();
	}
}
