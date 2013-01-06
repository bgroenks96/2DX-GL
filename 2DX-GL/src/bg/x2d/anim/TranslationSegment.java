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
 * Segment that translates the AffineTransform the specified amounts on the x
 * and y axes for the specified amount of time.
 * 
 * @author Brian Groenke
 * 
 */
public class TranslationSegment implements Segment {

	private long duration, start = -1, last;
	private int hyp;
	private double dxint, dyint, hyint;

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
	public TranslationSegment(int deltax, int deltay, long duration) {
		hyp = (int) Math
				.round(Math.sqrt((deltax * deltax) + (deltay * deltay)));
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
	public void transform(AffineTransform affine, Transform tf) {
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
			affine.rotate(-tf.rotation);
			affine.translate(dxm, dym);
			affine.rotate(tf.rotation);
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
