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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

/**
 * OpenGL Graphics Library Config - specifies configuration options for the
 * Snap2D OpenGL rendering engine (powered by JOGL 2.0). GLConfig loads default
 * values from existing Java system property values (creating them if they have
 * not yet been set) and stores its current configuration internally in a map.
 * <b>This means GLConfig will default to whatever values are currently set via
 * System.getProperty/setProperty.</b> You will need to explicitly call
 * {@link #set(Property, String)} on the GLConfig to change any values that have
 * been modified via system properties (i.e. the System class calls or through a
 * VM -D property value flag).
 * 
 * @author Brian Groenke
 *
 */
public class GLConfig {

    public static final String GL_PROFILE_HIGHEST = "max", GL_PROFILE_LOWEST = "min";

    private static final String[] GL_AVAILABLE_PROFILES;

    static {
        final String[] checkProfiles = new String[] { "GL4", "GL3", "GL2", "GL2ES2", "GL2ES1" };
        List<String> avail = new ArrayList<String>();
        for (int i = 0; i < checkProfiles.length; i++ ) {
            if (GLProfile.isAvailable(checkProfiles[i])) {
                avail.add(checkProfiles[i]);
            }
        }
        GL_AVAILABLE_PROFILES = new String[avail.size()];
        avail.toArray(GL_AVAILABLE_PROFILES);
    }

    HashMap<Property, String> configMap = new HashMap<Property, String>();

    public GLConfig() {

        for (Property p : Property.values()) {
            configMap.put(p, System.getProperty(p.property));
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
     * @return a list of OpenGL profile names applicable to Property.GL_PROFILE
     */
    public static String[] getAvailableProfiles() {

        return GL_AVAILABLE_PROFILES;
    }

    /**
     * Sets the system configuration property for Snap2D/OpenGL graphics
     * rendering.<br/>
     * 
     * @param property
     *            Property to set
     * @param value
     *            the String value for the property; ignores null values
     */
    public void set(final Property property, final String value) {

        if (value == null) {
            return;
        }

        configMap.put(property, value);
    }

    public String get(final Property property) {

        return configMap.get(property);
    }

    public boolean getAsBool(final Property property) {

        return Boolean.parseBoolean(get(property));
    }

    public int getAsInt(final Property property) {

        return Integer.parseInt(get(property));
    }

    /**
     * Properties used by Java2D (configurable at start or by
     * System.setProperty). Note that not all properties may be supported on
     * every platform. See
     * http://docs.oracle.com/javase/1.5.0/docs/guide/2d/flags.html for more
     * info. <br/>
     * <br/>
     * Take note that most of these properties may only be applied upon the
     * initial creation of the active GLDisplay.
     * 
     * @author Brian Groenke
     * 
     */
    public enum Property {

        /**
         * Boolean (default=true) * MS-Windows only * <br/>
         * <br/>
         * Sets whether or not Snap2D should try to force Windows to use a high
         * resolution timer by starting an indefinite sleeping thread.
         */
        SNAP2D_WINDOWS_HIGH_RES_TIMER("snap2d.gl.force_timer", "true"),

        /**
         * Boolean (default=true) <br/>
         * <br/>
         * Sets whether or not FPS and TPS will be printed to stdout each
         * second.
         */
        SNAP2D_PRINT_GLRENDER_STAT("snap2d.gl.opengl.printframes", "true"),

        /**
         * Boolean (default=true) <br/>
         * <br/>
         * If true, initialization and modifications to the OpenGL rendering
         * engine configuration will be printed to stdout.
         */
        SNAP2D_PRINT_GL_CONFIG("snap2d.gl.opengl.printconfig", "true"),

        /**
         * String (default=max) Specify which OpenGL version profile Snap2D
         * should attempt to load when initializing the renderer. "max" or "min"
         * can be specified to favor the highest or lowest available GL
         * rendering profile respectively. You may also specify a particular
         * profile by name, although whether or not it is used depends on what
         * is provided by the user's underlying hardware. Values for common GL
         * versions: OpenGL 2.1-3.0 = "GL2" OpenGL 3.1-3.3 = "GL3" OpenGL
         * 4.0-4.4 = "GL4" OpenGL ES 1.0 = "GL2ES1" OpenGL ES 2.0 = "GL2ES2"
         * Values for other JOGL profiles can be found under JOGL's
         * documentation, although it is not guaranteed that Snap2D will
         * function correctly with all of them.
         * 
         * @see <a
         *      href="https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/constant-values.html#javax.media.opengl.GLProfile.GL2">JOGL
         *      GLProfile Constant Values</a>
         */
        GL_PROFILE("snap2d.gl.opengl.use_profile", GL_PROFILE_HIGHEST),

        /**
         * Boolean (default=false if OpenGL core profile available, true
         * otherwise) <br/>
         * <br/>
         * If true, Snap2D will use fixed function pipeline for compatibility
         * with older OpenGL profiles.
         */
        GL_RENDER_COMPAT("snap2d.gl.opengl.compat", "false"),

        /**
         * Integer (default=4) <br/>
         * <br/>
         * If value > 0, GLDisplay will create a rendering environment with
         * multisampled anti-aliasing (MSAA) enabled, using the value as the
         * number of samples. Typically, MSAA samples are 2x, 4x, or 8x (given
         * value should be 2, 4, 8, respectively).
         */
        GL_RENDER_MSAA("snap2d.gl.opengl.msaa", "4"),

        /**
         * Boolean (default=true) <br/>
         * <br/>
         * If true, Snap2D will enable smoothing for text rendering. A few
         * graphics cards may not behave well with this option enabled - set
         * false to disable.
         */
        GL_RENDER_TEXT_SMOOTH("snap2d.gl.opengl.text_smooth", "true"),

        /**
         * Boolean (default=true) <br/>
         * <br/>
         * If true, Snap2D will enable mipmap generation for text rendering.
         * This may help text appear smoother when scaled down.
         */
        GL_RENDER_TEXT_MIPMAP("snap2d.gl.opengl.text_aa", "true"),

        /**
         * Boolean (default=true) <br/>
         * <br/>
         * If true, Snap2D will use vertex arrays for text rendering. A few
         * graphics cards may not behave well with this option enabled - set
         * false to disable.
         */
        GL_RENDER_TEXT_USE_VAO("snap2d.gl.opengl.text_vao", "true");

        private String property, defValue;

        Property(final String property, final String defValue) {

            this.property = property;
            this.defValue = defValue;
            String preset = System.getProperty(property);
            System.setProperty(property, (preset == null) ? defValue : preset);
        }

        public String getProperty() {

            return property;
        }

        public String getValue() {

            return defValue;
        }
    }

    public static class GLConfigException extends GLException {

        private static final long serialVersionUID = 8598185228456538034L;

        public GLConfigException(final String msg) {

            super(msg);
        }

        public GLConfigException(final String msg, final Throwable t) {

            super(msg, t);
        }
    }

    /*
     * Package only function for loading JOGL GLProfile according to current
     * user configuration.
     */
    static GLProfile loadGLProfile(final GLConfig config) throws GLConfigException {

        String profile = config.get(Property.GL_PROFILE);
        try {
            if (profile.equalsIgnoreCase(GL_PROFILE_HIGHEST)) {
                return GLProfile.getMaxFixedFunc(true);
            } else if (profile.equalsIgnoreCase(GL_PROFILE_LOWEST)) {
                return GLProfile.getMinimum(true);
            } else {
                return GLProfile.get(profile);
            }
        } catch (GLException gle) {
            throw new GLConfigException("failed to laod GL profile: " + profile, gle);
        }
    }
}
