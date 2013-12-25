/*
 *  Copyright © 2012-2013 Brian Groenke
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

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class GLHandle {

	public static final float[] DEFAULT_RECT_TEX_COORDS = new float[] {0, 0, 0, 1, 1, 1, 1, 0},
			INVERTED_RECT_TEX_COORDS = new float[] {0, 1, 0, 0, 1, 0, 1, 1};
	public static final int FILTER_LINEAR = GL_LINEAR, FILTER_NEAREST = GL_NEAREST;

	protected GL3bc gl;

	double vx, vy, vwt, vht;
	float ppu;
	float[] texCoords = DEFAULT_RECT_TEX_COORDS;
	boolean texEnabled, texBound;

	int magFilter = FILTER_LINEAR, minFilter = FILTER_LINEAR;;

	protected GLHandle() {
		//
	}

	public void setViewport(double x, double y, double width, double height, float ppu) {
		vx = x;
		vy = y;
		vwt = width / ppu;
		vht = height / ppu;
		this.ppu = ppu;
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(x, x + vwt, y, y + vht, 0, 1);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void setTextureEnabled(boolean enabled) {
		if(enabled)
			gl.glEnable(GL_TEXTURE_2D);
		else
			gl.glDisable(GL_TEXTURE_2D);
		texEnabled = enabled;
	}

	public void bindTexture(Texture2D tex) {
		if(!texEnabled) {
			tex.enable(gl);
			texEnabled = true;
		}

		tex.bind(gl);
		tex.setTexParameteri(gl, GL_TEXTURE_MIN_FILTER, minFilter);
		tex.setTexParameteri(gl, GL_TEXTURE_MAG_FILTER, magFilter);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE); 
		texBound = true;
	}

	/**
	 * LINEAR or NEAREST texture filtering for texture down-scaling
	 * @param filterType linear for best quality, nearest for best performance
	 * @param mipmapType linear or nearest; use -1 for no mipmapping
	 */
	public void setTextureMinFilter(int filterType, int mipmapType) {
		if(filterType != FILTER_NEAREST && filterType != FILTER_LINEAR)
			throw(new IllegalArgumentException("illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
		if(mipmapType != FILTER_NEAREST && mipmapType != FILTER_LINEAR)
			throw(new IllegalArgumentException("illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
		if(mipmapType < 0)
			minFilter = filterType;
		else {
			switch(filterType) {
			case FILTER_LINEAR:
				if(mipmapType == FILTER_LINEAR)
					minFilter = GL_LINEAR_MIPMAP_LINEAR;
				else if(mipmapType == FILTER_NEAREST)
					minFilter = GL_LINEAR_MIPMAP_NEAREST;
				break;
			case FILTER_NEAREST:
				if(mipmapType == FILTER_LINEAR)
					minFilter = GL_NEAREST_MIPMAP_LINEAR;
				else if(mipmapType == FILTER_NEAREST)
					minFilter = GL_NEAREST_MIPMAP_NEAREST;
				
			}
		}
	}
	
	public void setTextureMagFilter(int filterType) {
		if(filterType != FILTER_NEAREST && filterType != FILTER_LINEAR)
			throw(new IllegalArgumentException("illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
		magFilter = filterType;
	}

	public void setColor3f(float r, float g, float b) {
		gl.glColor3f(r, g, b);
	}

	public void setColor4f(float r, float g, float b, float a) {
		gl.glColor4f(r, g, b, a);
	}

	float theta, tx, ty, sx = 1, sy = 1;

	public void setRotation(float theta) {
		this.theta = theta;
	}

	public void setTranslation(float x, float y) {
		this.tx = x; this.ty = y;
	}

	public void setScale(float sx, float sy) {
		this.sx = sx; this.sy = sy;
	}

	/**
	 * Pushes the current transformations to the GL rendering pipeline via <code>glPushMatrix</code>.
	 * Rendering operations done after calling this method will be transformed until calling {@link #popTransform()}
	 */
	public void pushTransform() {
		gl.glPushMatrix();
		gl.glTranslatef(tx, ty, 0);
		gl.glRotatef(theta, 0, 0, 1);
		gl.glScalef(sx, sy, 1);
	}

	/**
	 * Pops the transformation from the pipeline.
	 */
	public void popTransform() {
		gl.glPopMatrix();
	}

	/**
	 * Resets all currently stored transformation values to the default configuration (untransformed).
	 */
	public void clearTransform() {
		this.theta = 0;
		this.sx = 1; this.sy = 1;
		this.tx = 0; this.ty = 0;
	}

	/**
	 * Sets the coordinates for <code>glTexCoord2f</code> when drawing texture-enabled geometry.
	 * Coordinates should be supplied in the glob or array as alternating x,y values - e.g:
	 * x0, y0, x1, y1, etc...
	 * <br/><br/>
	 * The default texture coordinate configuration before this method is called is a regular quad texture:
	 * 0, 0, 0, 1, 1, 1, 1, 1 (bottom-left, top-left, top-right, bottom-right)
	 * @param coords the alternating x and y texture coordinates
	 */
	public void setTexCoords(float... coords) {
		if(coords != null && coords.length > 2)
		this.texCoords = coords;
	}
	
	public void setTexCoords(Texture2D rectCoords) {
		texCoords[0] = rectCoords.getLeftCoord(); texCoords[1] = rectCoords.getBottomCoord();
		texCoords[2] = rectCoords.getLeftCoord(); texCoords[3] = rectCoords.getTopCoord();
		texCoords[4] = rectCoords.getRightCoord(); texCoords[5] = rectCoords.getTopCoord();
		texCoords[6] = rectCoords.getRightCoord(); texCoords[7] = rectCoords.getBottomCoord();
	}

	public void drawRect2f(float x, float y, float wt, float ht) {
		if(texBound && texEnabled) {
			setColor3f(1,1,1);
			if(texCoords.length < 8) // 4 corners * 2 coordinates per corner
				throw(new GLException("too few tex coords: " + texCoords.length + " < 8"));
		}
		int tcoord = 0;
		gl.glBegin(GL_QUADS);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x, y);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x + wt, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x + wt, y);
		gl.glEnd();
	}

	public void drawRect2d(double x, double y, double wt, double ht) {
		if(texBound && texEnabled) {
			setColor3f(1,1,1);
			if(texCoords.length < 8) // 4 corners * 2 coordinates per corner
				throw(new GLException("too few tex coords: " + texCoords.length + " < 8"));
		}
		int tcoord = 0;
		gl.glBegin(GL_QUADS);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2d(x, y);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2d(x, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2d(x + wt, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
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

	// -------- OpenGL Feature Control --------- //

	public void setEnabled(GLFeature feature, boolean enable) {
		if(enable)
			gl.glEnable(feature.getGLCommand());
		else
			gl.glDisable(feature.getGLCommand());
	}

	public enum GLFeature {

		BLENDING(GL_BLEND);

		private int mapping;

		GLFeature(int glMapping) {
			this.mapping = glMapping;
		}

		public int getGLCommand() {
			return mapping;
		}
	}

	/**
	 * Sets the alpha blending function.
	 * @param blendFunc
	 */
	public void setBlendFunc(AlphaFunc blendFunc) {
		switch(blendFunc) {
		case CLEAR:
			gl.glBlendFunc(GL_ZERO, GL_ZERO);
			break;
		case SRC:
			gl.glBlendFunc(GL_ONE, GL_ZERO);
			break;
		case DST:
			gl.glBlendFunc(GL_ZERO, GL_ONE);
			break;
		case SRC_OVER:
			gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_OVER:
			gl.glBlendFunc(GL_ONE_MINUS_DST_ALPHA, GL_ONE);
			break;
		case SRC_IN:
			gl.glBlendFunc(GL_DST_ALPHA, GL_ZERO);
			break;
		case DST_IN:
			gl.glBlendFunc(GL_ZERO, GL_SRC_ALPHA);
			break;
		case SRC_OUT:
			gl.glBlendFunc(GL_ONE_MINUS_DST_ALPHA, GL_ZERO);
			break;
		case DST_OUT:
			gl.glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
			break;
		case SRC_ATOP:
			gl.glBlendFunc(GL_DST_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_ATOP:
			gl.glBlendFunc(GL_ONE_MINUS_DST_ALPHA, GL_SRC_ALPHA);
			break;
		case ALPHA_XOR:
			gl.glBlendFunc(GL_ONE_MINUS_DST_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		}
	}

	public enum AlphaFunc {
		CLEAR, SRC, DST, SRC_OVER, DST_OVER, SRC_IN, DST_IN,
		SRC_OUT, DST_OUT, SRC_ATOP, DST_ATOP, ALPHA_XOR;
	}
}
