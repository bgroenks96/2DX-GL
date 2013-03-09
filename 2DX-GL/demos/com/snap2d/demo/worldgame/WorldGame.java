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
			ENTITY_NUMBER = 2500, SCROLL_TICK = 20;

	Display disp;
	RenderControl rc;
	ScrollWorld world;

	InputDispatch input;

	boolean up, down, left, right;

	private WorldGame() {
		input = new InputDispatch(true);
		input.registerKeyClient(new InputListener());
	}

	public void init() {
		disp = new Display(800, 600, Type.FULLSCREEN, GLConfig.getDefaultSystemConfig());
		disp.setTitle("Snapdragon2D: Game World Demo");

		rc = disp.getRenderControl(2);
		rc.addRenderable(new StaticBackground(), 0);

		world = new ScrollWorld(-200, 150, disp.getSize().width, disp.getSize().height, 2);
		generateRandomEntities();
		rc.addRenderable(new WorldUpdater(), RenderControl.POSITION_LAST);

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

	private class WorldUpdater implements Renderable {

		/**
		 *
		 */
		@Override
		public void render(Graphics2D g, float interpolation) {
			world.render(g, interpolation);
		}

		/**
		 *
		 */
		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			if(up) {
				world.moveViewport(0, SCROLL_TICK);
			} else if(down) {
				world.moveViewport(0, -SCROLL_TICK);
			}

			if(right) {
				world.moveViewport(SCROLL_TICK, 0);
			} else if(left) {
				world.moveViewport(-SCROLL_TICK, 0);
			}

			if(world.getX() < WORLD_MIN_X)
				world.setViewport(WORLD_MIN_X, world.getY(), world.getViewWidth(), world.getViewHeight());
			if(world.getX() > WORLD_MIN_X + WORLD_HT)
				world.setViewport(WORLD_MIN_X + WORLD_WT, world.getY(), world.getViewWidth(), world.getViewHeight());
			if(world.getY() > WORLD_MAX_Y)
				world.setViewport(world.getX(), WORLD_MAX_Y, world.getViewWidth(), world.getViewHeight());
			if(world.getY() < WORLD_MAX_Y - WORLD_HT)
				world.setViewport(world.getX(), WORLD_MAX_Y - WORLD_HT, world.getViewWidth(), world.getViewHeight());

			world.update(nanoTimeNow, nanosSinceLastUpdate);
		}

		/**
		 *
		 */
		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {

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

	private class InputListener implements KeyEventClient {

		/**
		 *
		 */
		@Override
		public void processKeyEvent(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_W:
				if(e.getID() == KeyEvent.KEY_PRESSED)
					up = true;
				else if(e.getID() == KeyEvent.KEY_RELEASED)
					up = false;
				break;
			case KeyEvent.VK_S:
				if(e.getID() == KeyEvent.KEY_PRESSED)
					down = true;
				else if(e.getID() == KeyEvent.KEY_RELEASED)
					down = false;
				break;
			case KeyEvent.VK_A:
				if(e.getID() == KeyEvent.KEY_PRESSED)
					left = true;
				else if(e.getID() == KeyEvent.KEY_RELEASED)
					left = false;
				break;
			case KeyEvent.VK_D:
				if(e.getID() == KeyEvent.KEY_PRESSED)
					right = true;
				else if(e.getID() == KeyEvent.KEY_RELEASED)
					right = false;
				break;
			case KeyEvent.VK_ESCAPE:
				if(e.getID() == KeyEvent.KEY_PRESSED) {
					disp.dispose(); // fully dispose of the Display before exiting
					System.exit(0);
				}
			}
		}

	}

	public static void main(String[] args) {
		WorldGame game = new WorldGame();
		game.init();
	}
}
