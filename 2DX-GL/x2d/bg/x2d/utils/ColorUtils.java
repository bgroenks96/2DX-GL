/*
 * Copyright � 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package bg.x2d.utils;

import java.awt.image.*;

/**
 * Provides static utility methods for color and pixel management.
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class ColorUtils {

	public static final int TYPE_ARGB = 0xAA, TYPE_RGBA = 0xAB,
			TYPE_RGB = 0xAC;

	public static int[] unpackInt(int argb, int type) {
		int[] vals = null;
		int p1 = 0;
		int p2 = 1;
		int p3 = 2;
		int p4 = 3;
		switch (type) {
		case TYPE_RGB:
			vals = new int[3];
			vals[p1] = argb >> 16 & 0xFF;
			vals[p2] = argb >> 8 & 0xFF;
			vals[p3] = argb & 0xFF;
			break;
		case TYPE_RGBA:
		case TYPE_ARGB:
			vals = new int[4];
			vals[p4] = argb & 0xFF;
			vals[p3] = argb >> 8 & 0xFF;
			vals[p2] = argb >> 16 & 0xFF;
			vals[p1] = argb >> 24 & 0xFF;
			break;
		default:
			throw (new IllegalArgumentException(
					"type must be a valid field defined by ColorUtils class"));
		}
		return vals;
	}
	
	public static int[] unpackInt(int[] vals, int argb) {
		switch (vals.length) {
		case 3:
			vals[0] = argb >> 16 & 0xFF;
			vals[1] = argb >> 8 & 0xFF;
			vals[2] = argb & 0xFF;
			break;
		case 4:
			vals[3] = argb & 0xFF;
			vals[2] = argb >> 8 & 0xFF;
			vals[1] = argb >> 16 & 0xFF;
			vals[0] = argb >> 24 & 0xFF;
			break;
		default:
			throw (new IllegalArgumentException(
					"type must be a valid field defined by ColorUtils class"));
		}
		return vals;
	}

	public static int packInt(int... rgbs) {

		
		if (rgbs.length != 3 && rgbs.length != 4) {
			throw (new IllegalArgumentException(
					"args must be valid RGB, ARGB or RGBA value."));
		}
		
		int color = rgbs[0];
		for (int i = 1; i < rgbs.length; i++) {
			color = (color << 8) + rgbs[i];
		}
		return color;
	}

	/**
	 * Assumes the image to be of integer-pixel format.
	 * @param bi
	 * @return
	 */
	public static int[] getImageData(BufferedImage bi) {
		return ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
	}
}
