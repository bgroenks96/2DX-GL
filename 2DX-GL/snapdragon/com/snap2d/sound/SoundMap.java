/*
 * Copyright � 2011-2012 Brian Groenke
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

package com.snap2d.sound;

import java.awt.*;
import java.util.*;

import paulscode.sound.*;


/**
 * @author Brian Groenke
 *
 */
public class SoundMap {
	
	static final int Z = 10;
	
	SoundSystem sound;
	Point listener;
	HashMap<String, Point> sources = new HashMap<String, Point>();
	
	public SoundMap(Sound2D context) throws SoundContextException {
		if(!context.isInitialized())
			throw(new SoundContextException());
		sound = context.soundSystem();
	}
}
