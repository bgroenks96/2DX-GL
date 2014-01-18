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

package com.snap2d.gl.jogl.light;

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

	public LightMap(int wt, int ht) {
		this.wt = wt;
		this.ht = ht;
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
		
	}

	public int[] applyLight(int px, int py, int[] argb) {
		return argb;
	}
}
