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

public class DilationSegment implements Segment {

	private long duration, start = -1, last;
	private double scalex, scaley, sxi, syi;
	private Dimension size;

	public DilationSegment(double scalex, double scaley, Dimension size, long duration) {
		this.scalex = scalex;
		this.scaley = scaley;
		this.duration = duration;
		this.size = size;
		
		sxi = (scalex > 1.0) ? 1.0 + ((scalex - 1.0) / 1000) : 1.0 - ((1.0 - scalex) / 1000);
		syi = (scaley > 1.0) ? 1.0 + ((scaley - 1.0) / 1000) : 1.0 - ((1.0 - scaley) / 1000);
	}
	
	public DilationSegment(double scalex, double scaley, long duration) {
		this(scalex, scaley, null, duration);
	}
	
	public DilationSegment(double scale, long duration) {
		this(scale, scale, duration);
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public double getUpdateInterval() {
		return ((scalex + scaley) / 2) / duration;
	}

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
			//if(size != null)
				//affine.translate(-((size.getWidth()*scalex) / 1000) * diff, -((size.getHeight()*scaley) / 1000) * diff);
			for(int i=0;i<diff;i++)
			    affine.scale(sxi, syi);
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
