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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class RotationSegment implements Segment {

	private long duration, start = -1, last;
	private int deltaTheta;
	private double dtInt;
	private Point2D anchor;

	public RotationSegment(int deltaTheta, long duration, Point2D anchor) {
		dtInt = (double) deltaTheta / duration;
		this.deltaTheta = deltaTheta;
		this.duration = duration;
		this.anchor = anchor;
	}

	public RotationSegment(int deltaTheta, long duration) {
		dtInt = (double) deltaTheta / duration;
		this.deltaTheta = deltaTheta;
		this.duration = duration;
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public double getUpdateInterval() {
		return dtInt;
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
			if(anchor !=null)
				affine.rotate(Math.toRadians(dtInt * diff), anchor.getX(), anchor.getY());
			else
				affine.rotate(Math.toRadians(dtInt * diff));
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
