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
	
	public void setViewport(double x, double y, int viewWidth, int viewHeight, float ppu) {
		vx = x;
		vy = y;
		vwt = viewWidth / ppu;
		vht = viewHeight / ppu;
		this.ppu = ppu;
		gl.glOrtho(x, y, x + vwt, y + vht, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void setTextureEnabled(boolean enabled) {
		if(enabled)
			gl.glEnable(GL.GL_TEXTURE_2D);
		else
			gl.glDisable(GL.GL_TEXTURE_2D);
	}

	public void bindTexture(Texture2D tex) {
		if(!texEnabled) {
			tex.enable(gl);
			texEnabled = true;
		}

		tex.bind(gl);
		texBound = true;
	}

	public void disableTexturing() {
		if(texEnabled) {
			gl.glDisable(GL.GL_TEXTURE_2D);
			texEnabled = false;
		}
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

	public void drawRect(int x, int y, int wt, int ht) {
		gl.glBegin(GL2.GL_POLYGON);
		if(texBound)
			gl.glTexCoord2f(-1.0f, -1.0f);
		gl.glVertex2f(x, y);
		if(texBound)
			gl.glTexCoord2f(-1.0f, 1.0f);
		gl.glVertex2f(x, y + ht);
		if(texBound)
			gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex2f(x + wt, y + ht);
		if(texBound)
			gl.glTexCoord2f(1.0f, -1.0f);
		gl.glVertex2f(x + wt, y);
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
