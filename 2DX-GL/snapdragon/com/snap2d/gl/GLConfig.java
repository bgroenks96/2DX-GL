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
		TRACE("sun.java2d.trace", null);

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
