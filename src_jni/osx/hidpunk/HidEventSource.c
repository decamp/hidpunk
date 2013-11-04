#include "HidPunk.h"
#include "jniOsxHidEventSource.h"
#include "jniEventSourceStruct.h"

struct {
    IOHIDDeviceInterface** mInterface;
    IOHIDQueueInterface*** mQueues;
    
    jobject mSelf;
    jclass mClass;
    jmethodID mCallback;
    
    int mElCount;
    int mElOffset;
    IOHIDElementCookie* mCookies;
    jobject* mEvents;
    jobject mValueBuf;
    UInt32* mValuePtr;
    
    CFRunLoopTimerRef mTimer;
    CFRunLoopSourceRef mAsync;
    
    int mQueueInit;
    
} typedef EventSourceStruct;



void 
hidpunk_freeEventSourceStruct
(JNIEnv* env, EventSourceStruct* s) 
{
    if(s == NULL || s->mSelf == NULL)
        return;
    
    (*env)->DeleteGlobalRef(env, s->mSelf);
    s->mSelf = NULL;
    
    if(s->mQueues) {
        free(s->mQueues);
    }
    
    if(s->mClass) 
        (*env)->DeleteGlobalRef(env, s->mClass);
    
    if(s->mCookies) 
        free(s->mCookies);
    
    if(s->mEvents) {
        int i;
        for(i = 0; i < s->mElCount; i++) {
            (*env)->DeleteGlobalRef(env, s->mEvents[i]);
        }
        
        free(s->mEvents);
    }
    
    if(s->mValueBuf) 
        (*env)->DeleteGlobalRef(env, s->mValueBuf);
    
    if(s->mTimer) 
        CFRelease(s->mTimer);
    
    if(s->mAsync)
        CFRelease(s->mAsync);
    
    free(s);
}



static void 
eventCallback
(CFRunLoopTimerRef timer, void* info) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDEventStruct event;
    HRESULT result;
    int i;
    
    for(i = 0; i < s->mElCount; i++) {
        result = (*s->mInterface)->getElementValue(s->mInterface, s->mCookies[i], &event);

        if(!result) 
            result = hidpunk_loadHidEvent(env, &event, s->mEvents[i]);
        
        if(result) {
            (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result);
            return;
        }
    }
    
    (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, 0);
}


static void 
queuedEventCallback
(CFRunLoopTimerRef timer, void* info) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDEventStruct event;
    HRESULT result;
    AbsoluteTime zeroTime = {0,0};
    int i;
      
    while(1) {
        for(i = 0; i < s->mElCount; i++) {
            IOHIDQueueInterface** q = s->mQueues[i];
            result = (*q)->getNextEvent(q, &event, zeroTime, 0);
        
            if(result) {
                if(result == kIOReturnUnderrun) {
                    if(s->mQueueInit) {
                        if(i == 0)
                            return;
                            
                        result = hidpunk_nullifyEvent(env, s->mEvents[i]);
                    }else{
                        result = (*s->mInterface)->getElementValue(s->mInterface, s->mCookies[i], &event);
                        if(!result)
                            result = hidpunk_loadHidEvent(env, &event, s->mEvents[i]);
                    }
                }
            }else{
                result = hidpunk_loadHidEvent(env, &event, s->mEvents[i]);
            }
        
            if(result) {
                (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result);
                return;
            }
        }
    
        s->mQueueInit = 1;
        (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, 0);
    }
}


static void 
valueCallback
(CFRunLoopTimerRef timer, void* info) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDEventStruct event;
    HRESULT result;
    int i;
        
    for(i = 0; i < s->mElCount; i++) {
        result = (*s->mInterface)->getElementValue(s->mInterface, s->mCookies[i], &event);
        
        if(result) {
            (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result);
            return;
        }
        
        s->mValuePtr[i] = event.value;
    }
    
    (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, 0);
}


static void
queuedValueCallback
(CFRunLoopTimerRef timer, void* info) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDEventStruct event;
    HRESULT result;
    AbsoluteTime zeroTime = {0,0};
    int i;
    
    while(1) {    
        for(i = 0; i < s->mElCount; i++) {
            IOHIDQueueInterface** q = s->mQueues[i];
            result = (*q)->getNextEvent(q, &event, zeroTime, 0);
        
            if(result) {
                if(result == kIOReturnUnderrun) {
                    if(s->mQueueInit) {
                        if(i == 0)
                            return;
                    }else{
                        result = (*s->mInterface)->getElementValue(s->mInterface, s->mCookies[i], &event);
                        if(!result)
                            s->mValuePtr[i] = event.value;
                    
                        if(result) {
                            (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result);
                            return;
                        }
                    }
                }else{
                    (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result);
                    return;
                }
            }else{
                s->mValuePtr[i] = event.value;
            }
        }
    
        s->mQueueInit = 1;
        (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, 0);
    }
}


static void queuedAsyncEventCallback
(void* target, IOReturn result, void* info, void* sender) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDQueueInterface** q = s->mQueues[0];
    IOHIDEventStruct event;
    AbsoluteTime zeroTime = {0,0};
    
    if(!result) {
        while(1) {
            result = (*q)->getNextEvent(q, &event, zeroTime, 0);
            if(result)
                break;
            
            result = hidpunk_loadHidEvent(env, &event, s->mEvents[0]);
            if(result)
                break;

            (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result, s->mElOffset);
        }
    }
    
    if(result != kIOReturnUnderrun)
        (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result, s->mElOffset);
}


static void queuedAsyncValueCallback
(void* target, IOReturn result, void* info, void* sender) 
{
    JNIEnv* env = hidpunk_getThreadEnv();
    EventSourceStruct* s = (EventSourceStruct*)info;
    IOHIDQueueInterface** q = s->mQueues[0];
    IOHIDEventStruct event;
    AbsoluteTime zeroTime = {0,0};
    
    if(!result)
        result = (*q)->getNextEvent(q, &event, zeroTime, 0);
        
    while(!result) {
        s->mValuePtr[s->mElOffset] = event.value;
        (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result, s->mElOffset);
        result = (*q)->getNextEvent(q, &event, zeroTime, 0);
    }
        
    if(result != kIOReturnUnderrun)
        (*env)->CallVoidMethod(env, s->mSelf, s->mCallback, result, s->mElOffset);
}



int 
initStructSelf
(JNIEnv* env, EventSourceStruct* s, jclass self) 
{
    s->mSelf = (*env)->NewGlobalRef(env, self);
    s->mClass = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, self));
    return 0;
}



int 
initStructCookies
(JNIEnv* env, EventSourceStruct* s, int elCount, jintArray cookieArr) 
{
    int i;
    
    s->mCookies = (IOHIDElementCookie*)malloc(sizeof(IOHIDElementCookie) * elCount);
        
    if(s->mCookies == NULL) {
        hidpunk_throwNullPointerException(env, "Failed to create PollingEventSource");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    if((*env)->GetArrayLength(env, cookieArr) < elCount) {
        hidpunk_throwIllegalArgumentException(env, "Invalid element array.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    jint* cookies = (*env)->GetIntArrayElements(env, cookieArr, NULL);
    if(cookies == NULL) {
        hidpunk_throwIOKitException(env, "Failed to retrieve elements from Java array.");
        return 1;
    }
    
    for(i = 0; i < elCount; i++) {
        s->mCookies[i] = (IOHIDElementCookie)cookies[i];
    }

    (*env)->ReleaseIntArrayElements(env, cookieArr, cookies, JNI_ABORT);
    return 0;
}


int 
initStructQueues
(JNIEnv* env, EventSourceStruct* s, int elCount, jlongArray queuePtrs) 
{
    int i;
    
    if(queuePtrs == NULL || (*env)->GetArrayLength(env, queuePtrs) < elCount) {
        hidpunk_throwIllegalArgumentException(env, "Invalid queue array.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    s->mQueues = malloc(sizeof(IOHIDQueueInterface**) * elCount);

    if(s->mQueues == NULL) {
        hidpunk_throwIOKitException(env, "Failed to allocate memory for queues.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }

    jlong* qptrs = (*env)->GetLongArrayElements(env, queuePtrs, NULL);
    if(qptrs == NULL) {
        hidpunk_throwIOKitException(env, "Failed to retrieve queues from Java array.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    for(i = 0; i < elCount; i++) {
        jlong q = qptrs[i];
        s->mQueues[i] = *(IOHIDQueueInterface***)&q;
    }
    
    (*env)->ReleaseLongArrayElements(env, queuePtrs, qptrs, JNI_ABORT);
    return 0;
}


int initStructEvents(JNIEnv* env, EventSourceStruct* s, int elCount, jobjectArray eventArr) {
    int i;
    
    s->mEvents = malloc(sizeof(jobject) * elCount);
    if(s->mEvents == NULL) {
        hidpunk_throwNullPointerException(env, "Failed to create PollingEventSource");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }

    if(eventArr == NULL || (*env)->GetArrayLength(env, eventArr) < elCount) {
        hidpunk_throwIllegalArgumentException(env, "Invalid event object array.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    for(i = 0; i < elCount; i++) {
        s->mEvents[i] = (*env)->GetObjectArrayElement(env, eventArr, i);
        s->mEvents[i] = (*env)->NewGlobalRef(env, s->mEvents[i]);
    }
    
    return 0;
}


int initStructValueBuf(JNIEnv* env, EventSourceStruct* s, int elCount, jobject valueBuf) {
    if(valueBuf == NULL || (*env)->GetDirectBufferCapacity(env, valueBuf) < elCount * 4) {
        hidpunk_throwIllegalArgumentException(env, "Invalid event value buffer.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    s->mValueBuf = (*env)->NewGlobalRef(env, valueBuf);
    s->mValuePtr = (UInt32*)(*env)->GetDirectBufferAddress(env, s->mValueBuf);
    
    if(s->mValuePtr == NULL) {
        hidpunk_throwIOKitException(env, "Failed to retrieve value buffer memory address.");
        hidpunk_freeEventSourceStruct(env, s);
        return 1;
    }
    
    return 0;
}


int initStructCallback(JNIEnv* env, EventSourceStruct* s, int useEvents, int async) {
    if(async) {
        if(useEvents) {
            s->mCallback = (*env)->GetMethodID(env, s->mClass, "asyncEventCallback", "(II)V");
            if(s->mCallback == NULL) {
                hidpunk_throwLinkageError(env, "Could not find HidEventSource.asyncEventCallback(int, int) method");
                hidpunk_freeEventSourceStruct(env, s);
                return 1;
            }
        }else{
            s->mCallback = (*env)->GetMethodID(env, s->mClass, "asyncValueCallback", "(II)V");
            if(s->mCallback == NULL) {
                hidpunk_throwLinkageError(env, "Could not find HidEventSource.asyncValueCallback(int, int) method");
                hidpunk_freeEventSourceStruct(env, s);
                return 1;
            }
        }
    }else{
        if(useEvents) {
            s->mCallback = (*env)->GetMethodID(env, s->mClass, "eventCallback", "(I)V");
            if(s->mCallback == NULL) {
                hidpunk_throwLinkageError(env, "Could not find HidEventSource.eventCallback(int) method");
                hidpunk_freeEventSourceStruct(env, s);
                return 1;
            }
        }else{
            s->mCallback = (*env)->GetMethodID(env, s->mClass, "valueCallback", "(I)V");
            if(s->mCallback == NULL) {
                hidpunk_throwLinkageError(env, "Could not find HidEventSource.valueCallback(int) method");
                hidpunk_freeEventSourceStruct(env, s);
                return 1;
            }
        }
    }
    
    return 0;
}



// POLLING EVENT

JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_newPollingEventStruct
(JNIEnv* env,           
 jobject self,   
 jlong interPtr,
 jlong pollMicros, 
 jint elCount,          
 jintArray cookieArr, 
 jobjectArray eventArr)
{
    CFRunLoopTimerContext context = {0, NULL, NULL, NULL, NULL};
    CFTimeInterval pollTime = (double)pollMicros / 1000000.0;    
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = elCount;    
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCookies(env, s, elCount, cookieArr))
        return 0;
    
    if(initStructEvents(env, s, elCount, eventArr))
        return 0;
    
    if(initStructCallback(env, s, true, false))
        return 0;
    
    context.info = s;
        
    s->mTimer = CFRunLoopTimerCreate( kCFAllocatorDefault,
                                      CFAbsoluteTimeGetCurrent() + pollTime,
                                      pollTime,
                                      0,
                                      0,
                                      eventCallback,
                                      &context);

    if(s->mTimer == NULL) {
        hidpunk_throwCFException(env, "Failed to create CFRunLoopTimer");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }

    return *(jlong*)&s;
}


// QUEUED POLLING EVENT

JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_newQueuedPollingEventStruct
(JNIEnv* env,           
 jobject self,   
 jlong interPtr,
 jlongArray queuePtrs,   
 jlong pollMicros, 
 jint elCount,          
 jintArray cookieArr, 
 jobjectArray eventArr)
{
    CFRunLoopTimerContext context = {0, NULL, NULL, NULL, NULL};
    CFTimeInterval pollTime = (double)pollMicros / 1000000.0;    
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = elCount;    
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCookies(env, s, elCount, cookieArr))
        return 0;
    
    if(initStructQueues(env, s, elCount, queuePtrs))
        return 0;
    
    if(initStructEvents(env, s, elCount, eventArr))
        return 0;
    
    if(initStructCallback(env, s, true, false))
        return 0;
    
    context.info = s;
        
    s->mTimer = CFRunLoopTimerCreate( kCFAllocatorDefault,
                                      CFAbsoluteTimeGetCurrent() + pollTime,
                                      pollTime,
                                      0,
                                      0,
                                      queuedEventCallback,
                                      &context);

    if(s->mTimer == NULL) {
        hidpunk_throwCFException(env, "Failed to create CFRunLoopTimer");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }

    return *(jlong*)&s;
}


// POLLING VALUE

JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_newPollingValueStruct
(JNIEnv* env,           
 jobject self,   
 jlong interPtr,
 jlong pollMicros, 
 jint elCount,          
 jintArray cookieArr, 
 jobject valueBuf)
{
    CFRunLoopTimerContext context = {0, NULL, NULL, NULL, NULL};
    CFTimeInterval pollTime = (double)pollMicros / 1000000.0;    
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = elCount;    
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCookies(env, s, elCount, cookieArr))
        return 0;
    
    if(initStructValueBuf(env, s, elCount, valueBuf))
        return 0;
        
    if(initStructCallback(env, s, false, false))
        return 0;
    
    context.info = s;
        
    s->mTimer = CFRunLoopTimerCreate( kCFAllocatorDefault,
                                      CFAbsoluteTimeGetCurrent() + pollTime,
                                      pollTime,
                                      0,
                                      0,
                                      valueCallback,
                                      &context);

    if(s->mTimer == NULL) {
        hidpunk_throwCFException(env, "Failed to create CFRunLoopTimer");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }

    return *(jlong*)&s;
}


// QUEUED POLLING VALUE

JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_newQueuedPollingValueStruct
(JNIEnv* env,           
 jobject self,   
 jlong interPtr,
 jlongArray queuePtrs,   
 jlong pollMicros, 
 jint elCount,          
 jintArray cookieArr, 
 jobject valueBuf)
{
    CFRunLoopTimerContext context = {0, NULL, NULL, NULL, NULL};
    CFTimeInterval pollTime = (double)pollMicros / 1000000.0;    
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = elCount;    
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCookies(env, s, elCount, cookieArr))
        return 0;
    
    if(initStructQueues(env, s, elCount, queuePtrs))
        return 0;
    
    if(initStructValueBuf(env, s, elCount, valueBuf))
        return 0;
    
    if(initStructCallback(env, s, false, false))
        return 0;
    
    context.info = s;
        
    s->mTimer = CFRunLoopTimerCreate( kCFAllocatorDefault,
                                      CFAbsoluteTimeGetCurrent() + pollTime,
                                      pollTime,
                                      0,
                                      0,
                                      queuedValueCallback,
                                      &context);

    if(s->mTimer == NULL) {
        hidpunk_throwCFException(env, "Failed to create CFRunLoopTimer");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }

    return *(jlong*)&s;
}


//QUEUED ASYNC EVENT

JNIEXPORT jlong JNICALL
Java_bits_hidpunk_osx_OsxHidEventSource_newAsyncEventStruct
(JNIEnv* env,
 jobject self,
 jlong interPtr,
 jlong queuePtr,
 jint elOffset,
 jint cookie,
 jobject event)
{
    IOReturn ret;
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = 1;
    s->mElOffset = elOffset;
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCallback(env, s, true, true))
            return 0;
    
    s->mCookies = malloc(sizeof(IOHIDElementCookie));
    if(s->mCookies == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mCookies[0] = (IOHIDElementCookie)cookie;
    
    s->mQueues = malloc(sizeof(IOHIDQueueInterface**));
    if(s->mQueues == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mQueues[0] = *(IOHIDQueueInterface***)&queuePtr;
    
    if(event == NULL) {
        hidpunk_throwNullPointerException(env, "NULL HidEvent");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mEvents = malloc(sizeof(jobject));
    if(s->mEvents == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mEvents[0] = (*env)->NewGlobalRef(env, event);
    
    ret = (*s->mQueues[0])->createAsyncEventSource(s->mQueues[0], &(s->mAsync));
    if(s->mAsync == NULL) {
        hidpunk_throwIOKitException(env, "Failed to create asynchronous event source.");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    ret = (*s->mQueues[0])->setEventCallout( s->mQueues[0], 
                                             queuedAsyncEventCallback,
                                             NULL,
                                             s);
    
    if(ret) {
        hidpunk_throwIOKitException(env, "Failed to create asynchronous event source.");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
        
    
    return *(jlong*)&s;
}
    

//QUEUED ASYNC VALUE

JNIEXPORT jlong JNICALL
Java_bits_hidpunk_osx_OsxHidEventSource_newAsyncValueStruct
(JNIEnv* env,
 jobject self,
 jlong interPtr,
 jlong queuePtr,
 jint elOffset,
 jint cookie,
 jobject valueBuf)
{
    IOReturn ret;
    EventSourceStruct* s = (EventSourceStruct*)malloc(sizeof(*s));
    
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        return 0;
    }
    
    memset(s, 0, sizeof(*s));
    s->mInterface = *(IOHIDDeviceInterface***)&interPtr;
    s->mElCount = 1;
    s->mElOffset = elOffset;
    
    if(initStructSelf(env, s, self))
        return 0;
    
    if(initStructCallback(env, s, false, true))
                return 0;
        
    s->mCookies = malloc(sizeof(IOHIDElementCookie));
    if(s->mCookies == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mCookies[0] = (IOHIDElementCookie)cookie;
    
    s->mQueues = malloc(sizeof(IOHIDQueueInterface**));
    if(s->mQueues == NULL) {
        hidpunk_throwNullPointerException(env, "Out of memory");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    s->mQueues[0] = *(IOHIDQueueInterface***)&queuePtr;
    
    if(initStructValueBuf(env, s, 1, valueBuf))
        return 0;
    
    ret = (*s->mQueues[0])->createAsyncEventSource(s->mQueues[0], &(s->mAsync));
    if(s->mAsync == NULL) {
        hidpunk_throwIOKitException(env, "Failed to create asynchronous event source.");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }

    ret = (*s->mQueues[0])->setEventCallout( s->mQueues[0], 
                                             queuedAsyncValueCallback,
                                             NULL,
                                             s);

    if(ret) {
        hidpunk_throwIOKitException(env, "Failed to create asynchronous event source.");
        hidpunk_freeEventSourceStruct(env, s);
        return 0;
    }
    
    return *(jlong*)&s;
}
    


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_getRunLoopTimer
(JNIEnv* env, jobject obj, jlong structPtr)
{
    EventSourceStruct* s = *(EventSourceStruct**)&structPtr;
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "NULL EventSourceStruct");
        return 0;
    }

    CFRetain(s->mTimer);
    return *(jlong*)&(s->mTimer);
}



JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidEventSource_getRunLoopSource
(JNIEnv* env, jobject self, jlong structPtr)
{
    EventSourceStruct* s = *(EventSourceStruct**)&structPtr;
    if(s == NULL) {
        hidpunk_throwNullPointerException(env, "NULL EventSourceStruct");
        return 0;
    }
    
    CFRetain(s->mAsync);
    return *(jlong*)&(s->mAsync);
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_EventSourceStruct_destruct
(JNIEnv* env, jobject self, jlong structPtr)
{
    EventSourceStruct* s = *(EventSourceStruct**)&structPtr;
    hidpunk_freeEventSourceStruct(env, s);
}