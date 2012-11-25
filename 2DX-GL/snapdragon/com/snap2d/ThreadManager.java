package com.snap2d;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ThreadManager {

	private static int n = 6;
	private static DaemonThreadFactory dtf = new DaemonThreadFactory();
	private static ExecutorService threadPool = Executors.newFixedThreadPool(n);
	private static ExecutorService daemons = Executors.newCachedThreadPool(dtf);
	private static volatile boolean running;

	// Prevent instantiation
	private ThreadManager() {
	}

	static {
		running = true;
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
		if (nthreads < 1) {
			n = 0;
			threadPool.shutdown();
			threadPool = Executors.newCachedThreadPool();
			return;
		}

		n = nthreads;
		threadPool.shutdown();
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