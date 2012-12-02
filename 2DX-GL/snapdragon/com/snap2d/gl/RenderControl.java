package com.snap2d.gl;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import bg.x2d.*;

import com.snap2d.*;

public class RenderControl {

	public static final int POSITION_LAST = 0x07FFFFFFF;
	public static final Color CANVAS_BACK = Color.WHITE;
	public static final Color LIGHT_COLOR = Color.BLACK;

	public static int stopTimeout = 2000;
	
	private static final long RESIZE_TIMER = 1 * (long) Math.pow(10, 8);

	public volatile boolean
	/**
	 * Determines whether or not auto-resizing should be used.  True by default.
	 */
	auto = true, 
	/**
	 * True if hardware acceleration (VolatileImage) should be used, false otherwise.
	 */
	accelerated = true;

	protected Canvas canvas;
	protected volatile BufferedImage pri, light;
	protected volatile VolatileImage disp;
	protected volatile int[] pixels;
	protected volatile long lastResizeFinish;
	protected volatile boolean updateHints;

	protected List<Renderable> rtasks = new ArrayList<Renderable>(), delQueue = new Vector<Renderable>();
	protected Map<Integer, Renderable> addQueue = new ConcurrentSkipListMap<Integer, Renderable>();
	protected RenderLoop loop;
	protected AutoResize resize;
	protected Future<?> taskCallback;
	protected int buffs;
	protected Map<RenderingHints.Key, Object> renderOps;

	private RenderControl inst;

	public RenderControl(int buffs) {
		this.canvas = new Canvas();
		this.buffs = buffs;
		this.inst = this;

		renderOps = new HashMap<RenderingHints.Key, Object>();
		loop = new RenderLoop();
		resize = new AutoResize();

		canvas.addComponentListener(resize);
		canvas.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				setRenderActive(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				setRenderActive(false);
			}
		});
	}

	public void startRenderLoop() {
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

	/**
	 * Fully releases system resources used by this RenderControl object and clears all registered Renderables.
	 * Note that once this method is called, the object is unusable and should be released for garbage collection.
	 * Continued use of a disposed RenderControl object will cause errors.
	 */
	public void dispose() {
		stopRenderLoop();
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs != null)
			bs.dispose();
		rtasks.clear();
		addQueue.clear();
		delQueue.clear();
		renderOps.clear();
		pri.flush();
		light.flush();
		disp.flush();

		// nullify references to potentially significant resource holders so that they are available
		// for garbage collection.
		canvas = null;
		loop = null;
		pri = null;
		light = null;
		disp = null;
		resize = null;
		taskCallback = null;
	}

	/**
	 * Gets the last recorded number of frames rendered per second.
	 * @return
	 */
	public int getCurrentFPS() {
		return loop.fps;
	}

	/**
	 * Gets the last recorded number of updates (ticks) per second.
	 * @return
	 */
	public int getCurrentTPS() {
		return loop.tps;
	}

	/**
	 * Sets the frame rate that the rendering algorithm will target when
	 * interpolating.
	 * @param fps frames per second
	 */
	public void setTargetFPS(int fps) {
		loop.setTargetFPS(fps);
	}

	/**
	 * Sets the frequency per second at which the Renderable.update method is called.
	 * @param tps ticks per second
	 */
	public void setTargetTPS(int tps) {
		loop.setTargetTPS(tps);
	}

	/**
	 * Sets the max number of times updates can be issued before a render must occur.
	 * If animations are "chugging" or skipping, it may help to set this value to a very
	 * low value (0-2).  Higher values will prevent the game updates from freezing.
	 * @param maxUpdates max number of updates to be sent before rendering.
	 */
	public void setMaxUpdates(int maxUpdates) {
		loop.setMaxUpdates(maxUpdates);
	}

	/**
	 * Checks to see if this RenderControl has a loop that is actively rendering/updating.
	 * @return true if active, false otherwise.
	 */
	public boolean isActive() {
		return loop.active;
	}

	/**
	 * Checks to see if this RenderControl has a currently running loop.
	 * @return true if a loop is running, false otherwise.
	 */
	public boolean isRunning() {
		return loop.running;
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
		if (pos == POSITION_LAST) {
			pos = (addQueue.size() == 0) ? rtasks.size():rtasks.size() + addQueue.size();
		}
		addQueue.put(Integer.valueOf(pos), r);
	}

	/**
	 * Removes the Renderable object from the queue, if it exists.
	 * 
	 * @param r
	 *            removes the Renderable from the queue.
	 */
	public synchronized void removeRenderable(Renderable r) {
		delQueue.add(r);
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

	/**
	 * Renders pixel data to the display buffer (not yet shown at the time of this call).
	 * @param xpos
	 * @param ypos
	 * @param wt
	 * @param ht
	 * @param colors
	 */
	public synchronized void render(int xpos, int ypos, int wt, int ht, int[] colors) {
		for(int y = 0; y < ht; y++) {

			int yp = ypos + y;

			if(yp >= pri.getHeight() || yp < 0)
				continue;

			for(int x = 0; x < wt; x++) {
				int xp = xpos + x;

				if(xp >= pri.getWidth() || xp < 0)
					continue;

				int pos = yp * pri.getWidth() + xp;
				if(pos < 0 || pos >= pixels.length)
					continue;
				
				pixels[pos] = colors[y*wt + x];
			}
		}
	}

	/**
	 * Internal method that is called by RenderLoop to draw rendered data to the screen.
	 * If hardware acceleration is enabled, the buffer's data is drawn to a VolatileImage.
	 * Otherwise, the buffer image itself is drawn.
	 */
	protected synchronized void render() {
		// If the component was being resized, cancel rendering until finished (prevents flickering).
		if(System.nanoTime() - lastResizeFinish < RESIZE_TIMER)
			return;
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null) {
			canvas.createBufferStrategy(buffs);
			canvas.requestFocus();
			return;
		}

		Graphics2D g = (Graphics2D) bs.getDrawGraphics();

		if(accelerated) {
			// Check the status of the VolatileImage and update/re-create it if neccessary.
			if(disp == null || disp.getWidth() != pri.getWidth() || disp.getHeight() != pri.getHeight()) {
				disp = ImageUtils.createVolatileImage(pri.getWidth(), pri.getHeight(), g);
				Graphics2D img = disp.createGraphics();
				img.drawRenderedImage(pri, new AffineTransform());
				img.dispose();
			}
			int stat = 0;
			do {
				if((stat=ImageUtils.validateVI(disp, g)) != VolatileImage.IMAGE_OK) {
					if(stat == VolatileImage.IMAGE_INCOMPATIBLE) {
						disp = ImageUtils.createVolatileImage(pri.getWidth(), pri.getHeight(), g);
					}
				}

				Graphics2D img = disp.createGraphics();
				img.drawRenderedImage(pri, new AffineTransform());
				img.dispose();
			} while(disp.contentsLost());
		} else {
			// If acceleration is now disabled but was previously enabled, release system resources held by
			// VolatileImage and set the reference to null.
			if(disp != null) {
				disp.flush();
				disp = null;
			}
		}

		g.setRenderingHints(renderOps);
		g.setColor(CANVAS_BACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		if(disp != null)
			g.drawImage(disp, 0, 0, null);
		else
			g.drawImage(pri, 0, 0, null);
		g.drawImage(light, 0, 0, null);
		g.dispose();
		bs.show();
	}

	protected synchronized void renderLight() {

	}

	private void createImages(int wt, int ht) {
		pri = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
		light = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt)pri.getRaster().getDataBuffer()).getData();
	}

	/**
	 * Loop where render/update logic is executed.
	 * @author Brian Groenke
	 *
	 */
	protected class RenderLoop implements Runnable {

		// Default values
		private final double TARGET_FPS = 60, TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS, TICK_HERTZ = 30, 
				TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ, MAX_UPDATES_BEFORE_RENDER = 3;

		private double targetFPS = TARGET_FPS, targetTimeBetweenRenders = TARGET_TIME_BETWEEN_RENDERS, tickHertz = TICK_HERTZ, 
				timeBetweenUpdates = TIME_BETWEEN_UPDATES, maxUpdates = MAX_UPDATES_BEFORE_RENDER;

		volatile int fps, tps;
		volatile boolean running, active;

		@Override
		public void run() {
			Thread.currentThread().setName("snap2d-render_loop");

			ThreadManager.newDaemon(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("snap2d-sleeper_thread");
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

			double lastUpdateTime = System.nanoTime();
			double lastRenderTime = System.nanoTime();
			int lastSecondTime = (int) (lastUpdateTime / 1000000000);
			int frameCount = 0, ticks = 0;
			running = true;
			active = true;
			while (running) {
				try {

					if(addQueue.size() > 0) {

						for(Integer i:addQueue.keySet()) {
							rtasks.add(i, addQueue.get(i));
						}
						addQueue.clear();
					}

					if(delQueue.size() > 0) {
						for(Renderable r:delQueue) {
							rtasks.remove(r);
						}
						delQueue.clear();
					}

					double now = System.nanoTime();
					if (active && (canvas.getWidth() > 0 && canvas.getHeight() > 0)) {

						if(pri == null || light == null) {
							createImages(canvas.getWidth(), canvas.getHeight());
						}

						int updateCount = 0;

						while( now - lastUpdateTime > timeBetweenUpdates && updateCount < maxUpdates ) {
							Iterator<Renderable> tasks = rtasks.iterator();
							while(tasks.hasNext())
								tasks.next().update((long) lastUpdateTime);
							lastUpdateTime += timeBetweenUpdates;
							updateCount++;
							ticks++;
						}

						if ( now - lastUpdateTime > timeBetweenUpdates)
						{
							lastUpdateTime = now - timeBetweenUpdates;
						}

						float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / timeBetweenUpdates));
						Iterator<Renderable> tasks = rtasks.iterator();
						while(tasks.hasNext())
							tasks.next().render(inst, interpolation);
						render();
						lastRenderTime = now;
						frameCount++;

						int thisSecond = (int) (lastUpdateTime / 1000000000);
						if (thisSecond > lastSecondTime) {
							fps = frameCount;
							tps = ticks;
							System.out.println(fps + " fps " + tps + " ticks");
							frameCount = 0;
							ticks = 0;
							lastSecondTime = thisSecond;
						}
					}

					while (now - lastRenderTime < targetTimeBetweenRenders && now - lastUpdateTime < timeBetweenUpdates) {
						Thread.yield();

						now = System.nanoTime();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		protected void setTargetFPS(int fps) {
			if(fps < 0)
				return;
			targetFPS = fps;
			targetTimeBetweenRenders = 1000000000.0 / targetFPS;
		}

		protected void setTargetTPS(int tps) {
			if(tps < 0)
				return;
			tickHertz = tps;
			timeBetweenUpdates = 1000000000.0 / tickHertz;
		}

		protected void setMaxUpdates(int max) {
			if(max >= 0)
				maxUpdates = max;
		}
	}

	protected class AutoResize extends ComponentAdapter {

		private int wt, ht;

		@Override
		public void componentResized(ComponentEvent e) {
			if (auto) {
				resize(e.getComponent().getWidth(), e.getComponent()
						.getHeight());
				Iterator<Renderable> itr = rtasks.listIterator();
				while (itr.hasNext()) {
					itr.next().onResize(
							new Dimension(wt, ht),
							new Dimension(e.getComponent().getWidth(), e
									.getComponent().getHeight()));
				}
				
				lastResizeFinish = System.nanoTime();
			}
		}

		protected void resize(int wt, int ht) {
			if (wt <= 0) {
				wt = 1;
			}
			if (ht <= 0) {
				ht = 1;
			}

			createImages(wt, ht);
			this.wt = wt;
			this.ht = ht;
		}

	}
}
