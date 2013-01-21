package com.snap2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import bg.x2d.*;

import com.snap2d.gl.*;
import com.snap2d.input.*;
import com.snap2d.input.InputDispatch.KeyEventClient;
import com.snap2d.physics.*;
import com.snap2d.world.*;

public class SnapdragonTestLauncher {

	RenderControl rc;

	public static void main(String[] args) {
		new SnapdragonTestLauncher().init(args);
	}

	public void init(String[] args) {
		GLConfig config = GLConfig.getDefaultSystemConfig();
		Display disp = new Display(1120, 820, Display.Type.FULLSCREEN, config);
		disp.setTitle("Snapdragon2D Engine Test (PRE-ALPHA)");
		rc = disp.getRenderControl(2);
		InputDispatch input = new InputDispatch(true);
		input.registerKeyClient(new KeyEventClient() {

			@Override
			public void processKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					rc.dispose();
					System.exit(0);
				}
			}
		});
		Random r = new Random();
		World2D world = new World2D(-1000, -1000, 2000, 2000, 2);
		rc.addRenderable(new TestRenderBack(1,1), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestStaticRenderObj(200, 200), RenderControl.POSITION_LAST);
		EntityManager em = new EntityManager();
		for(int i = 0; i < 3; i++) {
			rc.addRenderable(new TestRenderObj(r.nextInt(100), r.nextInt(100), world, em), RenderControl.POSITION_LAST);
		}
		rc.addRenderable(em, RenderControl.POSITION_LAST);
		rc.setMaxUpdates(2);
		//rc.setRenderOp(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//rc.setRenderOp(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		rc.setUseHardwareAcceleration(true);
		//rc.setGammaCorrectionEnabled(true);
		//rc.setGamma(1.6f);
		rc.startRenderLoop();
		disp.show(rc);
	}

	static class TestRenderObj extends Entity implements Renderable {

		static BufferedImage img;
		static CollisionModel coll;

		static {
			try {
				img = ImageIO.read(new File(
						"/media/WIN7/Users/Brian/Pictures/test_alpha.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				coll = new CollisionModel(img, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int limitx, limity, ox;
		double x, y, lx, ly, theta = 10;
		boolean reverse;

		public TestRenderObj(int x, int y, World2D world, EntityManager em) {
			super(new Rectangle2D.Double(world.screenToWorld(x, y).x, world.screenToWorld(x, y).y,
					img.getWidth() / world.getPixelsPerUnit(), img.getHeight() / world.getPixelsPerUnit()), world);
			em.registerEntity(this);
			this.x = x;
			this.y = y;
			this.ox = x;
			this.limitx = 10*x;
			this.limity = 10*y;
			
		}

		@Override
		public void render(Graphics2D g, float interpolation) {

			double x1 = (x - lx) * interpolation + lx;
			double y1 = (y - ly) * interpolation + ly;
		    Point p = world.worldToScreen(x1, y1);
			g.drawImage(img, p.x, p.y, null);

			/*
			oct.setLocation(Math.round(x1), Math.round(y1));
			oct.rotate(theta, Rotation.CLOCKWISE);
			//oct.setPaint(new GradientPaint(x, y, Color.BLACK, x + 100.0f, y + 100.0f, Color.BLUE));
			oct.draw(g);
			 */
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		double last;
		final double INTERVAL = 3.333E7;
		@Override
		public void update(long now, long lastUpdate) {
			if(last == 0)
				last = now;
			if(now - last > INTERVAL) {
				lx = worldLoc.getX();
				ly = worldLoc.getY();
				if(reverse) {
					if(x == ox)
						reverse =  false;
					x-= 10;
					y+= 8;
				} else {
					if(x >= limitx)
						reverse = true;
					x+= 10;
					y-= 8;
				}
				
				this.setWorldLoc(x, y);

				theta += 0.1;
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
						"/media/WIN7/Users/Brian/Pictures/fnrr_flag.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void render(Graphics2D g, float interpolation) {

			//rc.render((int) Math.round((x - lx) * interpolation + lx), (int) Math.round((y - ly) * interpolation + ly), img.getWidth(), img.getHeight(), data);
			g.drawImage(img, (int)((x - lx) * interpolation + lx), (int) ((y - ly) * interpolation + ly), null);
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
		//int[] data;
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
			//data = ((DataBufferInt)bi.getData().getDataBuffer()).getData();
			this.wt = wt;
			this.ht = ht;
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			//rc.render(0, 0, wt, ht, data);
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
	
	static class EntityManager implements Renderable {
		
		Entity[] entities = new Entity[0];
		
		public void registerEntity(Entity e) {
			entities = Arrays.copyOf(entities, entities.length + 1);
			entities[entities.length - 1] = e;
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			//
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			//
		}
		
	}
}
