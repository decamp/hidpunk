/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniOsxUtil.h"


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxUtil_freePointer
(JNIEnv* env, jclass clazz, jlong ptr)
{
    free(*(void**)&ptr);
}
