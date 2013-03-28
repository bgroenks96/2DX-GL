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

package com.snap2d.gl.jogl;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import com.jogamp.opengl.util.*;

/**
 * @author Brian Groenke
 *
 */
public class GLRenderControl implements GLEventListener {
	
	public static final int DEFAULT_TARGET_FPS = 60;
	
	Animator anim;
	
	int wt, ht;
	boolean active;
	
	GLRenderControl(GLCanvas canvas) {
		canvas.addGLEventListener(this);
		anim = new Animator(canvas);
		anim.setUpdateFPSFrames(DEFAULT_TARGET_FPS, System.out);
	}
	
	public void setRenderActive(boolean active) {
		this.active = active;
		if(active && anim.isPaused())
			anim.resume();
		else
			anim.pause();
	}

	/**
	 *
	 */
	@Override
	public void display(GLAutoDrawable arg0) {
		GL2 gl = arg0.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glColor3f(1, 0, 0);
        gl.glVertex2f(-1, -1);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(0, 1);
        gl.glColor3f(0, 0, 1);
        gl.glVertex2f(1, -1);
        gl.glEnd();
	}

	/**
	 *
	 */
	@Override
	public void dispose(GLAutoDrawable arg0) {
		arg0.destroy();
		dispose();
	}

	/**
	 *
	 */
	@Override
	public void init(GLAutoDrawable arg0) {
		anim.start();
	}

	/**
	 *
	 */
	@Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width,
			int height) {
		wt = width;
		ht = height;
		
		GL2 gl = arg0.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-width, width, -height, height, -1, 1);
        gl.glLoadIdentity();
        
	}
	
	public void dispose() {
		anim.stop();
	}

}
