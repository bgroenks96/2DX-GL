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

package com.snap2d.script.lib;

import java.util.*;

import bg.x2d.utils.Utils;

import com.snap2d.SnapLogger;
import com.snap2d.script.*;

/**
 * Library class initialized and attached by the runtime engine to manage timers created
 * in script code.
 * @author Brian Groenke
 *
 */
public class ScriptTimer {
	
	private static volatile int threadid = 0;
	
	private TimerThread timerThread;
	private ScriptProgram program;
	private boolean running = true;
	private int id;
	
	public ScriptTimer(ScriptProgram program) {
		this.program = program;
		this.id = threadid++;
		timerThread = new TimerThread();
		timerThread.setName("snap2d_script_timer_thread"+id);
		timerThread.setDaemon(true);
		timerThread.start();
	}
	
	private TreeSet<ScheduledTimerTask> tasks = new TreeSet<ScheduledTimerTask>();
	
	@ScriptLink
	public void timerAdd(int id, int delay, String scriptFunc) {
		ScheduledTimerTask t = new ScheduledTimerTask(System.currentTimeMillis() + delay);
		t.id = id; t.scriptFunc = scriptFunc; t.args = new Object[0];
		tasks.add(t);
		interruptTimerThread();
	}
	
	@ScriptLink
	public void timerArgInt(int id, int intArg) {
		ScheduledTimerTask timer = null;
		for(ScheduledTimerTask t : tasks)
			if(t.id == id) {
				timer = t;
				break;
			}
		if(timer == null)
			return;
		timer.args = Utils.appendArray(timer.args, intArg);
	}
	
	@ScriptLink
	public void timerArgFloat(int id, float floatArg) {
		ScheduledTimerTask timer = null;
		for(ScheduledTimerTask t : tasks)
			if(t.id == id) {
				timer = t;
				break;
			}
		if(timer == null)
			return;
		timer.args = Utils.appendArray(timer.args, floatArg);
	}
	
	@ScriptLink
	public void timerArgBool(int id, boolean boolArg) {
		ScheduledTimerTask timer = null;
		for(ScheduledTimerTask t : tasks)
			if(t.id == id) {
				timer = t;
				break;
			}
		if(timer == null)
			return;
		timer.args = Utils.appendArray(timer.args, boolArg);
	}
	
	@ScriptLink
	public void timerArgStr(int id, String strArg) {
		ScheduledTimerTask timer = null;
		for(ScheduledTimerTask t : tasks)
			if(t.id == id) {
				timer = t;
				break;
			}
		if(timer == null)
			return;
		timer.args = Utils.appendArray(timer.args, strArg);
	}
	
	@ScriptLink
	public boolean timerCancel(int id) {
		Object removeObj = null;
		for(ScheduledTimerTask t : tasks)
			if(t.id == id) {
				removeObj = t;
				break;
			}
		boolean hadElement = false;
		if(removeObj != null)
			hadElement = tasks.remove(removeObj);
		interruptTimerThread();
		return hadElement;
	}
	
	/**
	 * Stops the timer thread associated with this ScriptTimer object and
	 * clears the internal task list.  No further calls to {@link #addTimer(int, int, String, Object...)}
	 * or {@link #cancelTimer(int)} will have any effect.
	 */
	public void dispose() {
		running = false;
		interruptTimerThread();
		tasks.clear();
	}
	
	private void interruptTimerThread() {
		timerThread.interrupt();
	}
	
	private class TimerThread extends Thread implements Runnable {
		
		ScheduledTimerTask nextTask;

		/**
		 *
		 */
		@Override
		public void run() {
			while(running) {
				nextTask = (tasks.size() == 0) ? null : tasks.first();
				
				if(nextTask == null)
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (InterruptedException e) {}
				else {
					long sleepTime = nextTask.runTime.getTime() - System.currentTimeMillis();
					if(sleepTime < 0) {
						SnapLogger.printErr("timer-thread"+id+": task not valid - negative time difference!", true);
						SnapLogger.printErr("discarding task request...", true);
						tasks.pollFirst();
						continue;
					}
					
					try {
						Thread.sleep(sleepTime);
						Class<?>[] types = getArgClasses(nextTask.args);
						Function f = program.findFunction(nextTask.scriptFunc, types);
						try {
							program.invoke(f, nextTask.args);
						} catch (ScriptInvocationException e) {
							SnapLogger.printErr("timer-thread"+id+": error invoking script function: " + f, true);
						}
						tasks.pollFirst();
					} catch (InterruptedException e) {
						SnapLogger.log("timer-thread"+id+": interrupted while waiting for task: " + nextTask.id + 
								" func=" + nextTask.scriptFunc);
					}
				}
			}
			SnapLogger.println("");
		}
		
		private Class<?>[] getArgClasses(Object[] args) {
			Class<?>[] classes = new Class<?>[args.length];
			for(int i=0; i < args.length; i++)
				classes[i] = args[i].getClass();
			return classes;
		}
	}
	
	private class ScheduledTimerTask implements Comparable<ScheduledTimerTask> {
		
		int id;
		String scriptFunc;
		Object[] args;
		Date runTime;
		
		public ScheduledTimerTask(long runAt) {
			runTime = new Date(runAt);
		}

		/**
		 *
		 */
		@Override
		public int compareTo(ScheduledTimerTask task) {
			return runTime.compareTo(task.runTime);
		}
		
	}

}
