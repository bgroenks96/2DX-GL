package bg.x2d;

import java.util.Calendar;

/**
 * A convenience class consisting entirely of static methods regarding system information.  Many of these methods simply mirror the <code>System.getProperty</code> method in
 * Java's standard package.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class Local {

	/**
	 * Uses a simple algorithm to convert Calendar time into standard 24-hour time format.
	 * @return a four digit (usually) integer representing the current time in 24-hour format.
	 */
	public static int hourlyTime() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY) * 100;
		int minutes = cal.get(Calendar.MINUTE);
		int time = hour + minutes;
		return time;
	}

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
