//
// Created by litao on 2018/12/17.
//

#ifndef AVPRACTICE_LOG_H
#define AVPRACTICE_LOG_H

#include <android/log.h>

#undef __LOG_MODULE__
#define __LOG_MODULE__  "AVPractice"

// __VA_ARGS__是打印的日志内容
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, __LOG_MODULE__, __VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, __LOG_MODULE__, __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, __LOG_MODULE__, __VA_ARGS__);

#endif //AVPRACTICE_LOG_H
