/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

/**
 * Demo for Snapdragon2D - implementation of Pong.  Currently a work in progress.
 */
package com.snap2d.demo.pong;

import com.snap2d.gl.*;
import com.snap2d.gl.Display.Type;

/**
 * @author Brian Groenke
 *
 */
public class PongGame {
	
	Display disp;        // Our Display window object
	RenderControl rc;    // Our handle for controlling the rendering engine
	
	public void init() {
		
		// initialize Display; Type is an inner type of Display
		disp = new Display(1, 1, Type.FULLSCREEN);
		
		// obtain a rendering handle from Display
		// we will be using a double buffered render control for this demo
		rc = disp.getRenderControl(2);
	}
	
	public static void main(String[] args) {
		PongGame game = new PongGame();
		game.init();
	}
}
