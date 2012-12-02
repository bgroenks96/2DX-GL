package com.snap2d.gl;

import java.awt.*;

import javax.swing.*;

/**
 * 
 * @author brian
 * @since Snapdragon2D 1.0
 */
public class Display {

	private int wt, ht;
	private Type type;
	private JFrame frame;
	private RenderControl rc;

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
		
		frame.getContentPane().setBackground(RenderControl.CANVAS_BACK);
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
}
