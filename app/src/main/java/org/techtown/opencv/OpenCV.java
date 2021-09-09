package org.techtown.opencv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class OpenCV {

    public boolean mIsOpenCVReady = false;

    // native-lib.cpp 함수
    public native void ConvertRGBtoGray(long inputImage, long outputImage, int th1, int th2);
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

     // canny 적용 함수
//    public void detectEdge(Bitmap inputimg, ImageView outputimg) {
//        Mat src = new Mat();
//        Utils.bitmapToMat(inputimg, src);
//        Mat edge = new Mat();
//        Imgproc.Canny(src, edge, 50, 150);
//        Utils.matToBitmap(edge, inputimg);
//        src.release();
//        edge.release();
//        outputimg.setImageBitmap(inputimg);
//    }

    // detectEdgeJNI, ConvertRGBtoGray 적용 함수
    public void detectEdgeUsingJNI(Bitmap originBitmap, Bitmap changeBitmap) {
        if (!mIsOpenCVReady) {
            return;
        }

        Mat src = new Mat();
        Utils.bitmapToMat(originBitmap, src);//mat로 넣어주고
        Mat edge = new Mat();

        ConvertRGBtoGray(src.getNativeObjAddr(), edge.getNativeObjAddr(), 50, 150);//회색이미지로 변환
        Utils.matToBitmap(edge, changeBitmap);//변환된걸 changeBitmap에 넣어줌
        //changeImageView.setImageBitmap(changeBitmap);//변환된 이미지 띄우기

//        Mat src = new Mat();
//        Utils.bitmapToMat(originBitmap, src);//mat로 넣어주고
//        originImageView.setImageBitmap(changeBitmap);//오리지널이미지 띄우기
//        Mat edge = new Mat();
//
//        //detectEdgeJNI(src.getNativeObjAddr(), edge.getNativeObjAddr(), 50, 150);
//        ConvertRGBtoGray(src.getNativeObjAddr(), edge.getNativeObjAddr(), 50, 150);
//        Utils.matToBitmap(edge, originBitmap);

    }
}
