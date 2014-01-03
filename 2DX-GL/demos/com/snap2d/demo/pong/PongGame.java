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

/**
 * Demo for Snapdragon2D - implementation of Pong.
 */
package com.snap2d.demo.pong;

import java.awt.*;
import java.awt.event.*;

import bg.x2d.geo.*;
import bg.x2d.physics.PhysicsNode.Collision;

import com.snap2d.gl.*;
import com.snap2d.gl.Display.Type;
import com.snap2d.input.*;
import com.snap2d.world.*;
import com.snap2d.world.event.*;

/**
 * Base class for Pong demo
 * 
 * @author Brian Groenke
 * 
 */
public class PongGame {

	Display disp; // Our Display window object
	RenderControl rc; // Our handle for controlling the rendering engine
	EntityManager em; // Our managing facility for game Entities
	World2D world; // The world in which our game exists...

	InputDispatch input; // Our dispatcher for AWT input events

	Ball ball; // The Ball. We will need to reuse this elsewhere in the class.

	int lscore, rscore;

	private PongGame() {
		input = new InputDispatch(true); // create input-consuming dispatcher
		input.registerKeyClient(new ExitListener());
	}

	public void init() {

		// initialize Display; Type is an inner type of Display
		// the Display size doesn't really matter since we are using full-screen mode
		disp = new Display(800, 600, Type.FULLSCREEN,
				GLConfig.getDefaultSystemConfig());
		disp.setTitle("Snapdragon2D: Pong Demo");

		// obtain a rendering handle from Display
		// we will be using a double buffered render control for this demo
		rc = disp.getRenderControl(2);

		// create a new EntityManager for handling entities and register it as a Renderable task
		em = new EntityManager();
		rc.addRenderable(em, RenderControl.POSITION_LAST);

		Dimension dispSize = disp.getSize();
		// create a new background Renderable for the game using the size of our Display.
		GameBackground background = new GameBackground(dispSize.width,
				dispSize.height);

		// add the background as a Renderable. Note how it's added at position 0 because
		// the background should be the FIRST thing rendered.
		rc.addRenderable(background, 0);

		// create a new World where the origin is the center of the screen.
		// we will map pixels to world units 1:1 because there is a limited amount of physics or
		// realistic simulation we have to deal with in this demo.
		world = new World2D(-dispSize.width / 2.0, dispSize.height / 2.0,
				dispSize.width, dispSize.height, 1);

		// now we start adding our entities.
		// I have chosen for this simple game to manage their initial locations here....
		// but this can easily be done within the Entity's constructor.
		// add the ball first, because that's where we're assigning our listener
		PointLD ballStart = world.screenToWorld(dispSize.width / 2,
				dispSize.height / 2);
		ball = new Ball(ballStart, world, new ScoreListener());
		em.register(ball, new BallCollisionListener());

		Paddle p1 = new Paddle(world.screenToWorld(dispSize.width
				- Paddle.PADDLE_SIZE.width - 10, 0, Paddle.PADDLE_SIZE.height), world, input, true);
		Paddle p2 = new Paddle(world.screenToWorld(10, 0, Paddle.PADDLE_SIZE.height), world, input, false);
		em.register(p1);
		em.register(p2);

		// show the Display and tell the rendering handle to start the game loop.
		// typically the Display should be shown first to ensure everything is ready to start
		// rendering.
		disp.show();
		rc.startRenderLoop();
	}

	/*
	 * Listens for ball-out-of-bounds events from the Ball.
	 */
	private class ScoreListener implements Ball.ExitBoundsListener {

		/**
		 *
		 */
		@Override
		public void outOfBounds(Ball b) {
			if (b.getScreenX() <= 0) {
				rscore++;
			} else {
				lscore++;
			}
			b.setWorldLoc(0, 0);
		}

	}

	/*
	 * Triggers a physics collision when the Ball collides with a Paddle.
	 */
	private class BallCollisionListener implements EntityListener {

		/**
		 *
		 */
		@Override
		public void onCollision(CollisionEvent collEvt) {
			ball.getPhysics().collide(1, 0, Collision.Y);
		}

		/**
		 *
		 */
		@Override
		public void onAdd(AddEvent addEvt) {
			// we don't need to use these facilities for this demo
		}

		/**
		 *
		 */
		@Override
		public void onRemove(RemoveEvent remEvt) {
			//
		}
	}

	private class GameBackground implements Renderable {

		int wt, ht;

		final Scale sx, sy;

		final int SCORE_Y, SCORE_X;
		final String SCORE_STR = "Score: ";

		GameBackground(int wt, int ht) {
			this.wt = wt;
			this.ht = ht;

			// create a roughly good enough scale for our score string locations.
			sx = new Scale(wt / 1920.0);
			sy = new Scale(ht / 1080.0);
			SCORE_Y = sy.scale(100);
			SCORE_X = sx.scale(200);
		}

		/**
		 *
		 */
		@Override
		public void render(Graphics2D g, float interpolation) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, wt, ht);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, sy.scale(48)));
			int rwt = (int) g.getFontMetrics()
					.getStringBounds(SCORE_STR + rscore, g).getWidth();
			g.drawString(SCORE_STR + lscore, SCORE_X, SCORE_Y);
			g.drawString(SCORE_STR + rscore, wt - SCORE_X - rwt, SCORE_Y);
		}

		/**
		 *
		 */
		@Override
		public void update(long nanoTimeNow, long nanosSinceLastUpdate) {
			// there is no need to update the background
		}

		/**
		 *
		 */
		@Override
		public void onResize(Dimension oldSize, Dimension newSize) {
			// reset the width/height variables
			wt = newSize.width;
			ht = newSize.height;
			world.setLocation(-newSize.width / 2, newSize.height / 2);
			world.setViewSize(newSize.width, newSize.height, 1);
		}

	}

	private class ExitListener implements KeyEventClient {

		/**
		 *
		 */
		@Override
		public void processKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED
					&& e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				disp.dispose(); // fully dispose our Display on exit
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {
		PongGame game = new PongGame();
		game.init();
	}
}
