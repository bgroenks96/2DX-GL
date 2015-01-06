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

package com.snap2d.sound;

/**
 * @author Brian Groenke
 * 
 */
public class SoundContextException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 389563910801777238L;

    /**
     * 
     */
    public SoundContextException() {

        super();
    }

    /**
     * @param message
     */
    public SoundContextException(final String message) {

        super(message);
    }

    /**
     * @param cause
     */
    public SoundContextException(final Throwable cause) {

        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SoundContextException(final String message, final Throwable cause) {

        super(message, cause);
    }

}
