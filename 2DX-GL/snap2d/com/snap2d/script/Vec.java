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

import bg.x2d.geo.Vector2d;

public class Vec implements Operand {

	Vector2d vec;

	Vec(double x, double y) {
		vec = new Vector2d(x, y);
	}

	Vec(Vector2d vec) {
		this(vec.x, vec.y);
	}

	@Override
	public Vector2d getValue() {
		return vec;
	}

	/**
	 *
	 */
	 @Override
	 public boolean isVector() {
		 return true;
	 }

	 @Override
	 public String toString() {
		 return "["+vec.x+", "+vec.y+"]";
	 }
}