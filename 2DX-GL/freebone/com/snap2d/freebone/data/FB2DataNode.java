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
public class FB2DataNode {
	
	public final float x, y;
	public final int[] attIds;
	public final int id;
	
	public FB2DataNode(float x, float y, int id, int... imgIds) {
		this.x = x; this.y = y;
		this.attIds = imgIds;
		this.id = id;
	}
}
