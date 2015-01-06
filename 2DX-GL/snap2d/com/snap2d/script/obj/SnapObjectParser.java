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

package com.snap2d.script.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Parses SnapObject(s) from source strings.
 * 
 * @author Brian Groenke
 * @see {@link SnapObject}
 */
public class SnapObjectParser {

    public static final String OBJECT_SCRIPT_SPEC_VERSION = "0.0.1", TYPE_PROPERTY = "type";

    public SnapObject[] parseFromStream(final InputStream in) throws IOException, ScriptObjectParsingException {

        BufferedReader buffRead = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ( (line = buffRead.readLine()) != null) {
            sb.append(line + "\n");
        }
        buffRead.close();
        return parseFromSource(sb.toString());
    }

    public SnapObject[] parseFromFile(final File file) throws IOException, ScriptObjectParsingException {

        return parseFromStream(new FileInputStream(file));
    }

    public SnapObject[] parseFromSource(String src) throws ScriptObjectParsingException {

        src = src.trim();
        ArrayList <SnapObject> objList = new ArrayList <SnapObject>();
        for (int i = 0; i < src.length();) {
            int ind = src.indexOf("{", i);
            String dec = src.substring(i, ind);
            String objName = dec.trim();
            SnapObject snapObj = new SnapObject(objName);
            int end = findEndOfBlock(src.toCharArray(), ind + 1);
            parseProperties(src.substring(ind + 1, end), snapObj);
            if (snapObj.get(TYPE_PROPERTY) == null) {
                throw new ScriptObjectParsingException("error parsing object '" + snapObj.name
                        + "' - all objects must declare the '" + TYPE_PROPERTY + "' property");
            }
            objList.add(snapObj);
            i = end + 1;
        }
        return objList.toArray(new SnapObject[objList.size()]);
    }

    private void parseProperties(String str, final SnapObject obj) throws ScriptObjectParsingException {

        str = str.trim();
        String[] pts = str.split("\n");
        for (String s : pts) {
            String[] kv = s.split("=");
            if (kv.length != 2) {
                throw new ScriptObjectParsingException("error parsing properties: invalid assignment syntax in: " + s
                        + " [" + obj.name + "]");
            }
            kv[0] = kv[0].trim();
            kv[1] = kv[1].trim();
            if (!kv[1].startsWith("\"") || !kv[1].endsWith("\"")) {
                throw new ScriptObjectParsingException("error parsing properties: values must be delimited by quotes "
                        + s);
            }
            obj.put(kv[0], kv[1].substring(1, kv[1].length() - 1));
        }
    }

    private int findEndOfBlock(final char[] chars, int pos) {

        int stcount = 0;
        boolean inquot = false;
        for (; pos < chars.length; pos++ ) {
            if (chars[pos] == '{' && !inquot) {
                stcount++ ;
            } else if (chars[pos] == '"') {
                if (pos > 0 && chars[pos - 1] != '\\') {
                    inquot = !inquot;
                }
            } else if (chars[pos] == '}') {
                if (stcount == 0) {
                    return pos;
                } else {
                    stcount-- ;
                }
            }
        }
        if (stcount == 0) {
            return pos - 1;
        }
        return -1;
    }
}
