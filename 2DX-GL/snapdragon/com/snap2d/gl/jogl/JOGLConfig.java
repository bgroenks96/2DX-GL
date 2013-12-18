/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl;

import java.util.*;

/**
 * @author Brian Groenke
 *
 */
public class JOGLConfig {
	HashMap<Property, String> configMap = new HashMap<Property, String>();

	public JOGLConfig() {
		for (Property p : Property.values()) {
			configMap.put(p, p.defValue);
		}
	}

	/**
	 * @return the default OpenGL configuration for the system
	 */
	public static JOGLConfig getDefaultSystemConfig() {
		JOGLConfig config = new JOGLConfig();
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

	/**
	 * Package-only method to apply set properties.
	 */
	void apply() {
		for (Property f : Property.values()) {
			f.applyProperty(configMap);
		}
	}

	/**
	 * Properties used by Java2D (configurable at start or by System.setProperty). Note that not all
	 * properties may be supported on every platform. See
	 * http://docs.oracle.com/javase/1.5.0/docs/guide/2d/flags.html for more info.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	public enum Property {
		/**
		 * Boolean (default=true) * MS-Windows only *
		 */
		NO_DDRAW("sun.java2d.noddraw", "true"),
		
		/**
		 * Boolean (default=true)
		 */
		NO_ERASE_BACKGROUND("sun.awt.noerasebackground", "true"),
		
		/**
		 * Boolean (default=true) * MS-Windows only *
		 * <br/><br/>
		 * Sets whether or not Snapdragon2D should try to force Windows to use a
		 * high resolution timer by starting an indefinite sleeping thread.
		 */
		SNAP2D_WINDOWS_HIGH_RES_TIMER("com.snap2d.gl.force_timer", "true"),

		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * Sets whether or not FPS and TPS will be printed to stdout on each frame.
		 */
		SNAP2D_PRINT_RENDER_STAT("com.snap2d.gl.printframes", "true"),
		
		/**
		 * Boolean (default=true)
		 * <br/><br/>
		 * If true, initialization and modifications to the OpenGL rendering engine
		 * configuration will be printed to stdout.
		 */
		SNAP2D_PRINT_GL_CONFIG("com.snap2d.gl.jogl.printconfig", "true");

		private String property, defValue;

		Property(String property, String defValue) {
			this.property = property;
			this.defValue = defValue;
		}

		private void applyProperty(HashMap<Property, String> config) {
			String value = config.get(this);
			if (value != null) {
				System.setProperty(property, value);
			}
		}
		
		public String getProperty() {
			return property;
		}
		
		public String getValue() {
			return defValue;
		}
	}
}
