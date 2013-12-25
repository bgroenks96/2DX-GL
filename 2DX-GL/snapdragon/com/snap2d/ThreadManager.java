/*
 *  Copyright � 2012-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d;

import java.util.concurrent.*;

/**
 * Manages standard and daemon thread pools for the Snapdragon2D Engine.
 * 
 * @author Brian Groenke
 * 
 */
public final class ThreadManager {

	private int n = 6;
	private DaemonThreadFactory dtf = new DaemonThreadFactory();
	private ExecutorService threadPool = Executors.newFixedThreadPool(n);
	private ExecutorService daemons = Executors.newCachedThreadPool(dtf);


	public synchronized Future<?> submitJob(Runnable r) {
		return threadPool.submit(r);
	}

	public void newDaemon(Runnable r) {
		daemons.execute(r);
	}

	public <T> Future<T> newDaemonTask(Callable<T> task) {
		return daemons.submit(task);
	}

	public void shutdown() {
		if (!threadPool.isShutdown() && !threadPool.isTerminated()) {
			threadPool.shutdown();
		}
	}

	public void forceShutdown() {
		if (!threadPool.isTerminated()) {
			threadPool.shutdownNow();
		}
	}
	
	/**
	 * Blocks the current thread until this ThreadManager's thread pool has fully shutdown.
	 * @param timeout time in milliseconds to timeout
	 * @throws InterruptedException 
	 */
	public boolean awaitTermination(long timeout) throws InterruptedException {
		return threadPool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Recreates a new thread pool after the current one has been shutdown.  Uses the current
	 * number of threads (or cached pool if n < 1) in the newly created pool.  If the current thread
	 * pool has not been shutdown, {@link #forceShutdown()} is called.
	 */
	public void reboot() {
		if(!threadPool.isShutdown())
			forceShutdown();
		threadPool = null;
		System.runFinalization();
		System.gc();
		if(n < 1)
		    threadPool = Executors.newCachedThreadPool();
		else
			threadPool = Executors.newFixedThreadPool(n);
	}

	/**
	 * Sets the size of the current thread pool for the ThreadManager class. This method calls
	 * shutdown() on the current thread pool object then instantiates a new ExecutorService using
	 * the same reference with the number of threads specified. Make sure to call this method either
	 * a) before launching any thread jobs or b) when you are sure the threads in the current pool
	 * can be shutdown.
	 * 
	 * @param nthreads
	 *            The number of threads in the new thread pool. If n < 1, a cached thread pool will
	 *            be created.
	 */
	public void setThreadCount(int nthreads) {
		threadPool.shutdown();

		if (nthreads < 1) {
			n = 0;
			threadPool = Executors.newCachedThreadPool();
			return;
		}

		n = nthreads;
		threadPool = Executors.newFixedThreadPool(n);
	}

	/**
	 * The number of threads in the current thread pool, or zero if the current pool is cached.
	 * 
	 * @return
	 */
	public int getThreadCount() {
		return n;
	}
}
