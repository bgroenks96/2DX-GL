/*
 *  Copyright © 2011-2012 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
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
