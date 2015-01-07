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

import java.awt.Color;
import java.awt.Font;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import bg.x2d.geo.PointUD;
import bg.x2d.utils.Utils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.snap2d.gl.opengl.GLConfig.Property;

/**
 * @author Brian Groenke
 *
 */
public class GL2Handle implements GLHandle {

    private static final Logger log = Logger.getLogger(GLHandle.class.getCanonicalName());

    protected GLConfig config;
    protected TextRenderer textRender;

    float vx, vy, vwt, vht;
    int swt, sht;
    float ppu;
    float[] texCoords = DEFAULT_RECT_TEX_COORDS;
    boolean texEnabled, texBound;

    int magFilter = FILTER_LINEAR, minFilter = FILTER_LINEAR;

    FloatBuffer defColorBuff;

    // transformation values
    float theta, rx, ry, tx, ty, sx = 1, sy = 1;

    GL2Handle(final GLConfig config) {

        this.config = config;
        this.textRender = new TextRenderer(new Font("Arial", Font.PLAIN, 12), true, true, null,
                config.getAsBool(Property.GL_RENDER_TEXT_MIPMAP));
        this.defColorBuff = Buffers.newDirectFloatBuffer(new float[] { 1, 1, 1, 1 });
        log.info("initialized GLHandle (gl2-compat)");
    }

    @Override
    public void setViewport(final float x, final float y, final float width, final float height, final float ppu) {

        if (ppu <= 0) {
            throw (new IllegalArgumentException("ppu must be > 0"));
        }
        vx = x;
        vy = y;
        vwt = width / ppu;
        vht = height / ppu;
        this.ppu = ppu;

        GL2 gl = getGL2();
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(x, x + vwt, y, y + vht, 0, 1);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        return;
    }

    @Override
    public void setDisplaySize(final int width, final int height) {

        this.swt = width;
        this.sht = height;
    }

    @Override
    public float getViewportWidth() {

        return vwt;
    }

    @Override
    public float getViewportHeight() {

        return vht;
    }

    @Override
    public float getPPU() {

        return ppu;
    }

    @Override
    public void setTextureEnabled(final boolean enabled) {

        final GL gl = getGL();
        if (enabled) {
            gl.glEnable(GL.GL_TEXTURE_2D);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
        texEnabled = enabled;
    }

    @Override
    public void bindTexture(final Texture2D tex) {

        final GL gl = getGL();
        if (!texEnabled) {
            tex.enable(gl);
            texEnabled = true;
        }

        tex.bind(gl);
        tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, minFilter);
        tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, magFilter);
        // gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,
        // GL2.GL_MODULATE);
        texBound = true;
    }

    /**
     * LINEAR or NEAREST texture filtering for texture down-scaling
     * 
     * @param filterType
     *            linear for best quality, nearest for best performance
     * @param mipmapType
     *            linear or nearest; use -1 for no mipmapping
     */
    @Override
    public void setTextureMinFilter(final int filterType, final int mipmapType) {

        if (filterType != FILTER_NEAREST && filterType != FILTER_LINEAR) {
            throw (new IllegalArgumentException(
                    "illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
        }
        if (mipmapType != FILTER_NEAREST && mipmapType != FILTER_LINEAR) {
            throw (new IllegalArgumentException(
                    "illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
        }
        if (mipmapType < 0) {
            minFilter = filterType;
        } else {
            switch (filterType) {
            case FILTER_LINEAR:
                if (mipmapType == FILTER_LINEAR) {
                    minFilter = GL.GL_LINEAR_MIPMAP_LINEAR;
                } else if (mipmapType == FILTER_NEAREST) {
                    minFilter = GL.GL_LINEAR_MIPMAP_NEAREST;
                }
                break;
            case FILTER_NEAREST:
                if (mipmapType == FILTER_LINEAR) {
                    minFilter = GL.GL_NEAREST_MIPMAP_LINEAR;
                } else if (mipmapType == FILTER_NEAREST) {
                    minFilter = GL.GL_NEAREST_MIPMAP_NEAREST;
                }

            }
        }
    }

    @Override
    public void setTextureMagFilter(final int filterType) {

        if (filterType != FILTER_NEAREST && filterType != FILTER_LINEAR) {
            throw (new IllegalArgumentException(
                    "illegal argument value for 'filterType': expected FILTER_NEAREST or FILTER_LINEAR"));
        }
        magFilter = filterType;
    }

    @Override
    public void setColor3f(final float r, final float g, final float b) {

        setColor4f(r, g, b, 1);
    }

    @Override
    public void setColor4f(final float r, final float g, final float b, final float a) {

        float[] colors = new float[] { r, g, b, a };
        defColorBuff.rewind();
        defColorBuff.put(colors);
        defColorBuff.flip();
    }

    @Override
    public void setRotation(final float theta) {

        this.theta = theta;
    }

    @Override
    public void setRotationPoint(final float x, final float y) {

        this.rx = x;
        this.ry = y;
    }

    @Override
    public void setTranslation(final float x, final float y) {

        this.tx = x;
        this.ty = y;
    }

    @Override
    public void setScale(final float sx, final float sy) {

        this.sx = sx;
        this.sy = sy;
    }

    /**
     * Pushes the current transformations to the default/current shader
     * program's matrix. Rendering operations done after calling this method
     * will be transformed using the transformation settings at the time of the
     * call until this method is invoked again or {@link #clearTransform()} is
     * called to reset the transformation matrix.
     */
    @Override
    public void pushTransform() {

        GL2 gl = getGL2();
        gl.glPushMatrix();
        gl.glTranslatef(tx, ty, 0);
        gl.glRotatef(theta, rx, ry, 0);
        gl.glScalef(sx, sy, 1);
    }

    /**
     * Resets all currently stored transformation values to the default
     * configuration (untransformed) and uploads the cleared transform to the
     * active program via {@link #pushTransform()}
     */
    @Override
    public void clearTransform() {

        this.theta = 0;
        this.sx = 1;
        this.sy = 1;
        this.tx = 0;
        this.ty = 0;
        this.rx = 0;
        this.ry = 0;
        pushTransform();
    }

    /**
     * Sets the coordinates for <code>glTexCoord2f</code> when drawing
     * texture-enabled geometry. Coordinates should be supplied in the glob or
     * array as alternating x,y values - e.g: x0, y0, x1, y1, etc... <br/>
     * <br/>
     * The default texture coordinate configuration before this method is called
     * is a regular quad texture: 0, 0, 0, 1, 1, 1, 1, 1 (bottom-left, top-left,
     * top-right, bottom-right)
     * 
     * @param coords
     *            the alternating x and y texture coordinates
     */
    @Override
    public void setTexCoords(final float... coords) {

        if (coords != null && coords.length > 2) {
            this.texCoords = coords;
        }
    }

    /**
     * Sets the texture coordinates based on the four corners of the given
     * Texture2D.
     * 
     * @param rectCoords
     *            the texture to set texture coordinates from.
     */
    @Override
    public void setRectTexCoords(final Texture2D rectCoords) {

        texCoords[0] = rectCoords.getLeftCoord();
        texCoords[1] = rectCoords.getBottomCoord();
        texCoords[2] = rectCoords.getLeftCoord();
        texCoords[3] = rectCoords.getTopCoord();
        texCoords[4] = rectCoords.getRightCoord();
        texCoords[5] = rectCoords.getBottomCoord();
        texCoords[6] = rectCoords.getRightCoord();
        texCoords[7] = rectCoords.getTopCoord();
    }

    // ---- Data Store/Access and Drawing ---- //

    // private int[] buffIds;
    private BufferObject[] buffInfo;

    private final float[] color = new float[4]; // array that holds color data
    // for buffer I/O

    /**
     * Draws the buffer specified by 'buffId' to screen according to parameters
     * set using its associated buffer properties.
     * 
     * @param buffId
     *            the id of the vertex buffer to draw
     */
    @Override
    public void draw2f(final int buffId) {

        final GL2 gl = getGL2();
        final BufferObject buffObj = findBufferById(buffId);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffObj.vbo);
        checkGLError("glBindBuffer");

        if (buffObj.data != null && buffObj.data.limit() > 0) {
            // System.out.println("vbo="+buffObj.vbo + " vao="+buffObj.vao +
            // " nverts="+buffObj.vertNum[0]+" nobjs="+buffObj.nobjs + " " +
            // buffObj.data);
            buffObj.data.flip();
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
            checkGLError("glBufferSubData");
        }
        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        checkGLError("glEnableClientState [compat]");
        if (buffObj.textured) {
            gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
            checkGLError("glEnableClientState [compat]");
            gl.glVertexPointer(2, GL.GL_FLOAT, 4 * Buffers.SIZEOF_FLOAT, 0);
            checkGLError("glVertexPointer [compat]");
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 4 * Buffers.SIZEOF_FLOAT, 2 * Buffers.SIZEOF_FLOAT);
            checkGLError("glTexCoordPointer [compat]");
        } else {
            gl.glVertexPointer(2, GL.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 0);
            checkGLError("glVertexPointer [compat]");
            gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
            checkGLError("glEnableClientState [compat]");
            gl.glColorPointer(4, GL.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 2 * Buffers.SIZEOF_FLOAT);
            checkGLError("glColorPointer [cmopat]");
        }

        gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0,
                buffObj.objCount);
        checkGLError("glMultiDrawArrays");

        // disable arrays
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        if (buffObj.textured) {
            gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
        } else {
            gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
        }
    }

    @Override
    public void draw2d(final int buffId) {

        final GL2 gl = getGL2();
        final BufferObject buffObj = findBufferById(buffId);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffObj.vbo);
        checkGLError("glBindBuffer");

        if (buffObj.data != null && buffObj.data.limit() > 0) {
            // System.out.println("vbo="+buffObj.vbo + " vao="+buffObj.vao +
            // " nverts="+buffObj.vertNum[0]+" nobjs="+buffObj.nobjs + " " +
            // buffObj.data);
            buffObj.data.flip();
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, buffObj.size / buffObj.nobjs * buffObj.objCount, buffObj.data);
            checkGLError("glBufferSubData");
        }
        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        checkGLError("glEnableClientState [compat]");
        if (buffObj.textured) {
            gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
            checkGLError("glEnableClientState [compat]");
            gl.glVertexPointer(2, GL2GL3.GL_DOUBLE, 4 * Buffers.SIZEOF_DOUBLE, 0);
            checkGLError("glVertexPointer [compat]");
            gl.glTexCoordPointer(2, GL2GL3.GL_DOUBLE, 4 * Buffers.SIZEOF_DOUBLE, 2 * Buffers.SIZEOF_DOUBLE);
            checkGLError("glTexCoordPointer [compat]");
        } else {
            gl.glVertexPointer(2, GL2GL3.GL_DOUBLE, 6 * Buffers.SIZEOF_DOUBLE, 0);
            checkGLError("glVertexPointer [compat]");
            gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
            checkGLError("glEnableClientState [compat]");
            gl.glColorPointer(4, GL2GL3.GL_DOUBLE, 6 * Buffers.SIZEOF_DOUBLE, 2 * Buffers.SIZEOF_DOUBLE);
            checkGLError("glColorPointer [compat]");
        }

        gl.glMultiDrawArrays(buffObj.drawFunc.getGLCommand(), buffObj.vertIndices, 0, buffObj.vertNum, 0,
                buffObj.objCount);
        checkGLError("glMultiDrawArrays");

        // disable arrays
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        if (buffObj.textured) {
            gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
        } else {
            gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
        }
    }

    /**
     * Writes coordinate data for a given quad to the vertex buffer. Each
     * successive call to this method writes an additional object into the
     * buffer until N objects have been written, where N is the set number of
     * objects for this buffer, or until {@link #resetBuff(int)} is called. Any
     * calls made to {@link #draw2f(int)} will draw whatever number of objects
     * are available in the buffer. Once a buffer's object limit is reached or
     * it is reset, subsequent calls to this method will cause all of its vertex
     * data to be discarded and restart the process.<br/>
     * <br/>
     * <b>Note: DO NOT rely on {@link #resetBuff(int)} or writing N+1 objects to
     * wipe existing data from VRAM before {@link #draw2f(int)} is called.</b>
     * This likely WILL NOT happen, and the previously uploaded vertex data will
     * still be at least partially rendered. The only reliable way to do this is
     * by destroying and re-creating the buffer.
     * 
     * @param rectBuffId
     *            the id for the quad's vertex buffer
     * @param x
     * @param y
     * @param wt
     * @param ht
     * @param colorBuffer
     *            a FloatBuffer containing color data in strides of 4 for all of
     *            the vertices - if the color buffer contains fewer colors than
     *            there are vertices, the last color in the buffer will be
     *            reused. You may pass null in this argument to use the current
     *            default color set via
     *            {@link #setColor4f(float, float, float, float)}
     */
    @Override
    public void putQuad2f(final int rectBuffId, final float x, final float y, final float wt, final float ht,
            FloatBuffer colorBuffer) {

        if (buffInfo == null || buffInfo.length == 0) {
            throw (new GLException("can't write quad data: no allocated buffers"));
        }

        final GL2GL3 gl = getGL2GL3();

        if (colorBuffer == null || colorBuffer.limit() < 4) {
            defColorBuff.rewind();
            colorBuffer = defColorBuff;
        }

        BufferObject buffObj = findBufferById(rectBuffId);
        int buffSize = buffObj.size;
        if (buffObj.objCount >= buffObj.nobjs) {
            // number of objects, reset count
            buffObj.objCount = 0;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);
        checkGLError("glBindBuffer");

        if (buffObj.storeHint == BufferUsage.STATIC_DRAW) {
            ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            checkGLError("glMapBuffer");
            FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
            floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
            // FloatBuffer floatBuff = Buffers.newDirectFloatBuffer(buffSize /
            // Buffers.SIZEOF_FLOAT / buffObj.nobjs);

            floatBuff.put(x);
            floatBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[0]);
                floatBuff.put(texCoords[1]);
            }

            floatBuff.put(x);
            floatBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[2]);
                floatBuff.put(texCoords[3]);
            }

            floatBuff.put(x + wt);
            floatBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[4]);
                floatBuff.put(texCoords[5]);
            }

            floatBuff.put(x + wt);
            floatBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[6]);
                floatBuff.put(texCoords[7]);
            }

            // int buffpos = buffSize / buffObj.nobjs * buffObj.objCount;
            // gl.glBufferSubData(GL.GL_ARRAY_BUFFER, buffpos, buffSize -
            // buffpos, floatBuff);

            buffObj.objCount++ ; // increment object count
            boolean chkMapBuff = gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
            if (!chkMapBuff) {
                log.warning("putQuad2f: glUnmapBuffer returned false");
            }
            checkGLError("glUnmapBuffer");
        } else {
            FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
            floatBuff.limit(floatBuff.capacity());
            floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);

            floatBuff.put(x);
            floatBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[0]);
                floatBuff.put(texCoords[1]);
            }

            floatBuff.put(x);
            floatBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[2]);
                floatBuff.put(texCoords[3]);
            }

            floatBuff.put(x + wt);
            floatBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[4]);
                floatBuff.put(texCoords[5]);
            }

            floatBuff.put(x + wt);
            floatBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                floatBuff.put(color);
            } else {
                floatBuff.put(texCoords[6]);
                floatBuff.put(texCoords[7]);
            }

            buffObj.objCount++ ; // increment object count
        }
    }

    @Override
    public void putQuad2d(final int rectBuffId, final double x, final double y, final double wt, final double ht,
            FloatBuffer colorBuffer) {

        if (buffInfo == null || buffInfo.length == 0) {
            throw (new GLException("can't write quad data: no allocated buffers"));
        }

        final GL2GL3 gl = getGL2GL3();

        if (colorBuffer == null || colorBuffer.limit() < 4) {
            defColorBuff.rewind();
            colorBuffer = defColorBuff;
        }

        BufferObject buffObj = findBufferById(rectBuffId);
        int buffSize = buffObj.size;
        if (buffObj.objCount >= buffObj.nobjs) {
            // number of objects, reset count
            buffObj.objCount = 0;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);

        final double[] dcolor = new double[4];
        if (buffObj.storeHint == BufferUsage.STATIC_DRAW) {
            ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
            doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);

            doubleBuff.put(x);
            doubleBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[0]);
                doubleBuff.put(texCoords[1]);
            }

            doubleBuff.put(x);
            doubleBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[2]);
                doubleBuff.put(texCoords[3]);
            }

            doubleBuff.put(x + wt);
            doubleBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[4]);
                doubleBuff.put(texCoords[5]);
            }

            doubleBuff.put(x + wt);
            doubleBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[6]);
                doubleBuff.put(texCoords[7]);
            }

            /*
             * if(buffObj.textured) floatBuff.put(texCoords); else { for(int
             * i=0; i < buffObj.nverts; i++) { int readLen =
             * Math.min(colorBuffer.limit() - colorBuffer.position(),
             * color.length); colorBuffer.get(color, 0, readLen);
             * floatBuff.put(color); } }
             */
            buffObj.objCount++ ; // increment object count
            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
        } else {
            DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
            doubleBuff.limit(doubleBuff.capacity());
            doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);

            doubleBuff.put(x);
            doubleBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[0]);
                doubleBuff.put(texCoords[1]);
            }

            doubleBuff.put(x);
            doubleBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[2]);
                doubleBuff.put(texCoords[3]);
            }

            doubleBuff.put(x + wt);
            doubleBuff.put(y);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[4]);
                doubleBuff.put(texCoords[5]);
            }

            doubleBuff.put(x + wt);
            doubleBuff.put(y + ht);
            if (!buffObj.textured) {
                GLUtils.readAvailable(colorBuffer, color);
                doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
            } else {
                doubleBuff.put(texCoords[6]);
                doubleBuff.put(texCoords[7]);
            }

            buffObj.objCount++ ; // increment object count
        }
    }

    /**
     * Writes coordinate data for a given poly to the vertex buffer. Each
     * successive call to this method writes an additional object into the
     * buffer until N objects have been written, where N is the set number of
     * objects for this buffer, or until {@link #resetBuff(int)} is called. Any
     * calls made to {@link #draw2f(int)} will draw whatever number of objects
     * are available in the buffer. Once a buffer's object limit is reached or
     * it is reset, subsequent calls to this method will cause all of its vertex
     * data to be discarded and restart the process.<br/>
     * <br/>
     * <b>Note: DO NOT rely on {@link #resetBuff(int)} or writing N+1 objects to
     * wipe existing data from VRAM before {@link #draw2f(int)} is called.</b>
     * This likely WILL NOT happen, and the previously uploaded vertex data will
     * still be at least partially rendered. The only reliable way to do this is
     * by destroying and re-creating the buffer.
     * 
     * @param polyBuffId
     *            the id for the polygon's vertex buffer
     * @param colorBuffer
     *            a FloatBuffer containing color data in strides of 4 for all of
     *            the vertices - if the color buffer contains fewer colors than
     *            there are vertices, the last color in the buffer will be
     *            reused. You may pass null in this argument to use the current
     *            default color set via
     *            {@link #setColor4f(float, float, float, float)}
     * @param points
     *            the vertices of the polygon in world-space as a varargs
     */
    @Override
    public void putPoly2f(final int polyBuffId, FloatBuffer colorBuffer, final PointUD... points) {

        if (buffInfo == null || buffInfo.length == 0) {
            throw (new GLException("can't write poly data: no allocated buffers"));
        }

        final GL2GL3 gl = getGL2GL3();

        if (colorBuffer == null || colorBuffer.limit() < 4) {
            defColorBuff.rewind();
            colorBuffer = defColorBuff;
        }

        BufferObject buffObj = findBufferById(polyBuffId);
        int buffSize = buffObj.size;
        if (buffObj.objCount >= buffObj.nobjs) {
            // number of objects, reset count
            buffObj.objCount = 0;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, polyBuffId);

        if (buffObj.storeHint == BufferUsage.STATIC_DRAW) {
            ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            FloatBuffer floatBuff = buff.order(ByteOrder.nativeOrder()).asFloatBuffer();
            floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
            for (int i = 0, t = 0; i < points.length; i++ , t += 2) {
                PointUD pt = points[i];
                floatBuff.put(pt.getFloatX());
                floatBuff.put(pt.getFloatY());
                if (buffObj.textured) {
                    floatBuff.put(texCoords[t]);
                    floatBuff.put(texCoords[t + 1]);
                } else {
                    GLUtils.readAvailable(colorBuffer, color);
                    floatBuff.put(color);
                }
            }
            buffObj.objCount++ ; // increment object count
            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
        } else {
            FloatBuffer floatBuff = (FloatBuffer) buffObj.data;
            // if(buffObj.objCount != 0)
            // gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize /
            // buffObj.nobjs * buffObj.objCount, floatBuff);
            floatBuff.limit(floatBuff.capacity());
            floatBuff.position(buffSize / Buffers.SIZEOF_FLOAT / buffObj.nobjs * buffObj.objCount);
            for (int i = 0, t = 0; i < points.length; i++ , t += 2) {
                PointUD pt = points[i];
                floatBuff.put(pt.getFloatX());
                floatBuff.put(pt.getFloatY());
                if (buffObj.textured) {
                    floatBuff.put(texCoords[t]);
                    floatBuff.put(texCoords[t + 1]);
                } else {
                    GLUtils.readAvailable(colorBuffer, color);
                    floatBuff.put(color);
                }
            }
            floatBuff.flip();
            colorBuffer.flip();
            buffObj.objCount++ ; // increment object count
        }
    }

    @Override
    public void putPoly2d(final int rectBuffId, final PointUD[] points, FloatBuffer colorBuffer) {

        if (buffInfo == null || buffInfo.length == 0) {
            throw (new GLException("can't write poly data: no allocated buffers"));
        }

        final GL2GL3 gl = getGL2GL3();

        if (colorBuffer == null || colorBuffer.limit() < 4) {
            defColorBuff.rewind();
            colorBuffer = defColorBuff;
        }

        BufferObject buffObj = findBufferById(rectBuffId);
        int buffSize = buffObj.size;
        if (buffObj.objCount >= buffObj.nobjs) {
            // number of objects, reset count
            buffObj.objCount = 0;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, rectBuffId);

        final double[] dcolor = new double[4];
        if (buffObj.storeHint == BufferUsage.STATIC_DRAW) {
            ByteBuffer buff = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            DoubleBuffer doubleBuff = buff.order(ByteOrder.nativeOrder()).asDoubleBuffer();
            doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
            for (int i = 0, t = 0; i < points.length; i++ , t += 2) {
                PointUD pt = points[i];
                doubleBuff.put(pt.getFloatX());
                doubleBuff.put(pt.getFloatY());
                if (buffObj.textured) {
                    doubleBuff.put(texCoords[t]);
                    doubleBuff.put(texCoords[t + 1]);
                } else {
                    GLUtils.readAvailable(colorBuffer, color);
                    doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
                }
            }
            buffObj.objCount++ ; // increment object count
            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
        } else {
            DoubleBuffer doubleBuff = (DoubleBuffer) buffObj.data;
            // if(buffObj.objCount != 0)
            // gl.glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffSize /
            // buffObj.nobjs * buffObj.objCount, floatBuff);
            doubleBuff.limit(doubleBuff.capacity());
            doubleBuff.position(buffSize / Buffers.SIZEOF_DOUBLE / buffObj.nobjs * buffObj.objCount);
            for (int i = 0, t = 0; i < points.length; i++ , t += 2) {
                PointUD pt = points[i];
                doubleBuff.put(pt.getFloatX());
                doubleBuff.put(pt.getFloatY());
                if (buffObj.textured) {
                    doubleBuff.put(texCoords[t]);
                    doubleBuff.put(texCoords[t + 1]);
                } else {
                    GLUtils.readAvailable(colorBuffer, color);
                    doubleBuff.put(Buffers.getDoubleArray(color, 0, dcolor, 0, color.length));
                }
            }
            doubleBuff.flip();
            colorBuffer.flip();
            buffObj.objCount++ ; // increment object count
        }
    }

    /**
     * Resets the object data for the given buffer to zero. Vertex data from
     * previous calls to {@link #putQuad2d(int, double, double, double, double)}
     * will be discarded and overwritten upon the next call. This method can be
     * used to reset a partially filled buffer after it has been drawn.
     * 
     * @param buffId
     */
    @Override
    public void resetBuff(final int buffId) {

        findBufferById(buffId).objCount = 0;
    }

    /**
     * Allocates a vertex buffer for rendering a given number of quads. The
     * returned id must be stored and used in order write and draw the quad
     * data.
     * 
     * @param storeType
     *            a hint for how the graphics driver should treat the buffer
     *            data in VRAM
     * @param nobjs
     *            max number of quads in this buffer
     * @param textured
     *            true if this quad will be textured (so tex coords are stored),
     *            false otherwise
     * @return the id for the quad buffer
     */
    @Override
    public int createQuadBuffer2f(final BufferUsage storeType, final int nobjs, final boolean textured) {

        BufferObject buffObj = initVBO2f(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
        return buffObj.vbo;
    }

    @Override
    public int createQuadBuffer2d(final BufferUsage storeType, final int nobjs, final boolean textured) {

        BufferObject buffObj = initVBO2d(storeType, GeomFunc.TRIANGLE_STRIP, 4, nobjs, textured);
        return buffObj.vbo;
    }

    /**
     * Allocates a vertex buffer for rendering a given number of polygons with a
     * given number of vertices. Ther returned id must be stored and used in
     * order to write and draw the polygon data.
     * 
     * @param storeType
     *            a hint for how the graphics driver should treat the buffer
     *            data in VRAM
     * @param drawFunc
     *            the GL function that should be used for drawing the polygon
     *            vertices
     * @param verts
     *            number of vertices per polygon
     * @param nobjs
     *            number of polygons
     * @param textured
     *            true if this polygon will be textured (1 tex coord per
     *            vertex), false otherwise
     * @return the id for the poly buffer
     */
    @Override
    public int createPolyBuffer2f(final BufferUsage storeType, final GeomFunc drawFunc, final int verts,
            final int nobjs, final boolean textured) {

        BufferObject buffObj = initVBO2f(storeType, drawFunc, verts, nobjs, textured);
        return buffObj.vbo;
    }

    @Override
    public int createPolyBuffer2d(final BufferUsage storeType, final GeomFunc drawFunc, final int verts,
            final int nobjs, final boolean textured) {

        BufferObject buffObj = initVBO2d(storeType, drawFunc, verts, nobjs, textured);
        return buffObj.vbo;
    }

    /**
     * Deletes the VBO held with the given ID from memory.
     * 
     * @param id
     *            the id for the buffer returned by
     *            {@link #createRectBuff2f(BufferUsage)} and
     *            {@link #createRectBuff2d(BufferUsage)}
     * @return true if successful, false otherwise
     */
    @Override
    public boolean destroyBuff(final int id) {

        final GL2GL3 gl = getGL2GL3();
        BufferObject buffObj = findBufferById(id);
        // delete buffer from GL system
        gl.glDeleteBuffers(1, new int[] { buffObj.vbo }, 0);
        checkGLError("glDeleteBuffers");
        // delete buffer BufferObject from internal array store
        buffInfo = Utils.arrayDelete(buffInfo, new BufferObject[buffInfo.length - 1], findIndexOfBuffer(buffObj));
        return true;
    }

    /*
     * appends the buffer index to 'buffIds' and returns the BufferObject for
     * the newly created buffer
     */
    private BufferObject initVBO2f(final BufferUsage storeType, final GeomFunc drawFunc, final int nverts,
            final int nobjs, final boolean textured) {

        final GL2GL3 gl = getGL2GL3();
        if (buffInfo == null) {
            buffInfo = new BufferObject[1];
        } else {
            buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
        }
        int[] vboId = new int[1];
        gl.glGenBuffers(1, vboId, 0);
        checkGLError("glGenBuffers");
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
        checkGLError("glBindBuffer");
        int buffSize;
        if (textured) {
            buffSize = 4 * 3 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
        } else {
            buffSize = 2 * 3 * nobjs * nverts * Buffers.SIZEOF_FLOAT;
        }

        gl.glBufferData(GL.GL_ARRAY_BUFFER, buffSize, null, storeType.usageHint);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        checkGLError("glBindBuffer [unbind]");

        BufferObject buffObj = new BufferObject(vboId[0], nverts, nobjs, Buffers.SIZEOF_FLOAT, buffSize, textured,
                drawFunc, storeType);
        if (buffObj.storeHint != BufferUsage.STATIC_DRAW) {
            buffObj.data = Buffers.newDirectFloatBuffer(buffSize / Buffers.SIZEOF_FLOAT);
        }
        buffInfo[buffInfo.length - 1] = buffObj;
        return buffObj;
    }

    /*
     * appends the buffer index to 'buffIds' and returns the BufferObject for
     * the newly created buffer
     */
    private BufferObject initVBO2d(final BufferUsage storeType, final GeomFunc drawFunc, final int nverts,
            final int nobjs, final boolean textured) {

        final GL2GL3 gl = getGL2GL3();
        if (buffInfo == null) {
            buffInfo = new BufferObject[1];
        } else {
            buffInfo = Arrays.copyOf(buffInfo, buffInfo.length + 1);
        }
        int[] vboId = new int[1];
        gl.glGenBuffers(1, vboId, 0);
        checkGLError("glGenBuffers");
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
        checkGLError("glBindBuffer");
        int buffSize;
        if (textured) {
            buffSize = 4 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
        } else {
            buffSize = 2 * 3 * nobjs * nverts * Buffers.SIZEOF_DOUBLE;
        }

        gl.glBufferData(GL.GL_ARRAY_BUFFER, buffSize, null, storeType.usageHint);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        checkGLError("glBindBuffer [unbind]");

        BufferObject buffObj = new BufferObject(vboId[0], nverts, nobjs, Buffers.SIZEOF_DOUBLE, buffSize, textured,
                drawFunc, storeType);
        if (buffObj.storeHint != BufferUsage.STATIC_DRAW) {
            buffObj.data = Buffers.newDirectFloatBuffer(buffSize / Buffers.SIZEOF_DOUBLE);
        }
        buffInfo[buffInfo.length - 1] = buffObj;
        return buffObj;
    }

    /*
     * Find BufferObject by internal GL id. You do not need to check this
     * method's return value for null; it will fail with a GLException if the
     * buffer is not found.
     */
    private BufferObject findBufferById(final int id) throws GLException {

        for (int i = 0; i < buffInfo.length; i++ ) {
            if (id == buffInfo[i].vbo) {
                return buffInfo[i];
            }
        }
        throw (new GLException("failed to locate buffer - ID does not exist: " + id));
    }

    private int findIndexOfBuffer(final BufferObject obj) {

        for (int i = 0; i < buffInfo.length; i++ ) {
            if (obj == buffInfo[i]) {
                return i;
            }
        }
        throw (new GLException("failed to locate index of buffer in internal array store"));
    }

    /*
     * Holds data used for handling a Vertex Buffer and Vertex Array Objects
     * (VAO/VBO)
     */
    private class BufferObject {

        int[] vertIndices, vertNum;
        int vbo, nobjs, objCount, size;
        boolean textured;
        GeomFunc drawFunc;
        BufferUsage storeHint;

        Buffer data;

        // constructor sets values and pre-computes arrays that are needed for
        // glMultiDrawArrays function
        BufferObject(final int vboId, final int nverts, final int nobjs, final int typeSize, final int size,
                final boolean textured, final GeomFunc drawFunc, final BufferUsage usage) {

            this.vbo = vboId;
            this.nobjs = nobjs;
            this.size = size;
            this.textured = textured;
            this.drawFunc = drawFunc;
            this.storeHint = usage;
            vertIndices = new int[nobjs];
            vertNum = new int[nobjs];
            for (int i = 0; i < vertNum.length; i++ ) {
                vertNum[i] = nverts;
            }
            for (int i = 0; i < vertIndices.length; i++ ) {
                vertIndices[i] = size / typeSize / nobjs / ( (textured) ? 4 : 6) * i;
            }
        }
    }

    /**
     * Draws text to the given screen location. If the default shader program is
     * currently enabled, it will be disabled before text is rendered. If a
     * custom shader is enabled, it's up to the caller whether or not it should
     * be disabled.
     * 
     * @param text
     * @param color
     * @param x
     * @param y
     */
    @Override
    public void drawText(final String text, final Color color, final int x, final int y) {

        drawText(text, x, y, color.getComponents(new float[4]));
    }

    @Override
    public void drawText(final String text, final int x, final int y, final float[] rgba) {

        final GL2GL3 gl = getGL2GL3();
        int cprog = GLUtils.glGetInteger(gl, GL2ES2.GL_CURRENT_PROGRAM);
        gl.glUseProgram(0);

        textRender.beginRendering(swt, sht);
        textRender.setSmoothing(config.getAsBool(Property.GL_RENDER_TEXT_SMOOTH));
        textRender.setUseVertexArrays(false);
        textRender.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        textRender.draw(text, x, y);
        textRender.endRendering();

        checkGLError("drawText/TextRenderer");

        gl.glUseProgram(cprog);
    }

    /**
     * Draws the given array of text strings at their respective screen
     * coordinates specified in IntBuffer 'coords' with their respective colors
     * in FloatBuffer colors
     * 
     * @param texts
     * @param coords
     * @param colors
     */
    @Override
    public void drawTextBatch(final String[] texts, final IntBuffer coords, final FloatBuffer colors) {

        float[] color = new float[4];
        if (colors.limit() < 4) {
            throw (new IllegalArgumentException("color buffer must have at least 4 values"));
        }
        for (String s : texts) {
            int x = coords.get();
            int y = coords.get();
            if (colors.position() < colors.limit()) {
                colors.get(color, 0, Math.min(colors.remaining(), color.length));
            }
            drawText(s, x, y, color);
        }
    }

    /**
     * Re-creates the internal TextRenderer with the given Font.
     * 
     * @param font
     */
    @Override
    public void setFont(final Font font) {

        textRender.dispose();
        textRender = new TextRenderer(font, true, true, null, config.getAsBool(Property.GL_RENDER_TEXT_MIPMAP));
    }

    // -------- OpenGL Feature Control --------- //

    @Override
    public void setEnabled(final GLFeature feature, final boolean enable) {

        final GL gl = getGL();
        if (enable) {
            gl.glEnable(feature.getGLCommand());
        } else {
            gl.glDisable(feature.getGLCommand());
        }
    }

    /**
     * Sets the alpha blending function.
     * 
     * @param blendFunc
     */
    @Override
    public void setBlendFunc(final AlphaFunc blendFunc) {

        final GL gl = getGL();
        switch (blendFunc) {
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

    @Override
    public void dispose() {

        textRender.dispose();
        if (buffInfo != null) {
            for (BufferObject buffObj : buffInfo) {
                destroyBuff(buffObj.vbo);
            }
        }
    }

    /**
     *
     */
    @Override
    public boolean isGL3() {

        return false;
    }

    /**
     *
     */
    @Override
    public boolean isGL2() {

        return true;
    }

    /**
     *
     */
    @Override
    public GL3Handle asGL3() throws UnsupportedOperationException {

        throw (new UnsupportedOperationException("GL2Handle - not a GL3 implementation"));
    }

    /**
     *
     */
    @Override
    public GL2Handle asGL2() throws UnsupportedOperationException {

        return this;
    }

    protected int checkGLError(final String pre) {

        final GL2GL3 gl = getGL2GL3();
        int errno = gl.glGetError();
        if (errno != GL.GL_NO_ERROR) {
            log.warning(pre + ": " + mapGLErrorToString(errno) + " in " + Thread.currentThread().getStackTrace()[2]);
        }
        return errno;
    }

    protected static String mapGLErrorToString(final int errno) {

        switch (errno) {
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

    private GL2GL3 getGL2GL3() {

        return getGL().getGL2GL3();
    }

    private GL2 getGL2() {

        return getGL().getGL2();
    }

    private GL getGL() {

        return GLContext.getCurrentGL();
    }
}
