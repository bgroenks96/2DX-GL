/*
 * Copyright � 2011-2012 Brian Groenke
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

import java.awt.geom.*;

/**
 * Segment that rotates the AffineTransform a specified number of degrees, over
 * a specified amount of time and around an optional anchor point.
 * 
 * @author Brian Groenke
 * 
 */
public class RotationSegment implements Segment {

	private long duration, start = -1, last;
	private double dtInt;
	private Point2D anchor;

	/**
	 * Creates a new RotationSegment that rotates <code>deltaTheta</code>
	 * degrees for <code>duration</code> milliseconds around <code>anchor</code>
	 * point.
	 * 
	 * @param deltaTheta
	 *            the total degrees to rotate
	 * @param duration
	 *            the total time duration for the segment
	 * @param anchor
	 *            anchor point for the rotation; if null, the Graphics object
	 *            default anchor will be used (usually the x,y coordinate of
	 *            drawn objects).
	 */
	public RotationSegment(int deltaTheta, long duration, Point2D anchor) {
		dtInt = (double) deltaTheta / duration;
		this.duration = duration;
		this.anchor = anchor;
	}

	/**
	 * Calls <code>RotationSegment(deltaTheta, duration, null)</code>.
	 * 
	 * @param deltaTheta
	 * @param duration
	 * @see RotationSegment(int,long,Point2D)
	 */
	public RotationSegment(int deltaTheta, long duration) {
		this(deltaTheta, duration, null);
	}

	@Override
	public long getDuration() {
		return duration;
	}

	/**
	 * @return the degrees rotated each millisecond (approximate)
	 */
	@Override
	public double getUpdateInterval() {
		return dtInt;
	}

	@Override
	public void transform(AffineTransform affine) {
		if (start >= 0 && !isValid()) {
			throw (new IllegalArgumentException("reset() was not called."));
		} else if (start < 0) {
			start = System.currentTimeMillis();
			last = start;
		}
		long curr = System.currentTimeMillis();
		long diff = 0;
		if (isValid() && (diff = curr - last) > 0) {
			if (anchor != null) {
				affine.rotate(Math.toRadians(dtInt * diff), anchor.getX(),
						anchor.getY());
			} else {
				affine.rotate(Math.toRadians(dtInt * diff));
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
