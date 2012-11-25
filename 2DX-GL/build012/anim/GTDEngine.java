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

/**
 * Package containing classes that partake in animation management and definition.
 */
package bg.x2d.anim;

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * The base GTD animation engine provided by the 2DX library. Users are
 * encouraged to override the <code>drawFrame</code> method in particular to
 * implement project specific code. It is important, however, that this method
 * calls upon <code>runTasks</code> so that the registered Drawable objects can
 * be painted on-screen with each frame. <br>
 * <br>
 * GTD (Graphical Task Distribution Engine) submits its tasks to the
 * AWTEventQueue to be executed in order to avoid concurrent graphics
 * modification and adhere to Sun's recommended AWT/Swing programming behavior.
 * To ensure the best performance possible, it is recommended that you avoid
 * submitting other tasks to the AWTEventQueue while animating, so that the AWT
 * thread's execution of graphical tasks can be completed as quickly as
 * possible. <br>
 * <br>
 * <b>Note: Do not use SwingUtilities.invokeAndWait() (or any other blocking
 * method) in any Drawable objects passed to an GTDEngine.</b>
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public abstract class GTDEngine implements Animator {

	/**
	 * The current ArrayList that represents the engine's registered Drawable
	 * tasks.
	 */
	protected final List<Drawable> tasks = Collections
			.synchronizedList(new ArrayList<Drawable>());
	/**
	 * The boolean value that represents this engine's current blocking
	 * configuration.
	 */
	protected boolean wait = true;

	/**
	 * Orders this engine to draw an updated frame onto the given Graphics2D
	 * object. Users are encouraged to override this method so that they can
	 * implement their own procedure. It is important, however, that
	 * <code>runTasks(Graphics2D)</code><br>
	 * is called in order to execute all the engine's registered Drawable tasks.
	 * 
	 * @param g2
	 *            the Graphics2D object on which to draw.
	 * @see #runTasks(Graphics2D g2)
	 */
	@Override
	public void drawFrame(Graphics2D g2) {
		runTasks(g2);
	}

	/**
	 * Registers the given Drawable with this engine to be drawn with call to
	 * drawFrame(). This method is synchronized to prevent concurrent editing of
	 * the internal task List.
	 * 
	 * @param d
	 *            the Drawable to be registered
	 */
	public synchronized void register(Drawable d) {
		synchronized (tasks) {
			tasks.add(d);
		}
	}

	/**
	 * Removes the passed Drawable from this engine's task list, so it will no
	 * longer be executed in future update calls. If the Drawable is not
	 * recognized, nothing happens and <code>false</code> is returned. This
	 * method is synchronized to prevent concurrent editing of the internal task
	 * List.
	 * 
	 * @param d
	 *            the Drawable object to be removed.
	 * @return <code>true</code> if the Drawable existed in the engine's task
	 *         list, or <code>false</code> otherwise.
	 */
	public synchronized boolean decommission(Drawable d) {
		synchronized (tasks) {
			return tasks.remove(d);
		}
	}

	/**
	 * Configures this engine's blocking protocol. If set to true, the internal
	 * runTasks() method will block until all tasks have finished executing.
	 * Else, the method will return without waiting.<br>
	 * The default configuration is true.
	 * 
	 * @param block
	 *            value to which blocking should be configured.
	 */
	public void setBlocking(boolean block) {
		wait = block;
	}

	/**
	 * Gets the current blocking configuration of this engine. <br>
	 * The default setting is true.
	 * 
	 * @return the current block configuration value.
	 */
	public boolean willBlock() {
		return wait;
	}

	/**
	 * Fetches a copy of the engine's current task list. This method is
	 * synchronized to prevent concurrent editing of the internal task List.
	 * 
	 * @return the ArrayList<Drawable> containing the registered Drawables.
	 */
	public synchronized List<Drawable> getTasks() {
		synchronized (tasks) {
			return Collections.synchronizedList(new ArrayList<Drawable>(tasks));
		}
	}

	/**
	 * Called to execute all currently registered Drawables. Blocks until all
	 * tasks have completed, if this engine is configured to do so.<br>
	 * This method cannot be overridden by subclasses.
	 * 
	 * @param g2
	 */
	protected final void runTasks(final Graphics2D g2) {
		synchronized(tasks) {
			Iterator<Drawable> itr = tasks.iterator();
			while(itr.hasNext()) {
				final Drawable d = itr.next();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						d.draw(g2);
					}
				});
			}
		}
		if (!EventQueue.isDispatchThread() && wait) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						// Block until tasks have completed.
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
