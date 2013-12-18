/*
 *  Copyright © 2012-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.light;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import bg.x2d.utils.*;

/**
 * This class is under development and subject to changes.
 * 
 * @author Brian Groenke
 * 
 */
public class LightMap {

	HashSet<Light> lightSet = new HashSet<Light>();
	int wt, ht;

	BufferedImage lightMap;

	public LightMap(int wt, int ht) {
		this.wt = wt;
		this.ht = ht;
		lightMap = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB_PRE);
		setAmbientLight(ColorUtils.packInt(255, 0, 0, 0));
	}

	public void addLight(Light light) {
		lightSet.add(light);
	}

	public void removeLight(Light light) {
		lightSet.remove(light);
	}

	public void setAmbientLight(int color) {
		for (int y = 0; y < ht; y++) {
			for (int x = 0; x < wt; x++) {
				// lightMap[y][x] = ColorUtils.unpackInt(color, ColorUtils.TYPE_ARGB);
			}
		}
	}

	public void updateLight() {
		Graphics2D g2 = lightMap.createGraphics();
		for (Light l : lightSet) {
			g2.fillRect(0, 0, 100, 100);
		}
		g2.dispose();
	}

	public int[] applyLight(int px, int py, int[] argb) {
		return argb;
	}

	/**
	 * 
	 * @param src
	 *            the source colors in ARGB form.
	 * @param dst
	 *            the destination colors in ARGB form (alpha channel is ignored).
	 * @return
	 */
	private int[] blend(int[] src, int[] dst) {
		double srcA = src[0] / 255.0;
		double srcR = src[1] / 255.0;
		double srcG = src[2] / 255.0;
		double srcB = src[3] / 255.0;
		double dstR = dst[1] / 255.0;
		double dstG = dst[2] / 255.0;
		double dstB = dst[3] / 255.0;
		srcR *= srcA;
		srcG *= srcA;
		srcB *= srcA;
		dst[1] = (int) ((srcR + dstR * (1.0 - srcA)) * 255);
		dst[2] = (int) ((srcG + dstG * (1.0 - srcA)) * 255);
		dst[3] = (int) ((srcB + dstB * (1.0 - srcA)) * 255);
		return dst;
	}
}
