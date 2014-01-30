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

package com.snap2d;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.media.opengl.*;

import bg.x2d.*;
import bg.x2d.ImageUtils.ScaleQuality;
import bg.x2d.utils.*;

import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.snap2d.gl.opengl.*;

/**
 * Provides static utility methods for loading and scaling image resources.
 * 
 * @author Brian Groenke
 * 
 */
public class ImageLoader {
	
	/*
	 * File type wrappings for JOGL TextureIO constants.
	 */
	public static final String JPG = TextureIO.JPG, PNG = TextureIO.PNG, GIF = TextureIO.GIF,
			TIFF = TextureIO.TIFF, PAM = TextureIO.PAM, PPM = TextureIO.PPM, DDS = TextureIO.DDS;

	private ImageLoader() {
	}

	/**
	 * Loads a BufferedImage from the given InputStream. The caller is responsible for closing the
	 * stream.
	 * 
	 * @param stream
	 * @return the loaded BufferedImage, or null if an error occurred.
	 */
	public static BufferedImage load(InputStream stream) {
		if (stream == null) {
			return null;
		}
		BufferedImage img = null;
		try {
			img = ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	/**
	 * Loads a BufferedImage from the given URL.
	 * 
	 * @param location
	 * @return
	 */
	public static BufferedImage load(URL location) {
		if (location == null) {
			return null;
		}
		BufferedImage img = null;
		InputStream in = null;
		try {
			in = location.openStream();
			img = load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.closeStream(in);
		}

		return img;
	}

	/**
	 * Scales the given BufferedImage based on display size. The Dimensions <code>prevDisp</code>
	 * represents the screen dimensions <code>img</code> is sized for by default.
	 * 
	 * @param img
	 *            the BufferedImage to scale
	 * @param prevDisp
	 *            display size <code>img</code> is sized to be viewed on.
	 * @param quality
	 *            quality of the scaling operation
	 * @param aspectRatio
	 *            true if the image's aspect ratio should be maintained, false otherwise
	 * @return the scaled BufferedImage
	 */
	public static BufferedImage scaleFrom(BufferedImage img,
			Dimension prevDisp, ScaleQuality quality, boolean aspectRatio) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension newSize = new Dimension();
		if (aspectRatio) {
			if (img.getWidth() >= img.getHeight()) {
				double ratio = screen.getWidth() / prevDisp.getWidth();
				newSize.setSize(img.getWidth() * ratio, img.getHeight() * ratio);
			} else {
				double ratio = screen.getHeight() / prevDisp.getHeight();
				newSize.setSize(img.getWidth() * ratio, img.getHeight() * ratio);
			}
		} else {
			newSize.setSize(
					img.getWidth() * (screen.getWidth() / prevDisp.getWidth()),
					img.getHeight()
					* (screen.getHeight() / prevDisp.getHeight()));
		}
		return ImageUtils.scaleImage(img, newSize, img.getType(), quality);
	}

	/**
	 * Loads a Texture directly from the given URL
	 * @param url
	 * @param fileType
	 * @param mipmap
	 * @return
	 * @throws IOException
	 */
	public static Texture2D loadTexture(URL url, String fileType, boolean mipmap) throws IOException {
		Texture tex = null;
		try {
			tex = TextureIO.newTexture(url, mipmap, fileType);
			System.out.println(tex.getImageTexCoords() + " " + tex.getMustFlipVertically());
		} catch(GLException gl) {
			System.err.println("[Snap2D] OpenGL error in loading texture:");
			gl.printStackTrace();
		}
		return new Texture2D(tex);
	}
	
	public static Texture2D loadTexture(BufferedImage bimg, boolean mipmap) {
		Texture tex = null;
		try {
			tex = AWTTextureIO.newTexture(GLProfile.get(GLProfile.GL2), bimg, mipmap);
		} catch(GLException gl) {
			System.err.println("[Snap2D] OpenGL error in loading texture:");
			gl.printStackTrace();
		}
		return new Texture2D(tex);
	}
}
