/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.input;

import com.jogamp.newt.event.MouseEvent;

/**
 * @author Brian Groenke
 *
 */
public class GLMouseEvent {
	
	// ---- begin NEWT mouse event field binding ---- //
	public static final short BUTTON_COUNT = MouseEvent.BUTTON_COUNT;
	public static final short BUTTON1 = MouseEvent.BUTTON1;
	public static final short BUTTON2 = MouseEvent.BUTTON2;
	public static final short BUTTON3 = MouseEvent.BUTTON3;
	public static final short BUTTON4 = MouseEvent.BUTTON4;
	public static final short BUTTON5 = MouseEvent.BUTTON5;
	public static final short BUTTON6 = MouseEvent.BUTTON6;
	public static final short BUTTON7 = MouseEvent.BUTTON7;
	public static final short BUTTON8 = MouseEvent.BUTTON8;
	public static final short BUTTON9 = MouseEvent.BUTTON9;
	public static final short EVENT_MOUSE_CLICKED = MouseEvent.EVENT_MOUSE_CLICKED;
	public static final short EVENT_MOUSE_DRAGGED = MouseEvent.EVENT_MOUSE_DRAGGED;
	public static final short EVENT_MOUSE_ENTERED = MouseEvent.EVENT_MOUSE_ENTERED;
	public static final short EVENT_MOUSE_EXITED = MouseEvent.EVENT_MOUSE_EXITED;
	public static final short EVENT_MOUSE_MOVED = MouseEvent.EVENT_MOUSE_MOVED;
	public static final short EVENT_MOUSE_PRESSED = MouseEvent.EVENT_MOUSE_PRESSED;
	public static final short EVENT_MOUSE_RELEASED = MouseEvent.EVENT_MOUSE_RELEASED;
	public static final short EVENT_MOUSE_WHEEL_MOVED = MouseEvent.EVENT_MOUSE_WHEEL_MOVED;
	// ---- //

	MouseEvent evt;
	
	public GLMouseEvent(MouseEvent evt) {
		this.evt = evt;
	}

	public short getButton() {
		return evt.getButton();
	}
	
	public short getClickCount() {
		return evt.getClickCount();
	}
	
	public float getRotationScale() {
		return evt.getRotationScale();
	}
	
	public int getX() {
		return evt.getX();
	}
	
	public int getY() {
		return evt.getY();
	}
	
	public int getButtonDownCount() {
		return evt.getButtonDownCount();
	}
	
	public boolean isAltDown() {
		return evt.isAltDown();
	}
	
	public boolean isAltGraphDown() {
		return evt.isAltGraphDown();
	}
	
	public boolean isAnyButtonDown() {
		return evt.isAnyButtonDown();
	}
	
	public boolean isAutoRepeat() {
		return evt.isAutoRepeat();
	}
	
	public boolean isButtonDown(int btn) {
		return evt.isButtonDown(btn);
	}
	
	public boolean isConfined() {
		return evt.isConfined();
	}
	
	public boolean isControlDown() {
		return evt.isControlDown();
	}
	
	public boolean isInvisible() {
		return evt.isInvisible();
	}
	
	public boolean isMetaDown() {
		return evt.isMetaDown();
	}
	
	public boolean isShiftDown() {
		return evt.isShiftDown();
	}
	
	public boolean isConsumed() {
		return evt.isConsumed();
	}
	
	public long getWhen() {
		return evt.getWhen();
	}
	
	public short getEventType() {
		return evt.getEventType();
	}
	
	public void setConsumed(boolean consumed) {
		evt.setConsumed(consumed);
	}
	
	@Override
	public String toString() {
		return evt.toString();
	}
}
