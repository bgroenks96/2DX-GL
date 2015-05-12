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

package com.snap2d.script.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.snap2d.script.ScriptLink;
import com.snap2d.script.Vec2;

/**
 * @author Brian Groenke
 *
 */
public class VarStore {

    private final HashMap<String, Object> globals = new HashMap<String, Object>();
    private final HashMap<Integer, List<Integer>> intLists = new HashMap<Integer, List<Integer>>();
    private final HashMap<Integer, List<Float>> floatLists = new HashMap<Integer, List<Float>>();
    private final HashMap<Integer, List<String>> stringLists = new HashMap<Integer, List<String>>();
    private final HashMap<Integer, List<Boolean>> boolLists = new HashMap<Integer, List<Boolean>>();
    private final HashMap<Integer, List<Vec2>> vecLists = new HashMap<Integer, List<Vec2>>();

    private static volatile int listID = Integer.MIN_VALUE;

    boolean useDouble = false;

    public void setUseDouble(final boolean storeAsDouble) {

        useDouble = storeAsDouble;
    }

    @ScriptLink
    public boolean isUsingDouble() {

        return useDouble;
    }

    @ScriptLink
    public void putInt(final String name, final int value) {

        globals.put(name, value);
    }

    @ScriptLink
    public void putFloat(final String name, final double value) {

        globals.put(name, (useDouble) ? value : (float) value);
    }

    @ScriptLink
    public void putString(final String name, final String value) {

        globals.put(name, value);
    }

    @ScriptLink
    public void putBool(final String name, final boolean value) {

        globals.put(name, value);
    }

    @ScriptLink
    public void putVec(final String name, final Vec2 v) {

        globals.put(name, v);
    }

    @ScriptLink
    public int getInt(final String name) {

        return (Integer) globals.get(name);
    }

    @ScriptLink
    public double getFloat(final String name) {

        return (Double) globals.get(name);
    }

    @ScriptLink
    public boolean getBool(final String name) {

        return (Boolean) globals.get(name);
    }

    @ScriptLink
    public String getString(final String name) {

        return (String) globals.get(name);
    }

    @ScriptLink
    public Vec2 getVec(final String name) {

        return (Vec2) globals.get(name);
    }

    @ScriptLink
    public boolean isVarDefined(final String name) {

        return globals.containsKey(name);
    }

    @ScriptLink
    public int newIntList() {
        List<Integer> newList = new ArrayList<Integer>();
        int id = listID++ ;
        intLists.put(id, newList);
        return id;
    }

    @ScriptLink
    public void addToList(int value, int id) {
        intLists.get(id).add(value);
    }

    @ScriptLink
    public void addToList(int value, int pos, int id) {
        intLists.get(id).add(pos, value);
    }

    @ScriptLink
    public int intAt(int pos, int id) {
        return intLists.get(id).get(pos);
    }

    @ScriptLink
    public int newFloatList() {
        List<Float> newList = new ArrayList<Float>();
        int id = listID++ ;
        floatLists.put(id, newList);
        return id;
    }

    @ScriptLink
    public void addToList(float value, int id) {
        floatLists.get(id).add(value);
    }

    @ScriptLink
    public void addToList(float value, int pos, int id) {
        floatLists.get(id).add(pos, value);
    }

    @ScriptLink
    public float floatAt(int pos, int id) {
        return floatLists.get(id).get(pos);
    }

    @ScriptLink
    public int newBoolList() {
        List<Boolean> newList = new ArrayList<Boolean>();
        int id = listID++ ;
        boolLists.put(id, newList);
        return id;
    }

    @ScriptLink
    public void addToList(boolean value, int id) {
        boolLists.get(id).add(value);
    }

    @ScriptLink
    public void addToList(boolean value, int pos, int id) {
        boolLists.get(id).add(pos, value);
    }

    @ScriptLink
    public boolean boolAt(int pos, int id) {
        return boolLists.get(id).get(pos);
    }

    @ScriptLink
    public int newStringList() {
        List<String> newList = new ArrayList<String>();
        int id = listID++ ;
        stringLists.put(id, newList);
        return id;
    }

    @ScriptLink
    public void addToList(String value, int id) {
        stringLists.get(id).add(value);
    }

    @ScriptLink
    public void addToList(String value, int pos, int id) {
        stringLists.get(id).add(pos, value);
    }

    @ScriptLink
    public String stringAt(int pos, int id) {
        return stringLists.get(id).get(pos);
    }

    @ScriptLink
    public int newVecList() {
        List<Vec2> newList = new ArrayList<Vec2>();
        int id = listID++ ;
        vecLists.put(id, newList);
        return id;
    }

    @ScriptLink
    public void addToList(Vec2 value, int id) {
        vecLists.get(id).add(value);
    }

    @ScriptLink
    public void addToList(Vec2 value, int pos, int id) {
        vecLists.get(id).add(pos, value);
    }

    @ScriptLink
    public Vec2 vecAt(int pos, int id) {
        return vecLists.get(id).get(pos);
    }

    @ScriptLink
    public boolean listExists(int listId) {
        for (List<?> nextList : queryLists(listId)) {
            if (nextList != null) return true;
        }
        return false;
    }

    @ScriptLink
    public void printList(int listId) {
        for (List<?> nextList : queryLists(listId)) {
            if (nextList != null) ScriptUtils.println(nextList.toString());
        }
    }

    @ScriptLink
    public int size(int listId) {
        for (List<?> nextList : queryLists(listId)) {
            if (nextList != null) return nextList.size();
        }
        return -1;
    }

    private final List<?>[] queryLists(int listId) {
        return new List<?>[] { intLists.get(listId), floatLists.get(listId), stringLists.get(listId),
                boolLists.get(listId), vecLists.get(listId) };
    }
}
