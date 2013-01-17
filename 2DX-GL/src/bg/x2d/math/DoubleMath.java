/*
 * Copyright © 2011-2012 Brian Groenke
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

package bg.x2d.math;

/**
 * @author Brian Groenke
 *
 */
public class DoubleMath {	

	private static double round;
	
	static {
		setPrecision(6);
	}
	
	public static boolean equals(double arg0, double arg1) {
		return (arg1 <= arg0 + round && arg1 >= arg0 - round);
	}
	
	/**
	 * A much faster, simpler way of raising a number to an exponent than Math.pow.
	 * Note: The obvious limitation is the required integer exponent
	 * @param arg0 a number raised to the 'power'
	 * @param power nth power
	 * @return the first argument raised to the power of the second argument.
	 */
	public static double pow(double arg0, long power) {
		if(power == 0)
			return 1;
		
		double x = arg0;
		long lim = (long) Math.abs(power);
		for(long i = 1; i < lim; i++) {
			x *= (power > 0) ? arg0:1/arg0;
		}
		return x;
	}
	
	/**
	 * Sets the math precision value for floating point numbers.
	 * @param precision number of places to the right of the decimal to round.
	 */
	public static void setPrecision(int precision) {
		if(precision >= 0) {
			round = Math.pow(10, -precision);
		}
	}
	
	/**
	 * Default is 9 decimal places.
	 * @return precision - number of places to right of the decimal to round
	 */
	public static int getPrecision() {
		return (int) -Math.log(round);
	}
}
