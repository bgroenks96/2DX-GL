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

import java.awt.geom.*;
import java.util.*;

import paulscode.sound.*;


/**
 * Provides management of sound sources in a 2-dimensional sound space.  It is <b>highly recommended</b> that
 * only one instance of this class exists at a time in an application process.  While it isn't necessarily wrong
 * to create more than one instance, and there may be certain circumstances under which this is the correct
 * design to use, for most practical uses a one-instance only rule should be preferred.  All SoundMap objects
 * will inherently use the same SoundSystem, as only one should exist in the application.  Therefore, only one
 * listener location may be set (hence why it's a static member of SoundMap), and the implementer must be careful, 
 * if using multiple instances, not to overlap source identifiers with other SoundMap objects (they link to the same
 * system).  Therefore, in the interest of simplicity and safety, it should be preferred to keep only one instance of
 * SoundMap <b>active</b> in your application at a time.
 * @author Brian Groenke
 *
 */
public class SoundMap {

	public static final int 
	/**
	 * More realistic distance fading, but less predictable.  Probably best option
	 * for most sound sources.  Fade value should be between 0.0f-1.0f, where 0
	 * is indefinite and 1 is fastest sound fade-off.
	 */
	ATTENUATION_ROLLOFF = SoundSystemConfig.ATTENUATION_ROLLOFF, 
	/**
	 * Linear sound fading.  Less realistic but easily controlled.  Allows for
	 * easy determination of when sound will fade completely.  Fade value should be
	 * the exact, minimum, linear distance the sound should be heard from.
	 */
	ATTENUATION_LINEAR = SoundSystemConfig.ATTENUATION_LINEAR;

	static final int Z = 20;

	static Point2D.Float listener;

	SoundSystem sound;
	HashMap<String, SoundSource> sources = new HashMap<String, SoundSource>();
	ArrayList<String> playing = new ArrayList<String>();

	public SoundMap(Sound2D context, float listenerX, float listenerY) throws SoundContextException {
		if(!context.isInitialized())
			throw(new SoundContextException());
		sound = context.soundSystem();
		listener = new Point2D.Float(listenerX, listenerY);
	}

	public void moveListener(float x, float y) {
		listener.x += x;
		listener.y += y;
		sound.moveListener(x, y, 0);
	}

	public void setListenerPos(float x, float y) {
		listener.x += x;
		listener.y += y;
		sound.setListenerPosition(x, y, Z);
	}

	public void newSoundSource(String id, boolean priority, boolean stream, String fileUrl, float xpos, 
			float ypos, int attValue, float fade) throws IllegalArgumentException {
		if(attValue != ATTENUATION_ROLLOFF && attValue != ATTENUATION_LINEAR)
			throw(new IllegalArgumentException("illegal attenuation value"));
		SoundSource src= new SoundSource(priority, xpos, ypos);
		if(stream)
			sound.newStreamingSource(priority, id, fileUrl, false, xpos, ypos, Z, attValue, fade);
		else
			sound.newSource(priority, id, fileUrl, false, xpos, ypos, Z, attValue, fade);
		sources.put(id, src);
	}

	public boolean removeSoundSource(String id) {
		if(playing.contains(id))
			stop(id);
		return sources.remove(id) != null;
	}

	public boolean play(String id, boolean loop) {
		sound.setLooping(id, loop);
		sound.play(id);
		if(sound.playing(id)) {
			playing.add(id);
			return true;
		}

		return false;
	}

	public void pause(String id) {
		sound.pause(id);
	}

	public boolean stop(String id) {
		if(playing.contains(id)) {
			sound.stop(id);
			if(sound.playing(id)) {
				playing.remove(id);
				return true;
			}
		}

		return false;
	}

	public void quickPlay(String fileUrl, boolean priority, boolean loop, boolean stream, float xpos, 
			float ypos, int attValue, float fade) {
		if(stream)
			sound.quickStream(priority, fileUrl, loop, xpos, ypos, Z, attValue, fade);
		else
			sound.quickPlay(priority, fileUrl, loop, xpos, ypos, Z, attValue, fade);
	}

	public void moveSource(String id, float x, float y) {
		SoundSource src = sources.get(id);
		if(src == null)
			return;
		src.x += x;
		src.y += y;
		sound.setPosition(id, src.x, src.y, Z);
	}

	public void setSourcePos(String id, float xpos, float ypos) {
		SoundSource src= sources.get(id);
		if(src == null)
			return;
		src.x = xpos;
		src.y = ypos;
		sound.setPosition(id, xpos, ypos, Z);
	}

	public void setSourcePrioirty(String id, boolean priority) {
		SoundSource src = sources.get(id);
		if(src == null)
			return;
		src.priority = priority;
		sound.setPriority(id, priority);
	}

	public Point2D.Float getListenerPos() {
		return (Point2D.Float) listener.clone();
	}

	public Point2D.Float getSourcePos(String id) {
		SoundSource src = sources.get(id);
		if(src == null)
			return null;
		return new Point2D.Float(src.x, src.y);
	}

	public boolean getSourcePriority(String id) {
		SoundSource src = sources.get(id);
		if(src == null)
			throw(new NullPointerException("can't find a source with id: " + id));
		return src.priority;
	}

	private class SoundSource {
		
		float x,y;
		boolean priority;

		SoundSource(boolean p, float x, float y) {
			this.x = x; this.y = y;
			priority = p;
		}
	}
}
