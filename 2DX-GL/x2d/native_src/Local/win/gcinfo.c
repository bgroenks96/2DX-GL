/*
 *  Copyright © 2011-2012 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

#define WINVER 0x0500
#define _WIN32_WINNT 0x0500

#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <string.h>
#include <math.h>
#include "bg_x2d_Local.h"

typedef struct store {
  DWORD cb;
  TCHAR DeviceName[32];
  TCHAR DeviceString[128];
  DWORD StateFlags;
} DISPLAY_INFO;

int get_primary_device(DISPLAY_INFO *store) {

	DISPLAY_DEVICE DevInfo;
	DevInfo.cb = sizeof(DISPLAY_DEVICE);

	DWORD iDevNum = 0;
	unsigned int chk = 0;

	while (EnumDisplayDevices (NULL, iDevNum, &DevInfo, 0))
	{
		if((DISPLAY_DEVICE_ATTACHED_TO_DESKTOP & DevInfo.StateFlags) && (DevInfo.StateFlags & DISPLAY_DEVICE_PRIMARY_DEVICE)) {
			store -> cb = DevInfo.cb;
			strcpy(store -> DeviceName, DevInfo.DeviceName);
			strcpy(store -> DeviceString, DevInfo.DeviceString);
			store -> StateFlags = DevInfo.StateFlags;
			chk = 1;
			break;
		}
		iDevNum++;
	}
    return chk;
}

/*
 * Returns 1 if successful, 0 otherwise.
 */
int get_system_available_ram(DWORDLONG* ram_ptr) {
	MEMORYSTATUSEX mem_stat;
	mem_stat.dwLength = sizeof(mem_stat);
	BOOL success = GlobalMemoryStatusEx(&mem_stat);
	if(!success)
		return 0;
	*ram_ptr = mem_stat.ullAvailPhys;
	return 1;
}

/*
 * Returns 1 if successful, 0 otherwise.
 */
int get_system_total_ram(DWORDLONG* ram_ptr) {
	MEMORYSTATUSEX mem_stat;
	mem_stat.dwLength = sizeof(mem_stat);
	BOOL success = GlobalMemoryStatusEx(&mem_stat);
	if(!success)
		return 0;
	*ram_ptr = mem_stat.ullTotalPhys;
	return 1;
}

JNIEXPORT jstring JNICALL Java_bg_x2d_Local_getGraphicsDevice
  (JNIEnv *env, jclass jc) {
  	DISPLAY_INFO info;
	int success = get_primary_device(&info);
	if(!success)
		return NULL;
	TCHAR *arr = info.DeviceString;
	int size = strlen(arr);
	jchar jarr[size];
	int i;
	// cast to unsigned chars
	for(i=0;i<size;i++) {
		unsigned char conv = (unsigned char) arr[i];
		jarr[i] = conv;
	}
	jstring str = (*env) -> NewString(env, jarr, size);
	return str;
}

JNIEXPORT jlong JNICALL Java_bg_x2d_Local_getSystemAvailableRAM
  (JNIEnv *env, jclass jc) {
  DWORDLONG mem;
  int chk = get_system_available_ram(&mem);
  if(!chk)
	return -1;
  return (jlong) mem;
}

JNIEXPORT jlong JNICALL Java_bg_x2d_Local_getSystemTotalRAM
  (JNIEnv *env, jclass jc) {
    DWORDLONG mem;
	int chk = get_system_total_ram(&mem);
	if(!chk)
		return -1;
	return (jlong) mem;
}