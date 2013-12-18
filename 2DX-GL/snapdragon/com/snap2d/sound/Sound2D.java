/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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
import paulscode.sound.codecs.*;
import paulscode.sound.libraries.*;

/**
 * Main class for the Snapdragon2D Sound API. Allows you to initialize and shutdown the sound
 * system, as well as configure master-settings about Sound processing. You can obtain an instance
 * of Sound2D through the SoundAPI class.
 * 
 * Note that this class internally manages Paul Lamb's SoundSystem, and ONLY subclasses and other
 * classes within this packages should have access to it. Only one SoundSystem object should exist
 * per application process, so it is HIGHLY recommended that you do not attempt to use SoundSystem
 * or any corresponding libraries within Snapdragon2D directly, unless you are overriding Sound2D
 * with your own implementation.
 * 
 * Snapdragon2D Sound supports the following audio formats: Ogg Vorbis (.ogg), Waveform (.wav),
 * NeXT/Sun AU (.au), Apple AIFF (.aiff).
 * 
 * <br/>
 * <br/>
 * <b>SoundSystem for Java - credit goes to Paul Lamb http://www.paulscode.com/</b>
 * 
 * @author Brian Groenke
 * 
 */
public class Sound2D {

	private SoundSystem sound;

	/**
	 * Blocks constructor access. Only one instance of Sound2D should exist. It can be obtained
	 * through SoundAPI.
	 */
	protected Sound2D() {
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
			SoundSystemConfig.setCodec("au", CodecJSound.class);
			SoundSystemConfig.setCodec("aiff", CodecJSound.class);
		} catch (SoundSystemException e) {
			System.err.println("Snapdragon2D: error configuring sound system: "
					+ e.getMessage());
		}
	}

	/**
	 * Initializes the sound engine.
	 */
	public void initSystem() {
		if (sound != null) {
			shutdown();
		}
		sound = new SoundSystem();
	}

	/**
	 * 
	 * @return true if the sound system is currently initialized and running, false otherwise.
	 */
	public boolean isInitialized() {
		return sound != null;
	}

	/**
	 * Shuts down and releases all resources held by the sound system. SoundAPI automatically calls
	 * this method on exit, but it still may be called any time by the application. Sound2D can be
	 * re-initialized even after this method is called.
	 */
	public void shutdown() {
		if (sound == null) {
			return;
		}
		sound.cleanup();
		sound = null;
	}

	/**
	 * Sets the location where sound files are loaded from in the current JAR.
	 * 
	 * @param jarPkg
	 *            fully qualified path for the package in the JAR file (i.e com/example/sound/files)
	 */
	public void setSoundFilesPackage(String jarPkg) {
		if (jarPkg != null) {
			SoundSystemConfig.setSoundFilesPackage(jarPkg);
		}
	}

	/**
	 * Plays ambient background sound independent of world position.
	 * 
	 * @param id
	 * @param fileName
	 *            classpath or http url to file.
	 * @param loop
	 */
	public void playBackgroundMusic(String id, String fileName, boolean loop) {
		if (sound == null) {
			return;
		}
		sound.backgroundMusic(id, fileName, loop);
	}

	/**
	 * Typically should be used to pause currently playing background music, although because the
	 * same SoundSystem is used, passing an identifier to a sound source created elsewhere will
	 * still cause the sound to be paused (although this behavior isn't recommended - you should
	 * pause the sound from the same place where it was created).
	 * 
	 * @param id
	 */
	public void pauseSound(String id) {
		if (sound == null) {
			return;
		}
		sound.pause(id);
	}

	/**
	 * Typically should be used to stop currently playing background music, although because the
	 * same SoundSystem is used, passing an identifier to a sound source created elsewhere will
	 * still cause the sound to be stopped (although this behavior isn't recommended - you should
	 * stop the sound from the same place where it was created).
	 * 
	 * @param id
	 */
	public void stopSound(String id) {
		if (sound == null) {
			return;
		}
		sound.stop(id);
	}

	/**
	 * Pre-load a sound file into memory so it can be quickly played later.
	 * 
	 * @param fileName
	 */
	public void load(String fileName) {
		sound.loadSound(fileName);
	}

	/**
	 * Unload a sound file from memory.
	 * 
	 * @param fileName
	 */
	public void unload(String fileName) {
		sound.unloadSound(fileName);
	}

	/**
	 * Internal method for subclass implementations and/or classes within Snapdragon2D's sound API.
	 * Retrieves the underlying SoundSystem object from third party libraries that controls 3D audio
	 * mapping/playback.
	 * 
	 * @return the SoundSystem object (http://www.paulscode.com)
	 */
	protected SoundSystem soundSystem() {
		return sound;
	}
}
