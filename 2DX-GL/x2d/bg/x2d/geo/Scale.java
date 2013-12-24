/*
 *  Copyright © 2012-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.geo;

/**
 * @author Brian Groenke
 * 
 */
public class Scale {

	double ratio;

	/**
	 * 
	 */
	public Scale(double ratio) {
		this.ratio = ratio;
	}

	public double scale(double num) {
		return num * ratio;
	}

	public int scale(int num) {
		return (int) Math.round(num * ratio);
	}

	public double getRatio() {
		return ratio;
	}

}
