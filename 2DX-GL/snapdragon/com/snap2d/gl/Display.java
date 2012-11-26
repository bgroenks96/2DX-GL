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
		rc.setRenderActive(false);
		frame.setVisible(false);
	}

	public void dispose() {
		rc.dispose();
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
		rc.addRenderable(new TestRenderBack(1, 1), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(200), r.nextInt(200)), RenderControl.POSITION_LAST);
		rc.setRenderOp(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		rc.startRenderLoop();
		disp.show(rc);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
	}

	static class TestRenderObj implements Renderable {

		BufferedImage img;
		int[] data;
		int x, y, lx, ly;

		public TestRenderObj(int x, int y) {
			this.x = x;
			this.y = y;
			try {
				img = ImageIO.read(new File(
						"C:\\Users\\Brian\\Pictures\\fnrr_flag.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void render(RenderControl rc, float interpolation) {

			rc.render((int) Math.round((x - lx) * interpolation + lx), (int) Math.round((y - ly) * interpolation + ly), img.getWidth(), img.getHeight(), data);
			lx = x;
			ly = y;
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		@Override
		public void update() {
			x+=6;
			y+=2;
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
		public void render(RenderControl rc, float interpolation) {
			rc.render(0, 0, wt, ht, data);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
				setup((int) newSize.getWidth(), (int) newSize.getHeight());
			}
		}

		@Override
		public void update() {
			//
		}

	}

}
