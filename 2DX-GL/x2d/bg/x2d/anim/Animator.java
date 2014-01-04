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

package bg.x2d.anim;

import java.awt.*;

/**
 * Defines a standard for animation engines inside and outside of the 2DX library.<br>
 * Note: Java 5.0 or higher required as the java.util.concurrent class is utilized.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public interface Animator {

	public void drawFrame(Graphics2D g);
}
