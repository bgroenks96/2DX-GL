/*
 * Copyright � 2011-2012 Brian Groenke
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

package bg.x2d.geo;

import bg.x2d.TDXException;

public class GeoException extends TDXException {

	/**
	 * The standard exception thrown by all classes in the bg.tdx.geo package
	 * when a Geometric error occurs (often caused by invalid arguments passed
	 * to a geometry method).<br>
	 * This class does nothing but subclass type TDXException and slap its own
	 * name onto it.
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
