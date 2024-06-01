//
// Created by pika on 2023/1/5.
//

#include <android/log.h>
#include <unistd.h>
#include <sys/eventfd.h>
#include <pthread.h>
#include "signal_action.h"
#include "signal_exception.h"
#include "errno.h"

#define JNI_CLASS_NAME "com/pika/lib_signal/SignalController"


JavaVM *javaVm = NULL;
static int notifier = -1;
static jclass callClass;

static void sig_func(int sig_num, struct siginfo *info, void *ptr) {
    uint64_t data;
    data = sig_num;
    __android_log_print(ANDROID_LOG_INFO, TAG, "catch signal %llu %d", data,notifier);

    if (notifier >= 0) {
        write(notifier, &data, sizeof data);
    }
}

static void* invoke_crash(void *arg){
    JNIEnv *env = NULL;
    if(JNI_OK != (*javaVm)->AttachCurrentThread(javaVm,&env,NULL)){
        return NULL;
    }
    uint64_t data;
    read(notifier,&data,sizeof data);
    jmethodID id = (*env)->GetStaticMethodID(env,callClass, "callNativeException", "(ILjava/lang/String;)V");
//    jstring nativeStackTrace  = (*env)->NewStringUTF(env,backtraceToLogcat());
    jstring nativeStackTrace = (*env)->NewStringUTF(env,"");
    jint signal_tag = data;
    (*env)->CallStaticVoidMethod(env,callClass, id,signal_tag,nativeStackTrace);
    (*env)->DeleteLocalRef(env,nativeStackTrace);


}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    javaVm = vm;
    jclass cls;
    if (NULL == vm) return -1;
    if (JNI_OK != (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6)) return -1;
    if (NULL == (cls = (*env)->FindClass(env, JNI_CLASS_NAME))) return -1;

    // 此时的cls仅仅是一个局部变量，如果错误引用会出现错误
    callClass = (*env)->NewGlobalRef(env, cls);


    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_pika_lib_1signal_SignalController_initWithSignals(JNIEnv *env, jclass clazz,
                                                              jintArray signals) {
    init_with_signal(env, clazz, signals, sig_func);
    notifier = eventfd(0,EFD_CLOEXEC);


    // 启动异步线程
    pthread_t thd;
    if(0 != pthread_create(&thd, NULL,invoke_crash, NULL)){
        handle_exception(env);
        close(notifier);
        notifier = -1;
    }
}