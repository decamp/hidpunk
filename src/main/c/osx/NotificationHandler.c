/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniNotificationHandler.h"

struct {
    jclass mClass;
    jobject mSelf;
    jmethodID mCallback;
    io_iterator_t mIter;
    io_object_t mDevice;
} typedef NotificationData;



void 
hidpunk_freeNotifyStruct(JNIEnv* env, NotificationData* s)
{
    if(s == NULL || s->mSelf == NULL)
        return;
    
    (*env)->DeleteGlobalRef(env, s->mSelf);
    s->mSelf = NULL;
    
    if(s->mClass)
        (*env)->DeleteGlobalRef(env, s->mClass);
    
    if(s->mIter)
        IOObjectRelease(s->mIter);
    
    if(s->mDevice)
        IOObjectRelease(s->mDevice);
    
    free(s);
}


static void 
hidpunk_matchCallback
(void *refCon, io_iterator_t iterator)
{
    JNIEnv* env = hidpunk_getThreadEnv();
    NotificationData* d = (NotificationData*)refCon;
    int dev;
    
    if(env == NULL || d == NULL || d->mClass == NULL || d->mSelf == NULL || d->mCallback == NULL)
        return;
    
    while((dev = IOIteratorNext(iterator))) {
        (*env)->CallVoidMethod( env, 
                                d->mSelf, 
                                d->mCallback, 
                                bits_hidpunk_osx_NotificationHandler_CODE_MATCHED, 
                                dev);
        
        if((*env)->ExceptionOccurred(env)) {
            //Clear out iterator
            while((dev = IOIteratorNext(iterator))) {}
            break;
        }
    }
}


static void
hidpunk_terminateCallback
(void *refCon, io_service_t service, uint32_t messageType, void *messageArgument)
{
    JNIEnv* env = hidpunk_getThreadEnv();
    NotificationData* d = (NotificationData*)refCon;
    int code, dev;
    
    if(messageType != kIOMessageServiceIsTerminated)
        return;
    
    if(env == NULL || d == NULL || d->mClass == NULL || d->mSelf == NULL || d->mCallback == NULL)
        return;
    
    if(d->mDevice == 0) {
        code = bits_hidpunk_osx_NotificationHandler_CODE_ERROR;
        dev = 0;
    }else{
        code = bits_hidpunk_osx_NotificationHandler_CODE_TERMINATED;
        dev = d->mDevice;
    }
    
    (*env)->CallVoidMethod( env,
                            d->mSelf,
                            d->mCallback,
                            code,
                            dev);
}




JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_NotificationHandler_allocateStruct
(JNIEnv* env, jclass clazz)
{
    NotificationData* d = malloc(sizeof(*d));
    if(d == NULL) {
        hidpunk_throwOutOfMemoryError(env, "");
        return 0;
    }
    
    memset(d, 0, sizeof(*d));
    return *(jlong*)&d;
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_NotificationHandler_initStruct
(JNIEnv* env, jclass clazz, jlong structPtr, jobject self)
{
    NotificationData* d = *(NotificationData**)&structPtr;
    
    if(d == NULL) {
        hidpunk_throwNullPointerException(env, "NULL NotificationHandler struct");
        return;
    }

    if(self == NULL) {
        hidpunk_throwNullPointerException(env, "NULL NotificationHandler object");
        return;
    }
    
    d->mSelf = (*env)->NewGlobalRef(env, self);
    d->mClass = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, self));
    
    if(d->mClass == NULL) {
        hidpunk_throwLinkageError(env, "Could not find NotificationHandler class.");
        return;
    }
    
    d->mCallback = (*env)->GetMethodID(env, d->mClass, "callback", "(II)V");

    if(d->mCallback == NULL) {
        hidpunk_throwLinkageError(env, "Could not find NotificationHandler.callback(int, int) method.");
        return;
    }
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_NotificationHandler_freeStruct
(JNIEnv* env, jobject self, jlong ptr)
{
    NotificationData* d = *(NotificationData**)&ptr;
    if(d == NULL)
        return;
    
    hidpunk_freeNotifyStruct(env, d);
}


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_NotificationHandler_addFoundNotification
(JNIEnv* env, jclass clazz, jlong structPtr, jlong portPtr, jlong dictPtr)
{
    NotificationData* d = *(NotificationData**)&structPtr;
    IONotificationPortRef port = (IONotificationPortRef)portPtr;
    CFMutableDictionaryRef dict = *(CFMutableDictionaryRef*)&dictPtr;
    IOReturn result;
    
    if(port == NULL) {
        hidpunk_throwNullPointerException(env, "NULL notification port");
        return 0;
    }
    
    if(d == NULL) {
        hidpunk_throwNullPointerException(env, "NULL NotificationHandler struct");
        return 0;
    }
    
    if(dict == NULL) {
        hidpunk_throwNullPointerException(env, "NULL HID Matcher");
        return 0;
    }
    
    CFRetain(dict);
    result = IOServiceAddMatchingNotification( port,                       
                                               kIOMatchedNotification,     
                                               dict,
                                               hidpunk_matchCallback,
                                               d,
                                               &(d->mIter));

    if(result) {
        char err[256];
        sprintf(err, "Failed call to IOServiceAddMatchingNotification: 0x%X", result);
        hidpunk_throwIOKitException(env, err);
        return 0;
    }
    
    return d->mIter;    
}


JNIEXPORT jboolean JNICALL 
Java_bits_hidpunk_osx_NotificationHandler_addRemovedNotification
(JNIEnv* env, jclass clazz, jlong structPtr, jlong portPtr, jlong devID)
{
    IONotificationPortRef port = (IONotificationPortRef)portPtr;
    NotificationData* d = *(NotificationData**)&structPtr;
    io_object_t device = (io_object_t)devID;
    IOReturn result;
    
    if(port == NULL) {
        hidpunk_throwNullPointerException(env, "NULL notification port");
        return false;
    }
    
    if(d == NULL) {
        hidpunk_throwNullPointerException(env, "NULL NotificationHandler struct");
        return false;
    }
    
    if(devID == 0) {
        hidpunk_throwNullPointerException(env, "NULL IODevice reference");
        return false;
    }
    
    result = IOServiceAddInterestNotification( port,
                                               device,
                                               kIOGeneralInterest,
                                               hidpunk_terminateCallback,
                                               d,
                                               &(d->mDevice));
    
    if(result) {
        if(result == kIOReturnUnsupported) {
            //Device already terminated.
            return true;
        }else{
            char err[256];
            sprintf(err, "Failed call to IOServiceAddInterestNotification: 0x%X", result);
            hidpunk_throwIOKitException(env, err);
        }
    }
    
    return false;
}
