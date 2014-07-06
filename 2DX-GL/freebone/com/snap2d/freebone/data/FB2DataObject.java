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
public class FB2DataObject {
	
	public final int imgCount, nodeCount, objWt, objHt;
	public final FB2DataImage[] imgs;
	public final FB2DataNode[] nodes;
	
	public FB2DataObject(int objWidth, int objHeight, FB2DataImage[] images, FB2DataNode[] nodes) {
		this.imgCount = images.length;
		this.nodeCount = nodes.length;
		this.objWt = objWidth; this.objHt = objHeight;
		this.nodes = nodes;
		this.imgs = images;
	}
}
