#include "HidPunk.h"
#include "jniOsxHidInterface.h"
#include <CoreServices/CoreServices.h>


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxHidInterface_close
(JNIEnv* env, jobject self, jlong ptr)
{   
    IOReturn ioret;
    IOHIDDeviceInterface** interface = *(IOHIDDeviceInterface***)&ptr;
    if(interface == NULL)
        return;
    
    ioret = (*interface)->close(interface);
    if(ioret != kIOReturnSuccess && ioret != kIOReturnNotOpen)
        hidpunk_throwIOKitException(env, "Failed to close IOHIDDeviceInterface.");
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxHidInterface_release
(JNIEnv* env, jobject self, jlong ptr)
{
    IOReturn ioret;
    IOHIDDeviceInterface** interface = *(IOHIDDeviceInterface***)&ptr;
    if(interface == NULL)
        return;

    ioret = (*interface)->Release(interface);
    if(ioret != kIOReturnSuccess)
        hidpunk_throwIOKitException(env, "Failed to release IOHIDDeviceInterface.");
}


JNIEXPORT jint JNICALL 
Java_bits_hidpunk_osx_OsxHidInterface_getElementValue
(JNIEnv* env, jobject self, jlong interPtr, jint cookie)
{
    HRESULT result;
    IOHIDEventStruct hidEvent;
    IOHIDDeviceInterface** interface = *(IOHIDDeviceInterface***)&interPtr;

    result = (*interface)->getElementValue(interface, (IOHIDElementCookie)cookie, &hidEvent);

    if(result) {
        char err[256];
        sprintf(err, "Failed to retrieve HID element data: 0x%08X", result);
        hidpunk_throwIOKitException(env, err);
        return 0;
    }
    
    return hidEvent.value;
}



JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxHidInterface_getElementEvent
(JNIEnv* env, jobject self, jlong interPtr, jint cookie, jobject out)
{
	HRESULT result;
	IOHIDEventStruct hidEvent;
	IOHIDDeviceInterface** interface = *(IOHIDDeviceInterface***)&interPtr;
	
	result = (*interface)->getElementValue(interface, (IOHIDElementCookie)cookie, &hidEvent);
	
	if(result) {
	    char err[256];
	    sprintf(err, "Failed to retrieve HID element data: 0x%08X", result);
	    hidpunk_throwIOKitException(env, err);
	    return;
	}
	
	result = hidpunk_loadHidEvent(env, &hidEvent, out);
	
	if(result) {
	    char err[256];
	    sprintf(err, "Failed to load HID element data into HidEvent object: 0x%08X", result);
	    hidpunk_throwIOKitException(env, err);
	    return;
	}
}





