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

package com.snap2d.sound;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import paulscode.sound.*;
import paulscode.sound.codecs.*;
import paulscode.sound.libraries.LibraryJavaSound;
import bg.x2d.geo.Vector2f;

/**
 * Main facility for the Snap2D Sound API. Sound2D allows control over loading, playing, and
 * managing sound sources in 2D world space.  
 * 
 * Note that this class internally manages Paul Lamb's SoundSystem, and ONLY subclasses and other
 * classes within this packages should have access to it. Only one SoundSystem object should exist
 * per application process, so it is HIGHLY recommended that you do not attempt to use SoundSystem
 * or any corresponding libraries within Snap2D directly, unless you are overriding Sound2D
 * with your own implementation.
 * 
 * Snap2D Sound supports the following audio formats: Ogg Vorbis (.ogg), Waveform (.wav),
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
	
	/**
	 * More realistic distance fading, but less predictable. Fade value should be between 0.0f-1.0f, 
	 * where 0 is indefinite and 1 is fastest sound fade-off.
	 */
	public static final int
	ATTENUATION_ROLLOFF = SoundSystemConfig.ATTENUATION_ROLLOFF;
	
	/**
	 * Linear sound fading. Less realistic but easily controlled. Allows for easy determination of
	 * when sound will fade completely. Fade value should be the exact, minimum, linear distance the
	 * sound should be heard from.
	 */
	public static final int
	ATTENUATION_LINEAR = SoundSystemConfig.ATTENUATION_LINEAR;

	private static final String LIBRARY_JOAL_CLASS_NAME = "paulscode.sound.libraries.LibraryJOAL";
	private static final Logger log = Logger.getLogger(Sound2D.class.getCanonicalName());

	private SoundSystem sound;
	private boolean useJOAL;
	private Class<?> libJOAL;
	
	static final int Z = -10;

	HashMap<String, SoundSource> sources = new HashMap<String, SoundSource>();
	HashMap<String, SoundSource> staticSources = new HashMap<String, SoundSource>();
	HashSet<String> playing = new HashSet<String>();

	/**
	 * Blocks constructor access. Only one instance of Sound2D should exist. It can be obtained
	 * through SoundAPI.
	 * @throws SoundContextException 
	 * @throws SoundSystemException 
	 */
	protected Sound2D(boolean useJOAL) throws SoundSystemException {
		SoundSystemConfig.addLibrary(LibraryJavaSound.class);
		if(useJOAL) {
			libJOAL = loadJOALLibraryClass();
			if(libJOAL != null) {
				SoundSystemConfig.addLibrary(libJOAL);
				this.useJOAL = useJOAL;
			}
		}
		SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
		SoundSystemConfig.setCodec("wav", CodecWav.class);
		SoundSystemConfig.setCodec("au", CodecJSound.class);
		SoundSystemConfig.setCodec("aiff", CodecJSound.class);
	}

	/**
	 * Initializes the sound engine.  This is called by SoundAPI.
	 * @throws SoundSystemException 
	 */
	void initSystem() throws SoundSystemException {
		if (sound != null) {
			shutdown();
		}
		if(useJOAL)
			sound = new SoundSystem(libJOAL);
		else
			sound = new SoundSystem(LibraryJavaSound.class);
		
		sound.setListenerOrientation(0, 0, -1, 0, 1, 0);
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
	
	public void moveListener(float x, float y) {
		sound.moveListener(x, y, 0);
		for(String id : staticSources.keySet())
			sound.setPosition(id, getListenerPos().x, getListenerPos().y, Z);
	}

	public void setListenerPos(float x, float y) {
		sound.setListenerPosition(x, y, Z);
		for(String id : staticSources.keySet())
			sound.setPosition(id, x, y, Z);
	}
	
	public void setListenerVelocity(float x, float y) {
		sound.setListenerVelocity(x, y, 0);
		for(String id : staticSources.keySet())
			sound.setVelocity(id, x, y, 0);
	}

	/**
	 * Creates a new sound source at the given position in world space.  The sound source will
	 * play at the given position relative to the listener once {@link #play(String, boolean)} is
	 * called.  All sources are created with a constant Z coordinate defined by SoundMap.
	 * @param id a string id for this source source
	 * @param filename the name of the file relative to Sound2D.setSoundFilesPackage - if this
	 * sound has already been pre-loaded, the cached sound data will be used
	 * @param priority true if this sound source should take priority over others if 
	 * channel limits are reached, false otherwise
	 * @param stream true if this sound should be created on a streaming channel, false
	 * if it should be pre-loaded
	 * @param loop true if this sound should loop
	 * @param xpos the x coordinate of the sound source in world space
	 * @param ypos the y coordinate of the sound source in world space
	 * @param attValue the type of attenuation - ATTENUATION_LINEAR or ATTENUATION_ROLLOFF
	 * @param fade the fade value for sound attenuation - check attenuation type docs for information
	 * on setting the value
	 * @throws IllegalArgumentException
	 */
	public void newSoundSource(String id, String filename, boolean priority, boolean stream, boolean loop,
			float xpos, float ypos, int attValue, float fade)
			throws IllegalArgumentException {
		if (attValue != ATTENUATION_ROLLOFF && attValue != ATTENUATION_LINEAR) {
			throw (new IllegalArgumentException("illegal attenuation value"));
		}
		SoundSource src = new SoundSource(priority, loop, xpos, ypos);
		if (stream) {
			sound.newStreamingSource(priority, id, filename, loop, xpos, ypos,
					Z, attValue, fade);
		} else {
			sound.newSource(priority, id, filename, loop, xpos, ypos, Z,
					attValue, fade);
		}
		if(sources.put(id, src) != null)
			log.warning("SoundMap: overwrote previous source id '"+id+"'");
	}
	
	/**
	 * @param id a string id for this source source
	 * @param url the URL of the sound file
	 * @param filename an identifier for the sound file; the file name is recommended, but anything can be used.
	 * @param priority
	 * @param stream
	 * @param loop
	 * @param xpos
	 * @param ypos
	 * @param attValue
	 * @param fade
	 * @throws IllegalArgumentException
	 */
	public void newSoundSource(String id, URL url, String filename, boolean priority, boolean stream, boolean loop,
			float xpos, float ypos, int attValue, float fade) throws IllegalArgumentException {
		if (attValue != ATTENUATION_ROLLOFF && attValue != ATTENUATION_LINEAR) {
			throw (new IllegalArgumentException("illegal attenuation value"));
		}
		SoundSource src = new SoundSource(priority, loop, xpos, ypos);
		if (stream) {
			sound.newStreamingSource(priority, id, url, filename, loop, xpos, ypos,
					Z, attValue, fade);
		} else {
			sound.newSource(priority, id, url, filename, loop, xpos, ypos, Z,
					attValue, fade);
		}
		if(sources.put(id, src) != null)
			log.warning("SoundMap: overwrote previous source id '"+id+"'");
	}
	
	/**
	 * Creates a new sound source whose position will be attached to the listener.  Each time the
	 * listener position is modified in SoundMap, all static source positions will also be set accordingly.
	 * @param id
	 * @param filename
	 * @param priority
	 * @param loop
	 */
	public void newStaticSoundSource(String id, String filename, boolean priority, boolean loop) {
		newSoundSource(id, filename, priority, false, loop, getListenerPos().x, getListenerPos().y, 
				ATTENUATION_ROLLOFF, 0);
		staticSources.put(id, sources.get(id));
	}
	
	public void newStaticSoundSource(String id, URL url, String filename, boolean priority, boolean loop) {
		newSoundSource(id, url, filename, priority, false, loop, getListenerPos().x, getListenerPos().y, 
				ATTENUATION_ROLLOFF, 0);
		staticSources.put(id, sources.get(id));
	}

	/**
	 * Remove the specified sound source from memory and free associated
	 * system resources.
	 * @param id
	 * @return
	 */
	public boolean removeSoundSource(String id) {
		if (playing.contains(id)) {
			stop(id);
		}
		sound.removeSource(id);
		return sources.remove(id) != null | staticSources.remove(id) != null;
	}
	
	/**
	 * Loads and plays the given sound file.  Sounds played from 'quickPlay' can be freed all at once
	 * via {@link Sound2D.removeTempSources}.
	 * @param filename
	 * @param priority
	 * @param loop
	 * @param stream
	 * @param xpos
	 * @param ypos
	 * @param attValue
	 * @param fade
	 * @return the identifier for the new temporary source
	 */
	public String quickPlay(String filename, boolean priority, boolean stream,
			boolean loop, float xpos, float ypos, int attValue, float fade) {
		String tempID;
		if (stream) {
			tempID = sound.quickStream(priority, filename, loop, xpos, ypos, Z, attValue,
					fade);
		} else {
			tempID = sound.quickPlay(priority, filename, loop, xpos, ypos, Z, attValue,
					fade);
		}
		sources.put(tempID, new SoundSource(priority, loop, true, xpos, ypos));
		return tempID;
	}
	
	public String quickPlay(URL url, String filename, boolean priority, boolean stream,
			boolean loop, float xpos, float ypos, int attValue, float fade) {
		String tempID;
		if (stream) {
			tempID = sound.quickStream(priority, url, filename, loop, xpos, ypos, Z, attValue,
					fade);
		} else {
			tempID = sound.quickPlay(priority, url, filename, loop, xpos, ypos, Z, attValue,
					fade);
		}
		sources.put(tempID, new SoundSource(priority, loop, true, xpos, ypos));
		return tempID;
	}

	/**
	 * Plays the specified source.
	 * @param id the ID of the source to play
	 * @param loop true to loop playback, false otherwise
	 * @return
	 */
	public boolean play(String id) {
		sound.play(id);
		if (sound.playing(id)) {
			playing.add(id);
			return true;
		}

		return false;
	}

	public void pause(String id) {
		sound.pause(id);
	}

	public boolean stop(String id) {
		if (playing.contains(id)) {
			sound.stop(id);
			if (sound.playing(id)) {
				playing.remove(id);
				return true;
			}
		}

		return false;
	}
	
	public void setLooping(String id, boolean loop) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return;
		}
		src.loop = loop;
		sound.setLooping(id, loop);
	}
	
	public boolean isLooping(String id) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return false;
		}
		return src.loop && sound.playing(id);
	}

	public void moveSource(String id, float x, float y) {
		SoundSource src = sources.get(id);
		if (src == null || staticSources.containsKey(id)) {
			return;
		}
		src.x += x;
		src.y += y;
		sound.setPosition(id, src.x, src.y, Z);
	}

	public void setSourcePos(String id, float xpos, float ypos) {
		SoundSource src = sources.get(id);
		if (src == null || staticSources.containsKey(id)) {
			return;
		}
		src.x = xpos;
		src.y = ypos;
		sound.setPosition(id, xpos, ypos, Z);
	}
	
	public void setSourceVelocity(String id, Vector2f vel) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return;
		}
		src.xvel = vel.x;
		src.yvel = vel.y;
		sound.setVelocity(id, vel.x, vel.y, 0);
	}

	public void setSourcePrioirty(String id, boolean priority) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return;
		}
		src.priority = priority;
		sound.setPriority(id, priority);
	}

	public Point2D.Float getListenerPos() {
		ListenerData data = sound.getListenerData();
		return new Point2D.Float(data.position.x, data.position.y);
	}

	public Point2D.Float getSourcePos(String id) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return null;
		}
		return new Point2D.Float(src.x, src.y);
	}
	
	public Vector2f getSourceVelocity(String id) {
		SoundSource src = sources.get(id);
		if (src == null) {
			return null;
		}
		return new Vector2f(src.xvel, src.yvel);
	}

	public boolean getSourcePriority(String id) {
		SoundSource src = sources.get(id);
		if (src == null) {
			throw (new NullPointerException("can't find a source with id: "
					+ id));
		}
		return src.priority;
	}

	/**
	 * Plays ambient background sound independent of world position.
	 * This source has no recorded data about position, velocity, or priority.
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
		sources.put(id, new SoundSource(false, loop, 0, 0));
	}

	public void playBackgroundMusic(String id, URL url, String fileName, boolean loop) {
		if (sound == null) {
			return;
		}
		sound.backgroundMusic(id, url, fileName, loop);
		sources.put(id, new SoundSource(false, loop, 0, 0));
	}
	
	/**
	 * @return true if this Sound2D is playing sound from any source
	 */
	public boolean isPlaying() {
		return sound.playing();
	}
	
	/**
	 * @param id
	 * @return true if the specified source is playing
	 */
	public boolean isPlaying(String id) {
		return sound.playing(id);
	}
	
	/**
	 * Sets the pitch for the specified sound source
	 * @param id the id of the sound source
	 * @param pitch float value representing the pitch (0.5f-2.0f)
	 */
	public void setPitch(String id, float pitch) {
		sound.setPitch(id, pitch);
	}
	
	/**
	 * Sets the volume for the specified sound source
	 * @param id the id of the sound source
	 * @param value the new volume value (0.0f-1.0f)
	 */
	public void setVolume(String id, float value) {
		sound.setVolume(id, value);
	}
	
	/**
	 * Sets the master volume for the sound context
	 * @param id the id of the sound source
	 * @param value the new volume value (0.0f-1.0f)
	 */
	public void setMasterVolume(float value) {
		sound.setMasterVolume(value);
	}
	
	/**
	 * Get the pitch of the specified sound source
	 * @param id
	 * @return float value representing the pitch (0.5f-2.0f)
	 */
	public float getPitch(String id) {
		return sound.getPitch(id);
	}
	
	/**
	 * Get the volume of the specified sound source
	 * @param id
	 * @return float value representing the source volume setting (0.0f-1.0f)
	 */
	public float getVolume(String id) {
		return sound.getVolume(id);
	}
	
	/**
	 * Get the master volume of the sound context
	 * @return float value representing the master volume setting (0.0f-1.0f)
	 */
	public float getMasterVolume() {
		return sound.getMasterVolume();
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
	 * Pre-load a sound file into memory so it can be played on demand.
	 * @param url the URL of the sound file
	 * @param filename the filename or identifier of the file (this can be used to unload later)
	 */
	public void load(URL url, String filename) {
		sound.loadSound(url, filename);
	}

	/**
	 * Unload a sound file from memory. 
	 * @param fileName
	 */
	public void unload(String fileName) {
		sound.unloadSound(fileName);
	}
	
	/**
	 * Remove all temporary sound sources from the system and free associated
	 * system resources.
	 */
	public void removeTempSources() {
		for(String id : sources.keySet())
			if(sources.get(id).temp) {
				if(isPlaying(id))
					stop(id);
				sources.remove(id);
			}
		sound.removeTemporarySources();
	}

	/**
	 * Internal method for subclass implementations and/or classes within Snapdragon2D's sound API.
	 * Retrieves the underlying SoundSystem object from the third party library that controls 3D audio
	 * mapping/playback.
	 * 
	 * @return the SoundSystem object (http://www.paulscode.com)
	 */
	protected SoundSystem soundSystem() {
		return sound;
	}

	/*
	 * JOAL support is provided as an extension to the Snap2D Sound API.  If the user has provided
	 * the JOAL JAR in the classpath but not the LibraryJOAL JAR, we want to avoid unexpected
	 * ClassNotFoundError[s] and instead fall back to JavaSound, informing the user why the JOAL
	 * sound system was not able to be used.
	 */
	private Class<?> loadJOALLibraryClass() {
		if(libJOAL == null)
			try {
				libJOAL = ClassLoader.getSystemClassLoader().loadClass(LIBRARY_JOAL_CLASS_NAME);
				// if LibraryJOAL is available, make sure it is compatible with the system
				if(!SoundSystemConfig.libraryCompatible(libJOAL)) {
					log.warning("Sound2D: OpenAL not compatible");
					libJOAL = null;
				}
			} catch (ClassNotFoundException e) {
				log.warning("Sound2D: failed to locate " + LIBRARY_JOAL_CLASS_NAME);
			}
		return libJOAL;
	}
	
	/*
	 * Stores data about each sound source so that it can retrieved by accessors.
	 */
	private class SoundSource {

		float x, y, xvel, yvel;
		boolean priority, loop, temp;

		SoundSource(boolean p, boolean loop, boolean temp, float x, float y) {
			this.x = x;
			this.y = y;
			this.priority = p;
			this.loop = loop;
		}
		
		SoundSource(boolean p, boolean loop, float x, float y) {
			this(p, loop, false, x, y);
		}
	}
}
