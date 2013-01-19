/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.snap2d;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;

import bg.x2d.*;
import bg.x2d.ImageUtils.ScaleQuality;
import bg.x2d.utils.*;

/**
 * Provides static utility methods for loading and scaling image resources.
 * @author Brian Groenke
 *
 */
public class ImageLoader {

	private ImageLoader() {}

	/**
	 * Loads a BufferedImage from the given InputStream.  The caller is responsible
	 * for closing the stream.
	 * @param stream
	 * @return the loaded BufferedImage, or null if an error occurred.
	 */
	public static BufferedImage load(InputStream stream) {
		if(stream == null)
			return null;
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
	 * @param location
	 * @return
	 */
	public static BufferedImage load(URL location) {
		if(location == null)
			return null;
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

	public static BufferedImage scaleFrom(BufferedImage img, Dimension prevDisp, ScaleQuality quality, boolean aspectRatio) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension newSize = new Dimension();
		if(aspectRatio) {
			if(img.getWidth() >= img.getHeight()) {
				double ratio = screen.getWidth() / prevDisp.getWidth();
				newSize.setSize(img.getWidth() * ratio, img.getHeight() * ratio);
			} else {
				double ratio = screen.getHeight() / prevDisp.getHeight();
				newSize.setSize(img.getWidth() * ratio, img.getHeight() * ratio);
			}
		} else {
			newSize.setSize(img.getWidth() * (screen.getWidth() / prevDisp.getWidth()),
					img.getHeight() * (screen.getHeight() / prevDisp.getHeight()));
		}
		return ImageUtils.scaleImage(img, newSize, img.getType(), quality);
	}
}
