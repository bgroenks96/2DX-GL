package com.snap2d.gl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import bg.x2d.ImageUtils;

import com.snap2d.gl.RenderControl.Job;
import com.snap2d.input.InputDispatch;
import com.snap2d.input.InputDispatch.KeyEventClient;

public class Display {

	private int wt, ht;
	private Type type;
	private static JFrame frame;
	private static RenderControl rc;

	public Display(int width, int height, Type type) {
		this.wt = width;
		this.ht = height;
		this.type = type;
		init();
	}

	/**
	 * Internal method that performs initiation of the Display. Called by the
	 * constructor.
	 */
	protected void init() {
		switch (type) {
		case FULLSCREEN:
			frame = new JFrame();
			frame.setUndecorated(true);
			frame.setSize(getScreenSize());
			frame.setLocation(0, 0);
			break;
		case WINDOWED:
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(new Dimension(wt, ht));
			frame.setLocationRelativeTo(null);
			break;
		}
	}

	public void setTitle(String str) {
		frame.setTitle(str);
	}

	/**
	 * Convenience method for
	 * <code>Toolkit.getDefaultToolkit().getScreenSize()</code>.
	 * 
	 * @return the local monitor's screen dimensions.
	 */
	public Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public void show(RenderControl render) {
		rc = render;
		frame.add(rc.canvas);
		frame.setVisible(true);
	}

	public void hide() {
		// TODO: once render control methods are implemented, pause rendering
		frame.setVisible(false);
	}

	public void dispose() {
		// TODO: once render control methods are implemented, dispose render
		// control
		frame.dispose();
	}

	public enum Type {

		WINDOWED, FULLSCREEN;
	}

	public static void main(String[] args) {
		Display disp = new Display(800, 600, Display.Type.FULLSCREEN);
		disp.setTitle("Snapdragon2D Engine Test (PRE-ALPHA)");
		InputDispatch input = new InputDispatch(true);
		input.registerKeyClient(new KeyEventClient() {

			@Override
			public void processKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}

		});
		rc = new RenderControl(2);
		rc.setFrameSleep(0);
		rc.addRenderable(new TestRenderBack(1, 1), RenderControl.LAST);
		rc.addRenderable(new TestRenderObj(0, 0), RenderControl.LAST);
		rc.setRenderOp(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rc.startRenderLoop();
		disp.show(rc);
	}

	static class TestRenderObj implements Renderable {

		Job rjob;
		BufferedImage img;

		public TestRenderObj(int x, int y) {
			rjob = new Job();
			rjob.x = x;
			rjob.y = y;

			try {
				img = ImageIO.read(new File(
						"C:\\Users\\Brian\\Pictures\\fractal01.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
			} catch (IOException e) {
				e.printStackTrace();
			}
			rjob.bi = img;
		}

		long last = System.currentTimeMillis();

		@Override
		public void render() {

			if (System.currentTimeMillis() - last > 2) {
				rjob.x++;
				rjob.y++;
				last = System.currentTimeMillis();
			}

			rc.render(rjob);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

	}

	static class TestRenderBack implements Renderable {

		Job rjob;

		public TestRenderBack(int wt, int ht) {
			rjob = new Job();
			setup(wt, ht);
		}

		private void setup(int wt, int ht) {
			rjob.bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
			Graphics g = rjob.bi.createGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, wt, ht);
			g.dispose();
		}

		@Override
		public void render() {
			rc.render(rjob);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
				setup((int) newSize.getWidth(), (int) newSize.getHeight());
			}
		}

	}

}
