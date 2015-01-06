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

package com.snap2d.light;

import bg.x2d.geo.PointUD;

/**
 * @author Brian Groenke
 *
 */
public interface LightSource {

    public void setLocation(double x, double y);

    public void setColor(float[] color);

    public void setIntensity(float ifactor);

    public void setRadius(float radius);

    public void setEnabled(boolean enabled);

    public PointUD getLocation();

    public float[] getColor();

    public float getIntensity();

    public float getRadius();

    public boolean isEnabled();

}
