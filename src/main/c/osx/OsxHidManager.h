/* DO NOT EDIT THIS FILE - it is machine generated */
#include <JavaVM/jni.h>
/* Header for class bits_hidpunk_osx_OsxHidManager */

#ifndef _Included_bits_hidpunk_osx_OsxHidManager
#define _Included_bits_hidpunk_osx_OsxHidManager
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     bits_hidpunk_osx_OsxHidManager
 * Method:    initLibrary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_OsxHidManager_initLibrary
  (JNIEnv *, jclass);

/*
 * Class:     bits_hidpunk_osx_OsxHidManager
 * Method:    getIOMasterPort
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidManager_getIOMasterPort
  (JNIEnv *, jobject);

/*
 * Class:     bits_hidpunk_osx_OsxHidManager
 * Method:    getDeviceIterator
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxHidManager_getDeviceIterator
  (JNIEnv *, jobject, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
