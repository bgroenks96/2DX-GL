package bg.x2d;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.anim.*;
import bg.x2d.geo.*;
import bg.x2d.physics.*;

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

		Vector2d vec = new Vector2d(0, 0);
		Gravity grav = new Gravity();
		Rectangle rect = new Rectangle(300,50,100,100);

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
		}
		
		
		double xw = -100, yw = -50;
		
		World2D world;
		BufferedImage image;
		long last = System.currentTimeMillis();
		long zl = System.currentTimeMillis();
		double ppu = 1;
		public void draw(Graphics2D g2) {

			if(world == null) {
				world = new World2D(-getWidth()/2, -getHeight()/2, getWidth(), getHeight(), ppu);
			}
			if(System.currentTimeMillis() - last > 20) {
				xw++;
				yw--;
				last = System.currentTimeMillis();
			}
			
			if(System.currentTimeMillis() - zl > 1000) {
				world.setViewSize(getWidth(), getHeight(), ppu += 0.5);
				zl = System.currentTimeMillis();
			}
			Point p = world.worldToScreen(xw, yw);
			g2.setColor(Color.RED);
			g2.fillRect(p.x, p.y, (int) (20 * ppu), (int) (20 * ppu));
			
			/*
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, getWidth(), getHeight());
			if(image == null) {
				try {
					image = ImageLoader.load(new File("/media/WIN7/Users/Brian/Pictures/fnrr_flag.png").toURI().toURL());
					image = ImageLoader.scaleFrom(image, new Dimension(1920, 1080), ScaleQuality.HIGH, true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			g2.drawImage(image, 0, 0, null);
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
}
