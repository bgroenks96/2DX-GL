/**
 * Package containing classes that partake in animation management and definition.
 */
package bg.x2d.anim;

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

/**
 * The base animation engine provided by the 2DX library.  Users are encouraged to override the <code>drawFrame</code> method in particular to implement project specific code.  It is important, however,
 * that this method calls upon <code>runTasks</code> so that the registered Drawable objects can be painted on-screen with each frame.
 * <br>
 * <br>
 * EngineBase submits its tasks to the AWTEventQueue to be executed in order to avoid concurrent graphics modification and adhere to Sun's recommended AWT/Swing programming behavior.  To ensure the
 * best performance possible, it is recommended that you avoid submitting other tasks to the AWTEventQueue while animating, so that the AWT thread can execute graphical tasks can be completed as 
 * quickly as possible.
 * <br>
 * <br>
 * <b>Note: Do not use SwingUtilities.invokeAndWait() (or any other blocking method) in any Drawable objects passed to an EngineBase.<b>
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public abstract class EngineBase implements Animator {
	
	/**
	 * The current ArrayList that represents the engine's registered Drawable tasks.
	 */
	protected final ArrayList<Drawable> tasks = new ArrayList<Drawable>();
	/**
	 * The boolean value that represents this engine's current blocking configuration.
	 */
	protected boolean wait = true;
	
	/**
	 * Orders this engine to draw an updated frame onto the given Graphics2D object.
	 * Users are encouraged to override this method so that they can implement their own procedure.  It is important, however, that <code>runTasks(Graphics2D)</code><br>
	 * is called in order to execute all the engine's registered Drawable tasks.
	 * @param g2 the Graphics2D object on which to draw.
	 * @see #runTasks(Graphics2D g2)
	 */
	@Override
	public void drawFrame(Graphics2D g2) {
		runTasks(g2);
	}
	
	/**
	 * Registers the given Drawable with this engine to be drawn with call to drawFrame().  This method is synchronized to prevent concurrent editing of the internal task List.
	 * @param d the Drawable to be registered
	 */
	public synchronized void register(Drawable d) {
		tasks.add(d);
	}
	
	/**
	 * Removes the passed Drawable from this engine's task list, so it will no longer be executed in future update calls.  If the Drawable is not recognized, nothing happens
	 * and <code>false</code> is returned.  This method is synchronized to prevent concurrent editing of the internal task List.
	 * @param d the Drawable object to be removed.
	 * @return <code>true</code> if the Drawable existed in the engine's task list, or <code>false</code> otherwise.
	 */
	public synchronized boolean decommission(Drawable d) {
		return tasks.remove(d);
	}
	
	/**
	 * Configures this engine's blocking protocol.  If set to true, the internal runTasks() method will block until all tasks have finished executing.  Else, the method will return
	 * without waiting.<br>
	 * The default configuration is true.
	 * @param block value to which blocking should be configured.
	 */
	public void setBlocking(boolean block) {
		wait = block;
	}
	
	/**
	 * Gets the current blocking configuration of this engine.
	 * <br>
	 * The default setting is true.
	 * @return the current block configuration value.
	 */
	public boolean willBlock() {
		return wait;
	}
	
	/**
	 * Fetches the engine's current task list.  This method is synchronized to prevent concurrent editing of the internal task List.
	 * @return the ArrayList<Drawable> containing the registered Drawables.
	 */
	public synchronized ArrayList<Drawable> getTasks() {
		return tasks;
	}
	
	/**
	 * Called to execute all currently registered Drawables.  Blocks until all tasks have completed, if this engine is configured to do so.<br>
	 * This method cannot be overridden by subclasses.
	 * @param g2
	 */
	protected final void runTasks(final Graphics2D g2) {
		for(final Drawable d:tasks) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					d.draw(g2);
				}
			});

		}
		if(!EventQueue.isDispatchThread() && wait) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						//Block until tasks have completed.
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