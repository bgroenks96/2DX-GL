/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.geo;

import bg.x2d.*;

public class GeoException extends TDXException {

	/**
	 * The standard exception thrown by all classes in the bg.tdx.geo package when a Geometric error
	 * occurs (often caused by invalid arguments passed to a geometry method).<br>
	 * This class does nothing but subclass type TDXException and slap its own name onto it.
	 */
	private static final long serialVersionUID = 975059383220158740L;

	public GeoException() {
		super("Geometric error");
	}

	public GeoException(String arg0) {
		super(arg0);
	}

	public GeoException(Throwable arg0) {
		super(arg0);
	}

	public GeoException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
