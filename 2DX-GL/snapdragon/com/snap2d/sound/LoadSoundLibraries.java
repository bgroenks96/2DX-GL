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

import java.io.*;
import java.net.*;
import java.util.jar.*;

/**
 * Class used to load SoundSystem library from local Snapdragon2D jar.  This class is internal, and should never
 * be extended or called by code outside of the Snapdragon2D Sound API.
 * @author Brian Groenke
 *
 */
final class LoadSoundLibraries {

	public static String[] JAR_PATHS = new String[] {"com/snap2d/sound/libs/LibraryJavaSound.jar", 
		"com/snap2d/sound/libs/Codecs.jar", "com/snap2d/sound/libs/SoundSystem.jar"};

	public static void load() {
		URL[] urls = new URL[JAR_PATHS.length];
		for(int i = 0;i < JAR_PATHS.length;i++) {
			urls[i] = ClassLoader.getSystemClassLoader().getResource(JAR_PATHS[i]);
			System.out.println("[snap2d] Located sound library at: " + urls[i].toExternalForm());
		}
		URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
		for(String s:JAR_PATHS) {
			try {
				JarInputStream jarIn = new JarInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(s));
				JarEntry je = null;
				while((je=jarIn.getNextJarEntry()) != null) {
					if(je.isDirectory() || !je.getName().endsWith(".class"))
						continue;
					try {
						// format name in correct binary format
						String name = je.getName().replaceAll("/", "\\.");
						name = name.substring(0, name.indexOf(".class"));
						
						loader.loadClass(name);
					} catch (ClassNotFoundException e) {
						System.err.println("Snapdragon2D: failed to locate class " + je.getName() + " in library " + s);
					}
				}
				jarIn.close();
			} catch (IOException e) {
				System.err.println("Snapdragon2D: error loading sound library " + s);
			}
		}
	}
}
