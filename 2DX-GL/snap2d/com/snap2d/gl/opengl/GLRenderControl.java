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

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.media.opengl.*;

import bg.x2d.Local;
import bg.x2d.utils.ConfigLogHandler;

import com.jogamp.common.util.VersionNumber;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.FBObject.TextureAttachment;
import com.jogamp.opengl.util.Gamma;
import com.snap2d.ThreadManager;
import com.snap2d.gl.CrashReportWindow;
import com.snap2d.gl.opengl.GLConfig.Property;
import com.snap2d.gl.spi.RenderController;

/**
 * @author Brian Groenke
 *
 */
public class GLRenderControl implements RenderController, GLEventListener {

	public static final int DEFAULT_TARGET_FPS = RenderController.DEFAULT_TARGET_FPS, 
			POSITION_LAST = RenderController.POSITION_LAST;

	private static final Logger log = Logger.getLogger(GLRenderControl.class.getCanonicalName());
	{
		log.setLevel(Level.CONFIG);
		log.addHandler(new ConfigLogHandler(""));
	}

	int wt, ht;

	protected List<GLRenderable> rtasks = new ArrayList<GLRenderable>(),
			delQueue = new Vector<GLRenderable>(), initQueue = new Vector<GLRenderable>();
	protected List<QueuedGLRenderable> addQueue = new Vector<QueuedGLRenderable>();
	protected GLConfig config;
	protected GLWindow glWin;
	protected GLHandle handle;
	protected GLRenderLoop loop = new GLRenderLoop();
	protected ThreadManager exec = new ThreadManager();
	protected volatile boolean updateDisplay = true, vsync;
	protected volatile float gamma = 1.0f;

	private Semaphore loopChk = new Semaphore(1, true);
	private CountDownLatch awaitShutdown;

	GLRenderControl(GLWindow glWin, GLConfig config) {
		this.config = config;
		glWin.addGLEventListener(this);
		glWin.setAutoSwapBufferMode(true);
		this.glWin = glWin;
	}

	private volatile GLRenderable[] renderables = new GLRenderable[0]; // independent of task list

	private FBObject fbo;

	/**
	 *
	 */
	@Override
	public void display(GLAutoDrawable arg0) {
		final GL2 gl = arg0.getGL().getGL2();
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		if(updateDisplay) {
			updateVSync();
			updateGamma();
			updateDisplay = false;
		}

		checkAddQueue();

		for(GLRenderable r : renderables) {
			GLProgram.enableDefaultProgram();
			r.render(handle, loop.interpolation);
		}

	}

	/**
	 *
	 */
	@Override
	public void dispose(GLAutoDrawable arg0) {
		Gamma.resetDisplayGamma(arg0.getGL());
		for(GLRenderable glr : renderables)
			glr.dispose(handle);
		fbo.destroy(arg0.getGL());
		handle.dispose();
	}

	/**
	 *
	 */
	@Override
	public void init(GLAutoDrawable arg0) {
		checkCompat();
		if(config.getAsBool(Property.GL_RENDER_COMPAT))
		    handle = new GL2Handle(config);
		else
			handle = new GL3Handle(config);
		fbo = new FBObject();
		printInitReport();
	}

	private void checkAddQueue() {
		if (addQueue.size() > 0) {

			for (QueuedGLRenderable qr : addQueue) {
				rtasks.add(qr.pos, qr.r);
				qr.r.init(handle);
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
		handle.setDisplaySize(width, height);
		wt = width;
		ht = height;

		checkAddQueue();
		
		final GL gl = arg0.getGL();
		fbo.reset(gl, wt, ht, glWin.getChosenGLCapabilities().getNumSamples(), true);
		fbo.attachTexture2D(gl, 0, true);
		fbo.syncSamplingSink(gl);

		for(GLRenderable r : renderables) {
			r.resize(handle, width, height);
		}
		Gamma.resetDisplayGamma(arg0.getGL());
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

	public boolean isRunning() {
		return loop.running;
	}

	public void setRenderActive(boolean active) {
		loop.active = active;
	}

	public boolean isRenderActive() {
		return loop.active;
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

	public synchronized boolean isRegistered(GLRenderable r) {
		return rtasks.contains(r);
	}
	
	public void setVSync(boolean enabled) {
		vsync = enabled;
		updateDisplay = true;
	}

	public boolean isVSyncEnabled() {
		return vsync;
	}

	private void updateVSync() {
		final GL gl = GLContext.getCurrentGL();
		if(vsync)
			gl.setSwapInterval(1);
		else
			gl.setSwapInterval(0);
	}
	
	private void updateGamma() {
		final GL gl = GLContext.getCurrentGL();
		if(handle.isGL2()) {
			Gamma.setDisplayGamma(gl, gamma, 0, 1);
		} else if(handle.isGL3()) {
			int currProg = GLUtils.glGetInteger(gl, GL2.GL_CURRENT_PROGRAM);
			GLProgram.enableDefaultProgram();
			if(GLProgram.isDefaultProgEnabled())
				GLProgram.getDefaultProgram().setUniformf("gamma", gamma);
			gl.getGL2GL3().glUseProgram(currProg);
		}
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
		updateDisplay = true;
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

	public void setDisableUpdates(boolean disable) {
		loop.noUpdate = disable;
	}
	
	public boolean isUpdating() {
		return !loop.noUpdate;
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

	public GLWindow getGLWindow() {
		return glWin;
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
	 * Disposes of this GLRenderControl and all of its internally held resources with the
	 * exception of the GLWindow object responsible for the GLContext.  Subsequent calls
	 * to a disposed GLRenderControl's methods will result in errors.  This method blocks for a
	 * maximum of five seconds or until the GLRenderLoop successfully completes its shutdown
	 * operations.
	 */
	public void dispose() {
		try {
			stopRenderLoop();
		} catch (InterruptedException e1) {
			log.warning("GLRenderControl.dispose: interrupted before shutdown completion");
		}
		rtasks.clear();
		addQueue.clear();
		delQueue.clear();
		renderables = null;
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
		private static final double TARGET_FPS = 60,
				TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS,
				TICK_HERTZ = 30,
				TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ,
				MAX_UPDATES_BEFORE_RENDER = 3;

		private static final long SLEEP_WHILE_INACTIVE = 100;

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
			log.info("GLRenderLoop: initializing...");

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
							boolean print = Boolean
									.getBoolean(Property.SNAP2D_PRINT_GLRENDER_STAT.getProperty());
							while (!printFrames) {
								;
							}
							String printStr = fps + " fps " + tps + " ticks";
							if(print)
								System.out.println("[Snap2D] " + printStr);
							log.fine(printStr);
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
								.min(1.0f, (float) ((now - lastUpdateTime) / timeBetweenUpdates));
						glWin.display();
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
						// preserve CPU if loop is currently is currently inactive.
						// the constant can be lowered to reduce latency when re-focusing.
						Thread.sleep(SLEEP_WHILE_INACTIVE);
					}
				} catch (InterruptedException e) {
					log.warning("snap2d-render_loop interrupted");
				} catch (Throwable e) {
					System.err.println("Snap2D: error in rendering loop: " + e.toString() + "\nTerminating loop execution...");
					CrashReportWindow crashDisp = new CrashReportWindow();
					crashDisp.dumpToLog("Unhandled error detected in rendering loop - aborting execution", e);
					crashDisp.setVisible(true);
					running = false;
				}
			}
			exec.newDaemon(new Runnable() {

				@Override
				public void run() {
					log.fine("GLRenderLoop: Shutting down rendering thread pool...");
					exec.shutdown();
					boolean success = false;
					try {
						success = exec.awaitTermination(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						final String successMsg = "GLRenderLoop: All threads terminated successfully";
						final String failMsg = "GLRenderLoop: Warning - not all threads terminated successfully";
						if(success) {
							if(Boolean.getBoolean(Property.SNAP2D_PRINT_GLRENDER_STAT.getProperty()))
								log.info(successMsg);
							else
								log.info(successMsg);
						} else {
							if(Boolean.getBoolean(Property.SNAP2D_PRINT_GLRENDER_STAT.getProperty()))
								log.warning(failMsg);
							else
								log.warning(failMsg);
						}
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

	private void checkCompat() {
		GLContext ctxt = glWin.getContext();
		boolean isGL32Core = ctxt.getGLVersionNumber().compareTo(GLContext.Version320) >= 0 &&
				ctxt.getGLSLVersionNumber().compareTo(new VersionNumber("3.20")) >=0;
		if(!isGL32Core) {
			log.warning("enabling compatibility mode");
			config.set(Property.GL_RENDER_COMPAT, "true");
		}
	}
	
	private void printInitReport() {
		if(!Boolean.getBoolean(Property.SNAP2D_PRINT_GL_CONFIG.getProperty()))
			return;
		log.info("initialized OpenGL graphics pipeline");
		GLContext ctxt = glWin.getContext();
		log.config("|--------OpenGL Configuration--------|");
		log.config("version-#: " + ctxt.getGLVersionNumber());
		log.config("version-info: " + ctxt.getGLVersion());
		log.config("vendor-version: " + ctxt.getGLVendorVersionNumber());
		log.config("jogl-profile: " + glWin.getGLProfile().getName());
		boolean glsl = ctxt.hasGLSL();
		log.config("glsl-support=" + glsl);
		if(glsl)
			log.config("glsl-version: " + ctxt.getGLSLVersionString());
		for(GLConfig.Property jglp : Property.values()) {
			log.config(jglp.getProperty() + "=" + config.get(jglp));
		}
	}

}
