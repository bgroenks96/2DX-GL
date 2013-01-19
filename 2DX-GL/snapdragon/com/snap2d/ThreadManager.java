/*
 * Copyright � 2011-2012 Brian Groenke
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
