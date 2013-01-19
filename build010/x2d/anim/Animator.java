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
 * Defines a standard for animation engines inside and outside of the 2DX library.<br>
 * Note: Java 5.0 or higher required as the java.util.concurrent class is utilized.
 *@since 2DX 1.0 (1st Edition)
 */

public interface Animator {

	public void drawFrame(Graphics2D g);
}
