/*
 *  Copyright (C) 2011-2013 Brian Groenke
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

import java.util.logging.*;

/**
 * @author Brian Groenke
 *
 */
public class ConfigLogHandler extends Handler {
	
	private final String prefix;
	
	public ConfigLogHandler(String msgprefix) {
		this.prefix = msgprefix;
	}

	/**
	 *
	 */
	@Override
	public void publish(LogRecord record) {
		if(record.getLevel() != Level.CONFIG)
			return;
		System.err.println(prefix + " " + record.getMessage());
	}

	/**
	 *
	 */
	@Override
	public void flush() {
		
	}

	/**
	 *
	 */
	@Override
	public void close() throws SecurityException {
		
	}

}
