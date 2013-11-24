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

import java.util.*;
import java.util.concurrent.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import bg.x2d.*;

import com.snap2d.*;
import com.snap2d.gl.jogl.JOGLConfig.Property;

/**
 * @author Brian Groenke
 *
 */
public class GLRenderControl implements GLEventListener {

	public static final int DEFAULT_TARGET_FPS = 60, POSITION_LAST = 0x07FFFFFFF;

	int wt, ht;

	protected List<GLRenderable> rtasks = new ArrayList<GLRenderable>(),
			delQueue = new Vector<GLRenderable>();
	protected List<QueuedGLRenderable> addQueue = new Vector<QueuedGLRenderable>();
	protected GLCanvas canvas;
	protected GLHandle handle = new GLHandle();
	protected GLRenderLoop loop = new GLRenderLoop();
	protected volatile boolean updateConfig = true, vsync;
	protected volatile float gamma = 1.0f;

	private Semaphore loopChk = new Semaphore(1, true);

	GLRenderControl(GLCanvas canvas) {
		canvas.addGLEventListener(this);
		canvas.setAutoSwapBufferMode(true);	
		this.canvas = canvas;
	}

	public void setRenderActive(boolean active) {
		loop.active = active;
	}
	
	private volatile GLRenderable[] renderables = new GLRenderable[0]; // independent of task list

	/**
	 *
	 */
	@Override
	public void display(GLAutoDrawable arg0) {
		GL3bc gl = arg0.getGL().getGL3bc();
		if(updateConfig) {
			updateConfig(gl);
			updateConfig = false;
		}
		
		handle.gl = gl;
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		for(GLRenderable r : renderables) {
			r.render(handle, loop.interpolation);
		}
		
		//renderTest(gl);
	}

	/**
	 *
	 */
	@Override
	public void dispose(GLAutoDrawable arg0) {
		arg0.destroy();
	}

	/**
	 *
	 */
	@Override
	public void init(GLAutoDrawable arg0) {
		canvas.setIgnoreRepaint(true);
		ThreadManager.submitJob(loop);
		printInitReport();
	}

	/**
	 *
	 */
	@Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width,
			int height) {
		wt = width;
		ht = height;

		GL2 gl = arg0.getGL().getGL2();
		

	}

	private void updateConfig(GL3bc gl) {
		if(vsync)
			gl.setSwapInterval(1);
		else
			gl.setSwapInterval(0);
	}
	
	/**
	 * Registers the GLRenderable object with this GLRenderControl to be rendered on screen. The
	 * render(GLHandle,float) method will be called to draw to the OpenGL canvas.
	 * 
	 * @param r
	 *            the GLRenderable object to be called when rendering.
	 * @param pos
	 *            the position in the rendering queue to be placed. 0 is the first to be rendered on
	 *            each frame and LAST is provided as a convenience field to insert at position size
	 *            - 1 (aka the end of the queue, thus last to be rendered on each frame).
	 */
	public synchronized void addRenderable(GLRenderable r, int pos) {
		if (pos == POSITION_LAST) {
			pos = (addQueue.size() == 0) ? rtasks.size() : rtasks.size()
					+ addQueue.size();
		}
		QueuedGLRenderable qr = new QueuedGLRenderable();
		qr.pos = pos;
		qr.r = r;
		addQueue.add(qr);
	}

	/**
	 * Removes the GLRenderable object from the queue, if it exists.
	 * 
	 * @param r
	 *            removes the GLRenderable from the queue.
	 */
	public synchronized void removeRenderable(GLRenderable r) {
		delQueue.add(r);
	}
	
	public void setVSync(boolean enabled) {
		vsync = enabled;
		updateConfig = true;
	}
	
	public boolean isVSyncEnabled() {
		return vsync;
	}
	
	public void setGamma(float gamma) {
		this.gamma = gamma;
		updateConfig = true;
	}
	
	public float getGamma() {
		return gamma;
	}

	public void dispose() {

	}

	/**
	 * Loop where render/update logic is executed.  The loop is internally synchronized
	 * with the OpenGL rendering thread.
	 * <br/><br/>
	 * This class is a modified version of com.snap2d.gl.RenderControl.RenderLoop.
	 * 
	 * @author Brian Groenke
	 */
	protected class GLRenderLoop implements Runnable {

		// Default values
		private final double TARGET_FPS = 60,
				TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS,
				TICK_HERTZ = 30,
				TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ,
				MAX_UPDATES_BEFORE_RENDER = 3;

		private final long SLEEP_WHILE_INACTIVE = 100;

		private double targetFPS = TARGET_FPS,
				targetTimeBetweenRenders = TARGET_TIME_BETWEEN_RENDERS,
				tickHertz = TICK_HERTZ,
				timeBetweenUpdates = TIME_BETWEEN_UPDATES,
				maxUpdates = MAX_UPDATES_BEFORE_RENDER;

		volatile int fps, tps;
		volatile boolean running, active, noUpdate, printFrames;
		volatile float interpolation = 1.0f;

		@Override
		public void run() {
			Thread.currentThread().setName("snap2d-render_loop");

			ThreadManager.newDaemon(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("snap2d-sleeper_thread");
					try {
						if (Local.getPlatform().toLowerCase()
								.contains("windows")
								&& Boolean
								.getBoolean(Property.SNAP2D_WINDOWS_HIGH_RES_TIMER.getProperty())) {
							System.out
							.println("[Snap2D] started windows sleeper daemon");
							Thread.sleep(Long.MAX_VALUE);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

			ThreadManager.newDaemon(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("snap2d-fps_out_thread");
					while (running) {
						try {
							Thread.sleep(800);
							if (!Boolean
									.getBoolean(Property.SNAP2D_PRINT_RENDER_STAT.getProperty())) {
								continue;
							}
							while (!printFrames) {
								;
							}
							System.out.print("[Snap2D] ");
							System.out.println(fps + " fps " + tps + " ticks");
							printFrames = false;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			});

			double lastUpdateTime = System.nanoTime();
			double lastRenderTime = System.nanoTime();
			int lastSecondTime = (int) (lastUpdateTime / 1000000000);
			int frameCount = 0, ticks = 0;
			running = true;
			active = true;
			System.runFinalization();
			System.gc();
			while (running) {
				try {

					if (addQueue.size() > 0) {

						for (QueuedGLRenderable qr : addQueue) {
							rtasks.add(qr.pos, qr.r);
						}
						addQueue.clear();

						renderables = rtasks.toArray(new GLRenderable[rtasks
						                                            .size()]);
					}

					if (delQueue.size() > 0) {
						for (GLRenderable r : delQueue) {
							rtasks.remove(r);
						}
						delQueue.clear();
					}


					double now = System.nanoTime();
					if (active) {

						int updateCount = 0;

						while (now - lastUpdateTime > timeBetweenUpdates
								&& updateCount < maxUpdates && !noUpdate) {

							for (GLRenderable r : renderables) {
								r.update((long) now, (long) lastUpdateTime);
							}

							lastUpdateTime += timeBetweenUpdates;
							updateCount++;
							ticks++;
						}

						if (now - lastUpdateTime > timeBetweenUpdates
								&& !noUpdate) {
							lastUpdateTime = now - timeBetweenUpdates;
						}

						interpolation = Math
								.min(1.0f,
										(float) ((now - lastUpdateTime) / timeBetweenUpdates));
						canvas.display();
						lastRenderTime = now;
						frameCount++;

						int thisSecond = (int) (lastUpdateTime / 1000000000);
						if (thisSecond > lastSecondTime) {
							fps = frameCount;
							tps = ticks;
							printFrames = true;
							frameCount = 0;
							ticks = 0;
							lastSecondTime = thisSecond;
						}
					}

					loopChk.release();
					while (now - lastRenderTime < targetTimeBetweenRenders
							&& now - lastUpdateTime < timeBetweenUpdates) {
						Thread.yield();
						now = System.nanoTime();
					}
					loopChk.acquire();

					if (!active) {
						// preserve CPU if loop is currently is currently inactive.
						// the constant can be lowered to reduce latency when re-focusing.
						Thread.sleep(SLEEP_WHILE_INACTIVE);
					}
				} catch (Exception e) {
					System.err.println("Snapdragon2D: error in rendering loop");
					e.printStackTrace();
				}
			}
		}

		protected void setTargetFPS(int fps) {
			if (fps < 0) {
				return;
			}
			targetFPS = fps;
			targetTimeBetweenRenders = 1000000000.0 / targetFPS;
		}

		protected void setTargetTPS(int tps) {
			if (tps < 0) {
				return;
			}
			tickHertz = tps;
			timeBetweenUpdates = 1000000000.0 / tickHertz;
		}

		protected void setMaxUpdates(int max) {
			if (max > 0) {
				maxUpdates = max;
			}
		}
	}

	protected class QueuedGLRenderable {
		GLRenderable r;
		int pos;
	}
	
	private void printInitReport() {
		if(!Boolean.getBoolean(Property.SNAP2D_PRINT_GL_CONFIG.getProperty()))
			return;
		SnapLogger.println("initialized OpenGL graphics pipeline");
		GLContext ctxt = canvas.getContext();
		SnapLogger.println("OpenGL-version: " + ctxt.getGLVersion());
		boolean glsl = ctxt.hasGLSL();
		SnapLogger.println("GLSL-support=" + glsl);
		if(glsl)
			SnapLogger.print("GLSL-version: " + ctxt.getGLSLVersionString());
		for(JOGLConfig.Property jglp : Property.values()) {
			SnapLogger.println(jglp.getProperty() + "=" + jglp.getValue());
		}
	}
	
	void renderTest(GL2 gl) {
		gl.glOrtho(-500, 500, -500, 500, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor3f(0, 0, 1.0f);
		gl.glVertex2f(-400f, 200f);
		gl.glVertex2f(-400f, 0);
		gl.glVertex2f(-200f, 0);
		gl.glVertex2f(-200f, 200f);
		gl.glEnd();
		/*
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glColor3f(1, 0, 0);
		gl.glVertex2f(-1, -1);
		gl.glColor3f(0, 1, 0);
		gl.glVertex2f(0, 1);
		gl.glColor3f(0, 0, 1);
		gl.glVertex2f(1, -1);
		gl.glEnd();
		*/
	}

}
