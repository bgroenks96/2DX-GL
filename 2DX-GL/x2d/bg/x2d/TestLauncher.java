package bg.x2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.anim.*;
import bg.x2d.utils.*;

import com.snap2d.*;
import com.snap2d.input.*;
import com.snap2d.physics.*;
import com.snap2d.world.*;

@SuppressWarnings("unused")
class TestLauncher {

	private TestLauncher() {

	}

	static JFrame frame;

	public static void main(String[] args) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 800);
		Panel p = new Panel();
		frame.add(p);
		frame.setVisible(true);
		new Thread(p).start();
	}

	static class Panel extends Canvas implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		private Background background;
		private BufferedImage img;
		private Animation anim;
		private Point pt = new Point(0, 0);
		private Dimension panel = new Dimension(1, 1);

		volatile int x = 100, y = 100, ix, iy, iz;
		volatile double theta = 1.0;
		
		volatile boolean up, down, left, right;
		static final int MOVE = 2;
		
		Entity e1, e2, e3;

		public Panel() {
			background = new Background(new Drawable() {

				@Override
				public void draw(Graphics2D g2) {
					g2.setColor(Color.BLACK);
					g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
					g2.setColor(Color.WHITE);
					g2.drawString("2DX-GL Alpha", 5, 15);
				}

			});getWidth();

			RotationSegment rs = new RotationSegment(270, 1200, new Point2D.Double(x + 40, y + 40));
			TranslationSegment ts = new TranslationSegment(400, 300, 1800);
			HashSet<Segment> set = new HashSet<Segment>();
			set.add(rs);
			set.add(ts);
			ComboSegment combo = new ComboSegment(set);
			anim = new Animation(new Segment[] {combo}, true);
			
			InputDispatch input = new InputDispatch(true);
			input.registerKeyClient(new InputDispatch.KeyEventClient() {

				@Override
				public void processKeyEvent(KeyEvent e) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_W:
						if(e.getID() == KeyEvent.KEY_PRESSED)
							up = true;
						else if(e.getID() == KeyEvent.KEY_RELEASED)
							up = false;
						break;
					case KeyEvent.VK_S:
						if(e.getID() == KeyEvent.KEY_PRESSED)
							down = true;
						else if(e.getID() == KeyEvent.KEY_RELEASED)
							down = false;
						break;
					case KeyEvent.VK_A:
						if(e.getID() == KeyEvent.KEY_PRESSED)
							left = true;
						else if(e.getID() == KeyEvent.KEY_RELEASED)
							left = false;
						break;
					case KeyEvent.VK_D:
						if(e.getID() == KeyEvent.KEY_PRESSED)
							right = true;
						else if(e.getID() == KeyEvent.KEY_RELEASED)
							right = false;
						
					}
				}
				
			});
		}
		
		
		double xw = -100, yw = -200, xw2 = -90, yw2 = -205;
		
		World2D world;
		BufferedImage image;
		long last = System.currentTimeMillis();
		long zl = System.currentTimeMillis();
		double ppu = 3;
		public void draw(Graphics2D g2) {

			if(world == null) {
				world = new World2D(-getWidth()/2, -getHeight()/2, getWidth(), getHeight(), ppu);
				
			}
			
			if(up)
				e1.setWorldLoc(e1.getWorldX(), e1.getWorldY()+MOVE);
			if(down)
				e1.setWorldLoc(e1.getWorldX(), e1.getWorldY()-MOVE);
			if(right)
				e1.setWorldLoc(e1.getWorldX()+MOVE,e1.getWorldY());
			if(left)
				e1.setWorldLoc(e1.getWorldX()-MOVE, e1.getWorldY());
			
			e1.render(g2, 1.0f);
			e2.render(g2, 1.0f);
			e3.render(g2, 1.0f);
			
			/*
			Point p = world.worldToScreen(xw, yw);
			Point p2 = world.worldToScreen(xw2, yw2);
			g2.setColor(Color.BLUE);
			g2.fillRect(p.x, p.y, (int) (20 * ppu), (int) (20 * ppu));
			g2.setColor(Color.YELLOW);
			g2.fillRect(p2.x, p2.y, (int) (20 * ppu), (int) (20 * ppu));
			
			Rectangle2D coll = world.checkCollision(new Rectangle2D.Double(xw, yw, 20, 20), new Rectangle2D.Double(xw2, yw2, 20, 20));
			if(coll == null)
				return;
			g2.setColor(Color.GREEN);
			Point p3 = world.worldToScreen(coll.getX(), coll.getY());
			g2.fillRect(p3.x, p3.y, (int) (coll.getWidth()* ppu), (int) (coll.getHeight() * ppu));
			*/
		}

		@Override
		public void run() {
			createBufferStrategy(3);
			requestFocus();
			ix = 10;
			iz = 10;
			iy = 10;
			while (true) {

				iy += 10;

				BufferStrategy bs = getBufferStrategy();
				Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();
				background.redraw(g2);
				draw(g2);
				g2.dispose();
				bs.show();
				try {
					Thread.sleep(10);
				} catch (InterruptedException ie) {
				}
			}
		}
	}
	
	static class TestObj extends Entity {
		
		static BufferedImage img;
		static CollisionModel coll;
		
		static {
			img = ImageLoader.load(Utils.getFileURL(new File("/media/WIN7/Users/Brian/Pictures/test_alpha.png")));
			coll = new CollisionModel(img, 0);
		}

		/**
		 * @param worldBounds
		 * @param world
		 */
		public TestObj(Rectangle2D worldBounds, World2D world) {
			super(worldBounds, world);
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			int x = getScreenX();
			int y = getScreenY();
			g.drawImage(img, x, y, null);
		}

		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			
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
}
