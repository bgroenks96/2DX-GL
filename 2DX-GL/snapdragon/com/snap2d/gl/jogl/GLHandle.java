/*
 *  Copyright © 2012-2014 Brian Groenke
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

import static javax.media.opengl.GL2.*;

import java.nio.*;
import java.util.*;

import javax.media.opengl.*;

import bg.x2d.utils.*;

import com.jogamp.common.nio.*;

/**
 * @author Brian Groenke
 *
 */
public class GLHandle {

	public static final float[] DEFAULT_RECT_TEX_COORDS = new float[] {0, 0, 0, 1, 1, 0, 1, 1},
			INVERTED_RECT_TEX_COORDS = new float[] {0, 1, 0, 0, 1, 1, 1, 0};
	public static final int FILTER_LINEAR = GL_LINEAR, FILTER_NEAREST = GL_NEAREST;

	protected GL2 gl;

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
		texCoords[4] = rectCoords.getRightCoord(); texCoords[5] = rectCoords.getBottomCoord();
		texCoords[6] = rectCoords.getRightCoord(); texCoords[7] = rectCoords.getTopCoord();
	}

	// ---- Data Store/Access and Drawing ---- //

	private int[] buffIds;
	private BufferObject[] buffInfo;

	/**
	 * Draws the buffer specified by 'buffId' to screen according to parameters set by associated data.
	 * @param buffId the id of the vertex buffer to draw
	 */
	public void draw2f(int buffId) {
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		BufferObject buffObj = buffInfo[findIndexOfID(buffId)];
		int nverts = buffObj.nverts;
		// draw all quads in vertex buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffId);
		gl.glEnableClientState( GL_VERTEX_ARRAY);
		if(texBound && texEnabled) {
			gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			gl.glVertexPointer(2, GL_FLOAT, 0, 0);
			gl.glTexCoordPointer(2, GL_FLOAT, 0, 2 * nverts * Buffers.SIZEOF_FLOAT);
		} else {
			gl.glVertexPointer(2, GL_FLOAT, 0, 0);
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);

		// disable arrays
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(texBound && texEnabled)
			gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);

	}

	public void draw2d(int buffId, GeomFunc drawFunc) {
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		BufferObject buffObj = buffInfo[findIndexOfID(buffId)];
		int nverts = buffObj.nverts;
		// draw all quads in vertex buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffId);
		gl.glEnableClientState( GL_VERTEX_ARRAY);
		if(texBound && texEnabled) {
			gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			gl.glVertexPointer(2, GL_DOUBLE, 0, 0);
			gl.glTexCoordPointer(2, GL_DOUBLE, 0, 2 * nverts * Buffers.SIZEOF_DOUBLE);
		} else {
			gl.glVertexPointer(2, GL_DOUBLE, 0, 0);
		}

		gl.glMultiDrawArrays(drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);

		// disable arrays
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(texBound && texEnabled)
			gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	}

	/**
	 * Writes coordinate data for a given quad to the vertex buffer.  Each successive call to this
	 * method writes an additional object into the buffer until N objects have been written, where N
	 * is the set number of objects for this buffer, or until {@link #resetBuff(int)} is called.  Any
	 * calls made to {@link #draw2f(int)} will draw whatever number of objects are available in the buffer.
	 * Once a buffer's object limit is reached or it is reset, subsequent calls to this method will cause
	 * all of its vertex data to be discarded and restart the process.<br/><br/>
	 * <b>Note: DO NOT rely on {@link #resetBuff(int)} or writing N+1 objects to wipe existing data from VRAM
	 * before {@link #draw2f(int)} is called.  This likely WILL NOT happen, and the previously uploaded vertex
	 * data will still be at least partially rendered.  The only reliable way to do this is by destroying and re-creating
	 * the buffer.
	 * @param rectBuffId
	 * @param x
	 * @param y
	 * @param wt
	 * @param ht
	 */
	public void putQuad2f(int rectBuffId, float x, float y, float wt, float ht) {
		if(buffIds == null)
			throw(new GLException("can't write quad data: no allocated buffers"));

		BufferObject buffObj = buffInfo[findIndexOfID(rectBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, rectBuffId);
		
		if(buffObj.objCount == 0)
			gl.glBufferData(GL_ARRAY_BUFFER, buffSize, null, buffObj.buffStore.usageHint);
		
		if(buffObj.buffStore == BufferUsage.STATIC_DRAW) { // for static draw, don't use buffer mapping
			FloatBuffer floatBuff = Buffers.newDirectFloatBuffer(new float[buffSize]);
			if(buffObj.objCount != 0)
				gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			floatBuff.put(x); floatBuff.put(y);
			floatBuff.put(x); floatBuff.put(y + ht);
			floatBuff.put(x + wt); floatBuff.put(y);
			floatBuff.put(x + wt); floatBuff.put(y + ht);
			if(buffObj.textured)
				floatBuff.put(texCoords);
			floatBuff.flip();
			buffObj.objCount++; // increment object count
			gl.glBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
		} else {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
			FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			floatBuff.put(x); floatBuff.put(y);
			floatBuff.put(x); floatBuff.put(y + ht);
			floatBuff.put(x + wt); floatBuff.put(y);
			floatBuff.put(x + wt); floatBuff.put(y + ht);
			if(buffObj.textured)
				floatBuff.put(texCoords);
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		}

	}

	public void putQuad2d(int rectBuffId, double x, double y, double wt, double ht) {
		if(buffIds == null)
			throw(new GLException("can't write quad data: no allocated buffers"));

		BufferObject buffObj = buffInfo[findIndexOfID(rectBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, rectBuffId);
		
		if(buffObj.objCount == 0)
			gl.glBufferData(GL_ARRAY_BUFFER, buffSize, null, buffObj.buffStore.usageHint); // discard existing buffer data store

		if(buffObj.buffStore == BufferUsage.STATIC_DRAW) { // for static draw, don't use buffer mapping
			DoubleBuffer dbuff = Buffers.newDirectDoubleBuffer(new double[buffSize]);
			if(buffObj.objCount != 0)
				gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, dbuff);
			dbuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			dbuff.put(x); dbuff.put(y);
			dbuff.put(x); dbuff.put(y + ht);
			dbuff.put(x + wt); dbuff.put(y);
			dbuff.put(x + wt); dbuff.put(y + ht);
			if(buffObj.textured)
				dbuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 0, texCoords.length));
			dbuff.flip();
			buffObj.objCount++; // increment object count
			gl.glBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, dbuff);
		} else {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
			DoubleBuffer dbuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
			dbuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			dbuff.put(x); dbuff.put(y);
			dbuff.put(x); dbuff.put(y + ht);
			dbuff.put(x + wt); dbuff.put(y);
			dbuff.put(x + wt); dbuff.put(y + ht);
			if(buffObj.textured)
				dbuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 0, texCoords.length));
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		}

	}

	/**
	 * Resets the object data for the given buffer to zero.
	 * Vertex data from previous calls to {@link #putQuad2d(int, double, double, double, double)}
	 * will be discarded and overwritten upon the next call.  This method can be used to reset a
	 * partially filled buffer after it has been drawn.
	 * @param buffId
	 */
	public void resetBuff(int buffId) {
		int ind = findIndexOfID(buffId);
		buffInfo[ind].objCount = 0;
	}

	/**
	 * Allocates a vertex buffer for rendering a given number of quads.  The returned
	 * id must be stored and used in order write and draw the quad data.
	 * @param storeType a hint for how the graphics driver should treat the buffer data in VRAM
	 * @param nobjs max number of quads in this buffer
	 * @param textured true if this quad will be textured (so tex coords are stored), false otherwise
	 * @return the id for the quad buffer
	 */
	public int createQuadBuff2f(BufferUsage storeType, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2f(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
		return buffObj.id;
	}

	public int createQuadBuff2d(BufferUsage storeType, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2d(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
		return buffObj.id;
	}

	/**
	 * Deletes the VBO held with the given ID from memory.
	 * @param id the id for the buffer returned by {@link #createRectBuff2f(BufferUsage)}
	 * and {@link #createRectBuff2d(BufferUsage)}
	 * @return
	 */
	public boolean destroyBuff(int id) {
		int ind = findIndexOfID(id);
		if(ind < 0)
			return false;
		// delete buffer from GL system
		gl.glDeleteBuffers(1, buffIds, ind);
		// delete buffer id and BufferObject from GLHandle
		buffIds = Utils.arrayDelete(buffIds, ind);
		buffInfo = Utils.arrayDelete(buffInfo, new BufferObject[buffInfo.length - 1], ind);
		return true;
	}

	/*
	 * appends the buffer index to 'buffIds' and returns the BufferObject for the newly created buffer
	 */
	private BufferObject initVBO2f(BufferUsage storeType, GeomFunc drawFunc, int nverts, 
			int nobjs, boolean textured) {
		if(buffIds == null) {
			buffIds = new int[1];
			buffInfo = new BufferObject[1];
		} else {
			buffIds = Arrays.copyOf(buffIds, buffIds.length + 1);
			buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
		}
		int ind = buffIds.length - 1;
		gl.glGenBuffers(1, buffIds, ind);
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffIds[ind]);
		int buffSize;
		if(textured) {
			buffSize = 4 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		} else {
			buffSize = 2 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		}

		gl.glBufferData(GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		BufferObject buffObj = new BufferObject(buffIds[ind], nverts, nobjs, Buffers.SIZEOF_FLOAT, buffSize, 
				textured, drawFunc, storeType);
		buffInfo[buffInfo.length - 1] = buffObj;
		return buffObj;
	}

	/*
	 * appends the buffer index to 'buffIds' and returns the BufferObject for the newly created buffer
	 */
	private BufferObject initVBO2d(BufferUsage storeType, GeomFunc drawFunc, int nverts, 
			int nobjs, boolean textured) {
		if(buffIds == null) {
			buffIds = new int[1];
			buffInfo = new BufferObject[1];
		} else {
			buffIds = Arrays.copyOf(buffIds, buffIds.length + 1);
			buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
		}
		int ind = buffIds.length - 1;
		gl.glGenBuffers(1, buffIds, ind);
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffIds[ind]);
		int buffSize;
		if(textured) {
			buffSize = 4 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		} else {
			buffSize = 2 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		}

		gl.glBufferData(GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		BufferObject buffObj = new BufferObject(buffIds[ind], nverts, nobjs, Buffers.SIZEOF_DOUBLE, buffSize, 
				textured, drawFunc, storeType);
		buffInfo[buffInfo.length - 1] = buffObj;
		return buffObj;
	}

	private int findIndexOfID(int id) {
		for(int i=0; i < buffIds.length; i++) {
			if(id == buffIds[i])
				return i;
		}
		throw(new GLException("failed to locate buffer - ID does not exist: " + id));
	}

	/*
	 * Holds data used for handling Vertex Buffer Objects
	 */
	private class BufferObject {
		int[] vertIndices, vertNum;
		int id, nverts, nobjs, objCount, size;
		boolean textured;
		GeomFunc drawFunc;
		BufferUsage buffStore;

		// constructor sets values and pre-computes arrays that are needed for glMultiDrawArrays function
		BufferObject(int id, int nverts, int nobjs, int typeSize, int size, boolean textured, 
				GeomFunc drawFunc, BufferUsage usage) {
			this.id = id; this.nverts = nverts; this.nobjs = nobjs;
			this.size = size;
			this.textured = textured;
			this.drawFunc = drawFunc;
			this.buffStore = usage;
			vertIndices = new int[nobjs];
			vertNum = new int[nobjs];
			for(int i=0; i < vertNum.length; i++)
				vertNum[i] = nverts;
			for(int i=0; i < vertIndices.length; i++)
				vertIndices[i] = size / typeSize / nobjs / 2 * i;
		}
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

	/**
	 * Specifies standard methods of alpha blending.
	 * @author Brian Groenke
	 *
	 */
	public enum AlphaFunc {
		CLEAR, SRC, DST, SRC_OVER, DST_OVER, SRC_IN, DST_IN,
		SRC_OUT, DST_OUT, SRC_ATOP, DST_ATOP, ALPHA_XOR;
	}

	/**
	 * Represents a set number of supported OpenGL geometry drawing functions.
	 * @author Brian Groenke
	 *
	 */
	public enum GeomFunc {

		POINTS(GL_POINTS), LINES(GL_LINES), TRIANGLES(GL_TRIANGLES), TRIANGLE_STRIP(GL_TRIANGLE_STRIP), 
		POLYGON(GL_POLYGON);

		private int glcmd;

		GeomFunc(int glCommand) {
			this.glcmd = glCommand;
		}

		public int getGLCommand() {
			return glcmd;
		}
	}

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
		STATIC_DRAW(GL_STATIC_DRAW), 
		/**
		 * Should be used for data that will be updated frequently (i.e.
		 * every few frames).  Maps to field GL_DYNAMIC_DRAW.
		 */
		DYNAMIC_DRAW(GL_DYNAMIC_DRAW),
		/**
		 * Should be used for data that will be updated on every frame.
		 * Maps to field GL_STREAM_DRAW.
		 */
		STREAM_DRAW(GL_STREAM_DRAW);

		final int usageHint;

		BufferUsage(int usage) {
			this.usageHint = usage;
		}
	}

	/**
	 * Fetches the JOGL object used by this handle to make GL function calls.
	 * You must have JOGL 2.0+ on your build path in order to use this facility directly.
	 * @return the GL2 object backing this GLHandle
	 */
	public GL2 getGL() {
		return gl;
	}

	protected void onDestroy() {
		if(buffIds != null) {
			for(int i : buffIds)
				destroyBuff(i);
		}
	}

	// --- DEPRECATED / DISCARDED ---- //

	@Deprecated
	public void drawRect2f(float x, float y, float wt, float ht) {
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
			if(texCoords.length < 8) // 4 corners * 2 coordinates per corner
				throw(new GLException("too few tex coords: " + texCoords.length + " < 8"));
		}
		int tcoord = 0;
		gl.glBegin(GL_TRIANGLE_STRIP);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x, y);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x, y + ht);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x + wt, y);
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2f(x + wt, y + ht);
		gl.glEnd();
	}
	/*
	public void drawRect2d(double x, double y, double wt, double ht) {
		if(texBound && texEnabled) {
			setColor3f(1,1,1);
			if(texCoords.length < 8) // 4 corners * 2 coordinates per corner
				throw(new GLException("too few tex coords: " + texCoords.length + " < 8"));
		}
		int tcoord = 0;
		gl.glBegin(GL_TRIANGLE_STRIP);
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
		if(texBound && texEnabled)
			gl.glTexCoord2f(texCoords[tcoord++], texCoords[tcoord++]);
		gl.glVertex2d(x, y);
		gl.glEnd();
	}
	 */
}
