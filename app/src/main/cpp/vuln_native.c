#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#define LOG_TAG "vuln_native"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// JNI: java signature -> Java_com_example_vulnlab_NativeActivity_processInput
JNIEXPORT jstring JNICALL
Java_com_example_vulnlab_NativeActivity_processInput(JNIEnv *env, jobject thiz, jstring input) {
    const char *in = (*env)->GetStringUTFChars(env, input, 0);

    char buf[64];
    // VULN: unsafe strcpy causes buffer overflow when input > 63 bytes (+NUL)
    // PoC intention: long input (e.g., 1024 'A's) may crash the app/native layer.
    strcpy(buf, in); /* VULN: unsafe strcpy causes buffer overflow */

    size_t len = strlen(buf);
    for (size_t i = 0; i < len / 2; i++) {
        char t = buf[i];
        buf[i] = buf[len - 1 - i];
        buf[len - 1 - i] = t;
    }

    jstring out = (*env)->NewStringUTF(env, buf);
    (*env)->ReleaseStringUTFChars(env, input, in);
    return out;
}

/*
// Safe variant (not exported)
static jstring safe_process(JNIEnv *env, jstring input) {
    const char *in = (*env)->GetStringUTFChars(env, input, 0);
    size_t inlen = strlen(in);
    char *buf = (char *)malloc(inlen + 1);
    if (!buf) {
        (*env)->ReleaseStringUTFChars(env, input, in);
        return (*env)->NewStringUTF(env, "alloc failed");
    }
    // Use snprintf or strncpy to avoid overflow
    snprintf(buf, inlen + 1, "%s", in);
    for (size_t i = 0; i < inlen / 2; i++) {
        char t = buf[i];
        buf[i] = buf[inlen - 1 - i];
        buf[inlen - 1 - i] = t;
    }
    jstring out = (*env)->NewStringUTF(env, buf);
    free(buf);
    (*env)->ReleaseStringUTFChars(env, input, in);
    return out;
}
*/
