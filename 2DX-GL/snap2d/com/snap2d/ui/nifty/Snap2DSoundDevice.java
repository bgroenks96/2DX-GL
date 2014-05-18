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

package com.snap2d.ui.nifty;

import java.net.URL;

import com.snap2d.SnapLogger;
import com.snap2d.sound.*;

import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.*;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;

/**
 * Implementation of SoundDevice that forwards Nifty API calls through to the Snap2D Sound Libraries.
 * @author Brian Groenke
 *
 */
public class Snap2DSoundDevice implements SoundDevice {

	private NiftyResourceLoader loader;
	private Sound2D sound;

	public Snap2DSoundDevice(Sound2D sound) {
		this.sound = sound;
	}

	/**
	 *
	 */
	@Override
	public SoundHandle loadMusic(SoundSystem arg0, String arg1) {
		if(!sound.isInitialized()) {
			SnapLogger.printErr("failed to load sound: Sound2D is uninitialized", true);
			return null;
		}
		if(loader.getResource(arg1) == null) {
			SnapLogger.printErr("failed to load sound from resource loader: " + arg1, true);
			return null;
		} else
			return new MusicSoundHandle(arg1);
	}

	/**
	 *
	 */
	@Override
	public SoundHandle loadSound(SoundSystem arg0, String arg1) {
		if(!sound.isInitialized()) {
			SnapLogger.printErr("failed to load sound: Sound2D is uninitialized", true);
			return null;
		}
		if(loader.getResource(arg1) == null) {
			SnapLogger.printErr("failed to load sound from resource loader: " + arg1, true);
			return null;
		} else
			return new MiscSoundHandle(arg1);
	}

	/**
	 *
	 */
	@Override
	public void setResourceLoader(NiftyResourceLoader arg0) {
		this.loader = arg0;
	}

	/**
	 *
	 */
	@Override
	public void update(int arg0) {

	}

	private static int handleID = Integer.MAX_VALUE;

	private class MusicSoundHandle implements SoundHandle {

		URL url;
		String fileName;
		String id;

		MusicSoundHandle(String fileName) {
			this.id = String.valueOf(handleID--);
			this.fileName = fileName;
			if(loader != null)
				this.url = loader.getResource(fileName);
		}

		/**
		 *
		 */
		@Override
		public void dispose() {
			if(sound.isPlaying(id))
				sound.stop(id);
			sound.removeSoundSource(id);
		}

		/**
		 *
		 */
		@Override
		public float getVolume() {
			return sound.getVolume(id);
		}

		/**
		 *
		 */
		@Override
		public boolean isPlaying() {
			return sound.isPlaying(id);
		}

		/**
		 *
		 */
		@Override
		public void play() {
			if(url != null)
				sound.playBackgroundMusic(id, url, fileName, false);
			else
				sound.playBackgroundMusic(id, fileName, false);
		}

		/**
		 *
		 */
		@Override
		public void setVolume(float arg0) {
			sound.setVolume(id, arg0);
		}

		/**
		 *
		 */
		@Override
		public void stop() {
			sound.stop(id);
		}

	}
	
	private class MiscSoundHandle implements SoundHandle {
		
		String id;
		
		MiscSoundHandle(String fileName) {
			this.id = String.valueOf(handleID--);
			if(loader != null) {
				sound.newStaticSoundSource(id, loader.getResource(fileName), fileName, false, false);
			} else
				sound.newStaticSoundSource(id, fileName, false, false);
		}

		/**
		 *
		 */
		@Override
		public void dispose() {
			if(sound.isPlaying(id))
				sound.stop(id);
			sound.removeSoundSource(id);
		}

		/**
		 *
		 */
		@Override
		public float getVolume() {
			return sound.getVolume(id);
		}

		/**
		 *
		 */
		@Override
		public boolean isPlaying() {
			return sound.isPlaying(id);
		}

		/**
		 *
		 */
		@Override
		public void play() {
			sound.play(id);
		}

		/**
		 *
		 */
		@Override
		public void setVolume(float arg0) {
			sound.setVolume(id, arg0);
		}

		/**
		 *
		 */
		@Override
		public void stop() {
			sound.stop(id);
		}
	}

}
