/*
 * Copyright © 2011-2012 Brian Groenke, Private Proprietary Software
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

package bg.x2d.gen;

import java.util.Random;

/**
 * Generates a random number between the specified bounds using the parameterized number type.
 * <br>
 * This implementation of Generator finds a pseudo-random number between the max and min specified, using a simple algorithm that incorporates Math.random().
 * <br>
 * NumberGenerator will only take number types that are part of the Java(tm) language package (Integer, Double, Long, Float, Short, Byte).
 * @author Brian
 *
 * @param <T>Any of the Java primitive values (through auto-boxing) or their respective class representations.
 * @since 2DX 1.0 (1st Edition)
 */
public class NumberGenerator<T extends Number> implements Generator<T> {
	
	private T lowBound, highBound;
	private Random rand = new Random();
	
	public NumberGenerator(T min, T max) throws IllegalArgumentException {
		setBounds(min,max);
	}
	
	public NumberGenerator(T min, T max, long seed) throws IllegalArgumentException {
		rand.setSeed(seed);
	}
	
	public void setBounds(T min, T max) throws IllegalArgumentException {
		Number d = (Number) min;
		Number d2 = (Number) max;
		
		if(d.doubleValue() < d2.doubleValue()) {
			lowBound = min;
			highBound = max;
		} else {
			throw new IllegalArgumentException("Minimum value must be less than maximum value.");
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T generate() {
		double low = ((Number)lowBound).doubleValue();
		double high = ((Number)highBound).doubleValue();
		double diff = high - low;
		Double result = low + (rand.nextDouble()*diff);
		if(lowBound instanceof Double) return (T) result;
		else if(lowBound instanceof Integer) return (T) new Integer(result.intValue());
		else if(lowBound instanceof Long) return (T) new Long(result.longValue());
		else if(lowBound instanceof Short) return (T) new Short(result.shortValue());
		else if(lowBound instanceof Byte) return (T) new Byte(result.byteValue());
		else if(lowBound instanceof Float) return (T) new Float(result.floatValue());
		else throw(new NumberFormatException("NumberGenerator only supports the Java(tm) language Number (java.lang.Number) types."));
	}
}
