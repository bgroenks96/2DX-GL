package bg.x2d.anim;

import java.awt.geom.*;
import java.util.*;

/**
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class ComboSegment implements Segment {

	Set<Segment> segSet;

	long duration, start, last;

	public ComboSegment(Set<Segment> segSet) {
		this.segSet = segSet;
		long max = 0;
		for (Segment s : segSet) {
			if (s.getDuration() > max) {
				max = s.getDuration();
			}
		}
		duration = max;
	}

	/**
	 * @return the duration of the longest Segment in the combo-set.
	 */
	@Override
	public long getDuration() {
		return duration;
	}

	/**
	 * A combined set of Segments does not have its own update interval.
	 * 
	 * @return {@link Segment.INTERVAL_UNDEF}
	 */
	@Override
	public double getUpdateInterval() {
		return INTERVAL_UNDEF;
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
		if (isValid() && curr - last > 0) {
			for (Segment s : segSet) {
				if (s.isValid() || !s.isStarted()) {
					s.transform(affine, tf);
				}
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
		for (Segment s : segSet) {
			s.reset();
		}
	}

}
