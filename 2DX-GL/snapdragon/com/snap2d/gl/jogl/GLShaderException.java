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

import javax.media.opengl.*;

/**
 * @author Brian Groenke
 *
 */
public class GLShaderException extends GLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3619530090208304972L;
	
	GLShader assoc;
	String extMsg;
	long timestamp;
	
	public GLShaderException(String msg, GLShader assoc, String extMsg, long timeAtThrow) {
		super(msg);
		this.assoc = assoc;
		this.extMsg = extMsg;
		this.timestamp = timeAtThrow;
		
	}
	
	/**
	 * Returns the GLShader object that threw this exception or caused this exception to be thrown.
	 * @return
	 */
	public GLShader getErrorSource() {
		return assoc;
	}
	
	/**
	 * @return the extended report of this error (if one exists) i.e. shader compilation error
	 */
	public String getExtendedMessage() {
		return extMsg;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
