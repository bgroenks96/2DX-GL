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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.util.Random;

import bg.x2d.ImageUtils;
import bg.x2d.geo.*;
import bg.x2d.utils.Utils;

import com.jogamp.common.nio.Buffers;
import com.snap2d.ImageLoader;
import com.snap2d.gl.Display.Type;
import com.snap2d.gl.opengl.GLConfig.Property;
import com.snap2d.gl.opengl.GLHandle.AlphaFunc;
import com.snap2d.gl.opengl.GLHandle.BufferUsage;
import com.snap2d.gl.opengl.GLHandle.GLFeature;
import com.snap2d.gl.opengl.GLHandle.GeomFunc;
import com.snap2d.input.*;
import com.snap2d.ui.nifty.NiftyRenderable;
import com.snap2d.world.*;

/**
 * @author Brian Groenke
 *
 */
class JOGLTestLauncher {

	public static void main(String[] args) {
		GLConfig config = new GLConfig();
		config.set(Property.GL_RENDER_MSAA, "16");
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
		rc.addRenderable(new RenderText(rc), GLRenderControl.POSITION_LAST);
		gldisp.show();
		rc.setVSync(true);
		rc.startRenderLoop();
		//rc.setTargetFPS(5000);
	}

	static GLProgram prog;
	static GLShader vert, frag;
	static Random rand = new Random();

	static class TestObj implements GLRenderable {

		World2D world;
		Texture2D tex;
		int vwt, vht;
		Rect2D bounds = new Rect2D(-500, -500, 100, 100);

		int texCircleBuff, rectBuff, polyBuff;

		public TestObj(int vwt, int vht) {

		}

		final int N = 50;
		float tx=0,ty=0, lx=tx, ly=ty, theta;
		PointUD[] points = new PointUD[5];
		PointUD basePoint = new PointUD(200, 500), 
				origin = new PointUD(basePoint.ux, basePoint.uy - 50);
		@Override
		public void render(GLHandle handle, float interpolation) {

			handle.setTextureEnabled(true);
			handle.setRectTexCoords(tex);
			handle.setTextureMinFilter(GLHandle.FILTER_LINEAR, GLHandle.FILTER_NEAREST);
			handle.setEnabled(GLFeature.BLENDING, true);
			handle.setBlendFunc(AlphaFunc.SRC_BLEND);
			handle.bindTexture(tex);

			
			prog.enable();
			
			/*
			prog.setUniformi("tex_bound", 1);
			prog.setUniformi("tex", 0);
			Rectangle r = world.convertWorldRect(bounds);
			for(int i=1 ; i <= N ; i++) {
				handle.putQuad2f(texCircleBuff, rand.nextInt(1200), rand.nextInt(700), r.width, r.height, null);
			}

			handle.draw2f(texCircleBuff);
			*/
			
			
			handle.setTextureEnabled(false);
			prog.setUniformi("tex_bound", 0);

			float x = lx + (tx - lx) * interpolation;
			float y = ly + (ty - ly) * interpolation;
			lx = x;
			ly = y;
			handle.setColor4f(1f, 0f, 1f, 1f);
			handle.putQuad2f(rectBuff, x, y, 200, 200, null);
			handle.putQuad2f(rectBuff, 250 + x, 250 + y, 200, 200, null);
			handle.putQuad2f(rectBuff, 500 + tx, 500 + ty, 200, 200, null);
			handle.draw2f(rectBuff);
			handle.resetBuff(rectBuff);
			
			handle.setColor4f(1, 1, 0, 1);
			handle.putPoly2f(polyBuff, null, points);
			handle.draw2f(polyBuff);
			handle.resetBuff(polyBuff);
			
			handle.clearTransform();
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
			for(PointUD p : points)
				p.setLocation(p.ux+0.5, p.uy-=0.5);
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

			texCircleBuff = handle.createQuadBuffer2f(BufferUsage.STREAM_DRAW, N, true);
			rectBuff = handle.createQuadBuffer2f(BufferUsage.STREAM_DRAW, 3, false);
			polyBuff = handle.createPolyBuffer2f(BufferUsage.STREAM_DRAW, GeomFunc.TRIANGLE_FAN, points.length, 5, false);
			for(int i=0; i < points.length; i++) {
				points[i] = GeoUtils.rotatePoint(basePoint, origin, 2*Math.PI / points.length * i);
			}
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
			//handle.setColor4f(0.2f, 0.5f, 0.8f, 1f);
			prog.enable();
			prog.setUniformi("tex_bound", 0);
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
			prog.enable();
			handle.setViewport(0, 0, wt, ht, 1);
			handle.setColor4f(0.75f, 0.75f, 0.75f, 1f);
			handle.putQuad2f(buffId, 0, 0, wt, ht, null);
			prog.disable();
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			buffId = handle.createQuadBuffer2f(BufferUsage.STATIC_DRAW, 1, false);
			prog = new GLProgram(handle);
			try {
				try {
					String vertName = "test.vert";
					String fragName = "test.frag";
					vert = GLShader.loadLibraryShader(GLShader.TYPE_VERTEX, vertName);
					frag = GLShader.loadLibraryShader(GLShader.TYPE_FRAGMENT, fragName);
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
				prog.setUniformi("light_count", 3);
				FloatBuffer lights = Buffers.newDirectFloatBuffer(new float[] {300,300,900,600,600,450});
				prog.setUniformfv("lights", 2, lights);
				FloatBuffer lightColors = Buffers.newDirectFloatBuffer(new float[] {1,1,1,0.9f,0.6f,0.2f,0.5f,0.5f,1});
				prog.setUniformfv("light_colors", 3, lightColors);
				FloatBuffer radii = Buffers.newDirectFloatBuffer(new float[] {50, 100, 75});
				prog.setUniformfv("radius", 1, radii);
				FloatBuffer intensity = Buffers.newDirectFloatBuffer(new float[] {1, 5, 1.5f});
				prog.setUniformfv("intensity", 1, intensity);
				prog.setUniformf("ambient", 0.1f);
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
	
	static class RenderText implements GLRenderable {
		
		String text0 = "", text1 = "Seconds Elapsed: 00";
		
		int x0 = 10, y0 = 10, x1 = 50, y1 = 850;
		
		GLRenderControl rc;
		
		RenderText(GLRenderControl rc) {
			this.rc = rc;
		}

		/**
		 *
		 */
		@Override
		public void init(GLHandle handle) {
			handle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		}

		/**
		 *
		 */
		@Override
		public void dispose(GLHandle handle) {
			
		}

		/**
		 *
		 */
		@Override
		public void render(GLHandle handle, float interpolation) {
			String[] strs = new String[] {text0, text1};
			IntBuffer posBuff = Buffers.newDirectIntBuffer(new int[] {x0, y0, x1, y1});
			FloatBuffer colorBuff = Buffers.newDirectFloatBuffer(new float[] {1,1,1,1,0,0,1,1});
			handle.drawTextBatch(strs, posBuff, colorBuff);
		}

		int secs = 0;
		long last = 0;
		/**
		 *
		 */
		@Override
		public void update(long nanoTimeNow, long nanoTimeLast) {
			text0 = rc.getCurrentFPS()+" fps";
			if(Utils.nanoToSecs(nanoTimeNow - last) > 1) {
				text1 = "Seconds Elapsed: " + ((secs < 10) ? "0"+secs++ : secs++);
				last = nanoTimeNow;
			}
		}

		/**
		 *
		 */
		@Override
		public void resize(GLHandle handle, int wt, int ht) {
			
		}
	}

}
