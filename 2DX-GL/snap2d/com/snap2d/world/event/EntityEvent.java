/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.world.event;

import java.util.*;

import com.snap2d.world.*;

/**
 * @author Brian Groenke
 * 
 */
public class EntityEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8003284877529027936L;

	/**
	 * @param source
	 */
	public EntityEvent(Entity source) {
		super(source);
	}

}
