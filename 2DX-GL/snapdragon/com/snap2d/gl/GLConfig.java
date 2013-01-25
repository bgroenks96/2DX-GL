/*
 *  Copyright © 2011-2012 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl;

import java.util.*;

import bg.x2d.*;

/**
 * Object that represents configuration data for Java2D rendering.
 * @author Brian Groenke
 *
 */
public class GLConfig {

	HashMap<Property, String> configMap = new HashMap<Property, String>();

	public GLConfig() {
		for(Property p:Property.values())
			configMap.put(p, p.defValue);
	}
	
	/**
	 * Creates a GLConfig optimized to defaults appropriate for the current system.
	 * If the underlying OS is Windows, the default Property values are changed to take
	 * advantage of Windows native D3D systems over OpenGL and set other Windows-specific
	 * configurations.  Otherwise, the default Property values are used (they are, by default,
	 * more optimized for Linux/Solaris/Macintosh systems).
	 * @return
	 */
	public static GLConfig getDefaultSystemConfig() {
		if(Local.getPlatform().toLowerCase().contains("windows")) {
			GLConfig config = new GLConfig();
			config.set(Property.USE_D3D, "true");
			config.set(Property.NO_DDRAW, "false");
			config.set(Property.USE_OPENGL, "false");
			config.set(Property.ACC_THRESHOLD, "0");
			return config;
		} else
			return new GLConfig();
	}

	/**
	 * Sets the system configuration property for Java graphics rendering.
	 * @param property Property to set
	 * @param value the String value for the property; ignores null values
	 */
	public void set(Property property, String value) {
		if(value == null)
			return;
		configMap.put(property, value);
	}

	/**
	 * Package-only method to apply set properties.
	 */
	void apply() {
		for(Property f:Property.values()) {
			f.applyProperty(configMap);
		}
	}

	/**
	 * Properties used by Java2D (configurable at start or by System.setProperty).
	 * Note that not all properties may be supported on every platform.
	 * See http://docs.oracle.com/javase/1.5.0/docs/guide/2d/flags.html for more info.
	 * @author Brian Groenke
	 *
	 */
	public enum Property {
		/**
		 * Boolean (default=true)
		 */
		USE_OPENGL("sun.java2d.opengl", "true"), 
		/**
		 * Boolean (default=false) * MS-Windows only *
		 */
		USE_D3D("sun.java2d.d3d", "false"), 
		/**
		 * Boolean (default=true)  * MS-Windows only *
		 */
		NO_DDRAW("sun.java2d.noddraw", "true"), 
		/**
		 * Integer (default=null)  * MS-Windows only *
		 */
		ACC_THRESHOLD("sun.java2d.accthreshold", null),

		/**
		 * String - list form - (default=null)
		 */
		TRACE("sun.java2d.trace", null),
		
		/**
		 * Boolean (default=false)  * Linux/Solaris only *
		 */
		PM_OFF_SCREEN("sun.java2d.pmoffscreen", "false"),
		
		/**
		 * Boolean (default=true)
		 */
		SNAP2D_WINDOWS_HIGH_RES_TIMER("com.snap2d.gl.force_timer", "true");

		private String property, defValue;

		Property(String property, String defValue) {
			this.property = property;
			this.defValue = defValue;
		}

		private void applyProperty(HashMap<Property, String> config) {
			String value = config.get(this);
			if(value !=null)
				System.setProperty(property, value);
		}
	}
}
