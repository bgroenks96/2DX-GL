package bg.x2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.anim.*;
import bg.x2d.geo.*;
import bg.x2d.physics.*;

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

			});

			RotationSegment rs = new RotationSegment(270, 1200, new Point2D.Double(x + 40, y + 40));
			TranslationSegment ts = new TranslationSegment(400, 300, 1800);
			HashSet<Segment> set = new HashSet<Segment>();
			set.add(rs);
			set.add(ts);
			ComboSegment combo = new ComboSegment(set);
			anim = new Animation(new Segment[] {combo}, true);

			MouseAdapter ma = new MouseAdapter() {

				int prOffsX, prOffsY;
				boolean drag;

				@Override
				public void mousePressed(MouseEvent arg0) {
					if(rect.contains(arg0.getPoint())) {
						prOffsX = arg0.getX() - rect.x;
						prOffsY = arg0.getY() - rect.y;
						drag = true;
					}
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					drag = false;
				}

				@Override
				public void mouseDragged(MouseEvent me) {
					if(drag) {
						rect.setLocation(me.getX() - prOffsX, me.getY() - prOffsY);
					}
				}

			};

			addMouseListener(ma);
			addMouseMotionListener(ma);
		}

		StandardPhysics node = new StandardPhysics(vec, 1.0);
		GeneralForce normal = new GeneralForce(new Vector2d(0,9.807*1.0));
		GeneralForce xf = new GeneralForce(new Vector2d(15,0));
		Friction f = new Friction(0.5, 0.5, 0.4, 0.4, new Gravity());
		long i = System.currentTimeMillis();
		long last = System.nanoTime();
		long lm = i;
		public void draw(Graphics2D g2) {

			PointLD p = new PointLD(rect.getLocation());

			if(System.nanoTime() - last > 20000000) {
				//grav.applyTo((System.nanoTime() - last) / 100000000.0, 20.0, vec);
				if(System.currentTimeMillis() - i < 1500)
					node.applyForces((System.nanoTime() - last) / 1E9, f, normal, xf);
				else
					node.applyForces((System.nanoTime() - last) / 1E9, f, normal);
				node.getVelocity2d().applyTo(p, 10*(System.nanoTime() - last) / 1E9);
				//System.out.println(node.getVelocity2d().mag);
				last = System.nanoTime();
			}

			System.out.println(p);
			rect.setLocation(p.getIntX(), p.getIntY());

			g2.setColor(Color.CYAN);
			g2.fillRect(rect.x, rect.y, rect.width, rect.height);

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
