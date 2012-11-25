package com.snap2d;

import java.util.concurrent.ThreadFactory;

/**
 * Borrowed from the Groenke Commons Java Libraries.
 * 
 * @author Brian Groenke
 * 
 */
public class DaemonThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}
}
