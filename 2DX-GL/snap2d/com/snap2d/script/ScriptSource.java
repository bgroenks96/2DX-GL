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

package com.snap2d.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Brian Groenke
 *
 */
public class ScriptSource {

    private String src;

    public ScriptSource(final URL url) throws IOException {

        setSourceFrom(url);
    }

    public ScriptSource(final String src) {

        setSourceFrom(src);
    }

    public String getSource() {

        return src;
    }

    public void setSourceFrom(final String src) {

        this.src = src;
    }

    public void setSourceFrom(final URL url) throws IOException {

        InputStream in = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder src = new StringBuilder();
        String next = null;
        while ( (next = br.readLine()) != null) {
            src.append(next + "\n");
        }
        br.close();
        this.src = src.toString();
    }
}
