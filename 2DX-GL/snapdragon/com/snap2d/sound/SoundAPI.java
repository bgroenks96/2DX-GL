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

package com.snap2d.sound;

/**
 * SoundAPI acts as an entrance point to the Snapdragon2D Sound API.  You MUST call the <code>init</code> method
 * before an instance of Sound2D can be obtained.
 * <br/><br/>
 * Note: A shutdown hook is added to the system to ensure that sound-based resources are cleaned up properly.
 * You may still shutdown a Sound2D instance at any time.  The shutdown hook will simply abort its shutdown operation.
 * @author Brian Groenke
 *
 */
public class SoundAPI {
	
	private SoundAPI() {}
	
	static Sound2D sound2d;
	
	public static void init() {
		if(sound2d != null)
			return;
		System.out.println("Snapdragon2D Sound API - initializing libraries");
		sound2d = new Sound2D();
		Runtime.getRuntime().addShutdownHook(new Thread(new CleanupSoundSystem()));
		System.out.println("Snapdragon2D Sound API - initialized");
	}
	
	public static Sound2D getSound2D() throws IllegalStateException {
		if(sound2d == null)
			throw(new IllegalStateException("SoundAPI not yet initialized"));
		return sound2d;
	}
	
	private static class CleanupSoundSystem implements Runnable {

		@Override
		public void run() {
			if(sound2d != null)
				if(sound2d.isInitialized())
					sound2d.shutdown();
		}
		
	}
}
