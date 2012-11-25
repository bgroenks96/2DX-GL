package bg.x2d;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bg.x2d.anim.Drawable;
import bg.x2d.geo.Heptagon2D;
import bg.x2d.geo.Rotation;

abstract class TestLauncher {

	/**
	 * @param args
	 */
	
	static JFrame frame;
	private static Background background;
	
	public static void main(String[] args) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000,800);
		Panel p = new Panel();
		frame.setContentPane(p);
		frame.setVisible(true);
		new Thread(p).start();
	}

	static class Panel extends JPanel implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		Graphics2D g;
		volatile int x=100,y=100;
		volatile double theta=0;
		volatile boolean setup;
		GradientPaint gp = new GradientPaint(0,0,Color.blue, 300,300,Color.yellow,true);
		
		@Override
		public void paint(Graphics graphics) {
			Graphics2D g2 = (Graphics2D) graphics;
			if(background == null) {
				background = new Background(new Drawable() {

					@Override
					public void draw(Graphics2D g2) {
						g2.setColor(Color.black);
						g2.fillRect(0, 0, getWidth(), getHeight());
						g2.setColor(Color.white);
						g2.drawString("Alpha 0.1",5,10);
					}
					
				});
			} else background.redraw(g2);
			if(setup) {
				Heptagon2D t2d = new Heptagon2D(g2,background);
				t2d.drawHeptagon(x,y,400,gp,true);
				background.redraw(g2);
				t2d.rotate(theta, Rotation.CLOCKWISE);
			} else {
				setup = true;
			}
		}
		
		public void run() {
			while(!setup);
			while(true) {
				theta+=5.2;
				if(theta==360) theta = 0;
				repaint();
				try{
					Thread.sleep(35);
				} catch (InterruptedException ie) {}
			}
		}
	}
}
