#include "HidPunk.h"
#include "jniOsxHidManager.h"

JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxHidManager_initLibrary
(JNIEnv* env, jclass clazz)
{
    hidpunk_initLibrary(env);
}


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidManager_getIOMasterPort
(JNIEnv* env, jclass clazz)
{
    mach_port_t masterPort = 0;
    IOReturn ioret = IOMasterPort(bootstrap_port, &masterPort);
    
    if(ioret != kIOReturnSuccess) {
        hidpunk_throwIOKitException(env, "Failed to retrieve IO master port.");
        return 0;
    }
 
    return (jlong)masterPort;
}



JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidManager_getDeviceIterator
(JNIEnv* env, jclass clazz, jlong masterPort, jlong dictPtr)
{
	IOReturn ioret = kIOReturnSuccess;
	CFMutableDictionaryRef dict;
	io_iterator_t iter = 0;
	
	dict = *(CFMutableDictionaryRef*)&dictPtr;
	if(dict == NULL) {
		hidpunk_throwNullPointerException(env, "NULL matcher");
		return 0;
	}
	
	//Because IOServiceGetMatchingServices will CFRelease the dict.
	CFRetain(dict);
	ioret = IOServiceGetMatchingServices(masterPort, dict, &iter);
	if(ioret != kIOReturnSuccess) {
		hidpunk_throwIOKitException(env, "Failed to create IO device iterator.");
		return 0;
	}
	
	return (jlong)iter;
}

