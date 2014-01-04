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

import java.util.*;
import java.util.concurrent.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import bg.x2d.*;

import com.snap2d.*;
import com.snap2d.gl.*;
import com.snap2d.gl.jogl.JOGLConfig.Property;

/**
 * @author Brian Groenke
 *
 */
public class GLRenderControl implements GLEventListener {

	public static final int DEFAULT_TARGET_FPS = 60, POSITION_LAST = 0x07FFFFFFF;

	int wt, ht;

	protected List<GLRenderable> rtasks = new ArrayList<GLRenderable>(),
			delQueue = new Vector<GLRenderable>(), initQueue = new Vector<GLRenderable>();
	protected List<QueuedGLRenderable> addQueue = new Vector<QueuedGLRenderable>();
	protected JOGLConfig config;
	protected GLCanvas canvas;
	protected GLHandle handle;
	protected GLRenderLoop loop = new GLRenderLoop();
	protected ThreadManager exec = new ThreadManager();
	protected volatile boolean updateVSync = true, vsync;
	protected volatile float gamma = 1.0f;

	private Semaphore loopChk = new Semaphore(1, true);
	private CountDownLatch awaitShutdown;

	GLRenderControl(GLCanvas canvas, JOGLConfig config) {
		this.config = config;
		canvas.addGLEventListener(this);
		canvas.setAutoSwapBufferMode(true);	
		this.canvas = canvas;
	}

	public void setRenderActive(boolean active) {
		loop.active = active;
	}

	public boolean isRenderActive() {
		return loop.active;
	}

	private volatile GLRenderable[] renderables = new GLRenderable[0]; // independent of task list
	
	/**
	 *
	 */
	@Override
	public void display(GLAutoDrawable arg0) {
		GL2 gl = arg0.getGL().getGL2();

		if(updateVSync) {
			updateVSync(gl);
			updateVSync = false;
		}

		handle.gl = gl;
		
		checkAddQueue();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		for(GLRenderable r : renderables) {
			r.render(handle, loop.interpolation);
		}
	}

	/**
	 *
	 */
	@Override
	public void dispose(GLAutoDrawable arg0) {
		for(GLRenderable glr : renderables)
			glr.dispose(handle);
		handle.onDestroy();
	}

	/**
	 *
	 */
	@Override
	public void init(GLAutoDrawable arg0) {
		canvas.setIgnoreRepaint(true);
		handle = new GLHandle();
		handle.gl = arg0.getGL().getGL2();
		checkAddQueue();
		printInitReport();
	}
	
	private void checkAddQueue() {
		if (addQueue.size() > 0) {

			for (QueuedGLRenderable qr : addQueue) {
				rtasks.add(qr.pos, qr.r);
				qr.r.init(handle);;
			}
			
			addQueue.clear();

			renderables = rtasks.toArray(new GLRenderable[rtasks
			                                              .size()]);
		}
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
		handle.gl = gl;

		for(GLRenderable r : renderables) {
			r.onResize(handle, width, height);
		}
	}

	public void startRenderLoop() {
		if(loop.running)
			try {
				stopRenderLoop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		loop = new GLRenderLoop();
		awaitShutdown = new CountDownLatch(1);
		exec.submitJob(loop);
	}

	public void stopRenderLoop() throws InterruptedException {
		if(!loop.running)
			return;
		loopChk.acquire();
		loop.running = false;
		loopChk.release();
		awaitShutdown.await();
	}

	public boolean isLoopRunning() {
		return loop.running;
	}

	private void updateVSync(GL2 gl) {
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
		updateVSync = true;
	}

	public boolean isVSyncEnabled() {
		return vsync;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
		updateVSync = true;
	}

	public float getGamma() {
		return gamma;
	}
	
	/**
	 * Gets the last recorded number of frames rendered per second.
	 * 
	 * @return
	 */
	public int getCurrentFPS() {
		return loop.fps;
	}

	/**
	 * Gets the last recorded number of updates (ticks) per second.
	 * 
	 * @return
	 */
	public int getCurrentTPS() {
		return loop.tps;
	}

	/**
	 * Sets the frame rate that the rendering algorithm will target when interpolating.
	 * 
	 * @param fps
	 *            frames per second
	 */
	public void setTargetFPS(int fps) {
		loop.setTargetFPS(fps);
	}

	/**
	 * Sets the frequency per second at which the Renderable.update method is called.
	 * 
	 * @param tps
	 *            ticks per second
	 */
	public void setTargetTPS(int tps) {
		loop.setTargetTPS(tps);
	}

	/**
	 * Sets the max number of times updates can be issued before a render must occur. If animations
	 * are "chugging" or skipping, it may help to set this value to a very low value (1-2). Higher
	 * values will prevent the game updates from freezing.
	 * 
	 * @param maxUpdates
	 *            max number of updates to be sent before rendering.
	 */
	public void setMaxUpdates(int maxUpdates) {
		loop.setMaxUpdates(maxUpdates);
	}

	public GLCanvas getCanvas() {
		return canvas;
	}

	public void copyRenderablesTo(GLRenderControl rc) {
		rc.addQueue.addAll(addQueue);
		rc.delQueue.addAll(delQueue);
		for(GLRenderable glr : rtasks) {
			QueuedGLRenderable queued = new QueuedGLRenderable();
			queued.r = glr;
			queued.pos = rtasks.indexOf(glr);
			rc.addQueue.add(queued);
		}
	}

	/**
	 * Disposes of this GLRenderControl and all of its internally held resources.  Subsequent
	 * calls to the disposed object's methods will result in errors.  This method blocks for a
	 * maximum of five seconds or until the GLRenderLoop successfully completes its shutdown
	 * operations.
	 */
	public void dispose() {
		try {
			stopRenderLoop();
		} catch (InterruptedException e1) {
			SnapLogger.println("GLRenderControl.dispose: interrupted before shutdown completion");
		}
		canvas.destroy();
		rtasks.clear();
		delQueue.clear();
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
			SnapLogger.println("GLRenderLoop: initializing...");

			exec.newDaemon(new Runnable() {

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

			exec.newDaemon(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("snap2d-fps_out_thread");
					while (running) {
						try {
							Thread.sleep(850);
							if (!Boolean
									.getBoolean(Property.SNAP2D_PRINT_RENDER_STAT.getProperty())) {
								continue;
							}
							while (!printFrames) {
								//
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

					if (delQueue.size() > 0) {
						for (GLRenderable r : delQueue) {
							rtasks.remove(r);
						}
						delQueue.clear();
						
						renderables = rtasks.toArray(new GLRenderable[rtasks
						                                              .size()]);
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

						int thisSecond = (int) (now / 1000000000);
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
							&& (now - lastUpdateTime < timeBetweenUpdates || noUpdate)) {
						Thread.yield();
						now = System.nanoTime();
					}
					loopChk.acquire();

					if (!active) {
						System.out.println("hi");
						// preserve CPU if loop is currently is currently inactive.
						// the constant can be lowered to reduce latency when re-focusing.
						Thread.sleep(SLEEP_WHILE_INACTIVE);
					}
				} catch (InterruptedException e) {
					SnapLogger.println("snap2d-render_loop interrupted");
				} catch (Throwable e) {
					System.err.println("Snapdragon2D: error in rendering loop: " + e.toString() + "\nTerminating loop execution...");
					CrashReportWindow crashDisp = new CrashReportWindow();
					crashDisp.dumpToLog("Unhandled error detected in rendering loop - aborting execution", e);
					crashDisp.setVisible(true);
					running = false;
				}
			}
			exec.newDaemon(new Runnable() {

				@Override
				public void run() {
					SnapLogger.println("GLRenderLoop: Shutting down rendering thread pool...");
					exec.shutdown();
					boolean success = false;
					try {
						success = exec.awaitTermination(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						if(success)
							SnapLogger.println("GLRenderLoop: All threads terminated successfully");
						else
							SnapLogger.println("GLRenderLoop: Warning - not all threads terminated successfully");
						awaitShutdown.countDown();
					}
				}
			});
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
			SnapLogger.println(jglp.getProperty() + "=" + config.get(jglp));
		}
	}
}
