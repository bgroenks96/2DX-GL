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

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author Brian Groenke
 *
 */
public class FB2DataHeader {
	public static final byte[] SIGNATURE = {(byte)220, (byte)70, (byte)66, 
		(byte)50, (byte)68, (byte)10};
	
	public final byte versionMajor, versionMinor;
	
	public FB2DataHeader(int versionMajor, int versionMinor) {
		this.versionMajor = (byte) versionMajor;
		this.versionMinor = (byte) versionMinor;
	}
	
	/**
	 * Writes the format header for a Freebone2D data object into the given
	 * ByteBuffer at its current position.<br/>
	 * <br/>
	 * > signature (6 bytes)<br/>
	 * > major version (1 byte)<br/>
	 * > minor version (1 byte)<br/>
	 * @param buff
	 * @throws IOException
	 */
	public void write(ByteBuffer buff) throws IOException {
		buff.put(SIGNATURE);
		buff.put(versionMajor);
		buff.put(versionMinor);
	}
}
