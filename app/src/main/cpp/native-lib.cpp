#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>

using namespace cv;

#define LOG_TAG "EDGE_NATIVE"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_nativebridge_NativeBridge_initNative(
        JNIEnv *, jobject) {
LOGI("OpenCV Native init");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_nativebridge_NativeBridge_processFrame(
        JNIEnv *env,
jobject,
jbyteArray yPlane_,
        jint width,
jint height,
        jbyteArray outRgba_
) {
jbyte *yPlane = env->GetByteArrayElements(yPlane_, nullptr);
jbyte *outRgba = env->GetByteArrayElements(outRgba_, nullptr);

Mat gray(height, width, CV_8UC1, (unsigned char *)yPlane);
Mat edges;
Canny(gray, edges, 80, 150);

Mat rgba(height, width, CV_8UC4);
rgba.setTo(Scalar(0,0,0,255));
rgba.setTo(Scalar(255,255,255,255), edges);

memcpy(outRgba, rgba.data, width * height * 4);

env->ReleaseByteArrayElements(yPlane_, yPlane, JNI_ABORT);
env->ReleaseByteArrayElements(outRgba_, outRgba, 0);
}
