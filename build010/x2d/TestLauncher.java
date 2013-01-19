package bg.x2d;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import bg.x2d.anim.Drawable;

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
		frame.add(p);
		frame.setVisible(true);
		new Thread(p).start();
	}

	static class Panel extends Canvas implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3706593387875272803L;

		Graphics2D g;
		volatile int x=100,y=100;
		volatile double theta=0;
		GradientPaint gp = new GradientPaint(0,0,Color.blue, 300,300,Color.yellow,true);
		
		public void draw(Graphics2D g2) {
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
			g2.drawRect(100, 100, 100, 100);
			g2.drawOval(100, 100, 100, 100);
		}
		
		public void run() {
			createBufferStrategy(3);
			while(true) {
				
				theta+=.1;
				if(theta==360) theta = 0;
				
				BufferStrategy bs = getBufferStrategy();
				Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();
				draw(g2);
				g2.dispose();
				bs.show();
				try{
					Thread.sleep(3);
				} catch (InterruptedException ie) {}
			}
		}
	}
}
