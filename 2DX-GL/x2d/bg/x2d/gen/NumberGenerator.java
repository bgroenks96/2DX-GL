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

package bg.x2d.gen;

import java.util.Random;

/**
 * Generates a random number between the specified bounds using the
 * parameterized number type. <br>
 * This implementation of Generator finds a pseudo-random number between the max
 * and min specified, using a simple algorithm that incorporates Math.random(). <br>
 * NumberGenerator will only take number types that are part of the Java(tm)
 * language package (Integer, Double, Long, Float, Short, Byte).
 * 
 * @author Brian
 * 
 * @param <T>Any of the Java primitive values (through auto-boxing) or their
 *        respective class representations.
 * @since 2DX 1.0 (1st Edition)
 */
public class NumberGenerator<T extends Number> implements Generator<T> {

	private T lowBound, highBound;
	private Random rand = new Random();

	public NumberGenerator(T min, T max) throws IllegalArgumentException {
		setBounds(min, max);
	}

	public NumberGenerator(T min, T max, long seed)
			throws IllegalArgumentException {
		rand.setSeed(seed);
	}

	public void setBounds(T min, T max) throws IllegalArgumentException {
		Number d = min;
		Number d2 = max;

		if (d.doubleValue() < d2.doubleValue()) {
			lowBound = min;
			highBound = max;
		} else {
			throw new IllegalArgumentException(
					"Minimum value must be less than maximum value.");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T generate() {
		double low = ((Number) lowBound).doubleValue();
		double high = ((Number) highBound).doubleValue();
		double diff = high - low;
		Double result = low + (rand.nextDouble() * diff);
		if (lowBound instanceof Double) {
			return (T) result;
		} else if (lowBound instanceof Integer) {
			return (T) new Integer(result.intValue());
		} else if (lowBound instanceof Long) {
			return (T) new Long(result.longValue());
		} else if (lowBound instanceof Short) {
			return (T) new Short(result.shortValue());
		} else if (lowBound instanceof Byte) {
			return (T) new Byte(result.byteValue());
		} else if (lowBound instanceof Float) {
			return (T) new Float(result.floatValue());
		} else {
			throw (new NumberFormatException(
					"NumberGenerator only supports the Java(tm) language Number (java.lang.Number) types."));
		}
	}
}
