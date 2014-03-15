/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#ifndef _HID_PUNK_H_
#define _HID_PUNK_H_

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <sys/errno.h>
#include <sysexits.h>
#include <time.h>
#include <pthread.h>
#include <JavaVM/jni.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/IOCFPlugIn.h>
#include <IOKit/IOMessage.h>
#include <IOKit/HID/IOHIDLib.h>
//#include <IOKit/hid/IOHIDUsageTables.h>

int hidpunk_initLibrary(JNIEnv* env);

void hidpunk_throwIOKitException(JNIEnv* env, const char* msg);
void hidpunk_throwCFException(JNIEnv* env, const char* msg);
void hidpunk_throwNullPointerException(JNIEnv* env, const char* msg);
void hidpunk_throwOutOfMemoryError(JNIEnv* env, const char* msg);
void hidpunk_throwLinkageError(JNIEnv* env, const char* msg);
void hidpunk_throwIllegalArgumentException(JNIEnv* env, const char* msg);

jobject hidpunk_newDirectBuffer(JNIEnv* env, SInt32 size);
int hidpunk_loadHidEvent(JNIEnv* env, IOHIDEventStruct* in, jobject out);
int hidpunk_nullifyEvent(JNIEnv* env, jobject out);

void hidpunk_setThreadEnv(JNIEnv* env);
JNIEnv* hidpunk_getThreadEnv();


#endif //_HID_PUNK_H_
