#include <SDL.h>
#include <sys/sysinfo.h>
#include <string.h>
#include "bg_x2d_Local.h"

#define MAX_STR_LEN 128

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
    chk = SDL_Init(SDL_INIT_VIDEO);
    if(chk != 0)
      return chk;
    SDL_VideoDriverName(str, MAX_STR_LEN);
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
    jstring jstr = (*env) -> NewString(env, jchars, strlen(str));
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