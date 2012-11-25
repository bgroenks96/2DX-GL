package com.snap2d.gl;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import bg.x2d.ImageUtils;

import com.snap2d.ThreadManager;

public class RenderControl {

	public static final int LAST = 0x07FFFFFFF;
	public static final Color CANVAS_BACK = Color.WHITE;
	public static final Color LIGHT_COLOR = Color.BLACK;

	public static int stopTimeout = 2000;

	protected Canvas canvas;
	protected volatile BufferedImage pri, light;
	protected volatile boolean auto, valid, updateHints;
	protected List<Renderable> rtasks = Collections
			.synchronizedList(new ArrayList<Renderable>());
	protected RenderLoop loop;
	protected Future<?> taskCallback;
	protected int buffs;
	protected AutoResize autoResize;
	protected Map<RenderingHints.Key, Object> renderOps;

	public RenderControl(int buffs) {
		canvas = new Canvas();
		this.buffs = buffs;
		renderOps = new HashMap<RenderingHints.Key, Object>();
		loop = new RenderLoop();
		autoResize = new AutoResize();
		auto = true;
		canvas.addComponentListener(autoResize);
		canvas.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				loop.active = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				loop.active = false;
			}

		});
	}

	public void startRenderLoop() {
		loop.running = true;
		loop.active = true;
		taskCallback = ThreadManager.submitJob(loop);
	}

	public void setRenderActive(boolean active) {
		loop.active = active;
	}

	public void stopRenderLoop() {
		loop.running = false;
		loop.active = false;

		long st = System.currentTimeMillis();
		while (!taskCallback.isDone()) {
			if (System.currentTimeMillis() - st > stopTimeout) {
				taskCallback.cancel(true);
				break;
			}
		}
	}

	public void setFrameSleep(long milis) {
		loop.sleepTime = milis;
	}

	public int getCurrentFPS() {
		return loop.frames;
	}

	/**
	 * Registers the Renderable object with this RenderControl to be rendered on
	 * screen. The render() method will be called and the RenderData object used
	 * to show the image on screen.
	 * 
	 * @param r
	 *            the Renderable object to be called when rendering.
	 * @param pos
	 *            the position in the rendering queue to be placed. 0 is the
	 *            first to be rendered on each frame and LAST is provided as a
	 *            convenience field to insert at position size - 1 (aka the end
	 *            of the queue, thus last to be rendered on each frame).
	 */
	public synchronized void addRenderable(Renderable r, int pos) {
		if (pos == LAST) {
			pos = rtasks.size();
		}
		rtasks.add(pos, r);
	}

	/**
	 * Removes the Renderable object from the queue, if it exists.
	 * 
	 * @param r
	 *            removes the Renderable from the queue.
	 */
	public synchronized void removeRenderable(Renderable r) {
		rtasks.remove(r);
	}

	/**
	 * Fetches List.size() for the rendering queue.
	 * 
	 * @return
	 */
	public int getRenderQueueSize() {
		return rtasks.size();
	}

	public synchronized void addLight() {

	}

	public synchronized void removeLight() {

	}

	public void setAutoResize(boolean resize) {
		auto = resize;
	}

	public void setRenderOp(Key key, Object value) {
		renderOps.put(key, value);
		updateHints = true;
	}

	public void setRenderOps(Map<Key, ?> hints) {
		renderOps.putAll(hints);
		updateHints = true;
	}

	public Object getRenderOpValue(Key key) {
		return renderOps.get(key);
	}

	public void render(Job rjob) {
		Graphics2D g2 = pri.createGraphics();
		if (rjob.bi.getType() != pri.getType()) {
			rjob.bi = ImageUtils.convertBufferedImage(rjob.bi, pri.getType());
		}

		g2.drawImage(rjob.bi, rjob.x, rjob.y, null);
		g2.dispose();
		
		/*
		 * int x = rdata.x, y = rdata.x; BufferedImage src = rdata.img; int[]
		 * sdata = ((DataBufferInt)src.getRaster().getDataBuffer()).getData();
		 * int[] pdata =
		 * ((DataBufferInt)pri.getRaster().getDataBuffer()).getData(); for (int
		 * cy = 0; cy < src.getHeight(); cy++) { int yPixel = cy + y; if (yPixel
		 * < 0 || yPixel >= pri.getHeight()) continue; for (int cx = 0; cx <
		 * src.getWidth(); cx++) { int xPixel = cx + x; if (xPixel < 0 || xPixel
		 * >= pri.getWidth()) continue; int srcPixel = sdata[cx + cy *
		 * src.getWidth()]; if ((srcPixel >> 24) < 255 && (srcPixel >> 24) > 0)
		 * pdata[xPixel + yPixel * pri.getWidth()] = (pdata[xPixel + yPixel *
		 * pri.getWidth()] + srcPixel) / 2; else if(srcPixel != 0) pdata[xPixel
		 * + yPixel * pri.getWidth()] = srcPixel; } }
		 */
	}

	protected void renderLight() {

	}

	protected class RenderLoop implements Runnable {

		volatile int frames;
		volatile long sleepTime;
		volatile boolean running, active;

		@Override
		public void run() {
			Thread.currentThread().setName("snap2d-render_loop");
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					while (running) {
						System.out.println(frames + " fps");
						frames = 0;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			});
			t.setName("snap2d-render_monitor");
			t.setDaemon(true);
			t.start();
			while (running) {

				if (active && (canvas.getWidth() > 0 && canvas.getHeight() > 0)) {

					if (canvas.getBufferStrategy() == null) {
						canvas.createBufferStrategy(buffs);
						continue;
					}

					if (pri == null || light == null) {
						valid = false;
					}

					BufferStrategy bs = canvas.getBufferStrategy();
					Graphics2D g2d = (Graphics2D) bs.getDrawGraphics();

					if (!valid) {
						pri = new BufferedImage(canvas.getWidth(),
								canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
						light = new BufferedImage(canvas.getWidth(),
								canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
						valid = true;
					}

					g2d.setColor(CANVAS_BACK);
					g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // draw
																				// back
																				// canvas
																				// (prevents
																				// flashing)

					synchronized (rtasks) {

						Iterator<Renderable> itr = rtasks.listIterator();
						while (itr.hasNext()) {
							itr.next().render();
						}

					}

					renderLight();

					if (updateHints) {
						g2d.setRenderingHints(renderOps);
						updateHints = false;
					}
					g2d.drawRenderedImage(pri, new AffineTransform());
					g2d.dispose();

					bs.show();

					frames++;
				}

				Thread.yield();
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected class AutoResize implements ComponentListener {

		private int wt, ht;

		@Override
		public void componentResized(ComponentEvent e) {
			synchronized (rtasks) {
				Iterator<Renderable> itr = rtasks.listIterator();
				while (itr.hasNext()) {
					itr.next().onResize(
							new Dimension(wt, ht),
							new Dimension(e.getComponent().getWidth(), e
									.getComponent().getHeight()));
				}
			}
			if (auto) {
				resize(e.getComponent().getWidth(), e.getComponent()
						.getHeight());
			}
		}

		@Override
		public void componentMoved(ComponentEvent e) {

		}

		@Override
		public void componentShown(ComponentEvent e) {

		}

		@Override
		public void componentHidden(ComponentEvent e) {

		}

		protected void resize(int wt, int ht) {
			if (wt <= 0) {
				wt = 1;
			}
			if (ht <= 0) {
				ht = 1;
			}
			this.wt = wt;
			this.ht = ht;
			valid = false;
		}

	}

	public static class Job {

		public volatile BufferedImage bi;
		public volatile int x, y;
		public volatile ImageObserver monitor;
	}
}
