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

package com.snap2d.gl.opengl;

import java.util.*;

/**
 * OpenGL Graphics Library Config - specifies configuration options for the Snap2D
 * OpenGL rendering engine (powered by JOGL 2.0).
 * @author Brian Groenke
 *
 */
public class GLConfig {
	HashMap<Property, String> configMap = new HashMap<Property, String>();

	public GLConfig() {
		for (Property p : Property.values()) {
			configMap.put(p, p.defValue);
		}
	}

	/**
	 * @return the default OpenGL configuration for the system
	 */
	public static GLConfig getDefaultSystemConfig() {
		GLConfig config = new GLConfig();
		return config;
	}

	/**
	 * Sets the system configuration property for Java graphics rendering.
	 * 
	 * @param property
	 *            Property to set
	 * @param value
	 *            the String value for the property; ignores null values
	 */
	public void set(Property property, String value) {
		if (value == null) {
			return;
		}
		configMap.put(property, value);
	}
	
	public String get(Property property) {
		return configMap.get(property);
	}
	
	public boolean getAsBool(Property property) {
		return Boolean.parseBoolean(get(property));
	}
	
	public int getAsInt(Property property) {
		return Integer.parseInt(get(property));
	}

	/**
	 * Properties used by Java2D (configurable at start or by System.setProperty). Note that not all
	 * properties may be supported on every platform. See
	 * http://docs.oracle.com/javase/1.5.0/docs/guide/2d/flags.html for more info.
	 * <br/><br/>
	 * Take note that most of these properties may only be applied upon the initial creation of the active
	 * GLDisplay.
	 * @author Brian Groenke
	 * 
	 */
	public enum Property {
		
		/**
		 * Boolean (default=true) * MS-Windows only *
		 * <br/><br/>
		 * Sets whether or not Snap2D should try to force Windows to use a
		 * high resolution timer by starting an indefinite sleeping thread.
		 */
		SNAP2D_WINDOWS_HIGH_RES_TIMER("com.snap2d.gl.force_timer", "true"),

		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * Sets whether or not FPS and TPS will be printed to stdout each second.
		 */
		SNAP2D_PRINT_GLRENDER_STAT("com.snap2d.gl.opengl.printframes", "true"),
		
		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * If true, initialization and modifications to the OpenGL rendering engine
		 * configuration will be printed to stdout.
		 */
		SNAP2D_PRINT_GL_CONFIG("com.snap2d.gl.opengl.printconfig", "true"),
		
		/**
		 * Integer (default=4)
		 * <br/><br/>
		 * If value > 0, GLDisplay will create a rendering environment with multisampled
		 * anti-aliasing (MSAA) enabled, using the value as the number of samples.  Typically, MSAA
		 * samples are 2x, 4x, or 8x (given value should be 2, 4, 8, respectively).
		 */
		GL_RENDER_MSAA("com.snap2d.gl.opengl.msaa", "4"),
		
		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * If true, Snap2D will enable smoothing for text rendering.  A few graphics
		 * cards may not behave well with this option enabled - set false to disable.
		 */
		GL_RENDER_TEXT_SMOOTH("com.snap2d.gl.opengl.text_smooth", "true"),
		
		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * If true, Snap2D will enable mipmap generation for text rendering.  This
		 * may help text appear smoother when scaled down.
		 */
		GL_RENDER_TEXT_MIPMAP("com.snap2d.gl.opengl.text_aa", "true"),
		
		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * If true, Snap2D will use vertex arrays for text rendering.  A few graphics
		 * cards may not behave well with this option enabled - set false to disable.
		 */
		GL_RENDER_TEXT_USE_VAO("com.snap2d.gl.opengl.text_vao", "true");

		private String property, defValue;

		Property(String property, String defValue) {
			this.property = property;
			this.defValue = defValue;
			System.setProperty(property, defValue);
		}
		
		public String getProperty() {
			return property;
		}
		
		public String getValue() {
			return defValue;
		}
	}
}
