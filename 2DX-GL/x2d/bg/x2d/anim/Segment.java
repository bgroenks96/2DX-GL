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

import java.awt.geom.*;

/**
 * Defines methods for Segments of an Animation. Each Segment is called asynchronously, but is
 * expected to perform cumulative operations (per millisecond). Subclasses should make sure to
 * handle time standings accurately and take into account loss of time with each call (difference *
 * increment).
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public interface Segment {

	/**
	 * Value returned by <code>getUpdateInterval</code> if the Segment has no defined update
	 * interval.<br>
	 * Constant value = -1
	 */
	public static final int INTERVAL_UNDEF = -1;

	/**
	 * Fetches the time duration of this Segment.
	 * 
	 * @return the time duration in milliseconds.
	 */
	public long getDuration();

	/**
	 * Gets the update increment value for each millisecond of time. For Segments that use more than
	 * one interval (x and y for instance), this method will return some sort of combination between
	 * the two (average, hypotenuse, etc).
	 * 
	 * @return the interval value for each milisecond of time in this Segment or INTERVAL_UNDEF if
	 *         undefined.
	 */
	public double getUpdateInterval();

	/**
	 * Performs this Segment's cumulative operations on this AffineTransform since the last call. If
	 * this Segment has been completed and is no longer valid, this method does nothing.
	 * 
	 * @param affine
	 * @param transform
	 *            data for ongoing transformations
	 */
	public void transform(AffineTransform affine);

	/**
	 * Checks to see if this Segment has been started (if <code>transform</code> has been called.
	 * 
	 * @return true if started, false otherwise.
	 */
	public boolean isStarted();

	/**
	 * Checks to see if this Segment has completed or not (valid if not completed, not valid
	 * otherwise).
	 * 
	 * @return true if this Segment hasn't been started or is in progress, false otherwise.
	 */
	public boolean isValid();

	/**
	 * Resets the time variables of this Segment so that it becomes valid and ready to be started
	 * again.
	 */
	public void reset();
}
