package org.techtown.opencv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Tesseract {

    TessBaseAPI tess;
    public String mDataPath = ""; //언어데이터가 있는 경로

//    // tesseract api 초기화
//    public void tessInit(String dataPath){
//        String lang = "kor+eng";
//        tess = new TessBaseAPI();
//        tess.init(dataPath, lang);
//    }

//    // tesseract 파일 체크 함수
//    public void checkFile(File dir, String lang, String dataPath, Context context){
//        if(!dir.exists() && dir.mkdirs()) {
//            copyFiles(lang, dataPath, context);
//        }
//        if(dir.exists()){
//            String datafilePath = dataPath + "/tessdata/" + lang + ".traineddata";
//            File datafile = new File(datafilePath);
//            if(!datafile.exists()) {
//                copyFiles(lang, dataPath, context);
//            }
//        }
//    }

//    // tessdata 파일 복제 함수
//    private void copyFiles(String lang, String dataPath, Context context)
//    {
//        try{
//            String filepath = dataPath + "/tessdata/" + lang + ".traineddata";
//
//            AssetManager assetManager = context.getAssets();
//            InputStream inStream = assetManager.open("tessdata/" + lang + ".traineddata");
//            OutputStream outStream = new FileOutputStream(filepath);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = inStream.read(buffer)) != -1) {
//                outStream.write(buffer, 0, read);
//            }
//            inStream.close();
//            outStream.flush();
//            outStream.close();
//
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }

//    // 분석 결과 리턴 함수
//    public String processImage(Bitmap img){
//        tess.setImage(img);
//        String OCRresult = tess.getUTF8Text();
//        return OCRresult;
//    }

    //check file on the device
    public void checkFile(File dir, String Language, Context context) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(Language, context);
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if (dir.exists()) {
            String datafilepath = mDataPath + "tessdata/" + Language + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(Language, context);
            }
        }
    }

    //copy file to device
    private void copyFiles(String Language, Context context) {
        try {
            String filepath = mDataPath + "/tessdata/" + Language + ".traineddata";
            AssetManager assetManager = context.getAssets();
            InputStream instream = assetManager.open("tessdata/"+Language+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
