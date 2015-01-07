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

package com.snap2d.world.tile;

import com.snap2d.world.World2D;

/**
 * @author Brian Groenke
 *
 */
public class TiledWorld2D extends World2D {

    /**
     * @param minX
     * @param maxY
     * @param viewWidth
     * @param viewHeight
     * @param ppu
     */
    public TiledWorld2D(final double minX, final double maxY, final int viewWidth, final int viewHeight, final float ppu) {

        super(minX, maxY, viewWidth, viewHeight, ppu);
    }

}
