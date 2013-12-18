/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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

import java.util.*;

import bg.x2d.utils.*;

/**
 * Pre-calculates an internal table of gamma corrected RGB values and provides look-up functionality
 * to apply the gamma to integer pixels.
 * 
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
		if (gamma < 0) {
			throw (new IllegalArgumentException("illegal gamma value"));
		}
		this.gamma = gamma;
		buildGammaTable();
	}

	/**
	 * Applies the current gamma table to the given integer pixel.
	 * 
	 * @param color
	 *            the integer pixel to which gamma will be applied
	 * @param type
	 *            a pixel type defined by ColorUtils
	 * @param rgbArr
	 *            optional pre-instantiated array to use when unpacking. May be null.
	 * @return the modified pixel value
	 */
	public int applyGamma(int color, int type, int[] rgbArr) {
		int[] argb = (rgbArr != null) ? ColorUtils.unpackInt(rgbArr, color)
				: ColorUtils.unpackInt(color, type);
		for (int i = 0; i < argb.length; i++) {
			int col = argb[i];
			argb[i] = table[col];
		}
		int newColor = ColorUtils.packInt(argb);
		return newColor;
	}

	/**
	 * Same as {@link #applyGamma(int, int, int[])} but uses the separate ARGB values stored in the
	 * given array.
	 * 
	 * @param argb
	 * @return
	 */
	public int[] applyGamma(int[] argb) {
		for (int i = 0; i < argb.length; i++) {
			int col = argb[i];
			argb[i] = table[col];
		}
		return argb;
	}

	/**
	 * Applies gamma correction to a single ARGB component value. <br/>
	 * <br/>
	 * Note: Value must be within valid color range (0-255) or ArrayIndexOutOfBounds exception will
	 * be thrown by the VM.
	 * 
	 * @param val
	 * @return the gamma modified color component
	 */
	public int applyGamma(int val) {
		return table[val];
	}

	/**
	 * Sets the gamma and rebuilds the internal gamma table. The passed value should be >= 0.0
	 * specifying how much to darken or brighten the image, where 1.0 has no change, < 1 is darker,
	 * and > 1 is brighter. <br/>
	 * <br/>
	 * This method MAY consume noticeable time in the calling thread from rebuilding the gamma
	 * table.
	 * 
	 * @param gamma
	 *            the new gamma value
	 */
	public void setGamma(float gamma) {
		if (gamma >= 0) {
			this.gamma = gamma;
			buildGammaTable();
		}
	}

	public float getGamma() {
		return gamma;
	}

	/**
	 * @return a copy of the pre-calculated gamma values array.
	 */
	public int[] getTable() {
		return Arrays.copyOf(table, table.length);
	}

	/**
	 * Called when a new gamma value is set to rebuild the gamma table.
	 */
	private synchronized void buildGammaTable() {
		if (table == null || table.length != TABLE_SIZE) {
			table = new int[TABLE_SIZE];
		}
		float ginv = 1 / gamma;
		for (int i = 0; i < table.length; i++) {
			table[i] = (int) Math.round(COLORS * Math.pow(i / COLORS, ginv));
		}
	}

}
