package com.snap2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import bg.x2d.*;

import com.snap2d.gl.*;
import com.snap2d.input.*;
import com.snap2d.input.InputDispatch.KeyEventClient;

public class SnapdragonTestLauncher {

	RenderControl rc;

	public static void main(String[] args) {
		new SnapdragonTestLauncher().init(args);
	}

	public void init(String[] args) {
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
		Random r = new Random();
		rc.addRenderable(new TestRenderBack(1,1), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestStaticRenderObj(200, 200), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.addRenderable(new TestRenderObj(r.nextInt(300), r.nextInt(300)), RenderControl.POSITION_LAST);
		rc.setMaxUpdates(2);
		rc.setRenderOp(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		rc.setRenderOp(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		rc.setUseHardwareAcceleration(true);
		rc.startRenderLoop();
		disp.show(rc);
	}

	static class TestRenderObj implements Renderable {

		BufferedImage img;
		int[] data;
		int limitx, limity, ox;
		float x, y, lx, ly;
		boolean reverse;

		public TestRenderObj(int x, int y) {
			this.x = x;
			this.y = y;
			this.ox = x;
			this.limitx = 10*x;
			this.limity = 10*y;
			
			try {
				img = ImageIO.read(new File(
						"/media/WIN7/Users/Brian/Pictures/test_alpha.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			

			//rc.render((int)((x - lx) * interpolation + lx), (int) ((y - ly) * interpolation + ly), img.getWidth(), img.getHeight(), data);
			float x1 = (x - lx) * interpolation + lx;
			float y1 = (y - ly) * interpolation + ly;
			g.drawImage(img, Math.round(x1), Math.round(y1), null);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		double last;
		final double INTERVAL = 3.333E7;
		@Override
		public void update(long now, long lastUpdate) {
			if(last == 0)
				last = now;
			if(now - last > INTERVAL) {
				lx = x;
				ly = y;
				if(reverse) {
					if(x == ox)
						reverse =  false;
					x-= 10;
					y-= 8;
				} else {
					if(x >= limitx)
						reverse = true;
					x+= 10;
					y+= 8;
				}
				
				last += INTERVAL;
			}
		}

	}

	static class TestStaticRenderObj implements Renderable {

		BufferedImage img;
		int[] data;
		int x, y, lx, ly;

		public TestStaticRenderObj(int x, int y) {
			this.x = x;
			this.y = y;
			try {
				img = ImageIO.read(new File(
						"/media/WIN7/Users/Brian/Pictures/fnrr_flag.png"));
				img = ImageUtils.convertBufferedImage(img,
						BufferedImage.TYPE_INT_ARGB);
				data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void render(Graphics2D g, float interpolation) {

			//rc.render((int) Math.round((x - lx) * interpolation + lx), (int) Math.round((y - ly) * interpolation + ly), img.getWidth(), img.getHeight(), data);
			g.drawImage(img, (int)((x - lx) * interpolation + lx), (int) ((y - ly) * interpolation + ly), null);
			lx = x;
			ly = y;
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

		}

		@Override
		public void update(long now, long last) {

		}

	}

	static class TestRenderBack implements Renderable {

		BufferedImage bi;
		//int[] data;
		int wt, ht;

		public TestRenderBack(int wt, int ht) {
			setup(wt, ht);
		}

		private void setup(int wt, int ht) {
			bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, wt, ht);
			g.dispose();
			//data = ((DataBufferInt)bi.getData().getDataBuffer()).getData();
			this.wt = wt;
			this.ht = ht;
		}

		@Override
		public void render(Graphics2D g, float interpolation) {
			//rc.render(0, 0, wt, ht, data);
			g.drawImage(bi, 0, 0, null);
		}

		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
				setup((int) newSize.getWidth(), (int) newSize.getHeight());
			}
		}

		@Override
		public void update(long now, long last) {
			//
		}

	}
}
