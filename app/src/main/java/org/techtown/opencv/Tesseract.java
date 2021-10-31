package org.techtown.opencv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;


public class Tesseract {

    TessBaseAPI tess;
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭
    public String TAG="hello";

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

    // 분석 결과 리턴 함수
    @RequiresApi(api = Build.VERSION_CODES.M)
    public String processImage(Bitmap img){
        tess.setImage(img);
        String OCRresult = tess.getUTF8Text();

        //암호화 후 복호화 실행
//        String dec="";
//        try{
//            if (!AES.isExistKey(alias)) {
//                AES.generateKey(alias);
//            }
//            SecretKey secretKey = AES.getKeyStoreKey(alias);
//            String[] enc = AES.encByKeyStoreKey(secretKey, tess.getUTF8Text());
//            Log.d(TAG, "암호화 결과 : " + enc[0]);
//            Log.d(TAG, "암호화 IV : " + enc[1]);
//            dec = AES.decByKeyStoreKey(secretKey, enc[0], enc[1]);
//            Log.d(TAG, "복호화 결과 : " + dec);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //추출한 정보에대해 암호화하고 복호화해서 읽어주고 다시 암호화하는 기술 적용
        //무조건 출력된 결과 다 암, 복, 다시 암호화 진행.

        //만약 개인정보 ㅇ-> 걍 팝업창으로 개인정보가 포함되어있습니다. 결과를 열람하시겠습니까?- 아니요하면 결과,tts 안보여줌 (디비)
        //마스킹-> 개인정보가 들어있는 결과들만 암호화 진행?-- (디비아니면)

        //아니오 시 마스킹. 마스킹할것만 뽑고 암호화해서 디비에 저장하고 예를 누를시에 복호화해서 보여지게?
        //디비에 계속 쌓이는 문제는? 다시 안꺼낼건데 저장할 필요성이 무엇인가(????). - 하루지나면 폐기? 바로바로 폐기하는게 더 나음?

        //history...... 이전거 최근3일 보여주는거. ->디비: 텍스트 전체를 저장. ->이거면 위에둘다 해결가능

        //? 마스킹할거면 결과들 암호화할필요가 있는가?

        //return dec;
        return OCRresult;


    }
}
