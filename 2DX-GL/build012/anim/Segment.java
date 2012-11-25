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

/**
 * Defines methods for Segments of an Animation.  Each Segment is called asynchronously, but is expected to perform cumulative operations (per millisecond).
 * Subclasses should make sure to handle time standings accurately and take into account loss of time with each call (difference * increment).
 * @author Brian Groenke
 *
 */
public interface Segment {

	/**
	 * Fetches the time duration of this Segment.
	 * @return the time duration in milliseconds.
	 */
	public long getDuration();

	/**
	 * Gets the update increment value for each millisecond of time.  For Segments that use more than one interval (x and y for instance),
	 * this method will return some sort of combination between the two (average, hypotenuse, etc).
	 * @return the interval value for each milisecond of time in this Segment.
	 */
	public double getUpdateInterval();

	/**
	 * Performs this Segment's cumulative operations on this AffineTransform since the last call.
	 * If this Segment has been completed and is no longer valid, this method does nothing.
	 * @param affine
	 */
	public void transform(AffineTransform affine);

	/**
	 * Checks to see if this Segment has been started (if <code>transform</code> has been called.
	 * @return true if started, false otherwise.
	 */
	public boolean isStarted();

	/**
	 * Checks to see if this Segment has completed or not (valid if not completed, not valid otherwise).
	 * @return true if this Segment hasn't been started or is in progress, false otherwise.
	 */
	public boolean isValid();
	
	/**
	 * Resets the time variables of this Segment so that it becomes valid and ready to be started again.
	 */
	public void reset();
}
