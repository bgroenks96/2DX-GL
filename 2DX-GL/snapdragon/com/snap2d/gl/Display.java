package com.snap2d.gl;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import bg.x2d.*;

import com.snap2d.input.*;
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
		rc = new RenderControl(3);
		Random r = new Random();
		rc.addRenderable(new TestRenderBack(1, 1), RenderControl.LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(400), r.nextInt(400)), RenderControl.LAST);
		rc.setRenderOp(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rc.startRenderLoop();
		disp.show(rc);
	}

	static class TestRenderObj implements Renderable {

		BufferedImage img;
		int[] data;
		volatile int x, y;
		boolean started;

		public TestRenderObj(int x, int y) {
			this.x = x;
			this.y = y;
			try {
				img = ImageIO.read(new File(
						"C:\\Users\\Brian\\Pictures\\upload.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		long last = System.currentTimeMillis();

		@Override
		public void render(RenderControl rc) {

			if (System.currentTimeMillis() - last >= 20) {
				x+=4;
				y+=4;
				last = System.currentTimeMillis();
			}
			
			rc.render(x, y, img.getWidth(), img.getHeight(), data);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

	}

	static class TestRenderBack implements Renderable {

		int[] data;
		int wt, ht;

		public TestRenderBack(int wt, int ht) {
			setup(wt, ht);
		}

		private void setup(int wt, int ht) {
			BufferedImage bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, wt, ht);
			g.dispose();
			data = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
			this.wt = wt;
			this.ht = ht;
		}

		@Override
		public void render(RenderControl rc) {
			rc.render(0, 0, wt, ht, data);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
				setup((int) newSize.getWidth(), (int) newSize.getHeight());
			}
		}

	}

}
