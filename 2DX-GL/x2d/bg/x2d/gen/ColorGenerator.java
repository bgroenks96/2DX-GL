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

package bg.x2d.gen;

import java.awt.Color;

/**
 * Generates random colors using a custom configuration of constant and
 * randomized color channels.
 * 
 * @author Brian Groenke
 *
 */
public class ColorGenerator implements Generator <Color> {

    public static final int RED = 0x01, GREEN = 0x02, BLUE = 0x04, ALPHA = 0x08;

    int channels;

    private int r = 255, g = 255, b = 255, a = 255;

    private final NumberGenerator <Integer> numGen = new NumberGenerator <Integer>(0, 255);

    /**
     * Creates a ColorGenerator that will randomly generate the components
     * selected via <code>genChannels</code>
     * 
     * @param genChannels
     *            a bitmask OR of the desired color components
     * @see {@link #RED} {@link #GREEN} {@link #BLUE} {@link #ALPHA}
     */
    public ColorGenerator(final int genChannels) {

        this.channels = genChannels;
    }

    public void setDefaultRGBA(final int r, final int g, final int b, final int a) {

        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     *
     */
    @Override
    public Color generate() {

        int r, g, b, a;
        r = ( (RED & channels) > 0) ? numGen.generate() : this.r;
        g = ( (GREEN & channels) > 0) ? numGen.generate() : this.g;
        b = ( (BLUE & channels) > 0) ? numGen.generate() : this.b;
        a = ( (ALPHA & channels) > 0) ? numGen.generate() : this.a;
        return new Color(r, g, b, a);
    }

    public static ColorGenerator createRGB() {

        return new ColorGenerator(RED | GREEN | BLUE);
    }

    public static ColorGenerator createRGBA() {

        return new ColorGenerator(RED | GREEN | BLUE | ALPHA);
    }
}
