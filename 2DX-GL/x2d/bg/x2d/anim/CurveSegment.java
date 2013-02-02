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
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class CurveSegment implements Segment {

	int xs, ys, cx1, cy1, cx2, cy2, xe, ye, lx, ly;

	private long duration, start = -1, last;
	private double t = 0.01, inc;

	public CurveSegment(int x0, int y0, int x1, int y1, int x2, int y2, int x3,
			int y3, long duration) {
		this.xs = x0;
		this.ys = y0;
		this.lx = xs;
		this.ly = ys;
		this.cx1 = x1;
		this.cy1 = y1;
		this.cx2 = x2;
		this.cy2 = y2;
		this.xe = x3;
		this.ye = y3;
		this.duration = duration;

		inc = 1.0 / duration;
	}

	@Override
	public long getDuration() {
		return duration;
	}

	/**
	 * Bezier curves are neither linear nor a function. Thus, update intervals
	 * are undefined.
	 * 
	 * @return {@link Segment.INTERVAL_UNDEF}
	 */
	@Override
	public double getUpdateInterval() {
		return INTERVAL_UNDEF; // update interval is undefined for a bezier
								// curve (it's non-linear and not a function)
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
			double tinc = diff * inc;
			t += tinc;
			int nx = (int) Math.round(Math.pow(1 - t, 3) * xs + 3
					* Math.pow(1 - t, 2) * t * cx1 + 3 * (1 - t)
					* Math.pow(t, 2) * cx2 + Math.pow(t, 3) * xe);
			int ny = (int) Math.round(Math.pow(1 - t, 3) * ys + 3
					* Math.pow(1 - t, 2) * t * cy1 + 3 * (1 - t)
					* Math.pow(t, 2) * cy2 + Math.pow(t, 3) * ye);
			int dx = nx - lx;
			int dy = ny - ly;
			affine.translate(dx, dy);
			lx = nx;
			ly = ny;
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
		t = 0.01;
		lx = xs;
		ly = ys;
	}
}
