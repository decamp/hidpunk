/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniOsxQueue.h"

JNIEXPORT jlong JNICALL Java_bits_hidpunk_osx_OsxQueue_allocateQueue
(JNIEnv* env, jclass clazz, jlong interPtr, jint size)
{
    IOHIDQueueInterface** queue;
    IOHIDDeviceInterface** interface = *(IOHIDDeviceInterface***)&interPtr;
    HRESULT result;
    
    if(interface == NULL) {
        hidpunk_throwNullPointerException(env, "NULL interface");
        return 0;
    }

    queue = (*interface)->allocQueue(interface);

    if(!queue) {
        hidpunk_throwIOKitException(env, "Failed to allocate HID interface queue.");
        return 0;
    }
    
    result = (*queue)->create(queue, 0, size);
    if(result) {
        char err[256];
        sprintf(err, "Failed to create HID interface queue: 0x%lX", result);
        hidpunk_throwIOKitException(env, err);
        (*queue)->Release(queue);
        return 0;
    }
    
    return *(jlong*)&queue;
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxQueue_startQueue
(JNIEnv* env, jobject self, jlong ptr)
{
    HRESULT result;
    IOHIDQueueInterface** queue = *(IOHIDQueueInterface***)&ptr;
    
    if(queue == NULL) {
        hidpunk_throwNullPointerException(env, "NULL queue");
        return;
    }
    
    result = (*queue)->start(queue);
    
    if(result) {
        char err[256];
        sprintf(err, "Failed to start queue: %ld", result);
        hidpunk_throwIOKitException(env, err);
    }
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxQueue_stopQueue
(JNIEnv* env, jobject self, jlong ptr)
{
    HRESULT result;
    IOHIDQueueInterface** queue = *(IOHIDQueueInterface***)&ptr;

    if(queue == NULL) {
        hidpunk_throwNullPointerException(env, "NULL queue");
        return;
    }

    result = (*queue)->stop(queue);

    if(result) {
        char err[256];
        sprintf(err, "Failed to stop queue: 0x%lX", result);
        hidpunk_throwIOKitException(env, err);
    }
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxQueue_clearQueue
(JNIEnv* env, jobject self, jlong ptr)
{
    HRESULT result = 0;
    IOHIDQueueInterface** q = *(IOHIDQueueInterface***)&ptr;
    IOHIDEventStruct event;
    AbsoluteTime zeroTime = {0,0};
    
    if(q == NULL) {
        hidpunk_throwNullPointerException(env, "NULL queue");
        return;
    }

    while(result == 0) {
        result = (*q)->getNextEvent(q, &event, zeroTime, 0);
    }
    
    if(result != kIOReturnUnderrun) {
        char err[256];
        sprintf(err, "Failed to clear queue: 0x%lX", result);
        hidpunk_throwIOKitException(env, err);
    }
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxQueue_addElement
(JNIEnv* env, jobject self, jlong ptr, jint cookie)
{
    HRESULT result;
    IOHIDQueueInterface** queue = *(IOHIDQueueInterface***)&ptr;
    IOHIDElementCookie cook = (IOHIDElementCookie)cookie;
    
    if(queue == NULL) {
        hidpunk_throwNullPointerException(env, "NULL queue");
        return;
    }
    
    result = (*queue)->addElement(queue, cook, 0);
    
    if(result) {
        char err[256];
        sprintf(err, "Failed to add element to queue: 0x%lX", result);
        hidpunk_throwIOKitException(env, err);
    }
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxQueue_dispose
(JNIEnv* env, jobject self, jlong ptr)
{
    HRESULT result;
    IOHIDQueueInterface** queue = *(IOHIDQueueInterface***)&ptr;

    if(queue == NULL)
        return;
    
    result = (*queue)->stop(queue);
    
    if(result) {
        if(result != kIOReturnNoDevice) {
            char err[256];
            sprintf(err, "Failed to release queue 0: 0x%lX", result);
            hidpunk_throwIOKitException(env, err);
            return;
        }
        
        result = 0;
        
    }else{
        //  Why does this throw exception?
        result = (*queue)->dispose(queue);
    
        //if(result) {
        //  char err[256];
        //  sprintf(err, "Failed to release queue: 0x%lX", result);
        //  hidpunk_throwIOKitException(env, err);
        //  return;
        //}
    
        result = (*queue)->Release(queue);
    }
    
    if(result) {
        fflush(0);
        char err[256];
        sprintf(err, "Failed to release queue: 0x%lX", result);
        hidpunk_throwIOKitException(env, err);
    }
}
