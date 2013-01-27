/*
 *  Copyright © 2011-2013 Brian Groenke
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manages standard and daemon thread pools for the Snapdragon2D Engine.
 * @author Brian Groenke
 *
 */
public final class ThreadManager {

	private static int n = 6;
	private static DaemonThreadFactory dtf = new DaemonThreadFactory();
	private static ExecutorService threadPool = Executors.newFixedThreadPool(n);
	private static ExecutorService daemons = Executors.newCachedThreadPool(dtf);

	// Prevent instantiation
	private ThreadManager() {
	}

	public static synchronized Future<?> submitJob(Runnable r) {
		return threadPool.submit(r);
	}

	public static void newDaemon(Runnable r) {
		daemons.execute(r);
	}

	public static <T> Future<T> newDaemonTask(Callable<T> task) {
		return daemons.submit(task);
	}

	public static void shutdown() {
		if (!threadPool.isShutdown() && !threadPool.isTerminated()) {
			threadPool.shutdown();
		}
	}

	public static void forceShutdown() {
		if (!threadPool.isTerminated()) {
			threadPool.shutdownNow();
		}
	}

	public static void reboot() {
		System.gc();
		threadPool = null;
		threadPool = Executors.newCachedThreadPool();
	}

	/**
	 * Sets the size of the current thread pool for the ThreadManager class.
	 * This method calls shutdown() on the current thread pool object then
	 * instantiates a new ExecutorService using the same reference with the
	 * number of threads specified. Make sure to call this method either a)
	 * before launching any thread jobs or b) when you are sure the threads in
	 * the current pool can be shutdown.
	 * 
	 * @param nthreads
	 *            The number of threads in the new thread pool. If n < 1, a
	 *            cached thread pool will be created.
	 */
	public static void setThreadCount(int nthreads) {
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
	 * The number of threads in the current thread pool, or zero if the current
	 * pool is cached.
	 * 
	 * @return
	 */
	public static int getThreadCount() {
		return n;
	}
}
