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
