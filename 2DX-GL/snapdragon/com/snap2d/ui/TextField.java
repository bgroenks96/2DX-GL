/*
 *  Copyright © 2011-2013 Brian Groenke
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
import java.awt.font.*;
import java.awt.image.*;

/**
 * @author Brian Groenke
 *
 */
public class TextField extends Text {

	private static final int X_PAD = 5, CARET_WT = 2;

	BufferedImage background;
	StringBuilder editable;

	/**
	 * @param x
	 * @param y
	 * @param text
	 * @param paint
	 * @param font
	 * @param renderContext
	 */
	public TextField(int x, int y, int wt, int ht, Color textColor, Font font,
			FontRenderContext renderContext) {
		super(x, y, null, textColor, font, renderContext);
		setRawBounds(x, y, wt, ht);
		setBounds(x, y, wt, ht);
		editable = new StringBuilder();
	}

	long lastCaretChange;
	boolean renderCaret = true;

	@Override
	public void render(Graphics2D g, float interpolation) {
		if(background == null) {
			g.setColor(Color.WHITE);
			g.fillRoundRect(x, y, wt, ht, wt / 20, ht / 20);
		}

		if(System.currentTimeMillis() - lastCaretChange > 500) {
			renderCaret = !renderCaret;
			lastCaretChange = System.currentTimeMillis();
		}

		if(renderCaret) {
			g.setColor(Color.BLACK);
			g.fillRect(x + X_PAD, y, CARET_WT, ht);
		}

		g.setPaint(paint);
		g.drawString(editable.toString(), x + X_PAD, y + ht / 4);
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public void setText(String str) {
		editable = new StringBuilder(str);
	}

	public void setBackground(BufferedImage background) {
		this.background = background;
	}

}
