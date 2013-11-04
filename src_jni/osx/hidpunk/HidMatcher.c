#include "jniOsxHidMatcher.h"
#include "HidPunk.h"

CFStringRef 
hidpunk_findKey
(int key)
{
	CFStringRef ret = NULL;
	
	switch((long)key) {
	case bits_hidpunk_osx_OsxHidMatcher_KEY_USAGE_PAGE:
		ret = CFSTR(kIOHIDPrimaryUsagePageKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_USAGE:
		ret = CFSTR(kIOHIDPrimaryUsageKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_TRANSPORT:
		ret = CFSTR(kIOHIDTransportKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_VENDOR_ID:
		ret = CFSTR(kIOHIDVendorIDKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_PRODUCT_ID:
		ret = CFSTR(kIOHIDProductIDKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_VERSION_NUM:
		ret = CFSTR(kIOHIDVersionNumberKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_VENDOR_NAME:
		ret = CFSTR(kIOHIDManufacturerKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_PRODUCT_NAME:
		ret = CFSTR(kIOHIDProductKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_SERIAL_NUM:
		ret = CFSTR(kIOHIDSerialNumberKey);
		break;
	case bits_hidpunk_osx_OsxHidMatcher_KEY_LOCATION_ID:
		ret = CFSTR(kIOHIDLocationIDKey);
		break;
	}

	return ret;
}


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidMatcher_create
(JNIEnv* env, jclass clazz)
{
	CFMutableDictionaryRef dict = IOServiceMatching(kIOHIDDeviceKey);
	
	if(dict == NULL)
		return 0;
	
	CFRetain(dict);
	return *(jlong*)&dict;
}


JNIEXPORT void JNICALL
Java_bits_hidpunk_osx_OsxHidMatcher_setLongValue
(JNIEnv* env, jclass clazz, jlong ptr, jint key, jlong val)
{
	CFMutableDictionaryRef dict;
	CFNumberRef num;
	CFStringRef dictKey;
	long cval = (long)val;
	
	if(!ptr) {
		hidpunk_throwNullPointerException(env, "");
		return;
	}
	
	dict = *(CFMutableDictionaryRef*)&ptr;
	dictKey = hidpunk_findKey((int)key);
	
	if(dictKey == NULL)
		return;
	
	num = CFNumberCreate(kCFAllocatorDefault, kCFNumberLongType, &cval);
	CFDictionarySetValue(dict, dictKey, num);
	CFRelease(num);
}


JNIEXPORT void JNICALL
Java_bits_hidpunk_osx_OsxHidMatcher_setStringValue
(JNIEnv* env, jclass clazz, jlong ptr, jint key, jstring val)
{
	CFMutableDictionaryRef dict;
	CFStringRef dictValue;
	CFStringRef dictKey;
	jsize valLength;
	const char* valChars;
		
	if(!ptr) {
		hidpunk_throwNullPointerException(env, "");
		return;
	}
		
	dict = *(CFMutableDictionaryRef*)&ptr;
	dictKey = hidpunk_findKey((int)key);
		
	if(dictKey == NULL)
		return;
	
	valLength = (*env)->GetStringUTFLength(env, val);
	valChars = (*env)->GetStringUTFChars(env, val, NULL);
	
	if(valChars == NULL)  {
		hidpunk_throwCFException(env, "Failed to get string characters.");
		return;
	}
	
	dictValue = CFStringCreateWithBytes( kCFAllocatorDefault, 
										 (const UInt8*)valChars,
										 valLength,
										 kCFStringEncodingUTF8,
										 false);
		
	(*env)->ReleaseStringUTFChars(env, val, valChars);
	
	if(dictValue == NULL) {
		hidpunk_throwCFException(env, "Failed to create CFString.");
		return;
	}
	
	CFDictionarySetValue(dict, dictKey, dictValue);
	CFRelease(dictValue);

}