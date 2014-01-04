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

package bg.x2d.math;

import java.math.*;

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

	public static boolean equals(double arg0, double arg1, double round) {
		return (arg1 <= arg0 + round && arg1 >= arg0 - round);
	}

	/**
	 * A much faster, simpler way of raising a number to an exponent than Math.pow. Note: The
	 * obvious limitation is the required integer exponent
	 * 
	 * @param arg0
	 *            a number raised to the 'power'
	 * @param power
	 *            nth power
	 * @return the first argument raised to the power of the second argument.
	 */
	public static double pow(double arg0, long power) {
		if (power == 0) {
			return 1;
		}

		double x = arg0;
		long lim = Math.abs(power);
		for (long i = 1; i < lim; i++) {
			x *= (power > 0) ? arg0 : 1 / arg0;
		}
		return x;
	}
	
	/**
	 * Returns the given double value rounded to n decimal places.
	 * <br/>
	 * Note: the current precision setting of the DoubleMath class does NOT affect the result
	 * of this method.
	 * @param arg0 the double value to round
	 * @param n the scale (number of places) to which the double should be rounded
	 * @return
	 */
	public static double round(double arg0, int n) {
		return BigDecimal.valueOf(arg0).setScale(n, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * Sets the math precision value for floating point numbers.
	 * 
	 * @param precision
	 *            number of places to the right of the decimal to round.
	 */
	public static void setPrecision(int precision) {
		if (precision >= 0) {
			round = Math.pow(10, -precision);
		}
	}

	/**
	 * Default is 9 decimal places.
	 * 
	 * @return precision - number of places to right of the decimal to round
	 */
	public static int getPrecision() {
		return (int) -Math.log(round);
	}
}
