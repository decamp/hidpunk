/* DO NOT EDIT THIS FILE - it is machine generated */
#include <JavaVM/jni.h>
/* Header for class bits_hidpunk_osx_CFRunLoop */

#ifndef _Included_bits_hidpunk_osx_CFRunLoop
#define _Included_bits_hidpunk_osx_CFRunLoop
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopGetCurrent
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopGetCurrent
  (JNIEnv *, jclass);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopRun
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopRun
  (JNIEnv *, jclass);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopWakeUp
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopWakeUp
  (JNIEnv *, jclass, jlong);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopStop
  (JNIEnv *, jclass, jlong);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopAddSource
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopAddSource
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopRemoveSource
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopRemoveSource
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopAddTimer
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopAddTimer
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     bits_hidpunk_osx_CFRunLoop
 * Method:    runLoopRemoveTimer
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bits_hidpunk_osx_CFRunLoop_runLoopRemoveTimer
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif