/*
 * Copyright © 2011-2012 Brian Groenke
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
 * Snapdragon2D Sound supports the following audio formats: Ogg Vorbis (.ogg), Wave (.wav), NeXT/Sun AU (.au), Apple AIFF (.aiff).
 * @author Brian Groenke
 *
 */
public class Sound2D {

	static SoundSystem sound;

	/**
	 * Initializes the sound system by loading required libraries and configuring the sound system.
	 */
	public static void initSystem() {
		if(sound != null)
			shutdown();
		LoadSoundLibraries.load();
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
			SoundSystemConfig.setCodec("au", CodecJSound.class);
			SoundSystemConfig.setCodec("aiff", CodecJSound.class);
		} catch (SoundSystemException e) {
			System.err.println("Snapdragon2D: error configuring sound system: " + e.getMessage());
		}
		sound = new SoundSystem();
	}

	public static void shutdown() {
		sound.cleanup();
		sound = null;
	}

	/**
	 * Sets the location where sound files are loaded from in the current JAR.
	 * @param jarPkg fully qualified path for the package in the JAR file (i.e com/example/sound/files)
	 */
	public static void setSoundFilesPacakge(String jarPkg) {
		if(jarPkg != null)
			SoundSystemConfig.setSoundFilesPackage(jarPkg);
	}

	public static void main(String[] args) {
		Sound2D.initSystem();
		SoundSystemConfig.setSoundFilesPackage("com/snap2d/sound/libs/");
		SoundSystem sound = Sound2D.sound;
		sound.backgroundMusic("elipse", "calm2.ogg", false);
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sound.cleanup();
	}
}
