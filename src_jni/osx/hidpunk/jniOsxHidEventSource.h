/* DO NOT EDIT THIS FILE - it is machine generated */
#include <JavaVM/jni.h>
/* Header for class bits_hidpunk_osx_OsxHidEventSource */

#ifndef _Included_bits_hidpunk_osx_OsxHidEventSource
#define _Included_bits_hidpunk_osx_OsxHidEventSource
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newPollingEventStruct
 * Signature: (JJI[I[Lbits/hidpunk/HidEvent;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newPollingEventStruct
  (JNIEnv *, jobject, jlong, jlong, jint, jintArray, jobjectArray);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newQueuedPollingEventStruct
 * Signature: (J[JJI[I[Lbits/hidpunk/HidEvent;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newQueuedPollingEventStruct
  (JNIEnv *, jobject, jlong, jlongArray, jlong, jint, jintArray, jobjectArray);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newPollingValueStruct
 * Signature: (JJI[ILjava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newPollingValueStruct
  (JNIEnv *, jobject, jlong, jlong, jint, jintArray, jobject);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newQueuedPollingValueStruct
 * Signature: (J[JJI[ILjava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newQueuedPollingValueStruct
  (JNIEnv *, jobject, jlong, jlongArray, jlong, jint, jintArray, jobject);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newAsyncEventStruct
 * Signature: (JJIILbits/hidpunk/HidEvent;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newAsyncEventStruct
  (JNIEnv *, jobject, jlong, jlong, jint, jint, jobject);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    newAsyncValueStruct
 * Signature: (JJIILjava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_newAsyncValueStruct
  (JNIEnv *, jobject, jlong, jlong, jint, jint, jobject);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    getRunLoopTimer
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_getRunLoopTimer
  (JNIEnv *, jobject, jlong);

/*
 * Class:     bits_hidpunk_osx_OsxHidEventSource
 * Method:    getRunLoopSource
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidEventSource_getRunLoopSource
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif