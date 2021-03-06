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

package com.snap2d.editor;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;

import javax.imageio.ImageIO;

import bg.x2d.utils.Utils;

/**
 * Represents image and collision model data for Entities or "sprites." Can be
 * saved and loaded by the editor, loaded by the engine for creating collision
 * models.
 * 
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
    public int wt, ht;

    /**
     * Set by the static load method. NOT saved with the data file.
     */
    public transient BufferedImage loadedImage;

    /**
     * 
     * @param loc
     *            the URL where the file can be found (local 'file:' or remote
     *            'http:/ftp:' directory)
     * @param fileName
     *            name of the file (no extension)
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static SpriteData load(final URL loc, final String fileName) throws IOException, ClassNotFoundException {

        ObjectInputStream objIn = new ObjectInputStream(new URL(loc + "/" + fileName).openStream());
        SpriteData data = (SpriteData) objIn.readObject();
        objIn.close();
        URL img = new URL(loc.toString() + "/" + data.imgName);
        if (Utils.urlExists(img)) {
            data.loadedImage = ImageIO.read(img);
        } else {
            System.err.println("failed to locate sprite image in local directory: " + data.imgName);
        }

        return data;
    }

}
