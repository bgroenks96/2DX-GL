package com.snap2d.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

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

	public static int[] getImageBuffer(BufferedImage bi) {
		return ((DataBufferInt) bi.getData().getDataBuffer()).getData();
	}

	public static void main(String[] args) {
		for (int i : unpackInt(packInt(100, 0, 0), TYPE_RGB)) {
			System.out.print(i + " ");
		}
	}
}
