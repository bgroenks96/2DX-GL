/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.opengl;

import java.util.HashSet;

import com.jogamp.newt.event.*;
import com.snap2d.input.*;

/**
 * Internal Snap2D JOGL library class responsible for relaying NEWT input events
 * to com.snap2d.input API listeners.
 * @author Brian Groenke
 *
 */
class NEWTInputDispatcher implements KeyListener, MouseListener {
	
	private HashSet<GLKeyListener> keyListeners;
	private HashSet<GLMouseListener> mouseListeners;
	private boolean consume;
	
	NEWTInputDispatcher(boolean consumeEvents) {
		this.consume = consumeEvents;
		keyListeners = new HashSet<GLKeyListener>();
		mouseListeners = new HashSet<GLMouseListener>();
	}
	
	void registerKeyListener(GLKeyListener listener) {
		keyListeners.add(listener);
	}
	
	void registerMouseListener(GLMouseListener listener) {
		mouseListeners.add(listener);
	}
	
	void removeKeyListener(GLKeyListener listener) {
		keyListeners.remove(listener);
	}
	
	void removeMouseListener(GLMouseListener listener) {
		mouseListeners.remove(listener);
	}
	
	void dispose() {
		keyListeners.clear();
		mouseListeners.clear();
	}
	
	private void processKeyEvent(KeyEvent kevt) {
		for(GLKeyListener keyListener : keyListeners) {
			if(kevt.getEventType() == KeyEvent.EVENT_KEY_PRESSED)
				keyListener.keyPressed(new GLKeyEvent(kevt));
			else if(kevt.getEventType() == KeyEvent.EVENT_KEY_RELEASED)
				keyListener.keyReleased(new GLKeyEvent(kevt));
			kevt.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseClicked(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseDragged(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseEntered(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseExited(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseMoved(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
		
	}

	/**
	 *
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mousePressed(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseReleased(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void mouseWheelMoved(MouseEvent e) {
		for(GLMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseWheelMoved(new GLMouseEvent(e));
			e.setConsumed(consume);
		}
	}

	/**
	 *
	 */
	@Override
	public void keyPressed(KeyEvent event) {
		processKeyEvent(event);
	}

	/**
	 *
	 */
	@Override
	public void keyReleased(KeyEvent event) {
		processKeyEvent(event);
	}
}
