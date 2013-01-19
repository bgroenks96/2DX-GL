package bg.x2d.anim;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The base animation engine provided by the 2DX library.  Users are encouraged to override the <code>drawFrame</code> method in particular to implement project specific code.  It is important, however,
 * that this method calls upon <code>runTasks</code> so that the registered Drawable objects can be painted on-screen with each frame.
 * <br>
 * EngineBase implements the Animator interface and provides a single thread as its worker.  This thread will handle each submitted Drawable object's procedure.
 * <br>
 * Note: Do not use SwingUtilities.invokeAndWait() in any Drawable objects passed to an EngineBase.
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public abstract class EngineBase implements Animator {
	
	/**
	 * The current ExecutorService thread that EngineBase provides.
	 */
	protected ExecutorService worker = Executors.newSingleThreadExecutor();
	/**
	 * The current ArrayList that represents the engine's registered Drawable tasks.
	 */
	protected final ArrayList<Drawable> tasks = new ArrayList<Drawable>();
	/**
	 * The boolean value that represents this engine's current blocking configuration.
	 */
	protected boolean wait = true;
	
	/**
	 * Fetches the current thread ExecutorService running this engine.
	 * @return the ExecutorService thread.
	 * @see java.util.concurrent.ExecutorService
	 */
	public ExecutorService getThread() {
		return worker;
	}
	
	/**
	 * Attempts to set this engine's thread worker to the given ExecutorService.
	 * @param thread the ExecutorService that will serve as the engine's new worker.
	 * @throws AnimException if the current worker fails to shutdown properly or in time.
	 */
	public void setThread(ExecutorService thread) throws AnimException {
		worker.shutdown();
		try {
			if(!worker.awaitTermination(500, TimeUnit.MILLISECONDS)) 
				worker.shutdownNow();
			if(!worker.awaitTermination(1, TimeUnit.SECONDS))
				throw(new AnimException("Failed to shutdown current Executor"));
			else
				worker = thread;
			
			System.gc();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
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
	 * Registers the given Drawable with this engine to be drawn with call to drawFrame().
	 * @param d the Drawable to be registered
	 */
	public void register(Drawable d) {
		tasks.add(d);
	}
	
	/**
	 * Removes the passed Drawable from this engine's task list, so it will no longer be executed in future update calls.  If the Drawable is not recognized, nothing happens
	 * and <code>false</code> is returned.
	 * @param d the Drawable object to be removed.
	 * @return <code>true</code> if the Drawable existed in the engine's task list, or <code>false</code> otherwise.
	 */
	public boolean decommission(Drawable d) {
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
	 * Fetches the engine's current task list.
	 * @return the ArrayList<Drawable> containing the registered Drawables.
	 */
	public ArrayList<Drawable> getTasks() {
		return tasks;
	}
	
	/**
	 * Called to execute all currently registered Drawables.  Blocks until all tasks have completed, if this engine is configured to do so.<br>
	 * This method cannot be overridden by subclasses.
	 * @param g2
	 */
	protected final void runTasks(final Graphics2D g2) {
		ArrayList<Future<?>> jobs = new ArrayList<Future<?>>();
		for(final Drawable d:tasks) {
			Future<?> job = worker.submit(new Runnable() {
				public void run() {
					d.draw(g2);
				}
			});
			jobs.add(job);
		}
		boolean done = false;
		while(wait&&!done) {
			boolean allFinished = true;
			for(int i=0;i<jobs.size();i++) {
				if(!jobs.get(i).isDone()) 
					allFinished = false;
				
				if(i < jobs.size()-1)
					continue;
				
				if(allFinished) 
					done = true; 
			}
		}
	}
	
}
