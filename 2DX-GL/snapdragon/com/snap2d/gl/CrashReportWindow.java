/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.*;
import bg.x2d.utils.*;

/**
 * A frame that can be shown when an uncaught, usually unrecoverable exception or error is detected by system code
 * in order to show detailed information about the crash.  Upon closing of the window, the JVM will be terminated
 * with a non-zero exit status.
 * @author Brian Groenke
 *
 */
public class CrashReportWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7604174524459267051L;

	private static final File CRASH_LOG = new File(System.getProperty("java.io.tmpdir") + File.separator + "snap2d_crash_report.log");
	private static final String CRASH_MSG = "A fatal error has occurred that caused unexpected program termination.\nA dump file " +
			"has been created with this crash information at the following location: " + CRASH_LOG.getPath();
	private static final Dimension FRAME_SIZE = new Dimension(1000, 600);

	JTextPane disp;

	public CrashReportWindow() {
		super("Crash Report");
		disp = new JTextPane();
		disp.setFont(new Font("Courier New", Font.BOLD, 12));
		disp.setEditable(false);
		setContentPane(new JScrollPane(disp));
		setSize(FRAME_SIZE);
		setLocationRelativeTo(null);
		addWindowListener(new OnCloseListener());
	}

	/**
	 * The message and stack trace will be written to a log file in 
	 * @param message
	 * @param error
	 */
	public void dumpToLog(String message, Throwable error) {
		StringBuilder sb = new StringBuilder(Calendar.getInstance().getTime().toString() + "\n" + message + "\n");
		sb.append(error.toString() + "\n");
		for(StackTraceElement elem : error.getStackTrace())
			sb.append(elem.toString() + "\n");
		Throwable cause = error.getCause();
		if(cause != null) {
			sb.append("Caused by: " + cause.toString() + "\n");
			for(StackTraceElement elem : cause.getStackTrace())
				sb.append(elem.toString() + "\n");
		}

		String report = appendSysInfo(sb).toString();
		disp.setText(CRASH_MSG + "\n====== CRASH REPORT ======\n" + report);
		try {
			CRASH_LOG.createNewFile();
			Utils.writeToLogFile(CRASH_LOG, report, false, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuilder appendSysInfo(StringBuilder str) {
		long sysMem = -1, sysFree = -1;
		String deviceName = "Unavailable";
		try {
			sysMem = Local.getSystemTotalRAM();
			sysFree = Local.getSystemAvailableRAM();
			deviceName = Local.getGraphicsDevice();
		} catch (UnsatisfiedLinkError nativeError) {
			System.err.println("Failed to get native system info: " + nativeError.toString());
		}
		
		str.append("--------------------------------\n");
		str.append("Local System Information:\n");
		str.append("OS = " + Local.getPlatform()+"\n");
		str.append("Arch = " + Local.getOSArch()+"\n");
		str.append("Graphics Device = " + deviceName+"\n");
		str.append("Total RAM = " + sysMem+" bytes\n");
		str.append("Available RAM = " + sysFree+" bytes\n");
		str.append("Java Version = " + Local.getJavaVersion()+"\n");
		str.append("Java Arch = " + Local.getJavaArch()+"\n");
		str.append("JVM Name = " + Local.getJVM()+"\n");
		str.append("JVM CPUs = " + Runtime.getRuntime().availableProcessors()+"\n");
		str.append("JVM Memory Alloc = " + Local.getJVMAllocatedMemory()+" bytes\n");
		str.append("JVM Memory Used = " + Local.getJVMUsedMemory()+" bytes\n");
		return str;
	}

	private class OnCloseListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(1);
		}
	}

}
