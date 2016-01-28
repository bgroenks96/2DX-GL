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

package com.snap2d.gl;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * @author Brian Groenke
 *
 */
public class ScreenUtils {

    public static final Dimension HI_RES_1080 = new Dimension(1920, 1080);

    /**
     * Get the screen resolution of the the primary display device in pixel
     * dimensions.
     * 
     * @return
     */
    public static final Dimension getScreenResolution() {

        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Get the pixels-per-inch aka "dots-per-inch" of the primary display
     * device.
     * 
     * @return number of dots per inch in screen space
     */
    public static final int getScreenDpi() {

        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static final Dimension convertDimension(final Dimension baseScreenSize,
                                                   final Dimension toConvert,
                                                   final boolean keepAspectRatio) {

        Dimension screenSize = getScreenResolution();
        return toConvert;
    }
}
