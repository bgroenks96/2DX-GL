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

import java.awt.AWTEvent;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.snap2d.ThreadManager;

/**
 * Receives input events directly from Java's Abstract Windowing Toolkit and
 * dispatches them to registered listeners. This class will only work with
 * AWT/Swing windows and components. Applications using the OpenGL rendering
 * system must use the NEWT input interfaces also provided in this package.
 * 
 * @author Brian Groenke
 * 
 */
public class InputDispatch {

    private final boolean running;
    private boolean consume;
    private KeyboardFocusManager manager;
    private final List<KeyEventClient> keyClients;
    private final List<MouseEventClient> mouseClients;

    private final ThreadManager exec = new ThreadManager();

    private static final long MOUSE_INPUT_MASK = AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK
                    + AWTEvent.MOUSE_WHEEL_EVENT_MASK;

    public InputDispatch(final boolean consume) {

        running = true;
        keyClients = Collections.synchronizedList(new ArrayList<KeyEventClient>());
        mouseClients = Collections.synchronizedList(new ArrayList<MouseEventClient>());
        manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher());
        Toolkit.getDefaultToolkit().addAWTEventListener(new MouseDispatcher(), MOUSE_INPUT_MASK);
        exec.newDaemon(new CheckManager());
    }

    public synchronized void registerKeyClient(final KeyEventClient client) {

        keyClients.add(client);
    }

    public synchronized void registerMouseClient(final MouseEventClient client) {

        mouseClients.add(client);
    }

    public synchronized void removeKeyClient(final KeyEventClient client) {

        keyClients.remove(client);
    }

    public synchronized void removeMouseClient(final MouseEventClient client) {

        mouseClients.remove(client);
    }

    private class KeyDispatcher implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(final KeyEvent e) {

            synchronized (keyClients) {
                Iterator<KeyEventClient> i = keyClients.iterator();
                while (i.hasNext()) {
                    KeyEventClient client = i.next();
                    client.processKeyEvent(e);
                }
            }
            return consume;
        }

    }

    private class MouseDispatcher implements AWTEventListener {

        @Override
        public void eventDispatched(final AWTEvent event) {

            MouseEvent me = (MouseEvent) event;

            synchronized (mouseClients) {
                Iterator<MouseEventClient> i = mouseClients.iterator();
                while (i.hasNext()) {
                    MouseEventClient client = i.next();
                    client.processMouseEvent(me);
                }
            }
        }
    }

    private class CheckManager implements Runnable {

        @Override
        public void run() {

            Thread.currentThread().setName("snap2d-verify_dispatch");
            while (running && manager != null) {
                KeyboardFocusManager current = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                if ( !manager.equals(current)) {
                    manager = current;
                }
                try {
                    Thread.sleep(200);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
