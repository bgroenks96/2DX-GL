/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package bg.x2d.anim;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;

/**
 * Segment that scales the AffineTransform of this Graphics object based on x,y scale factors, time duration and size of the canvas
 * being draw upon (optional).
 * @author Brian Groenke
 *
 */
public class DilationSegment implements Segment {

	private long duration, start = -1, last;
	private double scalex, scaley, sxi, syi;
	private Dimension contextSize;

	/**
	 * Creates a new DilationSegment that will keep objects centered when scaling using the given <code>contextSize</code>.
	 * DilationSegment is immutable, but changes made to the passed Dimension object will be reflected upon the scaling operation.
	 * @param scalex
	 * @param scaley
	 * @param contextSize
	 * @param duration
	 */
	public DilationSegment(double scalex, double scaley, Dimension contextSize, long duration) {
		this.scalex = scalex;
		this.scaley = scaley;
		this.duration = duration;
		this.contextSize = contextSize;
		
		sxi = (scalex > 1.0) ? 1.0 + ((scalex - 1.0) / 1000) : 1.0 - ((1.0 - scalex) / 1000);
		syi = (scaley > 1.0) ? 1.0 + ((scaley - 1.0) / 1000) : 1.0 - ((1.0 - scaley) / 1000);
	}
	
	/**
	 * Creates a new DilationSegment that makes no attempt to keep objects centered when scaling.
	 * Calls <code>DilationSegment(scalex,scaley,null,duration)</code>.
	 * @param scalex
	 * @param scaley
	 * @param duration
	 * @see DilationSegment(double,double,Dimension,long)
	 */
	public DilationSegment(double scalex, double scaley, long duration) {
		this(scalex, scaley, null, duration);
	}
	
	/**
	 * Creates a new DilationSegment that makes no attempt to keep objects centered when scaling and uses the same
	 * x and y scale factors.
	 * Calls <code>DilationSegment(scale,scale,null,duration)</code>.
	 * @param scale
	 * @param duration
	 * @see DilationSegment(double,double,Dimension,long)
	 */
	public DilationSegment(double scale, long duration) {
		this(scale, scale, null, duration);
	}

	@Override
	public long getDuration() {
		return duration;
	}

	/**
	 * This implementation returns the average between the x and y scale factors.
	 */
	@Override
	public double getUpdateInterval() {
		return ((sxi + syi) / 2) / duration;
	}
	
	public double getXInterval() {
		return sxi;
	}
	
	public double getYInterval() {
		return syi;
	}

	/**
	 * Scales this AfineTransform the appropriate amount since the last call in the time frame.  
	 * If the context size was specified, a translation will be enacted to keep the objects centered.
	 */
	@Override
	public void transform(AffineTransform affine) {
		if (start >= 0 && !isValid()) {
			throw (new IllegalArgumentException(
					"reset() was not called."));
		} else if(start < 0) {
			start = System.currentTimeMillis();
			last = start;
		}
		long curr = System.currentTimeMillis();
		long diff = 0;
		if (isValid() && (diff = curr - last) > 0) {

			for(int i=0;i<diff;i++) {
				if(contextSize != null)
					affine.translate(
						    contextSize.getWidth()*(1-sxi)/2,
						    contextSize.getHeight()*(1-syi)/2
						);
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
