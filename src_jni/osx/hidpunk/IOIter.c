/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniIOIter.h"


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_IOIter_next
(JNIEnv* env, jclass clazz, jlong iter)
{
    return IOIteratorNext((io_iterator_t)iter);
}
