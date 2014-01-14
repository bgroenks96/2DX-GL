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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.FloatBuffer;
import java.util.Random;

import bg.x2d.ImageUtils;

import com.jogamp.common.nio.Buffers;
import com.snap2d.ImageLoader;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.jogl.GLHandle.AlphaFunc;
import com.snap2d.gl.jogl.GLHandle.BufferUsage;
import com.snap2d.gl.jogl.GLHandle.GLFeature;
import com.snap2d.gl.jogl.GLProgram.UniformType;
import com.snap2d.gl.jogl.JOGLConfig.Property;
import com.snap2d.input.*;
import com.snap2d.ui.nifty.NiftyRenderable;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 *
 */
class JOGLTestLauncher {

	public static void main(String[] args) {
		JOGLConfig config = new JOGLConfig();
		config.set(Property.SNAP2D_RENDER_MSAA, "16");
		final GLDisplay gldisp = new GLDisplay(1600, 900, Type.WINDOWED, config);
		gldisp.setExitOnClose(true);
		gldisp.initInputSystem(false);
		gldisp.addKeyListener(new GLKeyAdapter() {

			@Override
			public void keyPressed(GLKeyEvent event) {
				if(event.getKeyCode() == GLKeyEvent.VK_ESCAPE) {
					gldisp.dispose();
				}
			}
			
		});
		final GLRenderControl rc = gldisp.getRenderControl();
		rc.addRenderable(new TestObj(gldisp.getWidth(), gldisp.getHeight()), GLRenderControl.POSITION_LAST);
		rc.addRenderable(new TestBack(), 0);
		rc.addRenderable(new NiftyRenderable(gldisp), GLRenderControl.POSITION_LAST);
		gldisp.show();
		rc.startRenderLoop();
		rc.setTargetFPS(8000);
	}

	static GLProgram prog;
	static GLShader vert, frag;
	static Random rand = new Random();

	static class TestObj implements GLRenderable {

		World2D world;
		Texture2D tex;
		int vwt, vht;
		Rect2D bounds = new Rect2D(-500, -500, 100, 100);

		int buffId, buff2;

		public TestObj(int vwt, int vht) {

		}

		final int N = 50;
		float tx=0,ty=0, lx=tx, ly=ty;
		@Override
		public void render(GLHandle handle, float interpolation) {

			handle.setTextureEnabled(true);
			handle.setTexCoords(tex);
			handle.setTextureMinFilter(GLHandle.FILTER_LINEAR, GLHandle.FILTER_LINEAR);
			handle.setEnabled(GLFeature.BLENDING, true);
			handle.setBlendFunc(AlphaFunc.SRC_BLEND);
			handle.bindTexture(tex);


			prog.enable();
			/*
			prog.setUniform("tex_bound", 1, UniformType.INT, 1);
			prog.setUniform("tex", 1, UniformType.INT, 0);
			Rectangle r = world.convertWorldRect(bounds);
			for(int i=1 ; i <= N ; i++) {
				handle.putQuad2f(buffId, rand.nextInt(1200), rand.nextInt(700), r.width, r.height);
			}

			handle.draw2f(buffId);
			*/
			handle.setTextureEnabled(false);
			prog.setUniform("tex_bound", 1, UniformType.INT, 0);

			float x = lx + (tx - lx) * interpolation;
			float y = ly + (ty - ly) * interpolation;
			lx = x;
			ly = y;
			handle.putQuad2f(buff2, x, y, 200, 200);
			handle.putQuad2f(buff2, 250 + x, 250 + y, 200, 200);
			//handle.putQuad2f(buff2, 500 + tx++, 500 + ty++, 200, 200);
			handle.setColor4f(1f, 0f, 1f, 1f);
			handle.draw2f(buff2);
			handle.resetBuff(buff2);
			prog.disable();

			handle.setEnabled(GLFeature.BLENDING, false);

			/*
			Rect2D coll = world.checkCollision(bounds, b2);
			if(coll == null)
				return;
			Rectangle r3 = world.convertWorldRect(coll);
			handle.setColor3f(0, 0, 1f);
			handle.
			//handle.putQuad2f(buff2, 500 + tx++, 500 + ty++, 200, 2drawRect2f(r3.x, r3.y, r3.width, r3.height);
			 */
		}
		
		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			tx+=2; ty+=1;
		}


		float ppu = 1f;
		@Override
		public void resize(GLHandle handle, int wt, int ht) {
			vwt = wt; vht = ht;
			world = GLUtils.createGLWorldSystem(-800, -450, vwt, vht, ppu);
			world.setViewSize(vwt, vht, ppu);
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			try {
				BufferedImage bimg = ImageLoader.load(new URL("file:/media/WIN7/Users/Brian/Pictures/test_alpha.png"));
				bimg = ImageUtils.convertBufferedImage(bimg, BufferedImage.TYPE_INT_ARGB_PRE);
				tex = ImageLoader.loadTexture(bimg, true);
				//tex = ImageLoader.loadTexture(new URL("file:/media/WIN7/Users/Brian/Pictures/fnrr_flag.png"), ImageLoader.PNG, true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			buffId = handle.createQuadBuff2f(BufferUsage.STREAM_DRAW, N, true);
			buff2 = handle.createQuadBuff2f(BufferUsage.STREAM_DRAW, 3, false);
			/*
			for(int i=1 ; i <= N ; i++) {
				handle.putQuad2f(buffId, rand.nextInt(1000), rand.nextInt(700), 100, 100);
			}
			 */
		}

		/**
		 *
		 */
		@Override
		public void dispose(GLHandle handle) {

		}

	}

	static class TestBack implements GLRenderable {

		int wt, ht, buffId;

		@Override
		public void render(GLHandle handle, float interpolation) {
			handle.setColor4f(0,0,0,1);
			handle.setColor4f(0.5f, 0.5f, 0.5f, 0.5f);
			prog.enable();
			prog.setUniform("tex_bound", 1, UniformType.INT, 0);
			handle.draw2f(buffId);
			prog.disable();
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {

		}

		@Override
		public void resize(GLHandle handle, int wt, int ht) {
			this.wt = wt;
			this.ht = ht;
			handle.setViewport(0, 0, wt, ht, 1);
			prog.enable();
			handle.putQuad2f(buffId, 0, 0, wt, ht);
			prog.disable();
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			buffId = handle.createQuadBuff2f(BufferUsage.STATIC_DRAW, 1, false);
			prog = new GLProgram(handle);
			try {
				try {
					String vertName = "test.vert";
					String fragName = "test.frag";
					vert = GLShader.loadDefaultShader(handle, vertName, GLShader.TYPE_VERTEX);
					frag = GLShader.loadDefaultShader(handle, fragName, GLShader.TYPE_FRAGMENT);
				} catch (GLShaderException e) {
					System.err.println("error compiling shader");
					System.err.println(e.getExtendedMessage());
				}

				prog.attachShader(vert);
				prog.attachShader(frag);
				if(!prog.link()) {
					prog.printLinkLog();
				}
				prog.enable();
				prog.setUniform("light_count", 1, UniformType.INT, 2);
				FloatBuffer lights = Buffers.newDirectFloatBuffer(new float[] {300,300,900,600});
				prog.setUniformv("lights", 2, UniformType.FLOAT, lights.capacity(), lights);
				FloatBuffer lightColors = Buffers.newDirectFloatBuffer(new float[] {1,1,1,0.9f,0.6f,0.2f});
				prog.setUniformv("light_colors", 3, UniformType.FLOAT, lightColors.capacity(), lightColors);
				prog.setUniform("radius", 1, UniformType.FLOAT, 100);
				prog.setUniform("intensity", 1, UniformType.FLOAT, 1);
				prog.setUniform("ambient", 1, UniformType.FLOAT, 0.1f);
				prog.disable();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 *
		 */
		@Override
		public void dispose(GLHandle handle) {
			if(prog.getHandle() != handle)
				prog.setHandle(handle);
			prog.dispose();
		}
	}

}
