/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.freebone.data;

/**
 * @author Brian Groenke
 *
 */
public class FB2DataImage {
	
	private static int internalId = Integer.MIN_VALUE;
	
	public final String imgName;
	public final float ix, iy, theta;
	public final int id = internalId++;
	
	public FB2DataImage(String imgName, float ix, float iy, float theta) {
		this.imgName = imgName;
		this.ix = ix; this.iy = iy; this.theta = theta;
	}
}
