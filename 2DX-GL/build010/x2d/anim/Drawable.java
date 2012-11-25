/*
 * Copyright © 2011-2012 Brian Groenke, Private Proprietary Software
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

package bg.x2d.anim;

import java.awt.Graphics2D;

/**
 * A Drawable is an object that defines a specific image to be drawn with each call to its <code>draw(Graphics2D)</code> method.
 * <br>
 * Drawable is used by the Background object (bg.tdx.Background) to set an image that can be continuously redrawn on screen.  This functionality makes Drawable<br>
 * a good standard to use for animation.  This is why it is also used by the 2DX animation package to define Graphics related tasks that can be repeatedly called upon.
 * <br><br>
 * Drawable is often best used with anonymous classes:<br>
 * <code>
 * Background background = new Background(new Drawable() {<br>
 *     public void draw(Graphics2D g2) {<br>
 *         //Graphics2D drawing code here<br>
 *     }<br>
 * }
 * </code>
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */

public interface Drawable {
	
	public abstract void draw(Graphics2D g2);
}
