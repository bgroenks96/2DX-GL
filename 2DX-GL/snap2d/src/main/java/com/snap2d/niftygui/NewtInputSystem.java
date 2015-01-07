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

package com.snap2d.niftygui;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import de.lessvoid.nifty.NiftyInputConsumer;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;

/**
 * Implementation of NiftyGUI's InputSystem for NEWT.
 * 
 * @author Brian Groenke
 *
 */
public class NewtInputSystem implements InputSystem, KeyListener, MouseListener {

    Window inputWindow;

    NewtToNiftyKeyCodeConverter toNifty = new NewtToNiftyKeyCodeConverter();
    NiftyResourceLoader rscLoader;

    ConcurrentLinkedQueue <InputEvent> eventQueue = new ConcurrentLinkedQueue <InputEvent>();

    public NewtInputSystem(final Window inputWindow) {

        this.inputWindow = inputWindow;
        inputWindow.addKeyListener(this);
        inputWindow.addMouseListener(this);
    }

    /**
     *
     */
    @Override
    public void forwardEvents(final NiftyInputConsumer arg0) {

        InputEvent next = null;
        while ( (next = eventQueue.poll()) != null) {
            if (next instanceof MouseEvent) {
                MouseEvent mevt = (MouseEvent) next;
                boolean pressed = mevt.getEventType() == MouseEvent.EVENT_MOUSE_PRESSED
                        || mevt.getEventType() == MouseEvent.EVENT_MOUSE_DRAGGED;
                arg0.processMouseEvent(mevt.getX(), mevt.getY(), (int) mevt.getRotationScale(), mevt.getButton() - 1,
                        pressed);
            } else if (next instanceof KeyEvent) {
                KeyEvent kevt = (KeyEvent) next;
                arg0.processKeyboardEvent(new KeyboardInputEvent(toNifty.convertToNiftyKeyCode(kevt.getKeyCode()), kevt
                        .getKeyChar(), kevt.getEventType() == KeyEvent.EVENT_KEY_PRESSED, kevt.isShiftDown(), kevt
                        .isControlDown()));
            }
        }
    }

    /**
     *
     */
    @Override
    public void setMousePosition(final int arg0, final int arg1) {

        // Nifty uses screen coordinates for passing mouse coords to this
        // method, so we
        // need to convert to relative window coords to accomodate the NEWT
        // Window.
        int windowX = inputWindow.getX(), windowY = inputWindow.getY();
        inputWindow.warpPointer(arg0 - windowX, arg1 - windowY);
    }

    /**
     *
     */
    @Override
    public void setResourceLoader(final NiftyResourceLoader arg0) {

        this.rscLoader = arg0;
    }

    private void queueMouseEvent(final MouseEvent mevt) {

        eventQueue.offer(mevt);
    }

    private void queueKeyEvent(final KeyEvent kevt) {

        eventQueue.offer(kevt);
    }

    /**
     *
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseMoved(final MouseEvent e) {

        queueMouseEvent(e);

    }

    /**
     *
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void mouseWheelMoved(final MouseEvent e) {

        queueMouseEvent(e);
    }

    /**
     *
     */
    @Override
    public void keyPressed(final KeyEvent event) {

        queueKeyEvent(event);
    }

    /**
     *
     */
    @Override
    public void keyReleased(final KeyEvent event) {

        queueKeyEvent(event);
    }

}
