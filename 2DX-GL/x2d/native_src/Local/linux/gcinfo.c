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

#include <sys/sysinfo.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "bg_x2d_Local.h"

#define MAX_STR_LEN 128

// ---- constant strings needed for executing and parsing the command ----- //
const char* GET_DEVICE_CMD = "lspci | awk '/VGA/'";
const char* VGA = "VGA";
const char* RET_VAL_SEP = ": ";
// ---- //

typedef struct _meminfo {
  unsigned long mem_unit;
  unsigned long total_mem;
  unsigned long avail_mem;
} MEM_INFO;

/*
* Returns 0 if successful, any other value on failure.
*/
int get_primary_device(char *str) {

    int chk = 0;	
    FILE *fp;
    char path[1035];

    fp = popen(GET_DEVICE_CMD, "r");
    if (fp == NULL) {
      chk = -1;
      return chk;
    }

    char* out = fgets(path, sizeof(path)-1, fp);
    if(out == NULL) {
      chk = -1;
      return chk;
    }
    
    char* nstr = strstr(out, VGA);
    if(nstr == NULL) {
      chk = -1;
      return chk;
    }
    nstr = strstr(nstr, RET_VAL_SEP);
    if(nstr == NULL) {
      chk = -1;
      return chk;
    }
    
    int r_val_len = strlen(RET_VAL_SEP);
    char fstr[strlen(nstr) - r_val_len];
    int i;
    for(i=r_val_len;i<sizeof(fstr) + 1;i++) {
      fstr[i-r_val_len] = nstr[i];
    }
    
    strcpy(str, fstr);
    
    pclose(fp);
    return chk;
}

/*
* Returns 0 if successful, any other value on failure.
*/
int get_system_available_ram(unsigned long* avail) {
    int chk = 0;
    struct sysinfo info;
    chk = sysinfo(&info);
    if(chk != 0)
      return chk;
    *avail = info.freeram;
    return chk;
}

/*
* Returns 0 if successful, any other value on failure.
*/
int get_system_total_ram(unsigned long* total) {
    int chk = 0;
    struct sysinfo info;
    chk = sysinfo(&info);
    if(chk != 0)
      return chk;
    *total = info.totalram;
    return chk;
}

JNIEXPORT jstring JNICALL Java_bg_x2d_Local_getGraphicsDevice
  (JNIEnv *env, jclass jc) {
    char str[MAX_STR_LEN];
    int succ = get_primary_device(str);
    if(succ != 0)
      return NULL;
    int size = strlen(str);
    jchar jchars[size];
    int i;
    for(i=0;i<size;i++)
      jchars[i] = (jchar) str[i];
    jstring jstr = (*env) -> NewString(env, jchars, size);
    return jstr;
}

JNIEXPORT jlong JNICALL Java_bg_x2d_Local_getSystemAvailableRAM
  (JNIEnv *env, jclass jc) {
    unsigned long mem;
    int chk = get_system_available_ram(&mem);
    if(chk != 0)
      return -1;
    return (jlong) mem;
}

JNIEXPORT jlong JNICALL Java_bg_x2d_Local_getSystemTotalRAM
  (JNIEnv *env, jclass jc) {
    unsigned long mem;
    int chk = get_system_total_ram(&mem);
    if(chk != 0)
      return -1;
    return (jlong) mem;
}