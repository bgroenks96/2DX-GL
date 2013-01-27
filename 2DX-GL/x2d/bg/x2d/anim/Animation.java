/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.anim;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * A collection of Segments that perform operations on a Graphics object's
 * transform to create timed animation effects. The Segments provided will be
 * played in order over their respective time durations. When the Animation is
 * drawn, the current cumulative changes made to the AffineTransform of the
 * Graphics object are applied, and when released, the original Graphics
 * transform is restored. This allows for independent Animations per drawn
 * object in a simple, managed time model.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Animation implements Drawable {

	LinkedList<Segment> queue = new LinkedList<Segment>();
	LinkedList<Segment> dump = new LinkedList<Segment>();
	private volatile boolean finis = false, loop = false;
	private Transform tf;
	private Segment seg;

	private AffineTransform orig, curr;

	/**
	 * Creates this animation with the given segments.
	 * 
	 * @param segments
	 *            the Segments of this animation. <b>Must contain all Segments
	 *            when this constructor is called</b>. An internal list is
	 *            created with the contents of the given list, so any changes
	 *            made to the given list will not be reflected in the Animation
	 *            queue.
	 * @param autoLoop
	 *            if true this animation will automatically reset after
	 *            completion, else the responsibility will be left to the
	 *            caller.
	 */
	public Animation(List<Segment> segments, boolean autoLoop) {
		this(segments.toArray(new Segment[segments.size()]), autoLoop);
	}

	/**
	 * Creates this animation with the given segments.
	 * 
	 * @param segments
	 *            the Segments of this animation. <b>Must contain all Segments
	 *            when this constructor is called</b>. An internal list is
	 *            created with the contents of the given list, so any changes
	 *            made to the given list will not be reflected in the Animation
	 *            queue.
	 * @param autoLoop
	 *            if true this animation will automatically reset after
	 *            completion, else the responsibility will be left to the
	 *            caller.
	 */
	public Animation(Segment[] segments, boolean autoLoop) {
		if (segments == null || segments.length <= 0) {
			throw (new IllegalArgumentException(
					"Segment List was null or empty"));
		}
		queue = new LinkedList<Segment>();
		for (Segment s : segments) {
			queue.add(s);
		}
		loop = autoLoop;
		tf = new Transform();
	}

	/**
	 * Initiates the transformation for whatever Segment is currently in effect
	 * and sets the transform on the given Graphics2D object. Everything drawn
	 * between when this method is called and <code>release</code> is called
	 * will be affected by the transformations of this Animation.
	 */
	@Override
	public void draw(Graphics2D g2d) {
		orig = g2d.getTransform();
		if (curr == null) {
			curr = new AffineTransform(orig);
		}
		advance();
		g2d.setTransform(curr);
	}

	/**
	 * Resets the original AffineTranform of the Graphics2D object so the
	 * transformations will no longer affect it.
	 * 
	 * @param g2d
	 */
	public void release(Graphics2D g2d) {
		g2d.setTransform(orig);
	}

	/**
	 * Calls <code>reset</code> on all of the Segments in this Animation's dump
	 * queue, if and only if this Animation is finished. This must be called
	 * before this Animation will play again.
	 */
	public void resetAll() {
		if (!finis) {
			return;
		}
		for (Segment s : dump) {
			s.reset();
			queue.add(s);
		}
		dump.clear();
		tf = new Transform();
		finis = false;
	}

	/**
	 * Checks to see if this Animation is finished (all Segments have
	 * completed).
	 * 
	 * @return true if finished, false otherwise.
	 */
	public boolean isFinished() {
		return finis;
	}

	/**
	 * Fetches the currently active Segment in this Animation.
	 * 
	 * @return the Segment in effect during this Animation's time frame.
	 */
	public Segment getCurrentSegment() {
		return seg;
	}

	/**
	 * Advances the current Segment by calling its <code>transform</code>
	 * method. Also performs checks on the the Segment's validity and moves to
	 * the next Segment if it has been finished. Does nothing if all Segments
	 * have finished.
	 */
	protected void advance() {
		if (finis) {
			return;
		}

		seg = queue.peekFirst();
		if (seg == null) {
			finis = true;
			curr = new AffineTransform();
			if (loop) {
				resetAll();
			}
			return;
		} else if (!seg.isValid() && seg.isStarted()) {
			dump.add(queue.pollFirst());
		} else {
			seg.transform(curr, tf);
		}
	}
}
