/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.opengl.res;

import java.io.IOException;

import com.snap2d.gl.Display.Type;
import com.snap2d.gl.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class ShaderTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GLDisplay disp = new GLDisplay(1200, 900, Type.WINDOWED, GLConfig.getDefaultSystemConfig());
		final GLRenderControl glrc = disp.getRenderControl();
		glrc.addRenderable(new GLRenderable() {
			
			GLProgram prog;
			int quadBuff;

			@Override
			public void init(GLHandle handle) {
				prog = new GLProgram();
				try {
					GLShader vert = GLShader.loadLibraryShader(GLShader.TYPE_VERTEX, "snap2d-default.vert");
					GLShader frag = GLShader.loadLibraryShader(GLShader.TYPE_FRAGMENT, "snap2d-default.frag");
					prog.attachShader(vert);
					prog.attachShader(frag);
					if(!prog.link()) {
						prog.printLinkLog();
						System.exit(1);
					}
				} catch (GLShaderException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				quadBuff = handle.createQuadBuffer2f(BufferUsage.STATIC_DRAW, 1, false);
				handle.setColor4f(0.6f, 0.2f, 0.8f, 1);
				handle.putQuad2f(quadBuff, 100, 100, 500, 500, null);
			}

			@Override
			public void dispose(GLHandle handle) {
				
			}

			@Override
			public void render(GLHandle handle, float interpolation) {
				prog.enable();
				handle.draw2f(quadBuff);
				prog.disable();
			}

			@Override
			public void update(long nanoTimeNow, long nanoTimeLast) {
				
			}

			@Override
			public void resize(GLHandle handle, int wt, int ht) {
				prog.enable();
				handle.setViewport(0, 0, wt, ht, 1);
				prog.disable();
			}
			
		}, 0);
		glrc.setVSync(true);
		disp.centerOnScreen();
		disp.show();
		glrc.startRenderLoop();
	}
}
