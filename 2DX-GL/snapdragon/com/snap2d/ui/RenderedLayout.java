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

package com.snap2d.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import bg.x2d.anim.*;

import com.snap2d.gl.*;
import com.snap2d.input.*;

/**
 * Provides a mechanism for rendering UI components to the screen using the rendering engine's
 * default interface.  RenderedLayout keeps and manages a collection of RenderedCopmonents that
 * it is responsible for rendering, updating, resizing, and animating.  It implements the
 * MouseEventClient interface so that it can be registered with an InputDispatch to forward
 * relevant mouse events to child components.
 * <br/><br/>
 * Note: It is recommended that this class be handled by only one thread at a time, or is externally
 *     synchronized, as it frequently interfaces with non-thread-safe Java Collections.
 * @author Brian Groenke
 *
 */
public class RenderedLayout implements Renderable, MouseEventClient {

	public AffineTransform generalTransform = new AffineTransform();

	private final int INIT_WT, INIT_HT;
	private int wt, ht;
	private boolean autoScaleSize = true, autoScaleLoc = true, aspectRatio = true;

	private ArrayList<RenderedComponent> compList = new ArrayList<RenderedComponent>();
	private HashMap<RenderedComponent, Animation> compAnims = new HashMap<RenderedComponent, Animation>();

	/**
	 * 
	 */
	public RenderedLayout(int wt, int ht) {
		INIT_WT = wt;
		INIT_HT = ht;
		this.wt = wt;
		this.ht = ht;
	}

	/**
	 * Renders all components on screen, applying any set animations.
	 */
	@Override
	public void render(Graphics2D g, float interpolation) {
		AffineTransform prevTransform = g.getTransform();
		if(generalTransform != null)
			g.setTransform(generalTransform);

		for(RenderedComponent comp : compList) {
			Animation anim = compAnims.get(comp);
			if(anim == null) {
				comp.render(g, interpolation);
			} else {
				anim.draw(g);
				comp.render(g, interpolation);
				anim.release(g);
			}
		}

		g.setTransform(prevTransform);
	}

	/**
	 * Forwards the update call to all child components.
	 */
	@Override
	public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
		for(RenderedComponent comp : compList) {
			comp.update(nanoTimeNow, nanosSinceLastUpdate);
		}
	}

	/**
	 * Applies auto-scaling/translation to all children if configured to do so.
	 * The child's <code>onResize</code> method is invoked after scaling has occurred.
	 */
	@Override
	public void onResize(Dimension oldSize, Dimension newSize) {
		if(oldSize == null)
			return;
		double xratio = newSize.getWidth() / oldSize.getWidth();
		double yratio = newSize.getHeight() / oldSize.getHeight();

		if(autoScaleSize) {
			if(aspectRatio) {
				double ratio = (wt >= ht) ? xratio:yratio;
				wt = (int) Math.round(ratio * INIT_WT);
				ht = (int) Math.round(ratio * INIT_HT);
			} else {
				wt = (int) Math.round(xratio * newSize.getWidth());
				ht = (int) Math.round(yratio * newSize.getHeight());
			}
		}

		for(RenderedComponent child:compList) {
			int cwt = child.getWidth();
			int cht = child.getHeight();
			Point cloc = child.getLocation();

			if(autoScaleSize) {
				if(child.keepAspectRatio) {
					double ratio = (cwt >= cht) ? xratio:yratio;
					cwt = (int) Math.round(ratio * child.getRawWidth());
					cht = (int) Math.round(ratio * child.getRawHeight());
				} else {
					cwt = (int) Math.round(xratio * cwt);
					cht = (int) Math.round(yratio * cht);
				}
			}

			if(autoScaleLoc) {
				cloc.x = (int) Math.round((child.getRawLocation().x / (double)INIT_WT) * newSize.getWidth());
				cloc.y = (int) Math.round((child.getRawLocation().y / (double)INIT_HT) * newSize.getHeight());
			}

			child.setBounds(cloc.x, cloc.y, cwt, cht);
			child.onResize(oldSize, newSize);
		}
	}

	/**
	 * Sets an Animation for the given component.  The Animation
	 * will be started on the next render cycle.
	 * @param rc
	 * @param anim
	 */
	public void setAnimation(RenderedComponent rc, Animation anim) {
		if(compList.get(compList.indexOf(rc)) == null)
			return;
		compAnims.put(rc, anim);
	}

	/**
	 * Removes a previously set Animation for a component, if one exists.
	 * @param rc the RenderedComponent for which the animation should be removed.
	 * @return
	 */
	public boolean cancelAnimation(RenderedComponent rc) {
		return compAnims.remove(rc) != null;
	}

	/**
	 * Sets whether or not children should be automatically scaled after a resize.
	 * @param autoScale
	 */
	public void setAutoScaleSize(boolean autoScale) {
		autoScaleSize = autoScale;
	}

	/**
	 * Sets whether or not children should be automatically translated to maintain
	 * relative location after a resize.
	 * @param autoScale
	 */
	public void setAutoScaleLoc(boolean autoScale) {
		autoScaleLoc = autoScale;
	}

	/**
	 * Sets whether or not to keep this RenderedLayout's aspect ratio.  Has no effect
	 * on the scaling of child components.
	 * @param aspect
	 * @see com.snap2d.ui.RenderedComponent#keepAspectRatio
	 */
	public void setKeepAspectRatio(boolean aspect) {
		aspectRatio = aspect;
	}

	public void add(RenderedComponent rc) {
		compList.add(rc);
	}

	public void addAt(RenderedComponent rc, int index) {
		compList.add(index, rc);
	}

	public boolean remove(RenderedComponent rc) {
		return compList.remove(rc);
	}

	public RenderedComponent remove(int ind) {
		return compList.remove(ind);
	}

	/**
	 * Returns a copy of the internal List that stores components.
	 * @return
	 */
	public List<RenderedComponent> getComponents() {
		return new ArrayList<RenderedComponent>(compList);
	}

	public int getComponentCount() {
		return compList.size();
	}
	
	Point lastLoc;

	@Override
	/**
	 * Forwards mouse events to components whose bounds contain the mouse's current location.
	 * RenderedComponent children will begin receiving mouse events when the mouse enters their
	 * bounding box.  Upon the mouse exiting the component's area, RenderedLayout will send an extra
	 * MouseEvent with event ID MouseEvent.MOUSE_EXITED.
	 */
	public void processMouseEvent(MouseEvent me) {
		Point mloc = new Point(me.getX(), me.getY());
		if(lastLoc == null)
			lastLoc = new Point(mloc);
		for(RenderedComponent rc:compList) {
			if(rc.getBounds().contains(mloc)) {
				rc.processMouseEvent(me);
			} else if(rc.getBounds().contains(lastLoc)) {
				MouseEvent exit = new MouseEvent(me.getComponent(), MouseEvent.MOUSE_EXITED, me.getWhen(), 
						me.getModifiers(), mloc.x, mloc.y, me.getClickCount(), me.isPopupTrigger());
				rc.processMouseEvent(exit);
			}
		}
		
		lastLoc.setLocation(mloc);
	}
}
