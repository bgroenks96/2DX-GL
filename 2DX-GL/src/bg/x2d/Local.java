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

/**
 * 2DX home package containing generic utility classes and base API elements.
 */
package bg.x2d;

import java.util.Calendar;

/**
 * A convenience class consisting entirely of static methods regarding system
 * information. Many of these methods simply mirror the
 * <code>System.getProperty</code> method in Java's standard package.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class Local {

	/**
	 * Uses a simple algorithm to convert Calendar time into standard 24-hour
	 * time format.
	 * 
	 * @return a four digit (usually) integer representing the current time in
	 *         24-hour format.
	 */
	public static int hourlyTime() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY) * 100;
		int minutes = cal.get(Calendar.MINUTE);
		int time = hour + minutes;
		return time;
	}

	@Deprecated
	/**
	 * This method has little if any significant benefit over the java.lang.System class method currentTimeMillis()
	 */
	public static long timeInMillis() {
		long millis = System.currentTimeMillis();
		return millis;
	}

	public static String getPlatform() {
		return System.getProperty("os.name");
	}

	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}

	public static String getUserHome() {
		return System.getProperty("user.home");
	}

	public static String getWorkingDir() {
		return System.getProperty("user.dir");
	}
}
