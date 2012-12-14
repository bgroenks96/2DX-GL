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

package com.snap2d.sound;

import java.util.*;

import paulscode.sound.*;
import paulscode.sound.codecs.*;
import paulscode.sound.libraries.*;

import com.snap2d.sound.libs.*;

/**
 * Main class for the Snapdragon2D Sound API.  Allows you to initialize and shutdown the sound system, as well
 * as configure master-settings about Sound processing.
 * 
 * Note that this class internally manages Paul Lamb's SoundSystem, and ONLY subclasses and other classes within this packages should
 * have access to it.  Only one SoundSystem object should exist per application process, so it is HIGHLY recommended that you do not
 * attempt to use SoundSystem or any corresponding libraries within Snapdragon2D directly, unless you are overriding Sound2D with your
 * own implementation.
 * 
 * Snapdragon2D Sound supports the following audio formats: Ogg Vorbis (.ogg), Waveform (.wav), NeXT/Sun AU (.au), Apple AIFF (.aiff).
 * @author Brian Groenke
 *
 */
public class Sound2D {

	static Sound2D sound2d;
	
	private SoundSystem sound;

	/**
	 * Block constructor access.  Only one instance of Sound2D should exist.
	 */
	protected Sound2D() {
		//
	}

	public static Sound2D getInstance() {
		if(sound2d == null)
			sound2d = new Sound2D();
		return sound2d;
	}
	
	public void initSystem() {
		if(sound != null)
			shutdown();
		LoadSoundLibraries.load();
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
			SoundSystemConfig.setCodec("au", CodecJSound.class);
			SoundSystemConfig.setCodec("aiff", CodecJSound.class);
		} catch (SoundSystemException e) {
			System.err.println("Snapdragon2D: error configuring sound system: " + e.getMessage());
		}
		sound = new SoundSystem();
	}
	
	public boolean isInitialized() {
		return sound != null;
	}

	public void shutdown() {
		if(sound == null)
			return;
		sound.cleanup();
		sound = null;
	}

	/**
	 * Sets the location where sound files are loaded from in the current JAR.
	 * @param jarPkg fully qualified path for the package in the JAR file (i.e com/example/sound/files)
	 */
	public void setSoundFilesPackage(String jarPkg) {
		if(jarPkg != null)
			SoundSystemConfig.setSoundFilesPackage(jarPkg);
	}
	
	/**
	 * Plays ambient background music independent of world position.
	 * @param id
	 * @param fileName classpath or http url to file.
	 * @param loop
	 */
	public void playBackgroundMusic(String id, String fileName, boolean loop) {
		if(sound == null)
			return;
		sound.backgroundMusic(id, fileName, loop);
	}
	
	/**
	 * Typically should be used to pause currently playing background music, although because the
	 * same SoundSystem is used, passing an identifier to a sound source created elsewhere will still
	 * cause the sound to be paused (although this behavior isn't recommended - you should pause the sound
	 * from the same place where it was created).
	 * @param id
	 */
	public void pauseSound(String id) {
		if(sound == null)
			return;
		sound.pause(id);
	}
	
	/**
	 * Typically should be used to stop currently playing background music, although because the
	 * same SoundSystem is used, passing an identifier to a sound source created elsewhere will still
	 * cause the sound to be stopped (although this behavior isn't recommended - you should stop the sound
	 * from the same place where it was created).
	 * @param id
	 */
	public void stopSound(String id) {
		if(sound == null)
			return;
		sound.stop(id);
	}
	
	/**
	 * Pre-load a sound file into memory so it can be quickly played later.
	 * @param fileName
	 */
	public void load(String fileName) {
		sound.loadSound(fileName);
	}
	
	/**
	 * Unload a sound file from memory.
	 * @param fileName
	 */
	public void unload(String fileName) {
		sound.unloadSound(fileName);
	}
	
	/**
	 * Internal method for subclass implementations and/or classes within Snapdragon2D's sound API.  Retrieves the underlying
	 * SoundSystem object from third party libraries that controls 3D audio mapping/playback.
	 * @return the SoundSystem object (http://www.paulscode.com)
	 */
	protected SoundSystem soundSystem() {
		return sound;
	}

	static float x,y,z=10;
	public static void main(String[] args) {
		final Sound2D s2d = Sound2D.getInstance();
		s2d.initSystem();
		s2d.setSoundFilesPackage("com/snap2d/sound/libs/");
		s2d.sound.setListenerPosition(0, 0, 0);
		s2d.sound.newStreamingSource(false, "elipse", "Elipse.ogg", false, x, y, z, SoundSystemConfig.ATTENUATION_ROLLOFF, 0.2f);
		s2d.sound.play("elipse");
		//s2d.playBackgroundMusic("elipse", "Elipse.ogg", false);
		Scanner sc = new Scanner(System.in);
		String line = null;
		while((line=sc.nextLine()) != null) {
			if(line.equals("q"))
				break;
			else if(line.equals("r"))
				x++;
			else if(line.equals("l"))
				x--;
			else if(line.equals("u"))
				y++;
			else if(line.equals("d"))
				y--;
			s2d.sound.setPosition("elipse", x, y, z);
		}
		s2d.shutdown();
	}
}
