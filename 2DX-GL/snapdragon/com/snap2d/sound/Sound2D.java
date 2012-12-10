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
 * Snapdragon2D Sound supports the following audio formats: Ogg Vorbis (.ogg), Waveform (.wav), NeXT/Sun AU (.au), Apple AIFF (.aiff).
 * @author Brian Groenke
 *
 */
public class Sound2D {

	static Sound2D sound2d;
	
	SoundSystem sound;

	static {
		sound2d = new Sound2D();
	}

	/**
	 * Block constructor access.  Only one instance of Sound2D should exist.
	 */
	private Sound2D() {
		//
	}

	public static Sound2D getInstance() {
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
	
	public void playBackgroundMusic(String id, String fileName, boolean loop) {
		if(sound == null)
			return;
		sound.backgroundMusic(id, fileName, loop);
	}
	
	
	public void stopSound(String id) {
		if(sound == null)
			return;
		sound.stop(id);
	}

	static float x,y,z;
	public static void main(String[] args) {
		final Sound2D s2d = Sound2D.getInstance();
		s2d.initSystem();
		s2d.setSoundFilesPackage("com/snap2d/sound/libs/");
		s2d.sound.setListenerPosition(0, 0, 0);
		s2d.sound.newStreamingSource(false, "elipse", "Elipse.ogg", false, x, y, z, SoundSystemConfig.ATTENUATION_ROLLOFF, 0.2f);
		s2d.sound.play("elipse");
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(SoundSystem.initialized()) {
					//s2d.sound.setPosition("elipse", x+=1, y+=1, z);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}).start();
		//s2d.playBackgroundMusic("elipse", "Elipse.ogg", false);
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		s2d.shutdown();
	}
}
