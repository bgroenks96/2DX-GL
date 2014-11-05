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

import java.awt.*;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.logging.Logger;

import javax.media.opengl.*;

import bg.x2d.geo.PointUD;
import bg.x2d.utils.Utils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.snap2d.gl.opengl.GLConfig.Property;
import com.snap2d.light.LightSource;

/**
 * @author Brian Groenke
 *
 */
public class GL3Handle implements GLHandle {


	public static final String UNIFORM_ORTHO_MATRIX = "mOrtho", UNIFORM_TRANSLATE = "vTranslate",
			UNIFORM_ROTATE = "fRotate", UNIFORM_ROTATE_PIVOT = "vPivot", UNIFORM_SCALE = "vScale";

	private static final String DEFAULT_VERTEX_SHADER = "snap2d-default.vert", DEFAULT_FRAG_SHADER = "snap2d-default.frag";

	private static final Logger log = Logger.getLogger(GLHandle.class.getCanonicalName());

	protected GLConfig config;
	protected TextRenderer textRender;

	float vx, vy, vwt, vht;
	int swt, sht;
	float ppu;
	float[] texCoords = DEFAULT_RECT_TEX_COORDS;
	boolean texEnabled, texBound;

	int magFilter = FILTER_LINEAR, minFilter = FILTER_LINEAR;

	FloatBuffer defColorBuff, orthoMatrix;

	HashSet<LightSource> lights = new HashSet<LightSource>();

	// transformation values
	float theta, rx, ry, tx, ty, sx = 1, sy = 1;

	GL3Handle(GLConfig config) {
		this.config = config;
		this.textRender = new TextRenderer(new Font("Arial",Font.PLAIN,12), true, true, null, 
				config.getAsBool(Property.GL_RENDER_TEXT_MIPMAP));
		this.defColorBuff = Buffers.newDirectFloatBuffer(new float[] {1,1,1,1});

		// create default shader program
		try {
			GLShader vert = GLShader.loadLibraryShader(GLShader.TYPE_VERTEX, DEFAULT_VERTEX_SHADER);
			GLShader frag = GLShader.loadLibraryShader(GLShader.TYPE_FRAGMENT, DEFAULT_FRAG_SHADER);
			GLProgram prog = new GLProgram();
			prog.attachShader(vert);
			prog.attachShader(frag);
			if(!prog.link()) {
				log.warning("error linking default shaders: ");
				prog.printLinkLog();
			}
			GLProgram.setDefaultProgram(prog);
			prog.enable();
			prog.setUniformf("gamma", 1.0f);
			log.fine("successfully loaded default shaders");
		} catch (GLShaderException e) {
			log.warning("error loading default shaders:");
			e.printStackTrace();
		} catch (IOException e) {
			log.warning("error loading default shaders:");
			e.printStackTrace();
		}
		log.info("initialized GLHandle (gl3-core)");
	}

	/**
	 * Sets the coordinate viewport of the OpenGL context by creating an
	 * orthographic projection matrix that transforms vertices 2D space.
	 * The matrix will be uploaded into the 'mOrtho' mat4 uniform in the
	 * default shader program and the currently enabled program if it is
	 * not the default.
	 * @see {@link #setProgramTransform(GLProgram)}
	 * @param x
	 * @param y
	 * @param width the unit width of the viewport
	 * @param height the unit height of the viewport
	 * @param ppu pixels-per-unit; for normal use, just use 1
	 */
	public void setViewport(float x, float y, float width, float height, float ppu) {
		if(width <= 0 || height <= 0)
			throw(new IllegalArgumentException("viewport dimensions must be > 0"));
		if(ppu <= 0)
			throw(new IllegalArgumentException("ppu must be > 0"));
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
	}
	
	public void setDisplaySize(int width, int height) {
		this.swt = width;  this.sht = height;
	}

	public float getViewportWidth() {
		return vwt;
	}

	public float getViewportHeight() {
		return vht;
	}

	public float getPPU() {
		return ppu;
	}

	public void setTextureEnabled(boolean enabled) {
		final GL gl = getGL();
		if(enabled)
			gl.glEnable(GL.GL_TEXTURE_2D);
		else
			gl.glDisable(GL.GL_TEXTURE_2D);
		texEnabled = enabled;
	}

	public void bindTexture(Texture2D tex) {
		final GL gl = getGL();
		if(!texEnabled) {
			tex.enable(gl);
			texEnabled = true;
		}

		tex.bind(gl);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, minFilter);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, magFilter);
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
					minFilter = GL.GL_LINEAR_MIPMAP_LINEAR;
				else if(mipmapType == FILTER_NEAREST)
					minFilter = GL.GL_LINEAR_MIPMAP_NEAREST;
				break;
			case FILTER_NEAREST:
				if(mipmapType == FILTER_LINEAR)
					minFilter = GL.GL_NEAREST_MIPMAP_LINEAR;
				else if(mipmapType == FILTER_NEAREST)
					minFilter = GL.GL_NEAREST_MIPMAP_NEAREST;

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

	public void setRotationPoint(float x, float y) {
		this.rx = x; this.ry = y;
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
		checkGLError("pushTransform ["+UNIFORM_TRANSLATE+"]");
		prog.setUniformf(UNIFORM_ROTATE, theta);
		checkGLError("pushTransform ["+UNIFORM_ROTATE+"]");
		prog.setUniformf(UNIFORM_ROTATE_PIVOT, rx, ry);
		checkGLError("pushTransform ["+UNIFORM_ROTATE_PIVOT+"]");
		prog.setUniformf(UNIFORM_SCALE, sx, sy);
		checkGLError("pushTransform ["+UNIFORM_SCALE+"]");
	}

	/**
	 * Resets all currently stored transformation values to the default configuration (untransformed)
	 * and uploads the cleared transform to the active program via {@link #pushTransform()}
	 */
	public void clearTransform() {
		this.theta = 0;
		this.sx = 1; this.sy = 1;
		this.tx = 0; this.ty = 0;
		this.rx = 0; this.ry = 0;
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

	public void addLightSource(LightSource light) {
		lights.add(light);
	}

	public void removeLightSource(LightSource light) {
		lights.remove(light);
	}

	float[] ambientColor = new float[] {1,1,1};
	float ambientFactor;

	public void setAmbientLightFactor(float ambientFactor) {
		this.ambientFactor = ambientFactor;
	}

	public void setAmbientLightColor(float[] ambientLightColor) {
		System.arraycopy(ambientLightColor, 0, ambientColor, 0, ambientColor.length);
	}

	// lighting system string constants
	private static final String
	UNIFORM_LCOUNT = "light_count", UNIFORM_LCOORDS = "lights", UNIFORM_LCOLORS = "light_colors",
	UNIFORM_LINTENSITY = "intensity", UNIFORM_LRADIUS = "radius", UNIFORM_AMBIENT = "ambient",
	UNIFORM_AMBIENT_COLOR = "ambient_color";

	public void updateLightData() throws IllegalStateException {
		FloatBuffer lightCoords = Buffers.newDirectFloatBuffer(lights.size() * 2);
		FloatBuffer lightColors = Buffers.newDirectFloatBuffer(lights.size() * 3);
		FloatBuffer radii = Buffers.newDirectFloatBuffer(lights.size());
		FloatBuffer intensity = Buffers.newDirectFloatBuffer(lights.size());
		for(LightSource light : lights) {
			if(!light.isEnabled())
				continue;
			PointUD loc = light.getLocation();
			lightCoords.put(loc.getFloatX()); lightCoords.put(loc.getFloatY());
			float[] color = light.getColor();
			lightColors.put(color);
			float radius = light.getRadius();
			radii.put(radius);
			float ifactor = light.getIntensity();
			intensity.put(ifactor);
		}

		lightCoords.flip();
		lightColors.flip();
		radii.flip();
		intensity.flip();

		GLProgram prog = GLProgram.getCurrentProgram();
		prog.setUniformi(UNIFORM_LCOUNT, lights.size());
		checkGLError(UNIFORM_LCOUNT);
		prog.setUniformfv(UNIFORM_LCOORDS, 2, lightCoords);
		checkGLError(UNIFORM_LCOORDS);
		prog.setUniformfv(UNIFORM_LCOLORS, 3, lightColors);
		checkGLError(UNIFORM_LCOLORS);
		prog.setUniformfv(UNIFORM_LRADIUS, 1, radii);
		checkGLError(UNIFORM_LRADIUS);
		prog.setUniformfv(UNIFORM_LINTENSITY, 1, intensity);
		checkGLError(UNIFORM_LINTENSITY);
		prog.setUniformf(UNIFORM_AMBIENT, ambientFactor);
		checkGLError(UNIFORM_AMBIENT);
		prog.setUniformf(UNIFORM_AMBIENT_COLOR, ambientColor);
		checkGLError(UNIFORM_AMBIENT_COLOR);
	}

	// ---- Data Store/Access and Drawing ---- //

	//private int[] buffIds;
	private BufferObject[] buffInfo;

	private final float[] color = new float[4]; // array that holds color data for buffer I/O

	/**
	 * Draws the buffer specified by 'buffId' to screen according to parameters set using its
	 * associated buffer properties.
	 * @param buffId the id of the vertex buffer to draw
	 */
	public void draw2f(int buffId) {
		final GL2GL3 gl = getGL2GL3();
		final BufferObject buffObj = findBufferById(buffId);
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		// draw all quads in vertex buffer
		gl.glBindVertexArray(buffObj.vao);
		checkGLError("glBindVertexArray");
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffObj.vbo);
		checkGLError("glBindBuffer");

		if(buffObj.data != null && buffObj.data.limit() > 0) {
			//System.out.println("vbo="+buffObj.vbo + " vao="+buffObj.vao + " nverts="+buffObj.vertNum[0]+" nobjs="+buffObj.nobjs + " " + buffObj.data);
			buffObj.data.flip();
			gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
			checkGLError("glBufferSubData");
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);
		checkGLError("glMultiDrawArrays");

		// disable vertex array
		gl.glBindVertexArray(0);
		checkGLError("glBindVertexArray [unbind]");
	}

	public void draw2d(int buffId) {
		final GL2GL3 gl = getGL2GL3();
		final BufferObject buffObj = findBufferById(buffId);
		if(texBound && texEnabled) {
			setColor4f(1,1,1,1);
		}

		// draw all quads in vertex buffer
		gl.glBindVertexArray(buffObj.vao);
		checkGLError("glBindVertexArray");

		if(buffObj.data != null) {
			buffObj.data.flip();
			gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
			checkGLError("glBufferSubData");
		}

		gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0, buffObj.objCount);
		checkGLError("glMultiDrawArrays");

		// disable vertex array
		gl.glBindVertexArray(0);
		checkGLError("glBindVertexArray | (unbind)");
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
		if(buffInfo == null || buffInfo.length == 0)
			throw(new GLException("can't write quad data: no allocated buffers"));

		final GL2GL3 gl = getGL2GL3();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = findBufferById(rectBuffId);
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindVertexArray(buffObj.vao);
		checkGLError("glBindVertexArray");
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);
		checkGLError("glBindBuffer");

		if (buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
			checkGLError("glMapBuffer");
			FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			//FloatBuffer floatBuff = Buffers.newDirectFloatBuffer(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs);

			floatBuff.put(x); floatBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[0]); floatBuff.put(texCoords[1]);
			}

			floatBuff.put(x); floatBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[2]); floatBuff.put(texCoords[3]);
			}

			floatBuff.put(x + wt); floatBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[4]); floatBuff.put(texCoords[5]);
			}

			floatBuff.put(x + wt); floatBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[6]); floatBuff.put(texCoords[7]);
			}

			buffObj.objCount++; // increment object count
			if(!gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER))
				log.warning("putQuad2f: glUnmapBuffer returned false");
			checkGLError("glUnmapBuffer");
		} else {
			FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
			floatBuff.limit(floatBuff.capacity());
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);

			floatBuff.put(x); floatBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[0]); floatBuff.put(texCoords[1]);
			}

			floatBuff.put(x); floatBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[2]); floatBuff.put(texCoords[3]);
			}

			floatBuff.put(x + wt); floatBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[4]); floatBuff.put(texCoords[5]);
			}

			floatBuff.put(x + wt); floatBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				floatBuff.put(color);
			} else {
				floatBuff.put(texCoords[6]); floatBuff.put(texCoords[7]);
			}

			buffObj.objCount++; // increment object count
		}
		gl.glBindVertexArray(0);
		checkGLError("glBindVertexArray [unbind]");
	}

	public void putQuad2d(int rectBuffId, double x, double y, double wt, double ht, FloatBuffer colorBuffer) {
		if(buffInfo == null || buffInfo.length == 0)
			throw(new GLException("can't write quad data: no allocated buffers"));

		final GL2GL3 gl = getGL2GL3();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = findBufferById(rectBuffId);
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);

		final double[] dcolor = new double[4];
		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
			DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);

			doubleBuff.put(x); doubleBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[0]); doubleBuff.put(texCoords[1]);
			}

			doubleBuff.put(x); doubleBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[2]); doubleBuff.put(texCoords[3]);
			}

			doubleBuff.put(x + wt); doubleBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[4]); doubleBuff.put(texCoords[5]);
			}

			doubleBuff.put(x + wt); doubleBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[6]); doubleBuff.put(texCoords[7]);
			}


			/*
			if(buffObj.textured)
				floatBuff.put(texCoords);
			else {
				for(int i=0; i < buffObj.nverts; i++) {
					int readLen = Math.min(colorBuffer.limit() - colorBuffer.position(), color.length);
					colorBuffer.get(color, 0, readLen);
					floatBuff.put(color);
				}
			}*/
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
		} else {
			DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
			doubleBuff.limit(doubleBuff.capacity());
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);

			doubleBuff.put(x); doubleBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[0]); doubleBuff.put(texCoords[1]);
			}

			doubleBuff.put(x); doubleBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[2]); doubleBuff.put(texCoords[3]);
			}

			doubleBuff.put(x + wt); doubleBuff.put(y);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[4]); doubleBuff.put(texCoords[5]);
			}

			doubleBuff.put(x + wt); doubleBuff.put(y + ht);
			if(!buffObj.textured) {
				GLUtils.readAvailable(colorBuffer, color);
				doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
			} else {
				doubleBuff.put(texCoords[6]); doubleBuff.put(texCoords[7]);
			}

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
		if(buffInfo == null || buffInfo.length == 0)
			throw(new GLException("can't write poly data: no allocated buffers"));

		final GL2GL3 gl = getGL2GL3();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = findBufferById(polyBuffId);
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, polyBuffId);

		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
			FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			for(int i = 0, t = 0; i < points.length; i++, t+=2) {
				PointUD pt = points[i];
				floatBuff.put(pt.getFloatX()); 
				floatBuff.put(pt.getFloatY());
				if(buffObj.textured) {
					floatBuff.put(texCoords[t]); floatBuff.put(texCoords[t+1]);
				} else {
					GLUtils.readAvailable(colorBuffer, color);
					floatBuff.put(color);
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
		} else {
			FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			floatBuff.limit(floatBuff.capacity());
			floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
			for(int i = 0, t = 0; i < points.length; i++, t+=2) {
				PointUD pt = points[i];
				floatBuff.put(pt.getFloatX()); 
				floatBuff.put(pt.getFloatY());
				if(buffObj.textured) {
					floatBuff.put(texCoords[t]); floatBuff.put(texCoords[t+1]);
				} else {
					GLUtils.readAvailable(colorBuffer, color);
					floatBuff.put(color);
				}
			}
			floatBuff.flip();
			colorBuffer.flip();
			buffObj.objCount++; // increment object count
		}
	}

	public void putPoly2d(int rectBuffId, PointUD[] points, FloatBuffer colorBuffer) {
		if(buffInfo == null || buffInfo.length == 0)
			throw(new GLException("can't write poly data: no allocated buffers"));

		final GL2GL3 gl = getGL2GL3();

		if(colorBuffer == null || colorBuffer.limit() < 4) {
			defColorBuff.rewind();
			colorBuffer = defColorBuff;
		}

		BufferObject buffObj = findBufferById(rectBuffId);
		int buffSize = buffObj.size;
		if(buffObj.objCount >= buffObj.nobjs) // if we've reached the total number of objects, reset count
			buffObj.objCount = 0;

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);

		final double[] dcolor = new double[4];
		if(buffObj.storeHint == BufferUsage.STATIC_DRAW) {
			ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
			DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			for(int i = 0, t = 0; i < points.length; i++, t+=2) {
				PointUD pt = points[i];
				doubleBuff.put(pt.getFloatX()); 
				doubleBuff.put(pt.getFloatY());
				if(buffObj.textured) {
					doubleBuff.put(texCoords[t]); doubleBuff.put(texCoords[t+1]);
				} else {
					GLUtils.readAvailable(colorBuffer, color);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
				}
			}
			buffObj.objCount++; // increment object count
			gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
		} else {
			DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
			//if(buffObj.objCount != 0)
			//	gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize / buffObj.nobjs * buffObj.objCount, floatBuff);
			doubleBuff.limit(doubleBuff.capacity());
			doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
			for(int i = 0, t = 0; i < points.length; i++, t+=2) {
				PointUD pt = points[i];
				doubleBuff.put(pt.getFloatX()); 
				doubleBuff.put(pt.getFloatY());
				if(buffObj.textured) {
					doubleBuff.put(texCoords[t]); doubleBuff.put(texCoords[t+1]);
				} else {
					GLUtils.readAvailable(colorBuffer, color);
					doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
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
		findBufferById(buffId).objCount = 0;
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
		return buffObj.vbo;
	}

	public int createQuadBuffer2d(BufferUsage storeType, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2d(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
		return buffObj.vbo;
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
		return buffObj.vbo;
	}

	public int createPolyBuffer2d(BufferUsage storeType, GeomFunc drawFunc, int verts, int nobjs, boolean textured) {
		BufferObject buffObj = initVBO2d(storeType, drawFunc, verts, nobjs, textured);
		return buffObj.vbo;
	}

	/**
	 * Deletes the VBO held with the given ID from memory.
	 * @param id the id for the buffer returned by {@link #createRectBuff2f(BufferUsage)}
	 * and {@link #createRectBuff2d(BufferUsage)}
	 * @return true if successful, false otherwise
	 */
	public boolean destroyBuff(int id) {
		final GL2GL3 gl = getGL2GL3();
		BufferObject buffObj = findBufferById(id);
		gl.glDeleteVertexArrays(1, new int[] {buffObj.vao}, 0);
		// delete buffer from GL system
		gl.glDeleteBuffers(1, new int[] {buffObj.vbo}, 0);
		checkGLError("glDeleteBuffers");
		// delete buffer BufferObject from internal array store
		buffInfo = Utils.arrayDelete(buffInfo, new BufferObject[buffInfo.length - 1], findIndexOfBuffer(buffObj));
		return true;
	}

	/*
	 * appends the buffer index to 'buffIds' and returns the BufferObject for the newly created buffer
	 */
	private BufferObject initVBO2f(BufferUsage storeType, GeomFunc drawFunc, int nverts, 
			int nobjs, boolean textured) {
		final GL2GL3 gl = getGL2GL3();
		GLProgram currProg = GLProgram.getCurrentProgram();
		if(currProg == null)
			throw(new IllegalStateException("cannot initialize vertex buffer: no shader program in use"));
		final int vertCoordPos = currProg.getAttribLoc(GLShader.ATTRIB_VERT_COORD);
		final int vertColorPos = currProg.getAttribLoc(GLShader.ATTRIB_VERT_COLOR);
		final int texCoordPos = currProg.getAttribLoc(GLShader.ATTRIB_TEX_COORD);
		
		if(buffInfo == null) {
			buffInfo = new BufferObject[1];
		} else {
			buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
		}
		
		int[] vaoId = new int[1];
		gl.getGL2GL3().glGenVertexArrays(1, vaoId, 0);
		checkGLError("glGenVertexArrays");
		gl.getGL2GL3().glBindVertexArray(vaoId[0]);
		checkGLError("glBindVertexArray");
		int[] vboId = new int[1];
		gl.glGenBuffers(1, vboId, 0);
		checkGLError("glGenBuffers");
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
		checkGLError("glBindBuffer");
		gl.glEnableVertexAttribArray(vertCoordPos);
		checkGLError("glEnableVertexAttribArray");
		int buffSize;
		if(textured) {
			gl.glVertexAttribPointer(vertCoordPos, 2, GL.GL_FLOAT, false, 4 * Buffers.SIZEOF_FLOAT, 0);
			checkGLError("glVertexAttribPointer");
			gl.glEnableVertexAttribArray(texCoordPos);
			checkGLError("glEnableVertexAttribArray");
			gl.glVertexAttribPointer(texCoordPos, 2, GL.GL_FLOAT, false, 4 * Buffers.SIZEOF_FLOAT, 2 * Buffers.SIZEOF_FLOAT);
			checkGLError("glVertexAttribPointer");
			buffSize = 4 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		} else {
			gl.glVertexAttribPointer(vertCoordPos, 2, GL.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 0);
			checkGLError("glVertexAttribPointer");
			gl.glEnableVertexAttribArray(vertColorPos);
			checkGLError("glEnableVertexAttribArray");
			gl.glVertexAttribPointer(vertColorPos, 4, GL.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 2 * Buffers.SIZEOF_FLOAT);
			checkGLError("glVertexAttribPointer");
			buffSize = 6 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
		}

		gl.glBufferData(GL.GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);

		gl.glBindVertexArray(0);
		checkGLError("glBindVertexArray [unbind]");

		BufferObject buffObj = new BufferObject(vboId[0], vaoId[0], nverts, nobjs, Buffers.SIZEOF_FLOAT, buffSize, 
				textured, drawFunc, storeType);
		if(buffObj.storeHint != BufferUsage.STATIC_DRAW)
			buffObj.data = Buffers.newDirectFloatBuffer(buffSize / Buffers.SIZEOF_FLOAT);
		buffInfo[buffInfo.length - 1] = buffObj;
		return buffObj;
	}

	/*
	 * appends the buffer index to 'buffIds' and returns the BufferObject for the newly created buffer
	 */
	private BufferObject initVBO2d(BufferUsage storeType, GeomFunc drawFunc, int nverts, 
			int nobjs, boolean textured) {
		final GL2GL3 gl = getGL2GL3();
		GLProgram currProg = GLProgram.getCurrentProgram();
		if(currProg == null)
			throw(new IllegalStateException("cannot initialize vertex buffer: no shader program in use"));
		final int vertCoordPos = currProg.getAttribLoc(GLShader.ATTRIB_VERT_COORD);
		final int vertColorPos = currProg.getAttribLoc(GLShader.ATTRIB_VERT_COLOR);
		final int texCoordPos = currProg.getAttribLoc(GLShader.ATTRIB_TEX_COORD);
		
		if(buffInfo == null) {
			buffInfo = new BufferObject[1];
		} else {
			buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
		}
		
		int[] vaoId = new int[1];
		gl.getGL2GL3().glGenVertexArrays(1, vaoId, 0);
		checkGLError("glGenVertexArrays");
		gl.getGL2GL3().glBindVertexArray(vaoId[0]);
		checkGLError("glBindVertexArray");
		int[] vboId = new int[1];
		gl.glGenBuffers(1, vboId, 0);
		checkGLError("glGenBuffers");
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
		checkGLError("glBindBuffer");
		gl.glEnableVertexAttribArray(vertCoordPos);
		checkGLError("glEnableVertexAttribArray");
		int buffSize;
		if(textured) {
			gl.glVertexAttribPointer(vertCoordPos, 2, GL2.GL_DOUBLE, false, 4 * Buffers.SIZEOF_DOUBLE, 0);
			checkGLError("glVertexAttribPointer");
			gl.glEnableVertexAttribArray(texCoordPos);
			checkGLError("glEnableVertexAttribArray");
			gl.glVertexAttribPointer(texCoordPos, 2, GL2.GL_DOUBLE, false, 4 * Buffers.SIZEOF_DOUBLE, 2 * Buffers.SIZEOF_DOUBLE);
			checkGLError("glVertexAttribPointer");
			buffSize = 4 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		} else {
			gl.glVertexAttribPointer(vertCoordPos, 2, GL2.GL_DOUBLE, false, 6 * Buffers.SIZEOF_DOUBLE, 0);
			checkGLError("glVertexAttribPointer");
			gl.glEnableVertexAttribArray(vertColorPos);
			checkGLError("glEnableVertexAttribArray");
			gl.glVertexAttribPointer(vertColorPos, 4, GL2.GL_DOUBLE, false, 6 * Buffers.SIZEOF_DOUBLE, 2 * Buffers.SIZEOF_DOUBLE);
			checkGLError("glVertexAttribPointer");
			buffSize = 2 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
		}

		gl.glBufferData(GL.GL_ARRAY_BUFFER, buffSize, 
				null, storeType.usageHint);
		gl.glBindVertexArray(0);
		checkGLError("glBindVertexArray [unbind]");

		BufferObject buffObj = new BufferObject(vboId[0], vaoId[0], nverts, nobjs, Buffers.SIZEOF_DOUBLE, buffSize, 
				textured, drawFunc, storeType);
		if(buffObj.storeHint != BufferUsage.STATIC_DRAW)
			buffObj.data = Buffers.newDirectFloatBuffer(buffSize / Buffers.SIZEOF_DOUBLE);
		buffInfo[buffInfo.length - 1] = buffObj;
		return buffObj;
	}

	/*
	 * Find BufferObject by internal GL id.  You do not need to check this
	 * method's return value for null; it will fail with a GLException if the buffer
	 * is not found.
	 */
	private BufferObject findBufferById(int id) throws GLException {
		for(int i=0; i < buffInfo.length; i++) {
			if(id == buffInfo[i].vbo)
				return buffInfo[i];
		}
		throw(new GLException("failed to locate buffer - ID does not exist: " + id));
	}

	private int findIndexOfBuffer(BufferObject obj) {
		for(int i=0; i < buffInfo.length; i++) {
			if(obj == buffInfo[i])
				return i;
		}
		throw(new GLException("failed to locate index of buffer in internal array store"));
	}

	/*
	 * Holds data used for handling a Vertex Buffer and Vertex Array Objects (VAO/VBO)
	 */
	private class BufferObject {
		int[] vertIndices, vertNum;
		int vbo, vao, nobjs, objCount, size;
		boolean textured;
		GeomFunc drawFunc;
		BufferUsage storeHint;

		Buffer data;

		// constructor sets values and pre-computes arrays that are needed for glMultiDrawArrays function
		BufferObject(int vboId, int vaoId, int nverts, int nobjs, int typeSize, int size, boolean textured, 
				GeomFunc drawFunc, BufferUsage usage) {
			this.vbo = vboId; this.vao = vaoId; this.nobjs = nobjs;
			this.size = size;
			this.textured = textured;
			this.drawFunc = drawFunc;
			this.storeHint = usage;
			vertIndices = new int[nobjs];
			vertNum = new int[nobjs];
			for(int i=0; i < vertNum.length; i++)
				vertNum[i] = nverts;
			for(int i=0; i < vertIndices.length; i++)
				vertIndices[i] = size / typeSize / nobjs / ((textured) ? 4:6) * i;
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
		final GL2GL3 gl = getGL2GL3();
		int cprog = GLUtils.glGetInteger(gl, GL2.GL_CURRENT_PROGRAM);
		gl.glUseProgram(0);

		textRender.beginRendering(swt, sht);
		textRender.setSmoothing(config.getAsBool(Property.GL_RENDER_TEXT_SMOOTH));
		textRender.setUseVertexArrays(config.getAsBool(Property.GL_RENDER_TEXT_USE_VAO));
		textRender.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
		textRender.draw(text, x, y);
		textRender.endRendering();

		checkGLError("drawText/TextRenderer");

		gl.glUseProgram(cprog);
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

	public void setEnabled(GLFeature feature, boolean enable) {
		final GL gl = getGL();
		if(enable)
			gl.glEnable(feature.getGLCommand());
		else
			gl.glDisable(feature.getGLCommand());
	}

	/**
	 * Sets the alpha blending function.
	 * @param blendFunc
	 */
	public void setBlendFunc(AlphaFunc blendFunc) {
		final GL gl = getGL();
		switch(blendFunc) {
		case CLEAR:
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_ZERO);
			break;
		case SRC:
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
			break;
		case DST:
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_ONE);
			break;
		case SRC_OVER:
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_OVER:
			gl.glBlendFunc(GL.GL_ONE_MINUS_DST_ALPHA, GL.GL_ONE);
			break;
		case SRC_IN:
			gl.glBlendFunc(GL.GL_DST_ALPHA, GL.GL_ZERO);
			break;
		case DST_IN:
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_ALPHA);
			break;
		case SRC_OUT:
			gl.glBlendFunc(GL.GL_ONE_MINUS_DST_ALPHA, GL.GL_ZERO);
			break;
		case DST_OUT:
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case SRC_ATOP:
			gl.glBlendFunc(GL.GL_DST_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_ATOP:
			gl.glBlendFunc(GL.GL_ONE_MINUS_DST_ALPHA, GL.GL_SRC_ALPHA);
			break;
		case ALPHA_XOR:
			gl.glBlendFunc(GL.GL_ONE_MINUS_DST_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case SRC_DST:
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
			break;
		case SRC_BLEND:
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case DST_BLEND:
			gl.glBlendFunc(GL.GL_DST_ALPHA, GL.GL_ONE_MINUS_DST_ALPHA);
		}
	}

	public void dispose() {
		textRender.dispose();
		if(buffInfo != null) {
			for(BufferObject buffObj : buffInfo)
				destroyBuff(buffObj.vbo);
		}
	}
	
	/**
	 *
	 */
	@Override
	public boolean isGL3() {
		return true;
	}

	/**
	 *
	 */
	@Override
	public boolean isGL2() {
		return false;
	}

	/**
	 *
	 */
	@Override
	public GL3Handle asGL3() throws UnsupportedOperationException {
		return this;
	}

	/**
	 *
	 */
	@Override
	public GL2Handle asGL2() throws UnsupportedOperationException {
		throw(new UnsupportedOperationException("GL3Handle - not a GL2 implementation"));
	}

	protected int checkGLError(String pre) {
		final GL2GL3 gl = getGL2GL3();
		int errno = gl.glGetError();
		if(errno != GL.GL_NO_ERROR)
			log.warning(pre + ": " + mapGLErrorToString(errno) + " in " + Thread.currentThread().getStackTrace()[2]);
		return errno;
	}

	protected static String mapGLErrorToString(int errno) {
		switch(errno) {
		case GL.GL_NO_ERROR:
			return "No error";
		case GL.GL_INVALID_ENUM:
			return "Invalid enum";
		case GL.GL_INVALID_OPERATION:
			return "Invalid operation";
		case GL.GL_INVALID_FRAMEBUFFER_OPERATION:
			return "Invalid FBO operation";
		case GL.GL_OUT_OF_MEMORY:
			return "Out of memory!";
		default:
			return "unknown error";
		}
	}

	protected boolean isCompatMode() {
		return config.getAsBool(Property.GL_RENDER_COMPAT);
	}

	private GL2GL3 getGL2GL3() {
		return getGL().getGL2GL3();
	}

	private GL getGL() {
		return GLContext.getCurrentGL();
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
