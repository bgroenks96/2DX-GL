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

import java.util.HashMap;
import java.util.Map;

/**
 * SnapObjects are defined and parsed from object source code. It is designed to
 * be a very simple and clean way of defining static "object" data that can
 * later be used by Java or SnapScript code. The syntax goes as follows:<br/>
 * <br/>
 * <code>
 * [ObjectName]{
 *     [propertyName]=[propertyValue]
 *     [anotherProp]=[anotherValue]
 * }
 * </code> <br/>
 * <br/>
 * As many objects/properties can be defined in a single source file/string as
 * desired. <b>Note that every SnapObject must declare a property named
 * 'type.'</b> The value can be defined arbitrarily by the user, but it is
 * recommended that this be used as a loose system for grouping defined objects.
 * Here is an example of SnapObject source defining 2 objects each with 3
 * properties:<br/>
 * <br/>
 * <code>
 * Region1 {
 *     type="map_region"
 *     name="Mordor"
 *     difficulty="Really hard"
 * }
 * Region2 {
 *     type="map_region"
 *     name="The Shire"
 *     difficulty="Quite dandy"
 * }
 * </code> <br/>
 * <br/>
 * That's all there is to it! SnapObjects are meant to be a simple, open-ended
 * way of defining static data. How you use it is entirely up to you!
 * 
 * @author Brian Groenke
 */
public class SnapObject {

    public final String name;

    private final Map <String, String> propMap;

    SnapObject(final String name) {

        this.name = name;
        propMap = new HashMap <String, String>();
    }

    void put(final String k, final String v) {

        propMap.put(k, v);
    }

    public String get(final String propName) {

        return propMap.get(propName);
    }

    public String[] getPropertyNames() {

        return propMap.keySet().toArray(new String[propMap.size()]);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(name + "{ ");
        for (String s : propMap.keySet()) {
            sb.append(s + "=" + propMap.get(s) + " ");
        }
        sb.append("}");
        return sb.toString();
    }
}
