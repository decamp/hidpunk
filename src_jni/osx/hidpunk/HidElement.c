#include "HidPunk.h"
#include "jniOsxHidElement.h"


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidElement_getElementValue
(JNIEnv* env, jclass clazz, jlong dictPtr)
{
	CFMutableDictionaryRef dict = *(CFMutableDictionaryRef*)&dictPtr;
	CFTypeRef topRef;

	if(dict == NULL) {
		hidpunk_throwNullPointerException(env, "NULL dictionary");
		return 0;
	}

	topRef = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementKey));
	if(topRef == NULL) {
		hidpunk_throwIOKitException(env, "Error fetching device element.");
		return 0;
	}

	return *(jlong*)&topRef;
}


JNIEXPORT jint JNICALL 
Java_bits_hidpunk_osx_OsxHidElement_getArrayLength
(JNIEnv* env, jclass clazz, jlong arrPtr)
{
	CFTypeRef ref = *(CFTypeRef*)&arrPtr;
	CFTypeID type;
	
	if(ref == NULL) {
		hidpunk_throwNullPointerException(env, "NULL reference");
		return -1;
	}
	
	type = CFGetTypeID(ref);
	if(type != CFArrayGetTypeID())
		return -1;
	
	return CFArrayGetCount(ref);
}


JNIEXPORT jlong JNICALL 
Java_bits_hidpunk_osx_OsxHidElement_getArrayElement
(JNIEnv* env, jclass clazz, jlong arrPtr, jint idx)
{
	CFTypeRef arr = *(CFTypeRef*)&arrPtr;
	CFTypeRef el;
	
	if(arr == NULL) {
		hidpunk_throwNullPointerException(env, "NULL array");
		return 0;
	}

	el = CFArrayGetValueAtIndex(arr, idx);
	return *(jlong*)&el;
}


JNIEXPORT jboolean JNICALL 
Java_bits_hidpunk_osx_OsxHidElement_queryElementInfo
(JNIEnv* env, jclass clazz, jlong elPtr, jobject outBuf)
{
	CFMutableDictionaryRef dict = *(CFMutableDictionaryRef*)&elPtr;
	char* buf = (*env)->GetDirectBufferAddress(env, outBuf);
	int bufIdx = 0;
	Boolean retVal = false;
	CFTypeRef ref;
	
	if(dict == NULL) {
		hidpunk_throwNullPointerException(env, "NULL element");
		return 0;
	}
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementTypeKey));
	if(ref != NULL) {
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
		if(*(long*)(buf + bufIdx) == kIOHIDElementTypeCollection)
			retVal = true;
	}
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementUsagePageKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementUsageKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementCookieKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementMinKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementMaxKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementScaledMinKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementScaledMaxKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementSizeKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementIsRelativeKey));
	if(ref != NULL)
		*(buf + bufIdx) = (char)CFBooleanGetValue(ref);
	bufIdx += 1;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementIsWrappingKey));
	if(ref != NULL)
		*(buf + bufIdx) = (char)CFBooleanGetValue(ref);
	bufIdx += 1;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementIsNonLinearKey));
	if(ref != NULL)
		*(buf + bufIdx) = (char)CFBooleanGetValue(ref);
	bufIdx += 1;
	
#ifdef kIOHIDElementHasPreferredStateKey
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementHasPreferredStateKey));
#else // Mac OS X 10.0 has spelling error
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementHasPreferedStateKey));
#endif
	if(ref != NULL)
		*(buf + bufIdx) = (char)CFBooleanGetValue(ref);
	bufIdx += 1;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementHasNullStateKey));
	if(ref != NULL)
		*(buf + bufIdx) = (char)CFBooleanGetValue(ref);
	bufIdx += 1;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementUnitKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;

	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementUnitExponentKey));
	if(ref != NULL)
		CFNumberGetValue(ref, kCFNumberLongType, buf + bufIdx);
	bufIdx += 4;
	
	ref = CFDictionaryGetValue(dict, CFSTR(kIOHIDElementNameKey));
	if(ref != NULL)
		CFStringGetCString(ref, buf + bufIdx, 256, kCFStringEncodingUTF8);
	bufIdx += 256;
	
	//printf("BufLength: %d\n", bufIdx); //should be 305
	return retVal;
}
