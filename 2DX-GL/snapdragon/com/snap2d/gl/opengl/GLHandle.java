/*
 *  Copyright (C) 2012-2014 Brian Groenke
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

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.io.IOException;
import java.nio.*;
import java.util.Arrays;

import javax.media.opengl.*;

import bg.x2d.geo.PointUD;
import bg.x2d.utils.Utils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.snap2d.SnapLogger;
import com.snap2d.gl.opengl.GLConfig.Property;

/**
 * Main handle object for manipulating GL data and graphics.  This class serves as the primary
 * mechanism for allocating, modifying, or drawing geometry and text, as well as allowing
 * configuration of underyling OpenGL functions.  You can obtain a GLHandle object from
 * {@link #GLRenderControl} through a {@link #GLRenderable}.
 * @author Brian Groenke
 *
 */
public class GLHandle {

	public static final float[] DEFAULT_RECT_TEX_COORDS = new float[] {0, 0, 0, 1, 1, 0, 1, 1},
			INVERTED_RECT_TEX_COORDS = new float[] {0, 1, 0, 0, 1, 1, 1, 0};
	public static final int FILTER_LINEAR = GL_LINEAR, FILTER_NEAREST = GL_NEAREST;

	public static final String UNIFORM_ORTHO_MATRIX = "mOrtho", UNIFORM_TRANSLATE = "vTranslate",
			UNIFORM_ROTATE = "vRotate", UNIFORM_SCALE = "vScale";

	private static final String DEFAULT_VERTEX_SHADER = "default.vert", DEFAULT_FRAG_SHADER = "default.frag";

	protected GLConfig config;
	protected TextRenderer textRender;

	float vx, vy, vwt, vht;
	int swt, sht;
	float ppu;
	float[] texCoords = DEFAULT_RECT_TEX_COORDS;
	boolean texEnabled, texBound;

	int magFilter = FILTER_LINEAR, minFilter = FILTER_LINEAR;

	FloatBuffer defColorBuff, orthoMatrix;
	
	// transformation values
	float theta, tx, ty, sx = 1, sy = 1;

	protected GLHandle(GLConfig config) {
		this.config = config;
		this.textRender = new TextRenderer(new Font("Arial",Font.PLAIN,12), true, true, null, 
				config.getAsBool(Property.GL_RENDER_TEXT_MIPMAP));
		this.defColorBuff = Buffers.newDirectFloatBuffer(new float[] {1,1,1,1});

		// create default shader program
		try {
			GLShader vert = GLShader.loadLibraryShader(GLShader.TYPE_VERTEX, DEFAULT_VERTEX_SHADER);
			GLShader frag = GLShader.loadLibraryShader(GLShader.TYPE_FRAGMENT, DEFAULT_FRAG_SHADER);
			GLProgram prog = new GLProgram(this);
			prog.attachShader(vert);
			prog.attachShader(frag);
			if(!prog.link()) {
				SnapLogger.printErr("error linking default shaders: ", true);
				prog.printLinkLog();
			}
			GLProgram.setDefaultProgram(prog);
			prog.setDefaultShaderProgram(true);
			prog.enable();
			prog.setUniformf("gamma", 1.0f);
		} catch (GLShaderException e) {
			SnapLogger.printErr("error loading default shaders:", true);
			e.printStackTrace();
		} catch (IOException e) {
			SnapLogger.printErr("error loading default shaders:", true);
			e.printStackTrace();
		}
	}

	/**
	 * Sets the coordinate viewport of the OpenGL context by creating an
	 * orthographic projection matrix that transforms vertices 2D space.
	 * The matrix will be uploaded into the 'ortho' mat4 uniform in the
	 * default shader program and the currently enabled program if it is
	 * not the default.
	 * @see {@link #setProgramTransform(GLProgram)}
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param ppu
	 */
	public void setViewport(float x, float y, int width, int height, float ppu) {
		if(width <= 0 || height <= 0)
			throw(new IllegalArgumentException("viewport dimensions must be > 0"));
		swt = width;
		sht = height;
		vx = x;
		vy = y;
		vwt = width / ppu;
		vht = height / ppu;
		this.ppu = ppu;

		orthoMatrix = GLUtils.createOrthoMatrix(x, x + vwt, y, y + vht, -1, 1);
		pushTransform();
		if(!GLProgram.isDefaultProgEnabled()) {
			GLProgram curr = GLProgram.getCurrentProgram();
			GLProgram.getDefaultProgram().enable();
			pushTransform();
			curr.enable();
		}
		
		/*
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(x, x + vwt, y, y + vht, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		 */

	}

	public void setTextureEnabled(boolean enabled) {
		final GL2GL3 gl = getGL();
		if(enabled)
			gl.glEnable(GL_TEXTURE_2D);
		else
			gl.glDisable(GL_TEXTURE_2D);
		texEnabled = enabled;
	}

	public void bindTexture(Texture2D tex) {
		final GL2GL3 gl = getGL();
		if(!texEnabled) {
			tex.enable(gl);
			texEnabled = true;
		}

		tex.bind(gl);
		tex.setTexParameteri(gl, GL_TEXTURE_MIN_FILTER, minFilter);
		tex.setTexParameteri(gl, GL_TEXTURE_MAG_FILTER, magFilter);
		//gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE); 
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
		setColor4f(r, g, b, 1);
	}

	public void setColor4f(float r, float g, float b, float a) {
		float[] colors = new float[] {r, g, b, a};
		defColorBuff.rewind();
		defColorBuff.put(colors);
		defColorBuff.flip();
	}

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
	 * Pushes the current transformations to the default/current shader program's matrix.
	 * Rendering operations done after calling this method will be transformed using the
	 * transformation settings at the time of the call until this method is invoked again
	 * or {@link #clearTransform()} is called to reset the transformation matrix.
	 */
	public void pushTransform() {
		if(orthoMatrix == null)
			throw(new IllegalStateException("error in pushTransform: coordinate viewport uninitialized"));
		GLProgram prog = GLProgram.getCurrentProgram();
		prog.setUniformMatrix(UNIFORM_ORTHO_MATRIX, 4, orthoMatrix);
		orthoMatrix.rewind();
		prog.setUniformf(UNIFORM_TRANSLATE, tx, ty);
		prog.setUniformf(UNIFORM_ROTATE, theta);
		prog.setUniformf(UNIFORM_SCALE, sx, sy);
	}

	/**
	 * Resets all currently stored transformation values to the default configuration (untransformed)
	 * and uploads the cleared transform to the active program via {@link #pushTransform()}
	 */
	public void clearTransform() {
		this.theta = 0;
		this.sx = 1; this.sy = 1;
		this.tx = 0; this.ty = 0;
		pushTransform();
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

	/**
	 * Sets the texture coordinates based on the four corners of the given
	 * Texture2D.
	 * @param rectCoords the texture to set texture coordinates from.
	 */
	public void setRectTexCoords(Texture2D rectCoords) {
		texCoords[0] = rectCoords.getLeftCoord(); texCoords[1] = rectCoords.getBottomCoord();
		texCoords[2] = rectCoords.getLeftCoord(); texCoords[3] = rectCoords.getTopCoord();
		texCoords[4] = rectCoords.getRightCoord(); texCoords[5] = rectCoords.getBottomCoord();
		texCoords[6] = rectCoords.getRightCoord(); texCoords[7] = rectCoords.getTopCoord();
	}

	// ---- Data Store/Access and Drawing ---- //

	private int[] buffIds;
	private BufferObject[] buffInfo;

	private final float[] color = new float[4]; // array that holds color data for buffer I/O

	private static final int vertColorPos = 0, vertCoordPos = 1, texCoordPos = 2;

	/**
	 * Draws the buffer specified by 'buffId' to screen according to parameters set using its
	 * associated buffer properties.
	 * @param buffId the id of the vertex buffer to draw
	 */
	public void draw2f(int buffId) {
		final GL2GL3 gl = getGL();
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		BufferObject buffObj = buffInfo[findIndexOfID(buffId)];
		// draw all quads in vertex buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffId);

		if(buffObj.data != null) {
			buffObj.data.flip();
			gl.glBufferSubData(GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
		}

		gl.glEnableVertexAttribArray(vertCoordPos);
		gl.glVertexAttribPointer(vertCoordPos, 2, GL_FLOAT, false, 0, 0);
		if(texBound && texEnabled) {
			gl.glEnableVertexAttribArray(texCoordPos);
			gl.glVertexAttribPointer(texCoordPos, 2, GL_FLOAT, false, 0, 2 * buffObj.nverts * Buffers.SIZEOF_FLOAT);
		} else {
			gl.glEnableVertexAttribArray(vertColorPos);
			gl.glVertexAttribPointer(vertColorPos, 4, GL_FLOAT, false, 0, 2 * buffObj.nverts * Buffers.SIZEOF_FLOAT);
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);

		// disable arrays
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glDisableVertexAttribArray(vertCoordPos);
		gl.glDisableVertexAttribArray(vertColorPos);
		if(texBound && texEnabled)
			gl.glDisableVertexAttribArray(texCoordPos);

	}

	public void draw2d(int buffId) {
		final GL2GL3 gl = getGL();
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		BufferObject buffObj = buffInfo[findIndexOfID(buffId)];
		// draw all quads in vertex buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffId);

		if(buffObj.data != null) {
			buffObj.data.flip();
			gl.glBufferSubData(GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
		}

		gl.glEnableVertexAttribArray(vertCoordPos);
		gl.glVertexAttribPointer(vertCoordPos, 2, GL2.GL_DOUBLE, false, 0, 0);
		if(texBound && texEnabled) {
			gl.glEnableVertexAttribArray(texCoordPos);
			gl.glVertexAttribPointer(texCoordPos, 2, GL2.GL_DOUBLE, false, 0, 2 * buffObj.nverts * Buffers.SIZEOF_DOUBLE);
		} else {
			gl.glEnableVertexAttribArray(vertColorPos);
			gl.glVertexAttribPointer(vertColorPos, 4, GL2.GL_DOUBLE, false, 0, 2 * buffObj.nverts * Buffers.SIZEOF_DOUBLE);
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);

		// disable arrays
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glDisableVertexAttribArray(vertCoordPos);
		gl.glDisableVertexAttribArray(vertColorPos);
		if(texBound && texEnabled)
			gl.glDisableVertexAttribArray(texCoordPos);
	}

	/**
	 * Writes coordinate data for a given quad to the vertex buffer.  Each successive call to this
	 * method writes an additional object into the buffer until N objects have been written, where N
	 * is the set number of objects for this buffer, or until {@link #resetBuff(int)} is called.  Any
	 * calls made to {@link #draw2f(int)} will draw whatever number of objects are available in the buffer.
	 * Once a buffer's object limit is reached or it is reset, subsequent calls to this method will cause
	 * all of its vertex data to be discarded and restart the process.<br/><br/>
	 * <b>Note: DO NOT rely on {@link #resetBuff(int)} or writing N+1 objects to wipe existing data from VRAM
	 * before {@link #draw2f(int)} is called.</b>  This likely WILL NOT happen, and the previously uploaded vertex
	 * data will still be at least partially rendered.  The only reliable way to do this is by destroying and re-creating
	 * the buffer.
	 * @param rectBuffId the id for the quad's vertex buffer
	 * @param x
	 * @param y
	 * @param wt
	 * @param ht
	 * @param colorBuffer a FloatBuffer containing color data in strides of 4 for all of the vertices - if the color
	 * buffer contains fewer colors than there are vertices, the last color in the buffer will be reused.  You may
	 * pass null in this argument to use the current default color set via {@link #setColor4f(float, float, float, float)}
	 */
	public void putQuad2f(int rectBuffId, float x, float y, float wt, float ht, FloatBuffer colorBuffer) {
		if(buffIds == null)
			throw(new GLException("can't write quad data: no allocated buffers"));
		
		final GL2GL3 gl = getGL();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = buffInfo[findIndexOfID(rectBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL_ARRAY_BUFFER, rectBuffId);

		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
			FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);

			floatBuff.put(x); floatBuff.put(y);

			floatBuff.put(x); floatBuff.put(y + ht);

			floatBuff.put(x + wt); floatBuff.put(y);

			floatBuff.put(x + wt); floatBuff.put(y + ht);

			if(buffObj.textured)
				floatBuff.put(texCoords);
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					floatBuff.put(color);
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		} else {
			FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			floatBuff.limit(floatBuff.capacity());
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);

			floatBuff.put(x); floatBuff.put(y);

			floatBuff.put(x); floatBuff.put(y + ht);

			floatBuff.put(x + wt); floatBuff.put(y);

			floatBuff.put(x + wt); floatBuff.put(y + ht);

			if(buffObj.textured)
				floatBuff.put(texCoords);
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					floatBuff.put(color);
				}
			}
			buffObj.objCount++; // increment object count
		}
	}

	public void putQuad2d(int rectBuffId, double x, double y, double wt, double ht, FloatBuffer colorBuffer) {
		if(buffIds == null)
			throw(new GLException("can't write quad data: no allocated buffers"));
		
		final GL2GL3 gl = getGL();
		
		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = buffInfo[findIndexOfID(rectBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL_ARRAY_BUFFER, rectBuffId);

		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
			DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			doubleBuff.put(x); doubleBuff.put(y);
			doubleBuff.put(x); doubleBuff.put(y + ht);
			doubleBuff.put(x + wt); doubleBuff.put(y);
			doubleBuff.put(x + wt); doubleBuff.put(y + ht);
			if(buffObj.textured)
				doubleBuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 
						0, texCoords.length));
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, new double[color.length], 0, color.length));
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		} else {
			DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			doubleBuff.limit(doubleBuff.capacity());
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			doubleBuff.put(x); doubleBuff.put(y);
			doubleBuff.put(x); doubleBuff.put(y + ht);
			doubleBuff.put(x + wt); doubleBuff.put(y);
			doubleBuff.put(x + wt); doubleBuff.put(y + ht);
			if(buffObj.textured)
				doubleBuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 
						0, texCoords.length));
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, new double[color.length], 0, color.length));
				}
			}
			doubleBuff.flip();
			buffObj.objCount++; // increment object count
		}
	}

	/**
	 * Writes coordinate data for a given poly to the vertex buffer.  Each successive call to this
	 * method writes an additional object into the buffer until N objects have been written, where N
	 * is the set number of objects for this buffer, or until {@link #resetBuff(int)} is called.  Any
	 * calls made to {@link #draw2f(int)} will draw whatever number of objects are available in the buffer.
	 * Once a buffer's object limit is reached or it is reset, subsequent calls to this method will cause
	 * all of its vertex data to be discarded and restart the process.<br/><br/>
	 * <b>Note: DO NOT rely on {@link #resetBuff(int)} or writing N+1 objects to wipe existing data from VRAM
	 * before {@link #draw2f(int)} is called.</b>  This likely WILL NOT happen, and the previously uploaded vertex
	 * data will still be at least partially rendered.  The only reliable way to do this is by destroying and re-creating
	 * the buffer.
	 * @param polyBuffId the id for the polygon's vertex buffer
	 * @param colorBuffer a FloatBuffer containing color data in strides of 4 for all of the vertices - if the color
	 * buffer contains fewer colors than there are vertices, the last color in the buffer will be reused.  You may
	 * pass null in this argument to use the current default color set via {@link #setColor4f(float, float, float, float)}
	 * @param points the vertices of the polygon in world-space as a varargs
	 */
	public void putPoly2f(int polyBuffId, FloatBuffer colorBuffer, PointUD... points) {
		if(buffIds == null)
			throw(new GLException("can't write poly data: no allocated buffers"));
		
		final GL2GL3 gl = getGL();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = buffInfo[findIndexOfID(polyBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL_ARRAY_BUFFER, polyBuffId);

		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
			FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			for(PointUD pt : points) {
				floatBuff.put(pt.getFloatX()); 
				floatBuff.put(pt.getFloatY());
			}
			if(buffObj.textured)
				floatBuff.put(texCoords);
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					floatBuff.put(color);
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		} else {
			FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			floatBuff.limit(floatBuff.capacity());
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			for(PointUD pt : points) {
				floatBuff.put(pt.getFloatX()); 
				floatBuff.put(pt.getFloatY());
			}
			if(buffObj.textured)
				floatBuff.put(texCoords);
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					floatBuff.put(color);
				}
			}
			floatBuff.flip();
			colorBuffer.flip();
			buffObj.objCount++; // increment object count
		}
	}

	public void putPoly2d(int rectBuffId, PointUD[] points, FloatBuffer colorBuffer) {
		if(buffIds == null)
			throw(new GLException("can't write poly data: no allocated buffers"));
		
		final GL2GL3 gl = getGL();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = buffInfo[findIndexOfID(rectBuffId)];
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL_ARRAY_BUFFER, rectBuffId);

		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
			DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			for(PointUD pt : points) {
				doubleBuff.put(pt.getFloatX()); 
				doubleBuff.put(pt.getFloatY());
			}
			if(buffObj.textured)
				doubleBuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 
						0, texCoords.length));
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, new double[color.length], 0, color.length));
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		} else {
			DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			doubleBuff.limit(doubleBuff.capacity());
			doubleBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			for(PointUD pt : points) {
				doubleBuff.put(pt.getFloatX()); 
				doubleBuff.put(pt.getFloatY());
			}
			if(buffObj.textured)
				doubleBuff.put(Buffers.getDoubleArray(texCoords, 0, new double[texCoords.length], 
						0, texCoords.length));
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, new double[color.length], 0, color.length));
				}
			}
			doubleBuff.flip();
			colorBuffer.flip();
			buffObj.objCount++; // increment object count
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
	public int createQuadBuffer2f(BufferUsage storeType, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2f(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
		return buffObj.id;
	}

	public int createQuadBuffer2d(BufferUsage storeType, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2d(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
		return buffObj.id;
	}

	/**
	 * Allocates a vertex buffer for rendering a given number of polygons with a
	 * given number of vertices.  Ther returned id must be stored and used in order to
	 * write and draw the polygon data.
	 * @param storeType a hint for how the graphics driver should treat the buffer data in VRAM
	 * @param drawFunc the GL function that should be used for drawing the polygon vertices
	 * @param verts number of vertices per polygon
	 * @param nobjs number of polygons
	 * @param textured true if this polygon will be textured (1 tex coord per vertex), false otherwise
	 * @return the id for the poly buffer
	 */
	public int createPolyBuffer2f(BufferUsage storeType, GeomFunc drawFunc, int verts, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2f(storeType, drawFunc, verts, nobjs, textured);
		return buffObj.id;
	}

	public int createPolyBuffer2d(BufferUsage storeType, GeomFunc drawFunc, int verts, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2d(storeType, drawFunc, verts, nobjs, textured);
		return buffObj.id;
	}

	/**
	 * Deletes the VBO held with the given ID from memory.
	 * @param id the id for the buffer returned by {@link #createRectBuff2f(BufferUsage)}
	 * and {@link #createRectBuff2d(BufferUsage)}
	 * @return
	 */
	public boolean destroyBuff(int id) {
		final GL2GL3 gl = getGL();
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
		final GL2GL3 gl = getGL();
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
			buffSize = 2 * 2 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		} else {
			buffSize = 2 * 4 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		}

		gl.glBufferData(GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		BufferObject buffObj = new BufferObject(buffIds[ind], nverts, nobjs, Buffers.SIZEOF_FLOAT, buffSize, 
				textured, drawFunc, storeType);
		if(storeType != BufferUsage.STATIC_DRAW)
			buffObj.data = Buffers.newDirectFloatBuffer(buffSize);
		buffInfo[buffInfo.length - 1] = buffObj;
		return buffObj;
	}

	/*
	 * appends the buffer index to 'buffIds' and returns the BufferObject for the newly created buffer
	 */
	private BufferObject initVBO2d(BufferUsage storeType, GeomFunc drawFunc, int nverts, 
			int nobjs, boolean textured) {
		final GL2GL3 gl = getGL();
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
			buffSize = 4 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		} else {
			buffSize = 2 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		}

		gl.glBufferData(GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		BufferObject buffObj = new BufferObject(buffIds[ind], nverts, nobjs, Buffers.SIZEOF_DOUBLE, buffSize, 
				textured, drawFunc, storeType);
		buffObj.data = Buffers.newDirectDoubleBuffer(buffSize / Buffers.SIZEOF_DOUBLE);
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
		BufferUsage storeHint;

		Buffer data;

		// constructor sets values and pre-computes arrays that are needed for glMultiDrawArrays function
		BufferObject(int id, int nverts, int nobjs, int typeSize, int size, boolean textured, 
				GeomFunc drawFunc, BufferUsage usage) {
			this.id = id; this.nverts = nverts; this.nobjs = nobjs;
			this.size = size;
			this.textured = textured;
			this.drawFunc = drawFunc;
			this.storeHint = usage;
			vertIndices = new int[nobjs];
			vertNum = new int[nobjs];
			for(int i=0; i < vertNum.length; i++)
				vertNum[i] = nverts;
			for(int i=0; i < vertIndices.length; i++)
				vertIndices[i] = size / typeSize / nobjs / 2 * i;
		}
	}

	/**
	 * Draws text to the given screen location.  If the default shader
	 * program is currently enabled, it will be disabled before text is
	 * rendered.  If a custom shader is enabled, it's up to the caller
	 * whether or not it should be disabled.
	 * @param text
	 * @param color
	 * @param x
	 * @param y
	 */
	public void drawText(String text, Color color, int x, int y) {
		drawText(text, x, y, color.getComponents(new float[4]));
	}

	public void drawText(String text, int x, int y, float[] rgba) {
		boolean disableShaders = GLProgram.isDefaultProgEnabled();
		if(disableShaders)
			GLProgram.getDefaultProgram().disable();

		textRender.beginRendering(swt, sht);
		textRender.setSmoothing(config.getAsBool(Property.GL_RENDER_TEXT_SMOOTH));
		textRender.setUseVertexArrays(config.getAsBool(Property.GL_RENDER_TEXT_USE_VAO));
		textRender.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
		textRender.draw(text, x, y);
		textRender.endRendering();

		if(disableShaders)
			GLProgram.getDefaultProgram().enable();
	}

	/**
	 * Draws the given array of text strings at their respective screen coordinates 
	 * specified in IntBuffer 'coords' with their respective colors in FloatBuffer colors
	 * @param texts
	 * @param coords
	 * @param colors
	 */
	public void drawTextBatch(String[] texts, IntBuffer coords, FloatBuffer colors) {
		float[] color = new float[4];
		if(colors.limit() < 4)
			throw(new IllegalArgumentException("color buffer must have at least 4 values"));
		for(String s : texts) {
			int x = coords.get();
			int y = coords.get();
			if(colors.position() < colors.limit())
				colors.get(color, 0, Math.min(colors.remaining(), color.length));
			drawText(s, x, y, color);
		}
	}

	/**
	 * Re-creates the internal TextRenderer with the given Font.
	 * @param font
	 */
	public void setFont(Font font) {
		textRender.dispose();
		textRender = new TextRenderer(font, true, true, null, config.getAsBool(Property.GL_RENDER_TEXT_MIPMAP));
	}

	// -------- OpenGL Feature Control --------- //

	public void setEnabled(GLFeature feature, boolean enable) {
		final GL2GL3 gl = getGL();
		if(enable)
			gl.glEnable(feature.getGLCommand());
		else
			gl.glDisable(feature.getGLCommand());
	}

	/**
	 * Mappings to selected OpenGL features that may be enabled/disabled by the caller.
	 * @author Brian Groenke
	 *
	 */
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
		final GL2GL3 gl = getGL();
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
			break;
		case SRC_DST:
			gl.glBlendFunc(GL_ONE, GL_ONE);
			break;
		case SRC_BLEND:
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_BLEND:
			gl.glBlendFunc(GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA);
		}
	}

	/**
	 * Specifies standard methods of alpha blending.
	 * @author Brian Groenke
	 *
	 */
	public enum AlphaFunc {
		CLEAR, SRC, DST, SRC_OVER, DST_OVER, SRC_IN, DST_IN,
		SRC_OUT, DST_OUT, SRC_ATOP, DST_ATOP, ALPHA_XOR, SRC_DST, SRC_BLEND,
		DST_BLEND;
	}

	/**
	 * Represents a set number of supported OpenGL geometry drawing functions.
	 * @author Brian Groenke
	 *
	 */
	public enum GeomFunc {

		POINTS(GL_POINTS), 
		LINES(GL_LINES), 
		LINE_STRIP(GL_LINE_STRIP), 
		TRIANGLES(GL_TRIANGLES),
		TRIANGLE_STRIP(GL_TRIANGLE_STRIP), 
		TRIANGLE_FAN(GL_TRIANGLE_FAN);

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
		STREAM_DRAW(GL2.GL_STREAM_DRAW);

		final int usageHint;

		BufferUsage(int usage) {
			this.usageHint = usage;
		}
	}

	protected void onDispose() {
		textRender.dispose();
		if(buffIds != null) {
			for(int i : buffIds)
				destroyBuff(i);
		}
	}
	
	private GL2GL3 getGL() {
		return GLContext.getCurrentGL().getGL2GL3();
	}

	// --- DEPRECATED / DISCARDED ---- //

	/*
	@Deprecated
	public void drawRect2f(float x, float y, float wt, float ht) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
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
	 */
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

	/*
	private void drawNoGLSL2f(BufferObject buffObj) {
		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY);
		if(texBound && texEnabled) {
			gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl.glVertexPointer(2, GL_FLOAT, 0, 0);
			gl.glTexCoordPointer(2, GL_FLOAT, 0, 2 * buffObj.nverts * Buffers.SIZEOF_FLOAT);
		} else {
			gl.glVertexPointer(2, GL_FLOAT, 0, 0);
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);

		// disable arrays
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		if(texBound && texEnabled)
			gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
	 */
}
