/*
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl.jogl;

import java.io.*;
import java.net.*;
import java.util.jar.*;

import bg.x2d.*;
import bg.x2d.utils.*;

/**
 * JOGLNativeLibLoader provides static methods to locate and load appropriate JOGL native bundles.
 * @author Brian Groenke
 *
 */
class JOGLNativeLibLoader {
	
	public static final String[] JARS = new String[] {"gluegen-rt-natives","jogl-all-natives"};
	public static final String WIN = "windows", MAC = "macosx", LINUX = "linux", SOLARIS = "solaris",
			X86 = "i586", X64 = "amd64";

	/**
	 * Writes the appropriate JOGL native libraries by extracting the platform-specific JARs from
	 * the JAR bundle at the given location to the specified directory where the JOGL classes are located.
	 * @param bundleLoc the full path to the JOGL natives bundle on the classpath or on the file system.
	 * @param outLoc
	 * @return
	 * @throws IOException 
	 */
	static boolean load(String bundleLoc, String outLoc) throws IOException {
		URL url = ClassLoader.getSystemResource(bundleLoc);
		if(url == null) {
			File f = new File(bundleLoc);
			if(f.exists())
				url = Utils.getFileURL(f);
			else
				return false;
		}
		
		String plat = Local.getPlatform().toLowerCase();
		if(plat.contains("windows"))
			plat = WIN;
		else if(plat.contains("mac"))
			plat = MAC;
		else if(plat.contains("linux"))
			plat = LINUX;
		else if(plat.contains("solaris"))
			plat = SOLARIS;
		String arch = Local.getJavaArch();
		if(arch.equals(Local.JVM_X64))
			arch = X64;
		else if(arch.equals(Local.JVM_X86))
			arch = X86;
		if(arch == null || plat == null)
			return false;
		
		JarInputStream jarIn = new JarInputStream(url.openStream());
		JarEntry entry = jarIn.getNextJarEntry();
		while(entry != null) {
			String name = entry.getName();
			for(String s:JARS) {
				if(name.startsWith(s)) {
					if(name.contains(plat) && name.contains(arch)) {
						extract(jarIn, outLoc);
					}
				}
			}
			jarIn.closeEntry();
			entry = jarIn.getNextJarEntry();
		}
		jarIn.close();
		
		return true;
	}
	
	private static void extract(JarInputStream jarIn, String outLoc) throws IOException {
		FileOutputStream out = new FileOutputStream(outLoc);
		byte[] buff = new byte[8192];
		int len = 0;
		while((len=jarIn.read(buff)) > 0) {
			out.write(buff, 0, len);
			out.flush();
		}
		out.close();
	}
}
