/*
 *  Copyright © 2012-2013 Brian Groenke
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

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class JOGLConfig {
	HashMap<Property, String> configMap = new HashMap<Property, String>();
	HashMap<RenderOp, Object> renderOps = new HashMap<RenderOp, Object>();

	public JOGLConfig() {
		for (Property p : Property.values()) {
			configMap.put(p, p.defValue);
		}
		
		for(RenderOp op : RenderOp.values()) {
			renderOps.put(op, op.defValue);
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
	
	public void setRenderingOption(RenderOp op, Object value) {
		if(value == null)
			return;
		renderOps.put(op, value);
	}
	
	public Object getValue(RenderOp op) {
		return renderOps.get(op);
	}

	/**
	 * Package-only method to apply set properties.
	 */
	void applyProperties() {
		for (Property f : Property.values()) {
			f.applyProperty(configMap);
		}
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
	
	public static final Object TEX_FILTER_NEAREST = GL.GL_NEAREST, TEX_FILTER_LINEAR = GL.GL_LINEAR,
			AA_MULTISAMPLE = GL2.GL_MULTISAMPLE, AA_DISABLE = 0, MSAA_NV_HINT_QUALITY = GL.GL_NICEST, MSAA_NV_HINT_SPEED = GL.GL_FASTEST;
	
	/**
	 * Rendering options checked by the Snapdragon JOGL rendering engine.  Changes to these settings will be applied in the next render cycle.
	 * @author Brian Groenke
	 */
	public enum RenderOp {
		
		/**
		 * Setting for texture filtering quality:<br/>
		 * TEX_FILTER_NEAREST = nearest neighbor interpolation - best performance<br/>
		 * TEX_FILTER_LINEAR = linear interpolation - best quality<br/>
		 * Default value = TEX_FILTER_LINEAR
		 */
		TEXTURE_FILTERING("GL_TEXTURE_FILTERING", TEX_FILTER_LINEAR), 
		
		/**
		 * Setting for anti-aliasing mode:<br/>
		 * AA_MULTISAMPLE = enable MSAA as configured by the driver<br/>
		 * AA_DISABLE = no anti-aliasing<br/>
		 * Default value = AA_DISABLE
		 */
		ANTI_ALIASING_MODE("GL_ANTIALIAS", AA_DISABLE), 
		
		/**
		 * Setting for NVIDIA drivers only:<br/>
		 * MSAA_NV_HINT_QUALITY = 4xMSAA Best anti-aliasing quality<br/>
		 * MSAA_NV_HINT_SPEED = 2xMSAA Best anti-aliasing performance<br/>
		 * Default value = MSAA_NV_HINT_SPEED
		 */
		MSAA_FILTER_HINT_NV("GL_MSAA_QUALITY", MSAA_NV_HINT_SPEED);
		
		private String name;
		private Object defValue;
		
		RenderOp(String name, Object value) {
			this.name = name;
			this.defValue = value;
		}
	}
}
