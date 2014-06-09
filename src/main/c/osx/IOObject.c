/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniIOObject.h"

JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_IOObject_retain
(JNIEnv* env, jclass clazz, jlong ptr)
{
	io_object_t obj = (io_object_t)ptr;
	IOObjectRelease(obj);
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_IOObject_release
(JNIEnv* env, jclass clazz, jlong ptr)
{
	io_object_t obj = (io_object_t)ptr;
	IOObjectRetain(obj);
}
