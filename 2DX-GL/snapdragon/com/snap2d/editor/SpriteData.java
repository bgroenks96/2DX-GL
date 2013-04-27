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

package com.snap2d.editor;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

/**
 * Represents image and collision model data for Entities or "sprites."
 * Can be saved and loaded by the editor, loaded by the engine for creating collision models.
 * @author Brian Groenke
 *
 */
public class SpriteData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7379057230930174395L;
	
	public static final String FILE_SUFFIX = "sdat";
	
	public String imgName;
	public Point[] vertices;
	
	/**
	 * Set by the static load method.  NOT saved with the data file.
	 */
	public transient BufferedImage loadedImage;
	
	public static SpriteData load(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
		SpriteData data = (SpriteData) objIn.readObject();
		objIn.close();
		file = new File(file.getParent() + File.separator + data.imgName);
		data.loadedImage = ImageIO.read(file);
		return data;
	}

}
