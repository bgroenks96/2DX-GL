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

package com.snap2d.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

/**
 * @author Brian Groenke
 * 
 */
public class Text extends RenderedComponent {

	String text;
	Paint paint;
	Font font;
	FontRenderContext renderContext;

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Text(int x, int y, String text, Paint paint, Font font,
			FontRenderContext renderContext) {
		super(x, y, (int) font.getStringBounds(text, renderContext).getWidth(),
				(int) font.getStringBounds(text, renderContext).getHeight());
		this.paint = paint;
		this.text = text;
		this.font = font;
		this.renderContext = renderContext;

	}

	int lx, ly;

	/**
	 *
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		g.setPaint(paint);
		g.setFont(font);
		g.drawString(text, x, y);
	}

	public void setText(String str) {
		this.text = str;
		recalculateSize();
	}

	public String getText() {
		return text;
	}

	protected void recalculateSize() {
		super.setRawBounds(rawx, rawy,
				(int) font.getStringBounds(text, renderContext).getWidth(),
				(int) font.getStringBounds(text, renderContext).getHeight());
	}

	/**
	 * Override to perform mouse event actions. Default implementation does nothing.
	 */
	@Override
	public void processMouseEvent(MouseEvent me) {
		//
	}

	/**
	 * Override to perform key event actions. Default implementation does nothing.
	 */
	@Override
	public void processKeyEvent(KeyEvent e) {

	}

	/**
	 * Text implementation does nothing
	 */
	@Override
	public void focusChanged(int focusEventId) {
		//
	}

	/**
	 * Text implementation always returns false.
	 */
	@Override
	public boolean hasFocus() {
		return false;
	}

}
