package com.snap2d.gl;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import com.snap2d.*;

public class RenderControl {

	public static final int POSITION_LAST = 0x07FFFFFFF;
	public static final Color CANVAS_BACK = Color.WHITE;
	public static final Color LIGHT_COLOR = Color.BLACK;

	public static int stopTimeout = 2000;

	protected Canvas canvas;
	protected volatile BufferedImage pri, light;
	protected volatile boolean auto, updateHints;

	protected List<Renderable> rtasks = new ArrayList<Renderable>(), delQueue = new Vector<Renderable>();
	protected Map<Integer, Renderable> addQueue = new ConcurrentSkipListMap<Integer, Renderable>();
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
				setRenderActive(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				setRenderActive(false);
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

		// nullify references to potentially significant resource holders so that they are available
		// for garbage collection.
		canvas = null;
		loop = null;
		pri = null;
		light = null;
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
	 * Gets the last recorded number of updates per second.
	 * @return
	 */
	public int getCurrentTPS() {
		return loop.tps;
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

	/**
	 * Set whether or not this RenderControl should attempt to auto-resize it's components when it is resized.
	 * @param resize
	 */
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
		System.out.println(xpos);
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

	protected synchronized void render() {
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
		g.drawImage(light, 0, 0, null);
		g.dispose();
		bs.show();
	}

	protected synchronized void renderLight() {

	}

	protected class RenderLoop implements Runnable {

		final double TARGET_FPS = 60, TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS, TICK_HERTZ = TARGET_FPS / 2, 
				TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ, MAX_UPDATES_BEFORE_RENDER = 3;

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
							pri = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
							light = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
							pixels = ((DataBufferInt)pri.getRaster().getDataBuffer()).getData();
						}

						int updateCount = 0;

						while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER ) {
							Iterator<Renderable> tasks = rtasks.iterator();
							while(tasks.hasNext())
								tasks.next().update();
							System.out.println("after");
							lastUpdateTime += TIME_BETWEEN_UPDATES;
							updateCount++;
							ticks++;
						}

						if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES)
						{
							lastUpdateTime = now - TIME_BETWEEN_UPDATES;
						}

						float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
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

					while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
						Thread.yield();

						now = System.nanoTime();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected class AutoResize implements ComponentListener {

		private int wt, ht;

		@Override
		public void componentResized(ComponentEvent e) {
			Iterator<Renderable> itr = rtasks.listIterator();
			while (itr.hasNext()) {
				itr.next().onResize(
						new Dimension(wt, ht),
						new Dimension(e.getComponent().getWidth(), e
								.getComponent().getHeight()));
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
