/*
 *  Copyright © 2011-2012 Brian Groenke
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
 * SoundAPI acts as an entrance point to the Snapdragon2D Sound API.  You MUST call the <code>init</code> method
 * before an instance of Sound2D can be obtained.
 * 
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
		
		static {
			SoundAPI.init();
		}
		
		Sound2D s2d;
		SoundMap soundMap;
		InputDispatch input = new InputDispatch(true);
		
		int x, y, ox = 350, oy = 350;
		
		TestClass() throws SoundContextException {
			s2d = SoundAPI.getSound2D();
			s2d.initSystem();
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
