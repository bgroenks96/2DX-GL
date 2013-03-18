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

import java.io.*;
import java.net.*;
import java.util.jar.*;

import bg.x2d.utils.*;

@Deprecated
/**
 * Class used to load SoundSystem library from local Snapdragon2D jar.  This class is internal, and should never
 * be extended or called by code outside of the Snapdragon2D Sound API.
 * 
 * This method proved unreliable and is currently unused.
 * @author Brian Groenke
 *
 */
final class LoadSoundLibraries {

	public static final String[] JAR_PATHS = new String[] {
			"com/snap2d/sound/libs/LibraryJavaSound.jar",
			"com/snap2d/sound/libs/Codecs.jar",
			"com/snap2d/sound/libs/SoundSystem.jar" };

	private static final String TEMP_JAR_NAME = "com_snap2d_sound-libjar_";

	public static void load(boolean verbose) {
		URL[] urls = new URL[JAR_PATHS.length];
		for (int i = 0; i < JAR_PATHS.length; i++) {
			InputStream in = ClassLoader.getSystemClassLoader()
					.getResourceAsStream(JAR_PATHS[i]);
			try {
				File f = Utils.writeToTempStorage(in, TEMP_JAR_NAME + i
						+ ".jar");
				f.deleteOnExit();
				urls[i] = Utils.getFileURL(f);
			} catch (IOException e) {
				System.err
						.println("Error writing sound library to temp-storage: "
								+ e.getMessage());
			}
			System.out.println("[snap2d] Located sound library at: "
					+ urls[i].toExternalForm());
		}
		URLClassLoader loader = new URLClassLoader(urls,
				ClassLoader.getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(loader);
		for (String s : JAR_PATHS) {
			try {
				JarInputStream jarIn = new JarInputStream(ClassLoader
						.getSystemClassLoader().getResourceAsStream(s));
				JarEntry je = null;
				while ((je = jarIn.getNextJarEntry()) != null) {
					if (je.isDirectory() || !je.getName().endsWith(".class")) {
						continue;
					}
					try {
						// format name in correct binary format
						String name = je.getName().replaceAll("/", "\\.");
						name = name.substring(0, name.indexOf(".class"));

						Class<?> loaded = Class.forName(name, true, loader);

						if (verbose) {
							System.out.println("loaded " + loaded.getName());
						}
					} catch (ClassNotFoundException e) {
						System.err
								.println("Snapdragon2D: failed to locate class "
										+ je.getName() + " in library " + s);
					}
				}
				jarIn.close();
			} catch (IOException e) {
				System.err.println("Snapdragon2D: error loading sound library "
						+ s);
			}
		}
	}
}
