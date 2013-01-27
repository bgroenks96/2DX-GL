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

package com.snap2d.gl;

import bg.x2d.utils.*;

/**
 * Pre-calculates an internal table of gamma corrected RGB values and provides look-up functionality
 * to apply the gamma to integer pixels.
 * @author Brian Groenke
 *
 */
public class GammaTable {

	private static final float COLORS = 255;
	private static final int TABLE_SIZE = 256;

	private float gamma;
	private int[] table;

	/**
	 * 
	 */
	public GammaTable(float gamma) {
		if(gamma < 0)
			throw(new IllegalArgumentException("illegal gamma value"));
		this.gamma = gamma;
		buildGammaTable();
	}

	/**
	 * Applies the current gamma table to the given integer pixel.
	 * @param color the integer pixel to which gamma will be applied
	 * @param type a pixel type defined by ColorUtils
	 * @param rgbArr optional pre-instantiated array to use when unpacking.  May be null.
	 * @return the modified pixel value
	 */
	public int applyGamma(int color, int type, int[] rgbArr) {
		int[] argb = (rgbArr != null) ? ColorUtils.unpackInt(rgbArr, color):ColorUtils.unpackInt(color, type);
		for(int i = 0; i < argb.length; i++) {
			int col = argb[i];
			argb[i] = table[col];
		}
		int newColor = ColorUtils.packInt(argb);
		return newColor;
	}

	/**
	 * Sets the gamma and rebuilds the internal gamma table.  The passed value
	 * should be >= 0.0 specifying how much to darken or brighten the image, where
	 * 1.0 has no change, < 1 is darker, and > 1 is brighter.
	 * @param gamma the new gamma value
	 */
	public void setGamma(float gamma) {
		if(gamma >= 0) {
			this.gamma = gamma;
			buildGammaTable();
		}
	}

	public float getGamma() {
		return gamma;
	}

	/**
	 * Called when a new gamma value is set to rebuild the gamma table.
	 */
	private synchronized void buildGammaTable() {
		if(table == null || table.length != TABLE_SIZE)
			table = new int[TABLE_SIZE];
		float ginv = 1 / gamma;
		for(int i=0;i<table.length;i++) {
			table[i] = (int) Math.round(COLORS * Math.pow(i / COLORS, ginv)); 
		}
	}

}
