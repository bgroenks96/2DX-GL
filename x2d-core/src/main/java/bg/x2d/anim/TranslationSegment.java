/*
 * Copyright © 2011-2013 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.anim;

import java.awt.geom.AffineTransform;

/**
 * Segment that translates the AffineTransform the specified amounts on the x
 * and y axes for the specified amount of time.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class TranslationSegment implements Segment {

    private final long duration;
    private long start = -1;
    private long last;
    private final int hyp;
    private final double dxint, dyint, hyint;

    /**
     * Creates a new TranslationSegment that translates <code>deltax</code>
     * accross the x axis and <code>deltay</code> on the y axis.
     * 
     * @param deltax
     *            amount to translate on the x axis
     * @param deltay
     *            amount to translate on the y axis
     * @param duration
     *            total time duration of the Segment
     */
    public TranslationSegment(final int deltax, final int deltay, final long duration) {

        hyp = (int) Math.round(Math.sqrt( (deltax * deltax) + (deltay * deltay)));
        hyint = (double) hyp / duration;
        dxint = (double) deltax / duration;
        dyint = (double) deltay / duration;

        this.duration = duration;
    }

    @Override
    public long getDuration() {

        return duration;
    }

    /**
     * This implementation returns the magnitude of the vector formed by delta x
     * and delta y (aka the hypotenuse).
     */
    @Override
    public double getUpdateInterval() {

        return hyint;
    }

    public double getIntervalX() {

        return dxint;
    }

    public double getIntervalY() {

        return dyint;
    }

    @Override
    public void transform(final AffineTransform affine) {

        if (start >= 0 && !isValid()) {
            throw (new IllegalArgumentException("reset() was not called."));
        } else if (start < 0) {
            start = System.currentTimeMillis();
            last = start;
        }
        long curr = System.currentTimeMillis();
        long diff = 0;
        if (isValid() && (diff = curr - last) > 0) {
            double dxm = diff * dxint;
            double dym = diff * dyint;
            affine.translate(dxm, dym);
            last = curr;
        }
    }

    @Override
    public boolean isValid() {

        if (System.currentTimeMillis() - start > duration) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isStarted() {

        if (start < 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void reset() {

        start = -1;
    }

}
