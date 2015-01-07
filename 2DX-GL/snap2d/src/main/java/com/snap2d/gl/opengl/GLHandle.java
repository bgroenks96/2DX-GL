/*
 *  Copyright (C) 2011-2014 Brian Groenke
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import bg.x2d.geo.PointUD;

/**
 * Main handle object for manipulating GL data and graphics. This class serves
 * as the primary mechanism for allocating, modifying, or drawing geometry and
 * text, as well as allowing configuration of underlying OpenGL functions. You
 * can obtain a GLHandle object from {@link #GLRenderControl} through a
 * {@link #GLRenderable}.
 * 
 * @author Brian Groenke
 *
 */
public interface GLHandle {

    public static final float[] DEFAULT_RECT_TEX_COORDS = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 },
            INVERTED_RECT_TEX_COORDS = new float[] { 0, 1, 0, 0, 1, 1, 1, 0 };
    public static final int FILTER_LINEAR = GL.GL_LINEAR, FILTER_NEAREST = GL.GL_NEAREST;

    /**
     * Sets the 2D coordinate viewport of the OpenGL context according to the
     * dimensions and units specified here.
     * 
     * @see {@link #setProgramTransform(GLProgram)}
     * @param x
     * @param y
     * @param width
     * @param height
     * @param ppu
     *            pixels-per-unit; for normal use, just use 1
     */
    public void setViewport(float x, float y, float width, float height, float ppu);

    /**
     * Set the size of the display in the current OpenGL context. This should be
     * the current size of the window or panel on which the context is being
     * rendered.
     * 
     * @param width
     * @param height
     */
    public void setDisplaySize(int width, int height);

    public float getViewportWidth();

    public float getViewportHeight();

    public float getPPU();

    public void setTextureEnabled(boolean enabled);

    public void bindTexture(Texture2D tex);

    /**
     * LINEAR or NEAREST texture filtering for texture down-scaling
     * 
     * @param filterType
     *            linear for best quality, nearest for best performance
     * @param mipmapType
     *            linear or nearest; use -1 for no mipmapping
     */
    public void setTextureMinFilter(int filterType, int mipmapType);

    public void setTextureMagFilter(int filterType);

    public void setColor3f(float r, float g, float b);

    public void setColor4f(float r, float g, float b, float a);

    public void setRotation(float theta);

    public void setRotationPoint(float x, float y);

    public void setTranslation(float x, float y);

    public void setScale(float sx, float sy);

    /**
     * Pushes the current transformations to the default/current shader
     * program's matrix. Rendering operations done after calling this method
     * will be transformed using the transformation settings at the time of the
     * call until this method is invoked again or {@link #clearTransform()} is
     * called to reset the transformation matrix.
     */
    public void pushTransform();

    /**
     * Resets all currently stored transformation values to the default
     * configuration (untransformed) and uploads the cleared transform to the
     * active program via {@link #pushTransform()}
     */
    public void clearTransform();

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
    public void setTexCoords(float... coords);

    /**
     * Sets the texture coordinates based on the four corners of the given
     * Texture2D.
     * 
     * @param rectCoords
     *            the texture to set texture coordinates from.
     */
    public void setRectTexCoords(Texture2D rectCoords);

    // ---- Data Store/Access and Drawing ---- //

    /**
     * Draws the buffer specified by 'buffId' to screen according to parameters
     * set using its associated buffer properties.
     * 
     * @param buffId
     *            the id of the vertex buffer to draw
     */
    public void draw2f(int buffId);

    public void draw2d(int buffId);

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
    public void putQuad2f(int rectBuffId, float x, float y, float wt, float ht, FloatBuffer colorBuffer);

    public void putQuad2d(int rectBuffId, double x, double y, double wt, double ht, FloatBuffer colorBuffer);

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
    public void putPoly2f(int polyBuffId, FloatBuffer colorBuffer, PointUD... points);

    public void putPoly2d(int rectBuffId, PointUD[] points, FloatBuffer colorBuffer);

    /**
     * Resets the object data for the given buffer to zero. Vertex data from
     * previous calls to {@link #putQuad2d(int, double, double, double, double)}
     * will be discarded and overwritten upon the next call. This method can be
     * used to reset a partially filled buffer after it has been drawn.
     * 
     * @param buffId
     */
    public void resetBuff(int buffId);

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
    public int createQuadBuffer2f(BufferUsage storeType, int nobjs, boolean textured);

    public int createQuadBuffer2d(BufferUsage storeType, int nobjs, boolean textured);

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
    public int createPolyBuffer2f(BufferUsage storeType, GeomFunc drawFunc, int verts, int nobjs, boolean textured);

    public int createPolyBuffer2d(BufferUsage storeType, GeomFunc drawFunc, int verts, int nobjs, boolean textured);

    /**
     * Deletes the VBO held with the given ID from memory.
     * 
     * @param id
     *            the id for the buffer returned by
     *            {@link #createRectBuff2f(BufferUsage)} and
     *            {@link #createRectBuff2d(BufferUsage)}
     * @return true if successful, false otherwise
     */
    public boolean destroyBuff(int id);

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
    public void drawText(String text, Color color, int x, int y);

    public void drawText(String text, int x, int y, float[] rgba);

    /**
     * Draws the given array of text strings at their respective screen
     * coordinates specified in IntBuffer 'coords' with their respective colors
     * in FloatBuffer colors
     * 
     * @param texts
     * @param coords
     * @param colors
     */
    public void drawTextBatch(String[] texts, IntBuffer coords, FloatBuffer colors);

    /**
     * Re-creates the internal TextRenderer with the given Font.
     * 
     * @param font
     */
    public void setFont(Font font);

    public void setEnabled(GLFeature feature, boolean enabled);

    public void setBlendFunc(AlphaFunc func);

    public void dispose();

    public boolean isGL3();

    public boolean isGL2();

    public GL3Handle asGL3() throws UnsupportedOperationException;

    public GL2Handle asGL2() throws UnsupportedOperationException;
}
