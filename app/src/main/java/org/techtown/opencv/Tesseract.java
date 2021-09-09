package org.techtown.opencv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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


    // tesseract api 초기화
    public void tessInit(String dataPath){
        String lang = "kor+eng";
        tess = new TessBaseAPI();
        tess.init(dataPath, lang);
    }

    // tesseract 파일 체크 함수
    public void checkFile(File dir, String lang, String dataPath, Context context){
        if(!dir.exists() && dir.mkdirs()){
            copyFiles(lang, dataPath, context);
        }
        if(dir.exists()){
            String datafilePath = dataPath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilePath);
            if(!datafile.exists()) {
                copyFiles(lang, dataPath, context);
            }
        }
    }

    // tessdata 파일 복제 함수
    private void copyFiles(String lang, String dataPath, Context context)
    {
        try{
            String filepath = dataPath + "/tessdata/" + lang + ".traineddata";

            AssetManager assetManager = context.getAssets();
            InputStream inStream = assetManager.open("tessdata/" + lang + ".traineddata");
            OutputStream outStream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }
            inStream.close();
            outStream.flush();
            outStream.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // 분석 결과 제공 함수
    public void processImage(Bitmap img, TextView text){
        tess.setImage(img);
        String OCRresult = tess.getUTF8Text();
        text.setText(OCRresult);
    }
}
