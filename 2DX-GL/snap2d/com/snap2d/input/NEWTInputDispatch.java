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

import java.util.HashSet;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

/**
 * Internal Snap2D JOGL library class responsible for relaying NEWT input events
 * to com.snap2d.input API listeners.
 * 
 * @author Brian Groenke
 *
 */
public class NEWTInputDispatch implements KeyListener, MouseListener {

    private final HashSet <GLKeyListener> keyListeners;
    private final HashSet <GLMouseListener> mouseListeners;
    private final boolean consume;

    public NEWTInputDispatch(final boolean consumeEvents) {

        this.consume = consumeEvents;
        keyListeners = new HashSet <GLKeyListener>();
        mouseListeners = new HashSet <GLMouseListener>();
    }

    public void registerKeyListener(final GLKeyListener listener) {

        keyListeners.add(listener);
    }

    public void registerMouseListener(final GLMouseListener listener) {

        mouseListeners.add(listener);
    }

    public void removeKeyListener(final GLKeyListener listener) {

        keyListeners.remove(listener);
    }

    public void removeMouseListener(final GLMouseListener listener) {

        mouseListeners.remove(listener);
    }

    public void dispose() {

        keyListeners.clear();
        mouseListeners.clear();
    }

    private void processKeyEvent(final KeyEvent kevt) {

        for (GLKeyListener keyListener : keyListeners) {
            if (kevt.getEventType() == KeyEvent.EVENT_KEY_PRESSED) {
                keyListener.keyPressed(new GLKeyEvent(kevt));
            } else if (kevt.getEventType() == KeyEvent.EVENT_KEY_RELEASED) {
                keyListener.keyReleased(new GLKeyEvent(kevt));
            }
            kevt.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseClicked(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseDragged(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseEntered(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseExited(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseMoved(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseMoved(new GLMouseEvent(e));
            e.setConsumed(consume);
        }

    }

    /**
     *
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mousePressed(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseReleased(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void mouseWheelMoved(final MouseEvent e) {

        for (GLMouseListener mouseListener : mouseListeners) {
            mouseListener.mouseWheelMoved(new GLMouseEvent(e));
            e.setConsumed(consume);
        }
    }

    /**
     *
     */
    @Override
    public void keyPressed(final KeyEvent event) {

        processKeyEvent(event);
    }

    /**
     *
     */
    @Override
    public void keyReleased(final KeyEvent event) {

        processKeyEvent(event);
    }
}
