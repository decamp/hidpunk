#include "HidPunk.h"
#include "jniCFNotificationSource.h"


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_CFNotificationSource_createPort
(JNIEnv* env, jclass clazz, jlong masterPortPtr)
{
    mach_port_t masterPort = (mach_port_t)masterPortPtr;
    IONotificationPortRef port = IONotificationPortCreate(masterPort);
    return (jlong)port;
}



JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_CFNotificationSource_getLoopSource
(JNIEnv* env, jclass clazz, jlong portPtr)
{
    IONotificationPortRef port = (IONotificationPortRef)portPtr;
        
    if(port == NULL) {
        hidpunk_throwNullPointerException(env, "NULL NotificationPort");
        return 0;
    }
    
    CFRunLoopSourceRef source = IONotificationPortGetRunLoopSource(port);
    if(source)
        CFRetain(source);
    
    return *(jlong*)&source;
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_CFNotificationSource_destroyPort
(JNIEnv* env, jclass clazz, jlong portPtr)
{
    IONotificationPortRef port = (IONotificationPortRef)portPtr;
    if(port) {
        IONotificationPortDestroy(port);
    }
}
