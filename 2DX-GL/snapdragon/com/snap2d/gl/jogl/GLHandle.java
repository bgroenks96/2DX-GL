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

/**
 * @author Brian Groenke
 *
 */
public class GLHandle {

	protected GL3bc gl;

	double vx, vy, vwt, vht;
	float ppu;
	boolean texEnabled, texBound;

	protected GLHandle() {
		//
	}
	
	public void setViewport(double x, double y, double width, double height, float ppu) {
		vx = x;
		vy = y;
		vwt = width / ppu;
		vht = height / ppu;
		this.ppu = ppu;
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(x, x + vwt, y + vht, y, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void setTextureEnabled(boolean enabled) {
		if(enabled)
			gl.glEnable(GL.GL_TEXTURE_2D);
		else
			gl.glDisable(GL.GL_TEXTURE_2D);
		texEnabled = enabled;
	}

	public void bindTexture(Texture2D tex) {
		if(!texEnabled) {
			tex.enable(gl);
			texEnabled = true;
		}

		tex.bind(gl);
		texBound = true;
	}

	public void setColor3f(float r, float g, float b) {
		gl.glColor3f(r, g, b);
	}

	public void setColor4f(float r, float g, float b, float a) {
		gl.glColor4f(r, g, b, a);
	}

	float theta, tx, ty, sx, sy;

	public void setRotation(float theta) {
		this.theta = theta;
	}

	public void setTranslation(float x, float y) {
		this.tx = x;
		this.ty = y;
	}

	public void setScale(float sx, float sy) {
		this.sx = sx;
		this.sy = sy;
	}

	public void pushTransform() {
		gl.glPushMatrix();
		gl.glTranslatef(tx, ty, 0);
		gl.glRotatef(theta, 0, 0, 1);
		gl.glScalef(sx, sy, 1);
	}

	public void popTransform() {
		gl.glPopMatrix();
	}

	public void drawRect2f(float x, float y, float wt, float ht) {
		if(texBound && texEnabled)
			setColor3f(1,1,1);
		gl.glBegin(GL2.GL_POLYGON);
		if(texBound && texEnabled)
			gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex2f(x, y);
		if(texBound && texEnabled)
			gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex2f(x, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex2f(x + wt, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex2f(x + wt, y);
		gl.glEnd();
	}
	
	public void drawRect2d(double x, double y, double wt, double ht) {
		if(texBound)
			setColor3f(1,1,1);
		gl.glBegin(GL2.GL_POLYGON);
		if(texBound)
			gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex2d(x, y);
		if(texBound)
			gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex2d(x, y + ht);
		if(texBound)
			gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex2d(x + wt, y + ht);
		if(texBound)
			gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex2d(x + wt, y);
		gl.glEnd();
	}

	/**
	 * Fetches the GL pipeline represented by this GLHandle.
	 * <b>JOGL must be in your build path to use the classes directly.</b>
	 * @return the JOGL GL control class
	 */
	public GL3bc getGL() {
		return gl;
	}
}
