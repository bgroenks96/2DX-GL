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

package com.snap2d.world;

import com.snap2d.world.event.AddEvent;
import com.snap2d.world.event.CollisionEvent;
import com.snap2d.world.event.RemoveEvent;

public interface EntityListener {

    public void onCollision(CollisionEvent collEvt);

    public void onAdd(AddEvent addEvt);

    public void onRemove(RemoveEvent remEvt);

}
