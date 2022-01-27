#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

//extern "C" JNIEXPORT jstring JNICALL
//Java_org_techtown_opencv_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

extern "C"
JNIEXPORT void JNICALL
Java_org_techtown_opencv_OpenCV_ConvertRGBtoGray(JNIEnv *env, jobject thiz, jlong inputImage,
                                                       jlong outputimage, jint th1, jint th2) {
    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputimage;

    cvtColor(inputMat, outputMat, COLOR_RGB2GRAY);
}