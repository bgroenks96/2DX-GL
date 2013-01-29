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
	
	/**
	 * Draws the given geometry onto a BufferedImage and uses the image to create a CollisionModel.
	 * @param geometry
	 * @param texture
	 * @param transform
	 * @param fill
	 */
	public CollisionModel(Shape geometry, Paint texture, AffineTransform transform, boolean fill) {
		Rectangle bounds = geometry.getBounds();
		BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = img.createGraphics();
		g.setPaint(texture);
		g.setTransform(transform);
		if(fill)
			g.fill(geometry);
		else
			g.draw(geometry);
		g.dispose();
		init(img, 0);
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
		int x1 = Math.max(0, collisionArea.x - thisObj.x);
		int y1 = Math.max(0, collisionArea.y - thisObj.y);
		int x2 = Math.max(0, collisionArea.x - otherObj.x);
		int y2 = Math.max(0, collisionArea.y - otherObj.y);
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

	/*
	public void testWriteToFile() {
		PrintWriter pw =  null;
		try {
			pw = new PrintWriter(new File("/home/brian/Desktop/" + Math.random() + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		int wt = bitmasks[0].bitLength();
		int ht = bitmasks.length;
		for(int y = ht - 1; y >= 0; y--) {
			BigInteger bitmask = bitmasks[y];
			for(int x = wt - 1; x >= 0; x--) {
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
