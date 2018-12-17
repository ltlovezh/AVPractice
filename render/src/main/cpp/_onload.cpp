//
// Created by litao on 2018/12/17.
//


#include <jni.h>
#include <string>
#include "Log.h"

#define PACKAGE_NAME  "com/ltlovezh/avpractice/render/NativeLayer"

#undef __LOG_MODULE__
#define __LOG_MODULE__  "AVPractice.onload"
#define        ARRAY_ELEMENTS_NUM(p)        ((int) sizeof(p) / sizeof(p[0]))

jclass g_class = nullptr;

jstring JNICALL stringFromNative(JNIEnv *env, jclass clazz) {
    std::string hello = "Hello from AV Render";
    return env->NewStringUTF(hello.c_str());
}

static JNINativeMethod methods[] = {
        {"stringFromNative", "()Ljava/lang/String;", (void *) stringFromNative},
};

static int register_native_methods(JNIEnv *env) {
    LOGI("register_native_methods");
    if (env->RegisterNatives(g_class, methods, ARRAY_ELEMENTS_NUM(methods)) < 0) {
        return -1;
    }
    return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("JNI_OnLoad");

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass(PACKAGE_NAME);
    if (clazz == nullptr) {
        LOGE("find class : %s fail", PACKAGE_NAME);
        return JNI_EVERSION;
    }
    g_class = (jclass) env->NewGlobalRef(clazz);
    env->DeleteLocalRef(clazz);

    int result = register_native_methods(env);
    if (0 != result) {
        LOGE("native methods register failed");
    }
    return JNI_VERSION_1_6;
}

