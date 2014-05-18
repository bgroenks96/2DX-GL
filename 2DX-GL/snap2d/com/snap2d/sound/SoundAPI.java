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

package com.snap2d.sound;

import paulscode.sound.*;

import com.snap2d.SnapLogger;

/**
 * Acts as a static entrance point to the Snap2D Sound API. You MUST call the
 * <code>init</code> method before an instance of Sound2D can be obtained. <br/>
 * <br/>
 * Note: A shutdown hook is added to the system to ensure that sound-based resources are cleaned up
 * properly. You may still shutdown a Sound2D instance at any time. The shutdown hook will simply
 * abort its shutdown operation.
 * 
 * @author Brian Groenke
 * 
 */
public class SoundAPI {
	
	private static final String JOAL_VERSION_CLASS_NAME = "com.jogamp.openal.JoalVersion";
	
	static Sound2D sound2d;
	static CleanupSoundSystem shutdownTask = new CleanupSoundSystem();

	private SoundAPI() {}

	/**
	 * Initializes the Snap2D sound system by creating and initializing the static API
	 * instance of Sound2D.  If SoundAPI has already been initialized and Sound2D has not been shutdown, 
	 * this method will do nothing and return immediately.
	 */
	public static void init() {
		if (sound2d != null && sound2d.isInitialized()) {
			return;
		}
		try {
			if(sound2d == null) {
				SnapLogger.println("SoundAPI: initializing...");
				boolean joal = isJOALAvailable();
				sound2d = new Sound2D(joal);
				Runtime.getRuntime().addShutdownHook(
						new Thread(shutdownTask));
			} else
				SnapLogger.println("SoundAPI: re-initializing...");
			
			sound2d.initSystem();
			SnapLogger.println("SoundAPI: successfully initialized sound system");
		} catch (SoundSystemException e) {
			SnapLogger.printErr("failed to initialize Snap2D Sound API: " + e.getMessage(), true);
			sound2d = null;
		}
	}
	
	private static boolean isJOALAvailable() {
		try {
			ClassLoader.getSystemClassLoader().loadClass(JOAL_VERSION_CLASS_NAME);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Sets the location where sound files are loaded from in the current JAR.
	 * 
	 * @param jarPkg
	 *            fully qualified path for the package in the JAR file (i.e com/example/sound/files)
	 */
	public static void setSoundFilesPackage(String jarPkg) {
		if (jarPkg != null) {
			SoundSystemConfig.setSoundFilesPackage(jarPkg);
		}
	}

	/**
	 * Sets the maximum size allowed for files being loaded into the sound system.
	 * @param maxBytes
	 */
	public static void setMaxFileSize(int maxBytes) {
		SoundSystemConfig.setMaxFileSize(maxBytes);
	}
	
	/**
	 * Set the maximum number of normal audio channels in this sound system.
	 * Streaming channels are created first, so the more higher number created the
	 * fewer normal channels that will be available.  Some libraries may require
	 * that the total number of channels (streaming + non-streaming) = 32
	 * @param num number of standard audio channels
	 */
	public static void setMaxNormalChannels(int num) {
		SoundSystemConfig.setNumberNormalChannels(num);
	}
	
	/**
	 * Set the maximum number of streaming audio channels in this sound system.
	 * Streaming channels are created first, so the more higher number created the
	 * fewer normal channels that will be available.  Some libraries may require
	 * that the total number of channels (streaming + non-streaming) = 32
	 * @param num number of streaming audio channels
	 */
	public static void setMaxStreamingChannels(int num) {
		SoundSystemConfig.setNumberStreamingChannels(num);
	}

	/**
	 * Fetches the initialized sound system handle Sound2D for the Snap2D Sound API.
	 * @return the static instance of the 'Sound2D' API handle.
	 * @throws IllegalStateException
	 */
	public static Sound2D getSound2D() throws IllegalStateException {
		if (sound2d == null) {
			throw (new IllegalStateException("SoundAPI not yet initialized"));
		}
		return sound2d;
	}

	private static class CleanupSoundSystem implements Runnable {

		@Override
		public void run() {
			if (sound2d != null && sound2d.isInitialized()) {
				SnapLogger.println("SoundAPI: shutting down");
				sound2d.shutdown();
				sound2d = null;
			}
		}

	}
}
