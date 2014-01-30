/*
 * Copyright © 2011-2013 Brian Groenke
 * All rights reserved.
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
 * A Drawable is an object that defines a specific image to be drawn with each call to its
 * <code>draw(Graphics2D)</code> method. <br>
 * Drawable is used by the Background object (bg.tdx.Background) to set an image that can be
 * continuously redrawn on screen. This functionality makes Drawable<br>
 * a good standard to use for animation. This is why it is also used by the 2DX animation package to
 * define Graphics related tasks that can be repeatedly called upon. <br>
 * <br>
 * Drawable is often best used with anonymous classes:<br>
 * <code>
 * Background background = new Background(new Drawable() {<br>
 *     public void draw(Graphics2D g2) {<br>
 *         //Graphics2D drawing code here<br>
 *     }<br>
 * }
 * </code>
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */

public interface Drawable {

	public abstract void draw(Graphics2D g2);
}
