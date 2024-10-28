#include <jni.h>
#include <string>
extern "C" {
#include "libavcodec//avcodec.h"
}


extern "C" JNIEXPORT jint JNICALL
Java_livan_zhao_androidproject_natives_MyNativeClass_myNativeMethod(JNIEnv* env, jobject obj, jint param) {
    return param * 2;
}

extern "C" JNIEXPORT jstring JNICALL
Java_livan_zhao_androidproject_natives_MyNativeClass_ffmpegInfo(JNIEnv* env, jobject obj) {
    std::string ffmpegInfo = avcodec_configuration();
    return env->NewStringUTF(ffmpegInfo.c_str());
}