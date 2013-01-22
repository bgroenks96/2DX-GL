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
import java.awt.image.*;
import java.lang.ref.*;
import java.math.*;

import bg.x2d.utils.*;

/**
 * Allows for precise, efficient collision detection between two image-based entities.
 * @author Brian Groenke
 *
 */
public class CollisionModel {

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

	/**
	 * Checks to see if the bitmasks of the two entities share common bits (thus are colliding).
	 * @param collisionArea the area in screen coordinates where the two rectangles overlap
	 * @param thisObj the object in screen space represented by this CollisionModel
	 * @param otherObj the object in screen space to check for collision.
	 * @param otherModel the CollisionModel representing the other object.
	 * @return true if the bitmasks of the two objects' CollisionModels overlap in space (the models 
	 *     are colliding), false otherwise
	 * @throws IllegalStateException if <code>release</code> has been invoked on this CollisionModel
	 */
	public boolean collidesWith(Rectangle collisionArea, Rectangle thisObj, Rectangle otherObj, CollisionModel otherModel)
			throws IllegalStateException {
		if(bitmasks == null)
			throw(new IllegalStateException("no collision data"));
		int x1 = thisObj.x;
		int y1 = thisObj.y;
		int x2 = otherObj.x;
		int y2 = otherObj.y;
		int wt = collisionArea.width;
		int ht = collisionArea.height;
		for(int y = 0; y < ht; y++) {
			BigInteger bitmask1 = bitmasks[y1 + y];
			BigInteger bitmask2 = otherModel.bitmasks[y2 + y];
			BigInteger mask = BigInteger.ZERO.not();
			int startOffs = thisObj.width - (wt + x1);
			mask = (mask.shiftLeft(thisObj.width - x1 + 1).not()).
					and(mask.shiftLeft(startOffs));
			bitmask1 = bitmask1.and(mask);
			bitmask1 = bitmask1.shiftRight(startOffs);
			mask = BigInteger.ZERO.not();
			startOffs = otherObj.width - (wt + x2);
			mask = (mask.shiftLeft(otherObj.width - x2 + 1).not()).
					and(mask.shiftLeft(startOffs));
			bitmask2 = bitmask2.and(mask);
			bitmask2 = bitmask2.shiftRight(startOffs);
			if(!bitmask1.and(bitmask2).equals(BigInteger.ZERO))
				return true;
		}
		return false;
	}
	
	public void release() {
		ReferenceQueue<BigInteger> queue = new ReferenceQueue<BigInteger>();
		for(int i = 0; i < bitmasks.length; i++) {
			WeakReference<BigInteger> ref = new WeakReference<BigInteger>(bitmasks[i], queue);
			bitmasks[i] = null;
			ref.enqueue();
		}
		bitmasks = null;
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
