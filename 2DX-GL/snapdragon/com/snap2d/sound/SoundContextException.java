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
	public SoundContextException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SoundContextException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SoundContextException(String message, Throwable cause) {
		super(message, cause);
	}

}
