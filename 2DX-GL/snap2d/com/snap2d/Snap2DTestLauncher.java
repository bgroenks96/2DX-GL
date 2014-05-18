package com.snap2d;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.*;
import java.util.Random;

import javax.imageio.ImageIO;

import bg.x2d.ImageUtils;

import com.snap2d.gl.*;
import com.snap2d.input.*;
import com.snap2d.physics.GamePhysics;
import com.snap2d.world.*;

public class Snap2DTestLauncher {

	Display disp;
	RenderControl rc;

	public static void main(String[] args) {
		new Snap2DTestLauncher().init(args);
	}

	public void init(String[] args) {
		GraphicsConfig config = GraphicsConfig.getDefaultSystemConfig();
		disp = new Display(1120, 820, Display.Type.WINDOWED, config);
		disp.setTitle("Snap2D Engine Test (PRE-ALPHA)");
		rc = disp.getRenderControl(2);
		InputDispatch input = new InputDispatch(true);
		World2D world = new World2D(-1000, -1000, 2000, 2000, 2);
		final EntityManager em = new EntityManager();
		input.registerKeyClient(new KeyEventClient() {

			@Override
			public void processKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					disp.dispose();
					em.unregisterAll();
					System.exit(0);
				}
			}
		});
		Random r = new Random();
		rc.addRenderable(new TestRenderBack(1, 1), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestStaticRenderObj(200, 200),
				RenderControl.POSITION_LAST);
		for (int i = 0; i < 20; i++) {
			Entity e = new TestRenderObj(r.nextInt(200) - r.nextInt(1500),
					r.nextInt(200) - r.nextInt(1500), world);
			em.register(e);
		}
		rc.addRenderable(em, RenderControl.POSITION_LAST);
		rc.setMaxUpdates(2);
		rc.setUseHardwareAcceleration(true);
		disp.show();
		rc.startRenderLoop();
	}

	static class TestRenderObj extends Entity implements Renderable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7461311108451088948L;
		static BufferedImage img;
		static CollisionModel coll;

		static {
			try {
				img = ImageIO.read(new File(
						"/media/WIN7/Users/Brian/Pictures/test_alpha.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				coll = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		int limitx, limity, ox;
		double x, y, lx, ly;
		boolean reverse;

		public TestRenderObj(int x, int y, World2D world) {
			super(new Point2D.Double(world.screenToWorld(x, y).x,
					world.screenToWorld(x, y).y), world);
			initBounds(img.getWidth(), img.getHeight());
			this.x = x;
			this.y = y;
			this.ox = x;
			this.limitx = Math.abs(2 * x);
			this.limity = Math.abs(2 * y);

		}

		@Override
		public void render(Graphics2D g, float interpolation) {

			double x1 = (x - lx) * interpolation + lx;
			double y1 = (y - ly) * interpolation + ly;
			Point p = world.worldToScreen(x1, y1);
			g.drawImage(img, p.x, p.y, null);

			/*
			 * oct.setLocation(Math.round(x1), Math.round(y1)); oct.rotate(theta,
			 * Rotation.CLOCKWISE); //oct.setPaint(new GradientPaint(x, y, Color.BLACK, x + 100.0f,
			 * y + 100.0f, Color.BLUE)); oct.draw(g);
			 */
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		double last;
		final double INTERVAL = 3.333E7;

		@Override
		public void update(long now, long lastUpdate) {
			if (last == 0) {
				last = now;
			}
			if (now - last > INTERVAL) {
				lx = worldLoc.getX();
				ly = worldLoc.getY();
				if (reverse) {
					if (x == ox) {
						reverse = false;
					}
					x -= 10;
					y += 8;
				} else {
					if (x >= limitx) {
						reverse = true;
					}
					x += 10;
					y -= 8;
				}

				this.setWorldLoc(x, y);

				last += INTERVAL;
			}
		}

		@Override
		public void setAllowRender(boolean render) {

		}

		@Override
		public GamePhysics getPhysics() {
			return null;
		}

		@Override
		public CollisionModel getCollisionModel() {
			return coll;
		}

	}

	static class TestStaticRenderObj implements Renderable {

		BufferedImage img;
		int[] data;
		int x, y, lx, ly;

		public TestStaticRenderObj(int x, int y) {
			this.x = x;
			this.y = y;
			try {
				img = ImageIO.read(new File(
						"/media/WIN7/Users/Brian/Pictures/fractal01.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt) img.getRaster().getDataBuffer())
						.getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void render(Graphics2D g, float interpolation) {

			// rc.render((int) Math.round((x - lx) * interpolation + lx), (int) Math.round((y - ly)
			// * interpolation + ly), img.getWidth(), img.getHeight(), data);
			g.drawImage(img, (int) ((x - lx) * interpolation + lx),
					(int) ((y - ly) * interpolation + ly), null);
			lx = x;
			ly = y;
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		@Override
		public void update(long now, long last) {

		}
	}

	static class TestRenderBack implements Renderable {

		BufferedImage bi;
		// int[] data;
		int wt, ht;

		public TestRenderBack(int wt, int ht) {
			setup(wt, ht);
		}

		private void setup(int wt, int ht) {
			bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, wt, ht);
			g.dispose();
			// data = ((DataBufferInt)bi.getData().getDataBuffer()).getData();
			this.wt = wt;
			this.ht = ht;
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			// rc.render(0, 0, wt, ht, data);
			g.drawImage(bi, 0, 0, null);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
				setup((int) newSize.getWidth(), (int) newSize.getHeight());
			}
		}

		@Override
		public void update(long now, long last) {
			//
		}

	}
}
