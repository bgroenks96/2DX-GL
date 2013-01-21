/*
 * Copyright © 2011-2012 Brian Groenke
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
		 * True/false (default=true)
		 */
		USE_OPENGL("sun.java2d.opengl", "true"), 
		/**
		 * True/false (default=false)
		 */
		USE_D3D("sun.java2d.d3d", "false"), 
		/**
		 * True/false (default=true)
		 */
		NO_DDRAW("sun.java2d.noddraw", "true"), 
		/**
		 * Integer (default=null)
		 */
		ACC_THRESHOLD("sun.java2d.accthreshold", null),

		/**
		 * String - list form - (default=null)
		 */
		TRACE("sun.java2d.trace", null),
		
		/**
		 * True/false (default=true)
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
