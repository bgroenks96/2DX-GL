package bg.x2d;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import bg.x2d.anim.Animation;
import bg.x2d.anim.DilationSegment;
import bg.x2d.anim.Drawable;
import bg.x2d.anim.RotationSegment;
import bg.x2d.anim.Segment;
import bg.x2d.anim.TranslationSegment;
import bg.x2d.geo.Triangle2D;

abstract class TestLauncher {

	/**
	 * @param args
	 */

	static JFrame frame;
	private static Background background;
	private static BufferedImage img;
	private static Animation anim;
	private static Point pt = new Point(0,0);
	private static Dimension panel = new Dimension(1,1);

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
		/*
		try {
			img = ImageUtils.scaleBufferedImage(ImageIO.read(new File(
					"C:/Users/Brian/Pictures/pdn_misc/spinner_02.png")),
					new Dimension(100, 100), BufferedImage.TYPE_INT_ARGB,
					Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		RotationSegment rs = new RotationSegment(120, 1000, pt);
		TranslationSegment trans = new TranslationSegment(100, 20, 800);
		TranslationSegment trans2 = new TranslationSegment(-100, -20, 400);
		DilationSegment scale = new DilationSegment(1.2, 1.2, panel, 500);
		DilationSegment scale2 = new DilationSegment(1/1.2, 1/1.2, panel, 500);
		List<Segment> segs = new ArrayList<Segment>();
		segs.add(rs);
		segs.add(trans);
		segs.add(trans2);
		segs.add(scale);
		segs.add(scale2);
		anim = new Animation(segs, true);
		new Thread(p).start();
	}

	static class Panel extends Canvas implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		Graphics2D g;
		volatile int x = 100, y = 100;
		volatile double theta = 0;
		GradientPaint gp = new GradientPaint(0, 0, Color.blue, 300, 300,
				Color.yellow, true);
		Triangle2D tri = new Triangle2D(200, 200, 400, Color.RED, true);

		public void draw(Graphics2D g2) {
			background.redraw(g2);
			pt.setLocation(tri.getLocation().getX() + tri.getSize() / 2, tri.getLocation().getY() + tri.getSize() / 2);
			panel.setSize(getSize());
			anim.draw(g2);
			tri.draw(g2);
			anim.release(g2);
		}

		@Override
		public void run() {
			createBufferStrategy(3);
			requestFocus();
			while (true) {
				
				theta = (theta < 360) ? theta+1:0;

				BufferStrategy bs = getBufferStrategy();
				Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();
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
