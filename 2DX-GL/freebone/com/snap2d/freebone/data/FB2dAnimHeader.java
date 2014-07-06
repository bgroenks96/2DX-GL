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
public class FB2dAnimHeader {
	
	public static final byte[] SIGNATURE = {(byte)221, (byte)70, (byte)66, 
		(byte)50, (byte)68, (byte)10};
	
	public final byte versionMajor, versionMinor;
	
	public FB2dAnimHeader(int versionMajor, int versionMinor) {
		this.versionMajor = (byte) versionMajor;
		this.versionMinor = (byte) versionMinor;
	}
}
