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

package com.snap2d.gl.spi;

/**
 * @author Brian Groenke
 *
 */
public interface RenderController {

    public static final int DEFAULT_TARGET_FPS = 60, POSITION_LAST = 0x07FFFFFFF;

    public void startRenderLoop();

    public void stopRenderLoop() throws InterruptedException;

    public void setRenderActive(boolean active);

    public boolean isRenderActive();

    public void setMaxUpdates(int maxUpdates);

    public void setTargetFPS(int targetFPS);

    public void setTargetTPS(int targetTPS);

    public int getCurrentFPS();

    public int getCurrentTPS();

    public void setDisableUpdates(boolean disableUpdates);

    public boolean isUpdating();
}
