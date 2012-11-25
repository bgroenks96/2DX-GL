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

public class InputDispatch {

	private boolean running, consume;
	private KeyboardFocusManager manager;
	private List<KeyEventClient> keyClients;
	private List<MouseEventClient> mouseClients;

	private static final long MOUSE_INPUT_MASK = AWTEvent.MOUSE_EVENT_MASK
			+ AWTEvent.MOUSE_MOTION_EVENT_MASK
			+ AWTEvent.MOUSE_WHEEL_EVENT_MASK;

	public InputDispatch(boolean consume) {
		running = true;
		keyClients = Collections
				.synchronizedList(new ArrayList<KeyEventClient>());
		mouseClients = Collections
				.synchronizedList(new ArrayList<MouseEventClient>());
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyDispatcher());
		Toolkit.getDefaultToolkit().addAWTEventListener(new MouseDispatcher(),
				MOUSE_INPUT_MASK);
		ThreadManager.newDaemon(new CheckManager());
	}

	public synchronized void registerKeyClient(KeyEventClient client) {

		keyClients.add(client);
	}

	public synchronized void registerMouseClient(MouseEventClient client) {

		mouseClients.add(client);
	}

	public synchronized void removeKeyClient(KeyEventClient client) {

		keyClients.remove(client);
	}

	public synchronized void removeMouseClient(MouseEventClient client) {

		mouseClients.remove(client);
	}

	public interface KeyEventClient {

		public void processKeyEvent(KeyEvent e);
	}

	public interface MouseEventClient {

		public void processMouseEvent(MouseEvent me);
	}

	private class KeyDispatcher implements KeyEventDispatcher {

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {

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
		public void eventDispatched(AWTEvent event) {

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
				KeyboardFocusManager current = KeyboardFocusManager
						.getCurrentKeyboardFocusManager();
				if (!manager.equals(current)) {
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