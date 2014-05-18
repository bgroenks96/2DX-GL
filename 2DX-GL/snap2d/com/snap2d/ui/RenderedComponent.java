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

import com.snap2d.gl.*;
import com.snap2d.input.*;

/**
 * A base class for implementing UI components that can be rendered on screen. A RenderedCopmonent
 * keeps two sets of location/size information. One set is the current actual value, accessed by a
 * RenderedLayout and/or subclass, which reflects the component's state on screen. The second set is
 * the "raw" data. This is the original intended location and dimensions for the RenderedComponent.
 * It will not be modified by the layout, however, the layout will use this information in
 * auto-scaling/auto-translating the component. <br/>
 * <br/>
 * Subclass implementations should ALWAYS use the protected fields directly or the absolute position
 * methods for rendering the component on screen.
 * 
 * @author Brian Groenke
 * 
 */
public abstract class RenderedComponent implements Renderable,
		MouseEventClient, KeyEventClient {

	public boolean
	/**
	 * Whether or not this RenderedComponent should have its raw aspect ratio maintained. Used by
	 * RenderedLayout when auto-scaling.
	 */
	keepAspectRatio = true;

	protected volatile int x, y, wt, ht, rawx, rawy, raw_wt, raw_ht;
	protected Rectangle bounds;

	/**
	 * Creates a new RenderedComponent with raw and absolute data initialized to the given
	 * parameters.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public RenderedComponent(int x, int y, int width, int height) {
		this.rawx = x;
		this.rawy = y;
		this.raw_ht = height;
		this.raw_wt = width;
		setBounds(x, y, width, height);
	}

	public int getWidth() {
		return wt;
	}

	public int getHeight() {
		return ht;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	public int getRawWidth() {
		return raw_wt;
	}

	public int getRawHeight() {
		return raw_ht;
	}

	public Point getRawLocation() {
		return new Point(rawx, rawy);
	}

	/**
	 * Sets the "raw" location and size of the component on screen.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setRawBounds(int x, int y, int width, int height) {
		this.rawx = x;
		this.rawy = y;
		this.raw_ht = height;
		this.raw_wt = width;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Sets the absolute location and size of the component on screen. This should preferrably only
	 * be called by the RenderedLayout object responsible for managing this component.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.wt = width;
		this.ht = height;
		if (bounds == null) {
			bounds = new Rectangle();
		}
		bounds.setRect(x, y, width, height);
	}

	@Override
	/**
	 * The base implementation does nothing.
	 */
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
	}

	@Override
	/**
	 * The base implementation does nothing.
	 */
	public void onResize(Dimension oldSize, Dimension newSize) {
	}

	/**
	 * 
	 * @param focusEventId
	 *            a focus changed value from RenderedLayout
	 */
	public abstract void focusChanged(int focusEventId);

	public abstract boolean hasFocus();
}
