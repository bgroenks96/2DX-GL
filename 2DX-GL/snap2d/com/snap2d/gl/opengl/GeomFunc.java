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

import javax.media.opengl.GL;

/**
 * Represents a set number of supported OpenGL geometry drawing functions.
 * @author Brian Groenke
 *
 */
public enum GeomFunc {

	POINTS(GL.GL_POINTS), 
	LINES(GL.GL_LINES), 
	LINE_STRIP(GL.GL_LINE_STRIP), 
	TRIANGLES(GL.GL_TRIANGLES),
	TRIANGLE_STRIP(GL.GL_TRIANGLE_STRIP), 
	TRIANGLE_FAN(GL.GL_TRIANGLE_FAN);

	private int glcmd;

	GeomFunc(int glCommand) {
		this.glcmd = glCommand;
	}

	public int getGLCommand() {
		return glcmd;
	}
}