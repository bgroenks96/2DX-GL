/*
 *  Copyright © 2012-2013 Brian Groenke
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
 * Provides static general utility methods.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Utils {

	public static final File TEMP_DIR = new File(
			System.getProperty("java.io.tmpdir") + File.separator
			+ ".com_snap2d_tmp");

	public static volatile boolean 
	/**
	 * If true, the Snapdragon2D temp-dir won't be deleted on program exit.
	 */
	keepTempDir = false;

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
			System.err.println("Snapdragon2D: error creating temp-dir");
		} else {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					SnapLogger.log("Received exit signal");
					try {
						if(!keepTempDir)
							removeDirectory(TEMP_DIR, true);
						else
							SnapLogger.log("keepTempDir=true - skipping temp-dir removal");
					} catch (FileNotFoundException e) {
						System.err
						.println("Snapdragon2D: failed to remove temp-dir");
					}
				}

			}));
		}
	}

	private Utils() {
	};

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
	 * @throws IOException
	 * @return a File object representing the newly created temp-file.
	 */
	public static File writeToTempStorage(InputStream in, String fileName)
			throws IOException {
		BufferedInputStream buffIn = new BufferedInputStream(in);
		File outFile = new File(TEMP_DIR + File.separator + fileName);
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
	 * Write raw text to a log file in temporary storage.
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

	public static String readText(URL url) {
		String text = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sb = new StringBuilder();
			String next;
			while((next=br.readLine()) != null) 
				sb.append(next + System.getProperty("line.separator"));
			br.close();
			text = sb.toString();
		} catch(IOException e) {
			e.printStackTrace();
		}
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

	public static <T> T[] arrayDelete(T[] arr, T[] dest, T... dels) {
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
	@SuppressWarnings("unchecked")
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
}
