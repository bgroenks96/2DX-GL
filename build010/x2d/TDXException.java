/*
 * Copyright © 2011-2012 Brian Groenke, Private Proprietary Software
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

package bg.x2d;

public class TDXException extends Exception {

	/**
	 * Generic exception thrown by classes in the 2DX software when internal errors occur.  This class should not be extended, nor thrown by objects outside of the 2DX API itself.
	 */
	private static final long serialVersionUID = 975059383220158740L;

	public TDXException() {
		// TODO Auto-generated constructor stub
	}

	public TDXException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TDXException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TDXException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
