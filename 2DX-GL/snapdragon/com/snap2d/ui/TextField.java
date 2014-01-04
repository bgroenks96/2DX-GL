/*
 *  Copyright © 2012-2014 Brian Groenke
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
import java.awt.image.*;

/**
 * @author Brian Groenke
 * 
 */
public class TextField extends Text {

	private static final int X_PAD = 5, CARET_WT = 1;

	protected BufferedImage background;
	protected Color caretColor = Color.BLACK, selColor = Color.BLUE;
	protected StringBuilder editable;

	protected boolean focused;
	protected int caretPos = 1;

	private FontMetrics metrics;
	private Rectangle strBounds = new Rectangle();

	/**
	 * Creates a new TextField component at the given coordinates with the assigned size, font, text
	 * color, and FontRenderContext.
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param paint
	 * @param font
	 * @param renderContext
	 */
	public TextField(int x, int y, int wt, int ht, Color textColor, Font font,
			FontRenderContext renderContext) {
		super(x, y, "", textColor, font, renderContext);
		setRawBounds(x, y, wt, ht);
		setBounds(x, y, wt, ht);
		editable = new StringBuilder();
	}

	private int selStart;
	private long lastCaretChange;
	private boolean renderCaret = true, renderSel = false;

	@Override
	public void render(Graphics2D g, float interpolation) {
		if (background == null) {
			g.setColor(Color.WHITE);
			g.fillRect(x, y, wt, ht);
		} else {
			g.drawImage(background, x, y, wt, ht, null);
		}

		if (System.currentTimeMillis() - lastCaretChange > 500) {
			renderCaret = !renderCaret;
			lastCaretChange = System.currentTimeMillis();
		}

		g.setFont(font);
		metrics = g.getFontMetrics();
		int ht = metrics.getHeight();
		int wt = (int) metrics.getStringBounds(editable.toString(), g)
				.getWidth();
		strBounds.setBounds(x, y, wt, ht);

		if (renderCaret && focused) {
			g.setColor(caretColor);
			g.fillRect(
					x
							+ X_PAD
							+ wt
							- ((int) metrics.getStringBounds(
									editable.substring(caretPos), g).getWidth()),
					y + (this.ht - ht) / 2, CARET_WT, ht);
		}

		if (renderSel) {
			int st = Math.min(caretPos, selStart);
			int en = Math.max(caretPos, selStart);
			String str = editable.substring(st, en);
			g.setColor(selColor);
			g.fillRect(
					x
							+ X_PAD
							+ (int) metrics.getStringBounds(
									editable.substring(0, st), g).getHeight(),
					y + (this.ht - ht) / 2,
					(int) metrics.getStringBounds(str, g).getWidth(), ht);
			g.setColor(Color.WHITE);
		} else {
			g.setPaint(paint);
		}

		g.drawString(editable.toString(), x + X_PAD, y
				+ (this.ht - (this.ht - ht)));
	}

	/**
	 * Gets the text currently set in this TextField.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Sets the text for this TextField.
	 */
	@Override
	public void setText(String str) {
		editable = new StringBuilder(str);
	}

	/**
	 * Sets the TextField's background image. The default background is just a white rectangle.
	 * 
	 * @param background
	 */
	public void setBackground(BufferedImage background) {
		this.background = background;
	}

	/**
	 * 
	 * @return the TextField's background image, or null if nothing is set.
	 */
	public BufferedImage getBackground() {
		return background;
	}

	public void setSelectionColor(Color color) {
		if (color != null) {
			this.selColor = color;
		}
	}

	public Color getSelectionColor() {
		return selColor;
	}

	public void setCaretColor(Color color) {
		if (color != null) {
			this.caretColor = color;
		}
	}

	public Color getCaretColor() {
		return this.caretColor;
	}

	public void setCaretPos(int newCaretPos) {
		if (newCaretPos >= 0 && newCaretPos <= editable.length()) {
			caretPos = newCaretPos;
		}
	}

	public int getCaretPos() {
		return caretPos;
	}

	Cursor pre;
	boolean selStopped;

	/**
	 * Processes mouse events for positioning the cursor, focus control, and selecting text.
	 */
	@Override
	public void processMouseEvent(MouseEvent me) {
		if (me.getID() == MouseEvent.MOUSE_EXITED) {
			me.getComponent().setCursor(pre);
			pre = null;
		} else if (pre == null) {
			pre = me.getComponent().getCursor();
			me.getComponent().setCursor(
					Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}

		switch (me.getID()) {
		case MouseEvent.MOUSE_RELEASED:
			selStopped = true;
			break;
		case MouseEvent.MOUSE_DRAGGED:
			if (!renderSel) {
				selStart = caretPos;
			}
			// renderSel = true;
		case MouseEvent.MOUSE_PRESSED:
			if (selStopped) {
				selStopped = false;
				renderSel = false;
			}

			if (strBounds.contains(me.getX(), me.getY())) {
				int mx = me.getX() - strBounds.x;
				char[] chars = editable.toString().toCharArray();
				int tot = 0;
				for (int i = 0; i < chars.length; i++) {
					tot += metrics.charWidth(chars[i]);
					if (mx <= tot) {
						caretPos = i;
						break;
					}
				}
			} else if (me.getX() > strBounds.x + strBounds.width) {
				caretPos = editable.length();
			}
			break;
		}
	}

	/**
	 * Processes key events for typing and text modification.
	 */
	@Override
	public void processKeyEvent(KeyEvent ke) {
		if (focused && ke.getID() == KeyEvent.KEY_PRESSED) {
			switch (ke.getKeyCode()) {
			case KeyEvent.VK_BACK_SPACE:
				if (caretPos > 0) {
					editable.deleteCharAt(--caretPos);
				}
				break;
			case KeyEvent.VK_LEFT:
				if (caretPos > 0) {
					caretPos--;
				}
				break;
			case KeyEvent.VK_RIGHT:
				if (caretPos < editable.length()) {
					caretPos++;
				}
				break;
			default:
				char c = ke.getKeyChar();
				if (c != KeyEvent.CHAR_UNDEFINED) {
					if (caretPos == editable.length()) {
						editable.append(c);
						caretPos++;
					} else {
						editable.insert((caretPos++), c);
					}
				}
			}
		}
	}

	@Override
	public void focusChanged(int evt) {
		switch (evt) {
		case RenderedLayout.FOCUS_GAINED:
			focused = true;
			break;
		default:
			focused = false;
		}
	}

	@Override
	public boolean hasFocus() {
		return focused;
	}

}
