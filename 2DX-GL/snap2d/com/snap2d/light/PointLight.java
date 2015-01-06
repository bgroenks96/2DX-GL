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

import java.util.Arrays;

import bg.x2d.geo.PointUD;

/**
 * @author Brian Groenke
 *
 */
public class PointLight implements LightSource {

    PointUD loc = new PointUD(0, 0);
    float[] color = new float[3];
    float ifactor, radius;
    boolean enabled = true;

    public PointLight(final double x, final double y, final float[] color, final float intensity, final float radius) {

        setLocation(x, y);
        setColor(color);
        setIntensity(intensity);
        setRadius(radius);
    }

    /**
     *
     */
    @Override
    public void setLocation(final double x, final double y) {

        loc.setLocation(x, y);
    }

    /**
     *
     */
    @Override
    public void setColor(final float[] color) {

        System.arraycopy(color, 0, this.color, 0, this.color.length);
    }

    /**
     *
     */
    @Override
    public void setIntensity(final float ifactor) {

        this.ifactor = ifactor;
    }

    /**
     *
     */
    @Override
    public void setRadius(final float radius) {

        this.radius = radius;
    }

    /**
     *
     */
    @Override
    public void setEnabled(final boolean enabled) {

        this.enabled = enabled;
    }

    /**
     *
     */
    @Override
    public PointUD getLocation() {

        return new PointUD(loc);
    }

    /**
     *
     */
    @Override
    public float[] getColor() {

        return Arrays.copyOf(color, color.length);
    }

    /**
     *
     */
    @Override
    public float getIntensity() {

        return ifactor;
    }

    /**
     *
     */
    @Override
    public float getRadius() {

        return radius;
    }

    /**
     *
     */
    @Override
    public boolean isEnabled() {

        return enabled;
    }

}
