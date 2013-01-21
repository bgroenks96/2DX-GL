/*
 * Copyright © 2011-2012 Brian Groenke
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

package com.snap2d.world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.math.*;

import bg.x2d.utils.*;

/**
 * Allows for precise, efficient collision detection between two image-based entities.
 * @author Brian Groenke
 *
 */
public class CollisionModel {

	int[] testData;

	BigInteger[] bitmasks;

	/**
	 * Creates a collision model based on the pixel data of the given image.  The excluded
	 * color is the pixel value that is ignored in the image data.  For images with a transparent
	 * background, this value should be 0.  Otherwise, the integer value of the background pixel
	 * should be used.
	 * @param img the image from which pixel data will be read.
	 * @param excludedColor the pixel value that will be ignored.
	 */
	public CollisionModel(BufferedImage img, int excludedColor) {
		init(img, excludedColor);
	}

	private void init(BufferedImage img, int excludedColor) {
		int[] data = ColorUtils.getImageData(img);
		testData = data;
		int wt = img.getWidth();
		int ht = img.getHeight();
		bitmasks = new BigInteger[ht];
		for(int y = 0; y < ht; y++) {
			BigInteger bitmask = BigInteger.valueOf(0);
			for(int x = 0; x < wt; x++) {
				if(data[x + (y * wt)] != excludedColor) {
					bitmask = bitmask.shiftLeft(1).add(BigInteger.ONE);
				} else
					bitmask = bitmask.shiftLeft(1);
			}
			bitmasks[y] = bitmask;
		}
	}

	public boolean collidesWith(Rectangle2D collisionArea, World2D world, Entity e1, Entity e) {
		Point sloc = world.worldToScreen(collisionArea.getX(), collisionArea.getY());
		int x1 = sloc.x - e1.screenLoc.x;
		int y1 = sloc.y - e1.screenLoc.y;
		int x2 = sloc.x - e.screenLoc.x;
		int y2 = sloc.y - e.screenLoc.y;
		int wt = (int) Math.round(collisionArea.getWidth() * e1.world.getPixelsPerUnit());
		int ht = (int) Math.round(collisionArea.getHeight() * e1.world.getPixelsPerUnit());
		for(int y = ht - 1; y >= 0; y--) {
			BigInteger bitmask1 = bitmasks[y + y1];
			BigInteger bitmask2 = bitmasks[y + y2];
			BigInteger mask = BigInteger.ZERO.not();
			mask = (mask.shiftLeft((int)e1.getScreenBounds().width - x1 + 1).not()).and(mask.shiftLeft(x1 + wt));
			bitmask1 = bitmask1.and(mask);
			mask = BigInteger.ZERO.not();
			mask = (mask.shiftLeft((int)e.getScreenBounds().width - x2 + 1).not()).and(mask.shiftLeft(x2 + wt));
			bitmask2 = bitmask2.and(mask);
			if(!bitmask1.and(bitmask2).equals(BigInteger.ZERO))
				return true;
		}
		return false;
	}

	/*
	public void testWriteToFile() {
		PrintWriter pw =  null;
		try {
			pw = new PrintWriter(new File("/home/brian/Desktop/imageModel.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for(int y = (int)bounds.getHeight() - 1; y >= 0; y--) {
			BigInteger bitmask = bitmasks[y];
			for(int x = (int)bounds.getWidth() - 1; x >= 0; x--) {
				long a = bitmask.shiftRight(x).and(BigInteger.ONE).longValue();
				if(a != 0)
					pw.print("1");
				else
					pw.print("0");
			}
			pw.println();
		}
		pw.close();
	}
	 */
}
