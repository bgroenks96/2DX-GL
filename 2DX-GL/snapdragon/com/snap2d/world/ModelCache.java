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

package com.snap2d.world;

import java.lang.ref.*;
import java.util.*;

/**
 * Uses a simple caching system to store SoftReferences of CollisionModels. The GarbageCollector is
 * free to discard CollisionModels stored ONLY in this cache if available memory is becoming a
 * concern. This means that you may at some point receive null values when obtaining a stored
 * CollisionModel, so if it is needed at that present time, you will need to recreate it. <br/>
 * <br/>
 * The mapping system uses generic Object values as keys. You may associate any Object you like with
 * a CollisionModel for any purpose. For Entity management, it is recommended that you use the
 * Entity implemetnation's <code>Class<?></code> object, as it will allow you to keep one model per
 * Entity class stored in memory and easily accessible in this cache.
 * 
 * @author Brian Groenke
 * 
 */
public class ModelCache {

	private HashMap<Object, SoftReference<CollisionModel>> cmodelMap;

	public ModelCache() {
		cmodelMap = new HashMap<Object, SoftReference<CollisionModel>>();
	}

	/**
	 * Caches the given CollisionModel with the associated Object as a key.
	 * 
	 * @param key
	 * @param model
	 */
	public void put(Object key, CollisionModel model) {
		if (key == null || model == null) {
			throw (new IllegalArgumentException("null argument(s)"));
		}
		cmodelMap.put(key, new SoftReference<CollisionModel>(model));
	}

	/**
	 * Returns the CollisionModel associated with the given Object. This method may return null for
	 * stored values if the CollisionModel has been cleared from memory by the runtime environment.
	 * 
	 * @param key
	 * @return the CollisionModel associated with <code>obj</code>, null if cleared or the given key
	 *         isn't recognized.
	 */
	public CollisionModel getFor(Object key) {
		SoftReference<CollisionModel> ref = cmodelMap.get(key);
		CollisionModel model = ref.get();
		if (model == null) {
			cmodelMap.remove(key);
		}
		return model;
	}
}
