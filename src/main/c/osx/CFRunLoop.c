/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniCFRunLoop.h"

JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopGetCurrent
(JNIEnv* env, jclass clazz)
{
    CFRunLoopRef ret = CFRunLoopGetCurrent();
    if(ret) {
        CFRetain(ret);
    }
    return *(jlong*)&ret;
}


JNIEXPORT jboolean JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopRun
(JNIEnv* env, jclass clazz)
{
    SInt32 ret;
    hidpunk_setThreadEnv(env);
    ret = CFRunLoopRunInMode(kCFRunLoopDefaultMode, 120.0, false);
    return (jboolean)(ret == kCFRunLoopRunFinished);
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopWakeUp
(JNIEnv* env, jclass clazz, jlong ptr)
{
    CFRunLoopRef ref = *(CFRunLoopRef*)&ptr;
    if(ref == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }
    
    CFRunLoopWakeUp(ref);    
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopStop
(JNIEnv* env, jclass clazz, jlong ptr)
{
    CFRunLoopRef ref = *(CFRunLoopRef*)&ptr;
    if(ref == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }
    
    CFRunLoopStop(ref);
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopAddSource
(JNIEnv* env, jclass clazz, jlong ptr, jlong sourcePtr)
{
    CFRunLoopRef loop = *(CFRunLoopRef*)&ptr;
    CFRunLoopSourceRef source = *(CFRunLoopSourceRef*)&sourcePtr;
    
    if(loop == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }
    
    if(source == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoopSourcer");
        return;
    }
        
    CFRunLoopAddSource(loop, source, kCFRunLoopDefaultMode);
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopRemoveSource
(JNIEnv* env, jclass clazz, jlong ptr, jlong sourcePtr)
{
    CFRunLoopRef loop = *(CFRunLoopRef*)&ptr;
    CFRunLoopSourceRef source = *(CFRunLoopSourceRef*)&sourcePtr;

    if(loop == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }

    if(source == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoopSourcer");
        return;
    }

    CFRunLoopRemoveSource(loop, source, kCFRunLoopDefaultMode);
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopAddTimer
(JNIEnv* env, jclass clazz, jlong ptr, jlong timerPtr)
{
    CFRunLoopRef loop = *(CFRunLoopRef*)&ptr;
    CFRunLoopTimerRef timer = *(CFRunLoopTimerRef*)&timerPtr;

    if(loop == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }

    if(timer == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoopTimer");
        return;
    }

    CFRunLoopAddTimer(loop, timer, kCFRunLoopDefaultMode);   
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFRunLoop_runLoopRemoveTimer
(JNIEnv* env, jclass clazz, jlong ptr, jlong timerPtr)
{
    CFRunLoopRef loop = *(CFRunLoopRef*)&ptr;
    CFRunLoopTimerRef timer = *(CFRunLoopTimerRef*)&timerPtr;

    if(loop == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoop");
        return;
    }

    if(timer == NULL) {
        hidpunk_throwNullPointerException(env, "NULL RunLoopTimer");
        return;
    }

    CFRunLoopRemoveTimer(loop, timer, kCFRunLoopDefaultMode);   
}
