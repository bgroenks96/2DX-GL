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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;

public class Animation implements Drawable {

	LinkedList<Segment> queue = new LinkedList<Segment>();
	LinkedList<Segment> dump = new LinkedList<Segment>();
	private boolean finis = false, loop = false;
	private Segment seg;

	private AffineTransform orig, curr = new AffineTransform();

	/**
	 * Creates this animation with the given segments.
	 * @param segments the Segments of this animation.  <b>Must contain all Segments when this constructor is called</b>.  An internal list is created with the contents
	 * of the given list, so any changes made to the given list will not be reflected in the Animation queue.
	 * @param autoLoop if true this animation will automatically reset after completion, else the responsibility will be left to the caller.
	 */
	public Animation(List<Segment> segments, boolean autoLoop) {
		if (segments == null || segments.size() <= 0) {
			throw (new IllegalArgumentException(
					"Segment List was null or empty"));
		}
		queue = new LinkedList<Segment>(segments);
		loop = autoLoop;
	}

	@Override
	public void draw(Graphics2D g2d) {
		orig = g2d.getTransform();
		advance();
		g2d.setTransform(curr);
	}

	public void release(Graphics2D g2d) {
		g2d.setTransform(orig);
	}
	
	public void resetAll() {
		for(Segment s:dump) {
			s.reset();
			queue.add(s);
		}
		dump.clear();
		finis = false;
	}
	
	public boolean isFinished() {
		return finis;
	}

	public Segment getCurrentSegment() {
		return seg;
	}

	private void advance() {
		if(finis)
			return;
		
		seg = queue.peekFirst();
		if (seg == null) {
			finis = true;
			curr = new AffineTransform();
			if(loop)
				resetAll();
			return;
		} else if (!seg.isValid() && seg.isStarted()) {
			dump.add(queue.pollFirst());
		} else {
			seg.transform(curr);
		}
	}
}
