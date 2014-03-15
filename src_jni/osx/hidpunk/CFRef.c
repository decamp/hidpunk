/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniCFRef.h"

JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRef_retain
(JNIEnv* env, jclass clazz, jlong ptr)
{
	CFTypeRef ref = *(CFTypeRef*)&ptr;
	CFRetain(ref);
}

JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRef_release
(JNIEnv* env, jclass clazz, jlong ptr)
{
	CFTypeRef ref = *(CFTypeRef*)&ptr;
	CFRelease(ref);
}
