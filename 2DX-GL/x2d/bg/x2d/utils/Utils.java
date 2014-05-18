/*
 *  Copyright (C) 2011-2014 Brian Groenke
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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.snap2d.*;

/**
 * Static, miscellaneous utility methods.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Utils {

	public static final File TEMP_DIR = new File(
			System.getProperty("java.io.tmpdir") + File.separator
			+ ".com_x2d_tmp");
	
	public static final long INT_SIZE = Integer.SIZE / Byte.SIZE,
			LONG_SIZE = Long.SIZE / Byte.SIZE,
			FLOAT_SIZE = Float.SIZE / Byte.SIZE,
			DOUBLE_SIZE = Double.SIZE / Byte.SIZE,
			SHORT_SIZE = Short.SIZE / Byte.SIZE;

	/**
	 * If true, the 2DX-GL temp-dir won't be deleted on program exit.
	 */
	public static volatile boolean keepTempDir = false;

	static {
		boolean chk = false;
		if (TEMP_DIR.exists()) {
			try {
				removeDirectory(TEMP_DIR, true);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		for (int i = 0; i < 5; i++) {
			chk = TEMP_DIR.mkdir();
			if (chk) {
				break;
			}
		}
		if (!chk) {
			System.err.println("WARNING: error creating temp-dir");
		} else {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					SnapLogger.log("Received exit signal");
					try {
						if(!keepTempDir)
							removeDirectory(TEMP_DIR, true);
						else
							System.out.println("keepTempDir=true - skipping temp-dir removal");
					} catch (FileNotFoundException e) {
						System.err.println("WARNING: failed to remove temp-dir");
					}
				}

			}));
		}
	}

	private Utils() {}

	/**
	 * Closes the InputStream, catching the exception and returning false on failure. In the case
	 * that stream is null, this method will quietly return false.
	 * 
	 * @param stream
	 *            InputStream to close; a null value will cause false to be returned
	 * @return true if successful, false if null or otherwise.
	 */
	public static boolean closeStream(InputStream stream) {
		if (stream == null) {
			return false;
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Closes the OutputStream, catching the exception and returning false on failure. In the case
	 * that stream is null, this method will quietly return false.
	 * 
	 * @param stream
	 *            OutputStream to close; a null value will cause false to be returned
	 * @return true if successful, false if null or otherwise.
	 */
	public static boolean closeStream(OutputStream stream) {
		if (stream == null) {
			return false;
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Performs the recommended intermediary step of using toURI and then toURL to convert the File
	 * object. The MalformedURLException is caught if thrown and null will be returned.
	 * 
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

	/**
	 * Writes the data from the given InputStream to a file of the specified name in Java's default
	 * temp-dir. The given InputStream is closed by this method after writing completes.
	 * 
	 * @param in
	 * @param fileName
	 * @param useSystem if true, the temp file will go in the standard jav.io.tmpdir instead of the Snap2D
	 * common temp-dir, so it will not be removed by the shutdown hook.  It will remain in temp storage until
	 * either the system or user deletes it.
	 * @throws IOException
	 * @return a File object representing the newly created temp-file.
	 */
	public static File writeToTempStorage(InputStream in, String fileName, boolean useSystem)
			throws IOException {
		BufferedInputStream buffIn = new BufferedInputStream(in);
		String tmpdir = (useSystem) ? System.getProperty("java.io.tmpdir"):TEMP_DIR.getPath();
		File outFile = new File(tmpdir + File.separator + fileName);
		BufferedOutputStream buffOut = new BufferedOutputStream(
				new FileOutputStream(outFile));
		byte[] buff = new byte[8124];
		int len;
		while ((len = buffIn.read(buff)) > 0) {
			buffOut.write(buff, 0, len);
		}
		buffOut.close();
		buffIn.close();
		return outFile;
	}

	/**
	 * Writes the given String to temporary storage under the given file name.
	 * @param text
	 * @param fileName
	 * @param useSystem if true, the temp file will go in the standard jav.io.tmpdir instead of the Snap2D
	 * common temp-dir, so it will not be removed by the shutdown hook.  It will remain in temp storage until
	 * either the system or user deletes it.
	 * @return a File object representing the newly created temp-file.
	 * @throws IOException
	 */
	public static File writeToTempStorage(String text, String fileName, boolean useSystem)
			throws IOException {
		String tmpdir = (useSystem) ? System.getProperty("java.io.tmpdir"):TEMP_DIR.getPath();
		File outFile = new File(tmpdir + File.separator + fileName);
		PrintWriter pw = new PrintWriter(outFile);
		Scanner sc = new Scanner((text.endsWith("\n") ? text:text+"\n"));
		while(sc.hasNextLine()) {
			pw.println(sc.nextLine());
		}
		pw.flush();
		pw.close();
		sc.close();
		return outFile;
	}

	/**
	 * Write raw text to a log file.
	 * @param f
	 * @param text
	 * @param append
	 * @param newLine
	 * @return true if successful, false on error
	 */
	public static boolean writeToLogFile(File f, String text, boolean append, boolean newLine) {
		try {
			FileWriter fw = new FileWriter(f, append);
			PrintWriter pw = new PrintWriter(fw);
			if(newLine)
				pw.println(text);
			else
				pw.print(text);
			pw.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public static String readText(URL url) throws IOException {
		String text = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();
		String next;
		while((next=br.readLine()) != null) 
			sb.append(next + System.getProperty("line.separator"));
		br.close();
		text = sb.toString();
		return text;
	}

	/**
	 * Recursively removes all subfiles of the given directory and deletes it if desired.
	 * 
	 * @param dir
	 *            The directory to clear/delete.
	 * @param delete
	 *            removes the now empty directory if true, else it is left alone.
	 * @return true if successful. False otherwise.
	 * @throws FileNotFoundException
	 *             if the given File isn't a directory.
	 */
	public static boolean removeDirectory(File dir, boolean delete)
			throws FileNotFoundException {
		boolean deleted = true;
		if (dir.isDirectory()) {
			File[] subfiles = dir.listFiles();
			for (File sub : subfiles) {
				if (sub.isDirectory()) {
					removeDirectory(sub, delete);
				} else {
					if (!sub.delete()) {
						deleted = false;
					}
					;
				}
			}
		} else {
			throw (new FileNotFoundException(dir + " is not a directory."));
		}
		if (delete) {
			deleted = dir.delete();
		}

		return deleted;
	}

	public static <T> T[] arraySeekDelete(T[] arr, T[] dest, T... dels) {
		if (arr == null || dest == null
				|| dest.length != arr.length - dels.length) {
			throw (new IllegalArgumentException(
					"null or invalid array argument"));
		}
		for (int i = 0; i < dels.length; i++) {
			for (int ii = 0, offs = 0; ii < arr.length; ii++) {
				if (ii >= dest.length) {
					return null;
				}
				if (arr[ii] != dels[i]) {
					dest[ii - offs] = arr[ii];
				} else {
					offs++;
				}
			}
		}

		return dest;
	}

	/**
	 * Deletes the object at the specified index of 'src' array by storing
	 * its contents in 'dest' minus the index of deletion.
	 * @param src
	 * @param dest array to store new results in; the length MUST be src.length - 1
	 * @param ind
	 * @return the dest array
	 */
	public static <T> T[] arrayDelete(T[] src, T[] dest, int ind) {
		if(dest.length != src.length - 1)
			throw(new IllegalArgumentException("destination array length must equal source array length - 1"));
		int offs = 0;
		for(int i=0; i < src.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}
			dest[i - offs] = src[i];
		}
		return dest;
	}

	public static int[] arrayDelete(int[] arr, int ind) {
		int[] narr = new int[arr.length - 1];
		int offs = 0;
		for(int i=0; i < arr.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}

			narr[i - offs] = arr[i];
		}
		return narr;
	}
	public static float[] arrayDelete(float[] arr, int ind) {
		float[] narr = new float[arr.length - 1];
		int offs = 0;
		for(int i=0; i < arr.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}
			
			narr[i - offs] = arr[i];
		}
		return narr;
	}
	public static double[] arrayDelete(double[] arr, int ind) {
		double[] narr = new double[arr.length - 1];
		int offs = 0;
		for(int i=0; i < arr.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}
			
			narr[i - offs] = arr[i];
		}
		return narr;
	}
	public static boolean[] arrayDelete(boolean[] arr, int ind) {
		boolean[] narr = new boolean[arr.length - 1];
		int offs = 0;
		for(int i=0; i < arr.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}
			
			narr[i - offs] = arr[i];
		}
		return narr;
	}
	public static byte[] arrayDelete(byte[] arr, int ind) {
		byte[] narr = new byte[arr.length - 1];
		int offs = 0;
		for(int i=0; i < arr.length; i++) {
			if(i == ind) {
				offs = 1;
				continue;
			}
			
			narr[i - offs] = arr[i];
		}
		return narr;
	}

	public static int interpolate(int n, int lastN, float interpolation) {
		return Math.round(((n - lastN) * interpolation + lastN));
	}

	/**
	 * Computes the size of an Object by serializing it to memory and checking the buffer size.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static int sizeof(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		return bos.size();
	}

	/**
	 * Copies the contents of the given array into an array of the specified size.
	 * @param array the array to be copied ("resized")
	 * @param newSize the new size
	 * @return
	 */
	public static <T> T[] resizeArray(T[] array, int newSize) {
		return Arrays.copyOf(array, newSize);
		/*
		Class<T> type = (Class<T>) array.getClass();
		T[] narr = (T[]) Array.newInstance(type, newSize);
		for(int i=0;i<narr.length;i++) {
			if(i < array.length)
				narr[i] = array[i];
			else
				narr[i] = null;
		}
		return narr;
		 */
	}

	public static <T> T[] appendArray(T[] array, T newElem) {
		T[] narr = resizeArray(array, array.length + 1);
		narr[narr.length - 1] = newElem;
		return narr;
	}

	/**
	 * Reverses the array so that all the values currently set from front to
	 * back are reset to being back to front.
	 * 
	 * @param array
	 * @return the reversed array.
	 */
	public static <T> T[] flipArray(T[] array) {
		if (array == null) {
			throw (new NullPointerException("passed array is of null value"));
		}
		T[] copy = Arrays.copyOf(array, array.length);
		int inv = 0;
		for (int i = array.length - 1; i >= 0; i--) {
			array[i] = copy[inv];
			inv++;
		}
		return array;
	}

	public static boolean urlExists(URL url) {
		try {
			url.openStream();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static int getScreenResolution() {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}
	
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public static boolean sleep(long millis, int nanos) {
		try {
			Thread.sleep(millis, nanos);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public static float nanoToMillis(long nanoTime) {
	
		return nanoTime / 1000000.0f;
	}
	
	public static float nanoToSecs(long nanoTime) {
		return nanoTime / 1000000000.0f;
	}
}
