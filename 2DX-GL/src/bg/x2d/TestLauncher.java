package bg.x2d;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.anim.*;
import bg.x2d.geo.*;
import bg.x2d.physics.*;

@SuppressWarnings("unused")
class TestLauncher {

	private TestLauncher() {
		
	}

	static JFrame frame;
	private static Background background;
	private static BufferedImage img;
	private static Animation anim, a2;
	private static Point pt = new Point(0, 0);
	private static Dimension panel = new Dimension(1, 1);

	public static void main(String[] args) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 800);
		background = new Background(new Drawable() {

			@Override
			public void draw(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				g2.setColor(Color.WHITE);
				g2.drawString("2DX-GL Alpha", 5, 15);
			}

		});
		Panel p = new Panel();
		frame.add(p);
		frame.setVisible(true);
		RotationSegment rotate = new RotationSegment(270, 1000, new Point(200,200));
		DilationSegment dilate = new DilationSegment(0.25, 0.25, 1000);
		HashSet<Segment> aset1 = new HashSet<Segment>();
		aset1.add(dilate);
		aset1.add(rotate);
		ComboSegment combo = new ComboSegment(aset1);
		anim = new Animation(new Segment[] { dilate }, true);
		new Thread(p).start();
	}

	static class Panel extends Canvas implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		Graphics2D g;
		volatile int x = 100, y = 100, ix, iy, iz;
		volatile double theta = 1.0;
		GradientPaint gp = new GradientPaint(0, 0, Color.blue, 300, 300,
				Color.yellow, true);
		Octagon2D oct = new Octagon2D(100, 100, 200, gp, true);
		Vector2d vec = new Vector2d(0, -125);
		Gravity grav = new Gravity();
		PointLD p = new PointLD(200.0, 779);

		long last = System.nanoTime() - 20000000;
		public void draw(Graphics2D g2) {
			g2.setColor(Color.RED);
			if(System.nanoTime() - last > 20000000) {
				grav.applyTo((System.nanoTime() - last) / 100000000.0, 20.0, vec);
				vec.applyTo(p, (System.nanoTime() - last) / 100000000.0);
				last = System.nanoTime();
			}
			if(p.getIntY() >= 780) {
				p.setLocation(p.getX(), 755.0);
			}
			g2.fillRect(p.getIntX(), p.getIntY(), 20, 20);
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
