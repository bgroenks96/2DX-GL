package bg.x2d.geo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import bg.x2d.Background;

/**
 * Superclass of all undefined geometric construction tools, most prominently {@link Paintbrush2D}.  FreeDraw2D defines certain generic methods regarding a {@link Background}
 * object and Graphics2D <code>canvas</code> object that all subclasses of it will utilize.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class FreeDraw2D {
	
	Background background;
	Graphics2D canvas;
	
	protected FreeDraw2D(Graphics2D g) {
		canvas = g;
	}
	
	protected FreeDraw2D(Graphics g) {
		this((Graphics2D)g);
	}
	
	public void setBackground(Background b) {
		if(b!=null) background = b;
	}
	
	public Background getBackground() {
		return background;
	}
	
	public Graphics2D getCanvas() {
		return canvas;
	}
	
	public void setGraphics(Graphics g) {
		setGraphics((Graphics2D)g);
	}
	
	public void setGraphics(Graphics2D g2) {
		canvas = g2;
	}
	
	public void setCanvasPaint(Paint p) {
		canvas.setPaint(p);
	}
}
