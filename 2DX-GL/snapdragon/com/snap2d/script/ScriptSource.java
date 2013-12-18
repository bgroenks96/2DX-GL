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

package com.snap2d.script;

import java.io.*;
import java.net.*;

/**
 * @author Brian Groenke
 *
 */
public class ScriptSource {

	private String src;
	
	public ScriptSource(URL url) throws IOException {
		InputStream in = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder src = new StringBuilder();
		String next = null;
		while((next=br.readLine()) != null)
			src.append(next + "\n");
		br.close();
		this.src = src.toString();
	}
	
	public ScriptSource(String src) {
		this.src = src;
	}
	
	public String getSource() {
		return src;
	}
}
