//
// Created by pika on 2023/1/5.
//

#ifndef SIGNAL_SIGNAL_ACTION_H
#define SIGNAL_SIGNAL_ACTION_H
#include <jni.h>
#define SIGNAL_CRASH_STACK_SIZE (1024 * 128)
#define TAG "hi_signal"

void init_with_signal(JNIEnv *env, jclass klass,
                      jintArray signals,void (*handler)(int, struct siginfo *, void *));



#endif //SIGNAL_SIGNAL_ACTION_H

