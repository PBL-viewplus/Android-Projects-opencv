package com.pbl.viewplus;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
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

    // 흑백, 이진화, (엣지 검출), contour 추출
    public void cvtColor(Bitmap changeBitmap){
        OpenCVLoader.initDebug();

        Mat bmpMat = new Mat();
        Mat imgGray = new Mat();
        Mat imgBny = new Mat();
        Mat gradThresh = new Mat(); //matrix for threshold
        Mat hierarchy = new Mat(); //matrix for contour hierachy
//        Mat imgCny = new Mat();
        Utils.bitmapToMat(changeBitmap, bmpMat);

        Imgproc.cvtColor(bmpMat, imgGray, Imgproc.COLOR_BGR2GRAY); // GrayScale
//        Imgproc.Canny(imgGray, imgCny, 10, 100, 3, true); // Canny Edge 검출
        Imgproc.threshold(imgGray, imgBny, 125, 255, Imgproc.THRESH_BINARY); // Binary // global threshold
        // Binary = 특정값인 임계값 이하의 값을 갖는 픽셀을 검정색으로 변환하고, 임계값 이상의 값을 갖는 픽셀을 흰색으로 변환해 흑백 이미지를 얻음
        // maxval = 입력 이미지의 픽셀값이 thresh보다 클 경우 결과 이미지의 픽셀값은 maxval이 됨. thresh보다 작을 경우 0(흰색).

        // contour 추출 코드
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.adaptiveThreshold(imgGray, gradThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 12); // adaptive threshold
//        Imgproc.findContours(imgGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

//        if (contours.size()>0) {
//            for (int idx = 0; idx < contours.size(); idx++) {
//                Rect rect = Imgproc.boundingRect(contours.get(idx));
//                if (rect.height > 10 && rect.width > 40 && !(rect.width >= 512 - 5 && rect.height >= 512 - 5)){
//                }
//                    Imgproc.rectangle(bmpMat, new Point(rect.br().x - rect.width, rect.br().y - rect.height)
//                            , rect.br()
//                            , new Scalar(0, 255, 0), 5);
//                }
//
//            }

        Utils.matToBitmap(imgBny, changeBitmap);
    }

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
