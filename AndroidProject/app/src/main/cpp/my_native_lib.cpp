#include <jni.h>
#include <string>

extern "C" JNIEXPORT jint JNICALL
Java_livan_zhao_androidproject_natives_MyNativeClass_myNativeMethod(JNIEnv* env, jobject obj, jint param) {
    return param * 2;
}

