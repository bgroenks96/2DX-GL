package bg.x2d;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import javax.swing.JFrame;

import bg.x2d.anim.Animation;
import bg.x2d.anim.ComboSegment;
import bg.x2d.anim.CurveSegment;
import bg.x2d.anim.DilationSegment;
import bg.x2d.anim.Drawable;
import bg.x2d.anim.Segment;
import bg.x2d.geo.Octagon2D;

abstract class TestLauncher {

	/**
	 * @param args
	 */

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
			}

		});
		Panel p = new Panel();
		frame.add(p);
		frame.setVisible(true);
		CurveSegment curve = new CurveSegment(100, 100, 200, 700, 300, 20, 800,
				600, 3000);
		DilationSegment rotate = new DilationSegment(0.25, 0.25, 1000);
		HashSet<Segment> aset1 = new HashSet<Segment>();
		aset1.add(rotate);
		aset1.add(curve);
		ComboSegment combo = new ComboSegment(aset1);
		anim = new Animation(new Segment[] { combo }, true);
		new Thread(p).start();
	}

	static class Panel extends Canvas implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		Graphics2D g;
		volatile int x = 100, y = 100, ix, iy, iz;
		volatile double theta = 0;
		GradientPaint gp = new GradientPaint(0, 0, Color.blue, 300, 300,
				Color.yellow, true);
		Octagon2D oct = new Octagon2D(100, 100, 200, gp, true);

		public void draw(Graphics2D g2) {
			g2.setColor(Color.RED);
			anim.draw(g2);
			oct.draw(g2);
			anim.release(g2);
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
