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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import paulscode.sound.*;
import paulscode.sound.codecs.*;
import paulscode.sound.libraries.*;

import com.snap2d.input.*;
import com.snap2d.input.InputDispatch.KeyEventClient;
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
	public static void main(String[] args) throws SoundContextException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new TestClass());
		frame.setSize(800, 800);
		frame.setVisible(true);
	}
	
	private static class TestClass extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1225292075422922856L;
		Sound2D s2d;
		SoundMap soundMap;
		InputDispatch input = new InputDispatch(true);
		
		int x, y, ox = 350, oy = 350;
		
		TestClass() throws SoundContextException {
			s2d = Sound2D.getInstance();
			s2d.initSystem();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					s2d.shutdown();
				}
				
			}));
			s2d.setSoundFilesPackage("com/snap2d/sound/libs/");
			soundMap = new SoundMap(s2d, x, y);
			soundMap.newSoundSource("elipse", true, true, "Elipse.ogg", ox, oy, SoundMap.ATTENUATION_ROLLOFF, 0.1f);
			input.registerKeyClient(new KeyEventClient() {

				@Override
				public void processKeyEvent(KeyEvent e) {
					int lx = x, ly =y ;
					switch(e.getKeyCode()) {
					case KeyEvent.VK_W:
						y-=5;
						break;
					case KeyEvent.VK_A:
						x-=5;
						break;
					case KeyEvent.VK_S:
						y+=5;
						break;
					case KeyEvent.VK_D:
						x+=5;
					}
				
					soundMap.moveListener(x - lx, y - ly);
					repaint();
				}
				
			});
			soundMap.play("elipse", true);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.RED);
			g.fillRect(ox, oy, 20, 20);
			g.setColor(Color.BLUE);
			g.fillRect(x, y, 20, 20);
		}
	}
}
