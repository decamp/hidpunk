#include "HidPunk.h"
#include "jniOsxUtil.h"


JNIEXPORT void JNICALL 
Java_bits_hidpunk_osx_OsxUtil_freePointer
(JNIEnv* env, jclass clazz, jlong ptr)
{
    free(*(void**)&ptr);
}