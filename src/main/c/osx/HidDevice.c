/*
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
#include "HidPunk.h"
#include "jniOsxHidDevice.h"


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidDevice_getHidDictionary
(JNIEnv* env, jclass clazz, jlong ptr)
{
	kern_return_t kret;
	CFMutableDictionaryRef dict;
	io_object_t dev = (io_object_t)ptr;
	
	if(dev == 0) {
		hidpunk_throwNullPointerException(env, "Null device");
		return 0;
	}
	
	kret = IORegistryEntryCreateCFProperties (dev, &dict, kCFAllocatorDefault, kNilOptions);
	if(kret != KERN_SUCCESS || dict == NULL) {
		hidpunk_throwIOKitException(env, "Failed to create device properties dictionary.");
		return 0;
	}
	
	return *(jlong*)&dict;
}


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidDevice_getUsbDictionary
(JNIEnv* env, jclass clazz, jlong ptr)
{
	CFMutableDictionaryRef dict;
	io_registry_entry_t parent1, parent2;
	io_object_t dev = (io_object_t)ptr;
	
	if(dev == 0) {
		hidpunk_throwNullPointerException(env, "Null device");
		return 0;
	}
	
	if( IORegistryEntryGetParentEntry(dev, kIOServicePlane, &parent1) != KERN_SUCCESS ||
	    IORegistryEntryGetParentEntry(parent1, kIOServicePlane, &parent2) != KERN_SUCCESS ||
		IORegistryEntryCreateCFProperties(parent2, &dict, kCFAllocatorDefault, kNilOptions) ||
		dict == NULL)
    {
		hidpunk_throwIOKitException(env, "Failed to create device properties dictionary.");
		return 0;
	}
	
	return *(jlong*)&dict;
}


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxHidDevice_queryDeviceInfo
(JNIEnv* env, jclass clazz, jlong devPtr, jlong hidDictPtr, jlong usbDictPtr, jobject outBuf)
{
	char* buf;
	CFMutableDictionaryRef usbDict;
	CFMutableDictionaryRef hidDict;
	CFTypeRef ref;
	int bufIdx = 0;
	io_object_t dev = (io_object_t)devPtr;
		
	if(dev == 0) {
		hidpunk_throwNullPointerException(env, "NULL device");
		return;
	}
	
	usbDict = *(CFMutableDictionaryRef*)&usbDictPtr;
	hidDict = *(CFMutableDictionaryRef*)&hidDictPtr;
	
	if(usbDict == NULL || hidDict == NULL) {
		hidpunk_throwNullPointerException(env, "NULL dictionary");
		return;
	}
	
	buf = (char*)(*env)->GetDirectBufferAddress(env, outBuf);
	if(buf == NULL) {
		hidpunk_throwNullPointerException(env, "NULL buffer");
		return;
	}

	//Get transport.
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDTransportKey));
	if(ref)
		CFStringGetCString(ref, buf + bufIdx, 256, kCFStringEncodingUTF8);
	bufIdx += 256;

	//VendorID
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDVendorIDKey));
	if(!ref)
		ref = CFDictionaryGetValue(usbDict, CFSTR("idVendor"));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	//ProductID
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDProductIDKey));
	if(!ref)
		ref = CFDictionaryGetValue(usbDict, CFSTR("idProduct"));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	//Product version.
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDVersionNumberKey));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	//Manufacturer name
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDManufacturerKey));
	if(ref)
		ref = CFDictionaryGetValue(usbDict, CFSTR("USB Vendor Name"));
	if(ref)
		CFStringGetCString(ref, buf + bufIdx, 256, kCFStringEncodingUTF8);
	bufIdx += 256;

	//Product name
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDProductKey));
	if(!ref)
		ref = CFDictionaryGetValue(usbDict, CFSTR("USB Product Name"));
	if(ref)
		CFStringGetCString(ref, buf + bufIdx, 256, kCFStringEncodingUTF8);
	bufIdx += 256;

	//Serial
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDSerialNumberKey));
	if(ref)
		CFStringGetCString(ref, buf + bufIdx, 256, kCFStringEncodingUTF8);
	bufIdx += 256;

	//LocationID
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDLocationIDKey));
	if(!ref)
		ref = CFDictionaryGetValue(usbDict, CFSTR("locationID"));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	//Usage page
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDPrimaryUsagePageKey));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	//Usage
	ref = CFDictionaryGetValue(hidDict, CFSTR(kIOHIDPrimaryUsageKey));
	if(ref)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

}



JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidDevice_openInterface
(JNIEnv* env, jclass clazz, jlong ptr)
{
	IOReturn ioret;
	HRESULT plugret;
	IOCFPlugInInterface** plugin = NULL;	
	IOHIDDeviceInterface ** interface = NULL;
	SInt32 score = 0;
	
	io_object_t dev = (io_object_t)ptr;
	if(dev == 0) {
		hidpunk_throwNullPointerException(env, "Null device");
		return 0;
	}
	
	//Create intermediate plugin.
	ioret = IOCreatePlugInInterfaceForService( dev, 
											   kIOHIDDeviceUserClientTypeID,
											   kIOCFPlugInInterfaceID,
											   &plugin,
											   &score);
											   
	if(ioret != kIOReturnSuccess) {
		hidpunk_throwIOKitException(env, "Failed at IOCreatePlugInInterfaceForService");
		return 0;
	}
	
	//Use plugin to create device interface.
	plugret = (*plugin)->QueryInterface( plugin,
										 CFUUIDGetUUIDBytes(kIOHIDDeviceInterfaceID),
										 (void*)&interface );
	
	IODestroyPlugInInterface(plugin);
	
	if(plugret != S_OK || interface == NULL) {
		hidpunk_throwIOKitException(env, "Failed to query HID device interface from plugin interface");
		return 0;
	}
	
	ioret = (*interface)->open(interface, 0);
	
	if(ioret != kIOReturnSuccess) {
		hidpunk_throwIOKitException(env, "Failed to open interface.");
		return 0;
	}
	
	return *(jlong*)&interface;
}

