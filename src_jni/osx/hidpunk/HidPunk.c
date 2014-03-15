/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include <CoreServices/CoreServices.h>


pthread_key_t hidpunk_envKey;

jclass hidpunk_byteBufferClass = NULL;
jmethodID hidpunk_allocateDirectMethod = NULL;
jmethodID hidpunk_clearMethod = NULL;
jmethodID hidpunk_limitMethod = NULL;

jclass hidpunk_eventClass = NULL;
jfieldID hidpunk_eventTypeField = NULL;
jfieldID hidpunk_eventCookieField = NULL;
jfieldID hidpunk_eventValueField = NULL;
jfieldID hidpunk_eventTimestampField = NULL;
jfieldID hidpunk_eventLongValueSizeField = NULL;
jfieldID hidpunk_eventLongValueField = NULL;
jfieldID hidpunk_eventStaleField = NULL;


void
hidpunk_throwException
(JNIEnv* env, const char* exClass, const char* msg)
{
    jclass clazz;
    (*env)->ExceptionDescribe(env);
    (*env)->ExceptionClear(env);
    clazz = (*env)->FindClass(env, exClass);
    
    if(clazz == NULL) {
        printf("Unable to throw exception.\n");
        printf("%s\n", msg);
    }else{
        if(msg) {
            (*env)->ThrowNew(env, clazz, msg);
        }else{
            (*env)->ThrowNew(env, clazz, "");
        }
    }
}


int
hidpunk_initLibrary(JNIEnv* env)
{
    jclass localClass;

    if(pthread_key_create(&hidpunk_envKey, NULL)) {
        hidpunk_throwIOKitException(env, "Failed to create thread local storage.");
        return -1;
    }
    
    
    localClass = (*env)->FindClass(env, "java/nio/ByteBuffer");
    if(localClass)
        hidpunk_byteBufferClass = (*env)->NewGlobalRef(env, localClass);
    if(hidpunk_byteBufferClass == NULL) {
        hidpunk_throwLinkageError(env, "java.lang.ByteBuffer not found");
        return -1;
    }
    
    hidpunk_allocateDirectMethod = (*env)->GetStaticMethodID( env, 
                                             hidpunk_byteBufferClass,
                                             "allocateDirect",
                                             "(I)Ljava/nio/ByteBuffer;");
    
    if(hidpunk_allocateDirectMethod == NULL) {
        hidpunk_throwLinkageError(env, "java.lang.ByteBuffer.allocateDirect(int) not found."); 
        return -1;
    }
    
    hidpunk_clearMethod = (*env)->GetMethodID( env,
                                               hidpunk_byteBufferClass,
                                               "clear",
                                               "()Ljava/nio/Buffer;");
    
    if(hidpunk_clearMethod == NULL) {
        hidpunk_throwLinkageError(env, "java.lang.ByteBuffer.clear() not found.");
        return -1;
    }
    
    hidpunk_limitMethod = (*env)->GetMethodID( env,
                                               hidpunk_byteBufferClass,
                                               "limit",
                                               "(I)Ljava/nio/Buffer;");
    
    if(hidpunk_limitMethod == NULL) {
        hidpunk_throwLinkageError(env, "java.lang.ByteBuffer.limit() not found.");
        return -1;
    }

    
    localClass = (*env)->FindClass(env, "bits/hidpunk/HidEvent");
    if(localClass)
        hidpunk_eventClass = (*env)->NewGlobalRef(env, localClass);
    if(hidpunk_eventClass == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent not found");
        return -1;
    }
    
    hidpunk_eventTypeField = (*env)->GetFieldID(env, hidpunk_eventClass, "mType", "I");
    hidpunk_eventCookieField = (*env)->GetFieldID(env, hidpunk_eventClass, "mCookie", "I");
    hidpunk_eventValueField = (*env)->GetFieldID(env, hidpunk_eventClass, "mValue", "I");
    hidpunk_eventTimestampField = (*env)->GetFieldID(env, hidpunk_eventClass, "mTimestampMicros", "J");
    hidpunk_eventLongValueSizeField = (*env)->GetFieldID(env, hidpunk_eventClass, "mLongValueSize", "I");
    hidpunk_eventLongValueField = (*env)->GetFieldID(env, hidpunk_eventClass, "mLongValue", "Ljava/nio/ByteBuffer;");
    hidpunk_eventStaleField = (*env)->GetFieldID(env, hidpunk_eventClass, "mStale", "Z");
    
    if(hidpunk_eventTypeField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mType field not found");
        return -1;
    }
    
    if(hidpunk_eventCookieField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mCookie field not found");
        return -1;
    }

    if(hidpunk_eventValueField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mValue field not found");
        return -1;
    }

    if(hidpunk_eventTimestampField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mTimestampMicros field not found");
        return -1;
    }

    if(hidpunk_eventLongValueSizeField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mLongValueSize field not found");
        return -1;
    }
    
    if(hidpunk_eventLongValueField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mLongValue field not found");
        return -1;
    }
    
    if(hidpunk_eventStaleField == NULL) {
        hidpunk_throwLinkageError(env, "bits.hidpunk.HidEvent.mStale field not found");
        return -1;
    }
            
    
    return 0;
}



void 
hidpunk_throwCFException
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "bits/hidpunk/osx/CFException", msg);
}

void 
hidpunk_throwIOKitException
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "bits/hidpunk/osx/IOKitException", msg);
}

void 
hidpunk_throwNullPointerException
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "java/lang/NullPointerException", msg);
}

void
hidpunk_throwLinkageError
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "java/lang/LinkageError", msg);
}

void hidpunk_throwOutOfMemoryError
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "java/lang/OutOfMemoryError", msg);
}

void
hidpunk_throwIllegalArgumentException
(JNIEnv* env, const char* msg)
{
    hidpunk_throwException(env, "java/lang/IllegalArgumentException", msg);
}



jobject
hidpunk_newDirectBuffer
(JNIEnv* env, SInt32 size)
{
	jobject ret;
	
	if(hidpunk_allocateDirectMethod == NULL)
		return NULL;
	
	ret = (*env)->CallStaticObjectMethod( env, 
	                                      hidpunk_byteBufferClass,
			        					  hidpunk_allocateDirectMethod,
			        					  size );
	
	return ret;
}	
	

int
hidpunk_loadHidEvent
(JNIEnv* env, IOHIDEventStruct* in, jobject out) 
{
    Nanoseconds nanos = AbsoluteToNanoseconds(in->timestamp);
    jobject buf;
    jlong capacity;
    void* bufData;
    
    (*env)->SetIntField(env, out, hidpunk_eventTypeField, (jint)(in->type));
    (*env)->SetIntField(env, out, hidpunk_eventCookieField, (jint)(in->elementCookie));
    (*env)->SetIntField(env, out, hidpunk_eventValueField, (jint)(in->value));
    (*env)->SetLongField(env, out, hidpunk_eventTimestampField, *(jlong*)(&nanos));
    (*env)->SetIntField(env, out, hidpunk_eventLongValueSizeField, (jint)(in->longValueSize));
    (*env)->SetBooleanField(env, out, hidpunk_eventStaleField, 0);
    
    if(in->longValueSize <= 0)
        return 0;
    
    buf = (*env)->GetObjectField(env, out, hidpunk_eventLongValueField);
    if(buf == NULL) {
        capacity = 0;
    }else{
        capacity = (*env)->GetDirectBufferCapacity(env, buf);
    }

    if(capacity < in->longValueSize) {
        buf = hidpunk_newDirectBuffer(env, (jlong)in->longValueSize);
        
        if(buf == NULL) {
            hidpunk_throwIOKitException(env, "Failed to create ByteBuffer");
            return 0xA877;
        }

        (*env)->SetObjectField(env, out, hidpunk_eventLongValueField, buf);
    }else{
        (*env)->CallObjectMethod(env, buf, hidpunk_clearMethod);
        (*env)->CallObjectMethod(env, buf, hidpunk_limitMethod, in->longValueSize);
        
        if((*env)->ExceptionOccurred(env))
            return 0;
    }
    
    bufData = (*env)->GetDirectBufferAddress(env, buf);
    memcpy(buf, in->longValue, in->longValueSize);
    return 0;
}


int
hidpunk_nullifyEvent
(JNIEnv* env, jobject out)
{
    (*env)->SetBooleanField(env, out, hidpunk_eventStaleField, 1);
    return 0;
}


void hidpunk_setThreadEnv(JNIEnv* env) {
    pthread_setspecific(hidpunk_envKey, env);
}


JNIEnv* hidpunk_getThreadEnv() {
    return (JNIEnv*)pthread_getspecific(hidpunk_envKey);
}
