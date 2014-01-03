/*
 *  Copyright © 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.utils;

import java.util.*;

/**
 * Allows for reflexive Map modification between two recursively mapped values. Each put operation
 * adds the key to an internal HashMap that maps value->key (type <V2,V1>).
 * 
 * This class is useful for situations where two values should have an associative relationship
 * rather than a key/value relationship.
 * 
 * Note: Like its parent and the rest of the Java Collections Framework, ReflexiveHashMap is not
 * thread safe and must be externally synchronized for concurrent use.
 * 
 * @author Brian Groenke
 * 
 */
public class ReflexiveHashMap<V1, V2> extends HashMap<V1, V2> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2719366762063798788L;

	private HashMap<V2, V1> reverse = new HashMap<V2, V1>();

	@Override
	public V2 put(V1 valA, V2 valB) {
		reverse.put(valB, valA);
		return super.put(valA, valB);
	}

	@Override
	public void putAll(Map<? extends V1, ? extends V2> map) {
		for (V1 key : map.keySet()) {
			reverse.put(map.get(key), key);
		}
		super.putAll(map);
	}

	@Override
	public V2 remove(Object key) {
		reverse.remove(reverse.get(key));
		return super.remove(key);
	}

	public V1 putByValue(V2 valA, V1 valB) {
		super.put(valB, valA);
		return reverse.put(valA, valB);
	}

	public V1 getByValue(V2 value) {
		return reverse.get(value);
	}

	public V1 removeByValue(V2 value) {
		super.remove(super.get(value));
		return reverse.remove(value);
	}

}
