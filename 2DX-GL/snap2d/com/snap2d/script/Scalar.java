/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.script;


public class Scalar implements Operand {

	double val;

	Scalar(double val) {
		this.val = val;
	}

	@Override
	public Double getValue() {
		return val;
	}

	@Override
	public boolean isVector() {
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(val);
	}
}