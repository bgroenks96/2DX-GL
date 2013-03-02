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

package com.snap2d.demo.worldgame;

import java.awt.*;
import java.awt.event.*;

import bg.x2d.gen.*;
import bg.x2d.geo.*;

import com.snap2d.gl.*;
import com.snap2d.gl.Display.Type;
import com.snap2d.input.*;

/**
 * Demo "game" for showing how to manage world and world scrolling with character
 * movement effectively.
 * @author Brian Groenke
 *
 */
public class WorldGame {
	
	public static final int WORLD_MIN_X = -5000, WORLD_MAX_Y = 5000, WORLD_WT = 10000, WORLD_HT = 10000, 
			ENTITY_NUMBER = 1;
	
	Display disp;
	RenderControl rc;
	ScrollWorld world;

	InputDispatch input;
	
	private WorldGame() {
		input = new InputDispatch(true);
		input.registerKeyClient(new ExitListener());
	}
	
	public void init() {
		disp = new Display(800, 600, Type.FULLSCREEN, GLConfig.getDefaultSystemConfig());
		disp.setTitle("Snapdragon2D: Game World Demo");
		
		rc = disp.getRenderControl(2);
		rc.addRenderable(new StaticBackground(), 0);
		
		world = new ScrollWorld(WORLD_MIN_X, WORLD_MAX_Y, WORLD_WT, WORLD_HT, 2);
		generateRandomEntities();
		rc.addRenderable(world, RenderControl.POSITION_LAST);
		
		disp.show();
		rc.startRenderLoop();
	}
	
	private void generateRandomEntities() {
		PointGenerator rand = new PointGenerator(WORLD_MIN_X, WORLD_MAX_Y - WORLD_HT, WORLD_MIN_X + WORLD_WT, WORLD_MAX_Y);
		for(int i = 0; i < ENTITY_NUMBER; i++) {
			PointLD pos = rand.generate();
			GenericEntity ge = new GenericEntity(pos, world);
			world.addEntity(ge);
		}
	}
	
	private class StaticBackground implements Renderable {

		/**
		 *
		 */
		@Override
		public void render(Graphics2D g, float interpolation) {
			g.setColor(Color.GRAY);
			g.fillRect(0, 0, disp.getSize().width, disp.getSize().height);
		}

		/**
		 *
		 */
		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			//
		}

		/**
		 *
		 */
		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			//
		}
		
	}
	
	private class ExitListener implements KeyEventClient {

		/**
		 *
		 */
		@Override
		public void processKeyEvent(KeyEvent e) {
			if(e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				disp.dispose(); // fully dispose our Display on exit
				System.exit(0);
			}
		}
	}
	
	public static void main(String[] args) {
		WorldGame game = new WorldGame();
		game.init();
	}
}
