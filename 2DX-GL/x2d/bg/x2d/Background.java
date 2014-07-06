/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d;

import java.awt.*;

import bg.x2d.anim.*;

/**
 * Background defines an object of type Drawable or Paint to be used as a static background for each frame. 
 * If no Drawable object is specified, the default Paint object will be used instead.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public class Background {

	private Paint color;
	private int width, height;
	private Drawable redraw;

	public Background(int w, int h, Paint p) {
		if (p != null) {
			color = p;
			width = w;
			height = h;
		} else {
			throw (new NullPointerException("Paint object cannot be null"));
		}
	}

	public Background(Drawable task) {
		redraw = task;
	}

	public Paint getPaint() {
		return color;
	}

	public void setPaint(Paint p) {
		color = p;
	}

	public void setSize(int w, int h) {
		width = w;
		height = h;
	}

	public int[] getSize() {
		int[] ints = { width, height };
		return ints;
	}

	public void redraw(Graphics2D g2) {
		if (redraw != null) {
			redraw.draw(g2);
		} else if(color != null) {
			g2.setPaint(color);
			g2.fillRect(0, 0, width, height);
		}
	}

	public void redraw(Graphics g) {
		redraw((Graphics2D) g);
	}

	public void dispose() {
		color = null;
		width = 0;
		height = 0;
	}
}
