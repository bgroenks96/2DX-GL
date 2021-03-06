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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;

/**
 * Segment that scales the AffineTransform of this Graphics object based on x,y
 * scale factors, time duration and size of the canvas being draw upon
 * (optional).
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class DilationSegment implements Segment {

    private final long duration;
    private long start = -1;
    private long last;
    private final double sxi, syi;
    private final Dimension contextSize;

    /**
     * Creates a new DilationSegment that will keep objects centered when
     * scaling using the given <code>contextSize</code>. DilationSegment is
     * immutable, but changes made to the passed Dimension object will be
     * reflected upon the scaling operation.
     * 
     * @param scalex
     * @param scaley
     * @param contextSize
     * @param duration
     */
    public DilationSegment(final double scalex, final double scaley, final Dimension contextSize, final long duration) {

        this.duration = duration;
        this.contextSize = contextSize;

        sxi = (scalex > 1.0) ? 1.0 + ( (scalex - 1.0) / 1000) : 1.0 - ( (1.0 - scalex) / 1000);
        syi = (scaley > 1.0) ? 1.0 + ( (scaley - 1.0) / 1000) : 1.0 - ( (1.0 - scaley) / 1000);
    }

    /**
     * Creates a new DilationSegment that makes no attempt to keep objects
     * centered when scaling. Calls
     * <code>DilationSegment(scalex,scaley,null,duration)</code>.
     * 
     * @param scalex
     * @param scaley
     * @param duration
     * @see DilationSegment(double,double,Dimension,long)
     */
    public DilationSegment(final double scalex, final double scaley, final long duration) {

        this(scalex, scaley, null, duration);
    }

    /**
     * Creates a new DilationSegment that makes no attempt to keep objects
     * centered when scaling and uses the same x and y scale factors. Calls
     * <code>DilationSegment(scale,scale,null,duration)</code>.
     * 
     * @param scale
     * @param duration
     * @see DilationSegment(double,double,Dimension,long)
     */
    public DilationSegment(final double scale, final long duration) {

        this(scale, scale, null, duration);
    }

    @Override
    public long getDuration() {

        return duration;
    }

    /**
     * This implementation returns the average between the x and y scale
     * factors.
     */
    @Override
    public double getUpdateInterval() {

        return ( (sxi + syi) / 2) / duration;
    }

    public double getXInterval() {

        return sxi;
    }

    public double getYInterval() {

        return syi;
    }

    /**
     * Scales this AfineTransform the appropriate amount since the last call in
     * the time frame. If the context size was specified, a translation will be
     * enacted to keep the objects centered.
     */
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

            for (int i = 0; i < diff; i++ ) {
                if (contextSize != null) {
                    affine.translate(contextSize.getWidth() * (1 - sxi) / 2, contextSize.getHeight() * (1 - syi) / 2);
                }
                affine.scale(sxi, syi);
            }
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
