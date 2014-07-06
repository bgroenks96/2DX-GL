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

package com.snap2d.gl.opengl;

import javax.media.opengl.*;

/**
 * Provides a set of hints used when allocating Vertex Buffer Objects to tell
 * the system how often the data will be updated.
 * @author Brian Groenke
 *
 */
public enum BufferUsage {
	/**
	 * Should be used for data that will be written once and updated
	 * infrequently.  Maps to field GL_STATIC_DRAW.
	 */
	STATIC_DRAW(GL.GL_STATIC_DRAW), 
	/**
	 * Should be used for data that will be updated frequently (i.e.
	 * every few frames).  Maps to field GL_DYNAMIC_DRAW.
	 */
	DYNAMIC_DRAW(GL.GL_DYNAMIC_DRAW),
	/**
	 * Should be used for data that will be updated on every frame.
	 * Maps to field GL_STREAM_DRAW.
	 */
	STREAM_DRAW(GL2.GL_STREAM_DRAW);

	final int usageHint;

	BufferUsage(int usage) {
		this.usageHint = usage;
	}
}
