package com.snap2d.gl;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import com.snap2d.*;

public class RenderControl {

	public static final int LAST = 0x07FFFFFFF;
	public static final Color CANVAS_BACK = Color.WHITE;
	public static final Color LIGHT_COLOR = Color.BLACK;

	public static int stopTimeout = 2000;

	protected Canvas canvas;
	protected volatile BufferedImage pri, light;
	protected volatile boolean auto, updateHints;

	protected List<Renderable> rtasks = Collections
			.synchronizedList(new ArrayList<Renderable>());
	protected RenderLoop loop;
	protected AutoResize resize;
	protected Future<?> taskCallback;
	protected int buffs;
	protected Map<RenderingHints.Key, Object> renderOps;
	protected int[] pixels;

	private RenderControl inst;

	public RenderControl(int buffs) {
		this.canvas = new Canvas();
		this.buffs = buffs;
		this.inst = this;

		renderOps = new HashMap<RenderingHints.Key, Object>();
		loop = new RenderLoop();
		auto = true;
		resize = new AutoResize();

		canvas.addComponentListener(resize);
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

	public synchronized void render(int xpos, int ypos, int wt, int ht, int[] colors) {
		for(int y = 0; y < ht; y++) {

			int yp = ypos + y;

			if(yp >= pri.getHeight() || yp < 0)
				continue;
			for(int x = 0; x < wt; x++) {

				int xp = xpos + x;

				if(xp >= pri.getWidth() || xp < 0)
					continue;

				pixels[yp * pri.getWidth() + xp ] = colors[y*wt + x];
			}
		}
	}

	protected void render() {
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null) {
			canvas.createBufferStrategy(buffs);
			canvas.requestFocus();
			return;
		}

		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setRenderingHints(renderOps);
		g.setColor(CANVAS_BACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.drawImage(pri, 0, 0, null);
		g.dispose();
		bs.show();
	}

	protected void renderLight() {

	}

	protected class RenderLoop implements Runnable {
		
		final int FPS_MAX = 60, FPS_MIN = 20;

		volatile int frames;
		volatile long sleepTime, last, lastMsg;
		volatile boolean running, active;

		@Override
		public void run() {
			Thread.currentThread().setName("snap2d-render_loop");
			/*
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					long last = 0;
					int prevFrames = 0;
					while (running) {
						if(System.currentTimeMillis() - last >= 1000) {
							System.out.println(frames + " fps @ " + (System.currentTimeMillis() - last));
							last = System.currentTimeMillis();
							frames = 0;
						}

						try {
							Thread.yield();
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			});
			t.setName("snap2d-render_monitor");
			t.setDaemon(true);
			t.start();
			 */
			last = System.nanoTime();
			while (running) {

				if (active && (canvas.getWidth() > 0 && canvas.getHeight() > 0)) {

					if(pri == null || light == null) {
						pri = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
						light = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
						pixels = ((DataBufferInt)pri.getRaster().getDataBuffer()).getData();
					}

					synchronized(rtasks) {
						Iterator<Renderable> tasks = rtasks.iterator();
						while(tasks.hasNext())
							tasks.next().render(inst);
					}

					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								render();
							}

						});
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					}

					frames++;

					long diff = System.nanoTime() - last;
					last = System.nanoTime();
					int fps = (int) Math.round(1000000000.0 / diff);
					if(System.currentTimeMillis() - lastMsg >= 1000) {
						System.out.println(fps + " fps @ " + sleepTime + " sleep");
						lastMsg = System.currentTimeMillis();
					}
					
					if(fps > FPS_MAX)
						sleepTime++;
					else if(fps < FPS_MIN)
						sleepTime--;
					
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
		}

	}
}
