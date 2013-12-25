/*
 * Copyright � 2011-2013 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

/**
 * 2DX home package containing generic utility classes and base API elements.
 */
package bg.x2d;

import java.io.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import bg.x2d.utils.*;

/**
 * A convenience class consisting entirely of static methods regarding system information. Many of
 * these methods simply mirror the <code>System.getProperty</code> method in Java's standard
 * package.
 * 
 * This class also deals with how 2DX loads and handles native libraries. By default, 2DX loads
 * native code from the natives JAR file in the classpath. Inside of this JAR, each platform
 * specific directory must exist in order to be counted as supported. Any platforms which do not
 * have an existing directory within the native library location will not be listed as supported by
 * the 2DX-GL software distribution.
 * 
 * The native library location shouldn't change, but a setter method is provided in case it must be
 * changed programmatically. Changes to the default library location should be made to the default
 * value of the 'nativeLib' member of this class.
 * 
 * @since 2DX 1.0 (1st Edition)
 */

public abstract class Local {

	public static final String
	/**
	 * Field value="x64"
	 */
	JVM_X64 = "x64",
	/**
	 * Field value="x86"
	 */
	JVM_X86 = "x86", NATIVE_WIN32 = "/win32", NATIVE_WIN64 = "/win64",
			NATIVE_LINUX32 = "/linux32", NATIVE_LINUX64 = "/linux64",
			NATIVE_BSD32 = "/freebsd32", NATIVE_BSD64 = "/freebsd64",
			NATIVE_MAC32 = "/mac32", NATIVE_MAC64 = "/mac64", NATIVE_SOLARIS32 = "/solaris32",
			NATIVE_SOLARIS64 = "/solaris64";

	private static String[] nativeSupported;

	private static String nativeLib = "x2d_native_libs", current;

	static {
		checkNativeSupport();
		loadNative("lib_local");
	}

	/**
	 * Converts Calendar time into standard 24-hour time format.
	 * 
	 * @return a four digit (usually) integer representing the current time in 24-hour format.
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

	/**
	 * Returns the name of the underlying operating system.
	 * 
	 * @return
	 */
	public static String getPlatform() {
		return System.getProperty("os.name");
	}

	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}

	/**
	 * Checks to see if the current Java Runtime's version is at or higher than the given minimum.
	 * The two arguments stand for the two parts of the version ID (excluding version updates). i.e.
	 * Java 1.7 or higher can be checked with <code>minJavaVersion(1,7)</code>.
	 * 
	 * @param v1
	 *            the first part of the version ID
	 * @param v2
	 *            the second part of the version ID
	 * @return true if the Java Runtime's version meets the supplied minimum.
	 */
	public static boolean minJavaVersion(int v1, int v2) {
		String[] pts = getJavaVersion().split("\\.");
		if (Integer.parseInt(pts[0]) >= v1 && Integer.parseInt(pts[1]) >= v2) {
			return true;
		} else {
			return false;
		}
	}

	public static String getUserHome() {
		return System.getProperty("user.home");
	}

	public static String getWorkingDir() {
		return System.getProperty("user.dir");
	}

	public static String getJVM() {
		return System.getProperty("java.vm.name");
	}

	public static String getOSArch() {
		return System.getProperty("os.arch");
	}

	/**
	 * Returns the architecture of the current VM (32 or 64 bit)
	 * 
	 * @return a String value representing the architecture: JVM_X64 constant if 64-bit, JVM_X86
	 *         constant otherwise.
	 */
	public static String getJavaArch() {
		return (getJVM().toLowerCase().contains("64-bit")) ? JVM_X64 : JVM_X86;
	}

	/**
	 * Attempts to determine the human-readable identifier of the graphics device installed on this
	 * machine. Usually this method returns the name of the installed video hardware responsible for
	 * handling VGA output. The exact model/name returned may vary depending on the underlying OS.
	 * 
	 * @throws UnsatisfiedLinkError
	 *             if 2DX does not support native libraries on the current platform
	 */
	public static native String getGraphicsDevice() throws UnsatisfiedLinkError;

	/**
	 * Fetches the amount of RAM the system has available. As opposed to the built in Java
	 * functions, this method obtains the amount of free RAM system-wide, outside of the virtual
	 * machine.
	 * 
	 * @return free system RAM in bytes, or -1 if an error occurred.
	 * @throws UnsatisfiedLinkError
	 *             if 2DX does not support native libraries on the current platform
	 */
	public static native long getSystemAvailableRAM()
			throws UnsatisfiedLinkError;

	/**
	 * Fetches the total amount of RAM installed on the system. As opposed to the built in Java
	 * functions, this method obtains the total amount of RAM installed on the hardware. The
	 * accuracy of the result may vary depending on the platform.
	 * 
	 * @return total system installed RAM in bytes, or -1 if an error occurred.
	 * @throws UnsatisfiedLinkError
	 *             if 2DX does not support native libraries on the current platform
	 */
	public static native long getSystemTotalRAM() throws UnsatisfiedLinkError;

	/**
	 * @return the underlying OS identifier for the current process
	 * @throws UnsatisfiedLinkError
	 *             if 2DX does not support native libraries on the current platform
	 */
	public static native int getProcessId() throws UnsatisfiedLinkError;
	
	/**
	 * @return the total amount of memory the JVM has allocated in bytes
	 */
	public static long getJVMAllocatedMemory() {
		MemoryUsage heap = getHeapMemoryUsage();
		MemoryUsage nonHeap = getNonHeapMemoryUsage();
		return heap.getCommitted() + nonHeap.getCommitted();
	}
	
	/**
	 * @return the total amount of memory the JVM has actually used in bytes
	 */
	public static long getJVMUsedMemory() {
		MemoryUsage heap = getHeapMemoryUsage();
		MemoryUsage nonHeap = getNonHeapMemoryUsage();
		return heap.getUsed() + nonHeap.getUsed();
	}
	
	public static MemoryUsage getHeapMemoryUsage() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
	}
	
	public static MemoryUsage getNonHeapMemoryUsage() {
		return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
	}

	/**
	 * 
	 * @return the file suffix for shared libraries on the native platform (no '.') or "lib" if
	 *         unknown.
	 */
	public static String getNativeLibrarySuffix() {
		String os = getPlatform().toLowerCase();
		if (os.contains("windows")) {
			return "dll";
		} else if (os.contains("linux") || os.contains("bsd")) {
			return "so";
		} else if (os.contains("mac")) {
			return "jnilib";
		} else {
			return "lib";
		}
	}

	/**
	 * Sets the location on the classpath from which native libraries should be loaded from. This
	 * method automatically calls checkNativeSupport after setting the new variable.
	 * 
	 * @param pathLoc
	 *            the name of the package from which native code should be loaded
	 * @return true if successful, false otherwise
	 */
	public static boolean setNativeLibraryLocation(String pathLoc) {
		if (pathLoc == null) {
			return false;
		}
		nativeLib = pathLoc;
		checkNativeSupport();
		return true;
	}

	public static String getNativeLibraryLocation() {
		return nativeLib;
	}
	
	/**
	 * Returns the current platform's directory name for native libraries.
	 * Used both by 2DX native libraries and third party native dependencies.
	 * @return
	 */
	public static String getNativeLibraryPlatform() {
		return current;
	}

	public static boolean isNativeCodeSupported() {
		return Arrays.binarySearch(nativeSupported, current) >= 0;
	}
	
	/**
	 * Adds the given path string to the "java.library.path" system property.
	 * This method is sort of a hack, as it utilizes Java Reflections to set
	 * an field used internally in the java.lang.ClassLoader system class.
	 * This allows the original path variable loaded at runtime to be changed,
	 * as well as setting the external system property.
	 * @param s
	 * @throws IOException
	 */
	public static void addToLibPath(String s) throws IOException {
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			field.set(null, tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
		} catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
		}
	}

	/**
	 * Loads the specified library into the system. The native library is located via the system
	 * class loader and written to a temporary file before being loaded into the system.
	 * 
	 * @param native library name (no extension or path).
	 * @return true if successful, false otherwise.
	 */
	public static boolean loadNative(String name) {
		if (!isNativeCodeSupported()) {
			return false;
		}
		name = name + '.' + getNativeLibrarySuffix();
		try {
			URL url = ClassLoader.getSystemResource(nativeLib + current + "/"
					+ name);
			if (url == null) {
				throw (new UnsatisfiedLinkError(
						"failed to locate native library"));
			}
			File tmp = Utils.writeToTempStorage(url.openStream(), name);
			System.load(tmp.getPath());
			tmp.deleteOnExit();
			return true;
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("error loding library " + name + ": "
					+ ule.getMessage());
		} catch (IOException e) {
			System.err.println("error loding library " + name + ": "
					+ e.getMessage());
		}

		return false;
	}

	private static synchronized void checkNativeSupport() {

		final int SYS_COUNT = 10; // this must be changed if platforms are added/removed.

		HashSet<String> supported = new HashSet<String>();
		for (int i = 0; i < SYS_COUNT; i++) {
			String platform = null;
			switch (i) {
			case 0:
				platform = NATIVE_WIN32;
				if (getPlatform().toLowerCase().contains("windows")
						&& getJavaArch().equals(JVM_X86)) {
					current = platform;
				}
				break;
			case 1:
				platform = NATIVE_WIN64;
				if (getPlatform().toLowerCase().contains("windows")
						&& getJavaArch().equals(JVM_X64)) {
					current = platform;
				}
				break;
			case 2:
				platform = NATIVE_LINUX32;
				if (getPlatform().toLowerCase().contains("linux")
						&& getJavaArch().equals(JVM_X86)) {
					current = platform;
				}
				break;
			case 3:
				platform = NATIVE_LINUX64;
				if (getPlatform().toLowerCase().contains("linux")
						&& getJavaArch().equals(JVM_X64)) {
					current = platform;
				}
				break;
			case 4:
				platform = NATIVE_BSD32;
				if (getPlatform().toLowerCase().contains("bsd")
						&& getJavaArch().equals(JVM_X86)) {
					current = platform;
				}
				break;
			case 5:
				platform = NATIVE_BSD64;
				if (getPlatform().toLowerCase().contains("bsd")
						&& getJavaArch().equals(JVM_X64)) {
					current = platform;
				}
				break;
			case 6:
				platform = NATIVE_MAC32;
				if (getPlatform().toLowerCase().contains("mac")
						&& getJavaArch().equals(JVM_X86)) {
					current = platform;
				}
				break;
			case 7:
				platform = NATIVE_MAC64;
				if (getPlatform().toLowerCase().contains("mac")
						&& getJavaArch().equals(JVM_X64)) {
					current = platform;
				}
			case 8:
				platform = NATIVE_SOLARIS32;
				if (getPlatform().toLowerCase().contains("solaris")
						&& getJavaArch().equals(JVM_X86)) {
					current = platform;
				}
				break;
			case 9:
				platform = NATIVE_SOLARIS64;
				if (getPlatform().toLowerCase().contains("solaris")
						&& getJavaArch().equals(JVM_X64)) {
					current = platform;
				}
			}

			if (ClassLoader.getSystemResource(nativeLib + platform + "/") != null) {
				supported.add(platform);
			}
		}
		nativeSupported = supported.toArray(new String[supported.size()]);
		Arrays.sort(nativeSupported);
	}
}
