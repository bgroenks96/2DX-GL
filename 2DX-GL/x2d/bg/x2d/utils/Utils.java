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

package bg.x2d.utils;

import java.io.*;
import java.net.*;

/**
 * Provides static general utility methods.
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Utils {

	private Utils() {};
	
	/**
	 * Closes the InputStream, catching the exception and returning
	 * false on failure.  In the case that stream is null, this method will
	 * quietly return false.
	 * @param stream InputStream to close; a null value will cause false to be returned
	 * @return true if successful, false if null or otherwise.
	 */
	public static boolean closeStream(InputStream stream) {
		if(stream == null)
			return false;
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Closes the OutputStream, catching the exception and returning
	 * false on failure.  In the case that stream is null, this method will
	 * quietly return false.
	 * @param stream OutputStream to close; a null value will cause false to be returned
	 * @return true if successful, false if null or otherwise.
	 */
	public static boolean closeStream(OutputStream stream) {
		if(stream == null)
			return false;
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Performs the recommended intermediary step of using toURI and then toURL to convert the File object.
	 * The MalformedURLException is caught if thrown and null will be returned.
	 * @param f
	 * @return the URL or null on error.
	 */
	public static URL getFileURL(File f) {
		try {
			return f.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
