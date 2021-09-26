package org.techtown.opencv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.InputStream;


import com.bumptech.glide.request.RequestOptions;


public class OCR_TTS extends AppCompatActivity {

    private Bitmap originBitmap;
    private Bitmap changeBitmap;
    private ImageButton backButton;
    private ImageView originImageView;
    private ImageButton minusButton;
    private ImageButton plusButton;
    private TextView mTextResult;
    private ImageButton pictureButton;
    private String dataPath = "";

    Tesseract tesseract = new Tesseract();
    Gallery gallery = new Gallery();
    OpenCV opencv = new OpenCV();
    Camera camera = new Camera();
    TTS_controller tts = new TTS_controller();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyze_picture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        originImageView = findViewById(R.id.origin_iv);
        mTextResult = findViewById(R.id.text_result);
        minusButton = findViewById(R.id.btn_minus);
        plusButton = findViewById(R.id.btn_plus);
        backButton = findViewById(R.id.btn_back);
        pictureButton = findViewById(R.id.btn_picture);

        // Mainactivity의 intent value값 받아 버튼 종류 결정
        Intent intent = getIntent();
        int value = intent.getExtras().getInt("value");
        if (value == 1){
            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.picturebutton));
        }
        if (value == 2){
            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.gallerybutton));
        }

        // 카메라 권한 체크
        Permission permission = new Permission();
        permission.permissioncheck(getApplicationContext());

        // 갤러리 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!gallery.hasPermissions(gallery.PERMISSIONS,getApplicationContext())) {
                requestPermissions(gallery.PERMISSIONS, gallery.PERMISSIONS_REQUEST_CODE);
            }
        }

        // tesseract 언어 데이터 경로
        dataPath = getFilesDir()+ "/tesseract/";
        tesseract.checkFile(new File(dataPath+"tessdata/"),"kor", dataPath, getApplicationContext());
        tesseract.checkFile(new File(dataPath+"tessdata/"),"eng", dataPath, getApplicationContext());

        // tesseract 객체 초기화
        tesseract.tessInit(dataPath);

        // TTS 객체 초기화
        tts.initTTS(getApplicationContext());

        // 사진 찍기, 갤러리 버튼
        pictureButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (value == 1){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    camera.cameraStart(getApplicationContext(), intent);
                    startActivityForResult(intent, 1);
                }
                if (value == 2){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
            }
        });

        // 돋보기 +버튼
        minusButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTextResult.setTextSize(mTextResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
            }
        });

        // 돋보기 -버튼
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextResult.setTextSize(mTextResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density + 10);
            }
        });

        // 뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            opencv.mIsOpenCVReady = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        RequestOptions requestOptions= new RequestOptions();
        requestOptions = requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE); // 캐시비우기

        float degree = 0; // 회전에 필요한 각도

        // 카메라 실행
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                String path = camera.imageFilePath;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                originBitmap = BitmapFactory.decodeFile(path, options);
                changeBitmap = BitmapFactory.decodeFile(path, options);

                camera.exifInterface();
                degree = camera.exifDegree;
                camera.fileOpen(getApplicationContext(), originBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

            AsyncTask<InputStream,String,String> ocrTask = new AsyncTask<InputStream, String, String>() {
                String result;
                // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                ProgressDialog progressDialog = new ProgressDialog(OCR_TTS.this); // 실시간 진행 상태 알림

                @Override // 작업시작
                protected void onPreExecute() {
                    progressDialog.show();
                } // progressdialog 생성

                @Override // 진행중
                protected String doInBackground(InputStream... inputStreams) {
                    tts.speakOutString("분석중입니다");
                    publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거

                    result = tesseract.processImage(changeBitmap);
                    return result;
                }

                @SuppressLint("StaticFieldLeak")
                @Override // 종료
                protected void onPostExecute(String s){

                    if(TextUtils.isEmpty(s)){
                        progressDialog.dismiss();
                        mTextResult.setText("인식할 수 없습니다");
                        tts.speakOut(mTextResult);
                    }
                    else {
                        progressDialog.dismiss();
                        mTextResult.setText(result);
                        tts.speakOut(mTextResult);
                    }
                }

                @Override
                protected void onProgressUpdate(String... values){
                    progressDialog.setMessage(values[0]);
                }
            };
            ocrTask.execute();
        }

        // 갤러리 실행
        if (requestCode == 2 && resultCode == Activity.RESULT_OK){
            try {
                String path = gallery.getImagePathFromURI(data.getData(), getApplicationContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                originBitmap = BitmapFactory.decodeFile(path, options);
                changeBitmap = BitmapFactory.decodeFile(path, options);

                gallery.exifInterface();
                degree = gallery.exifDegree;

            } catch (Exception e) {
                e.printStackTrace();
            }

            AsyncTask<InputStream,String,String> ocrTask = new AsyncTask<InputStream, String, String>() {
                String result;
                // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                ProgressDialog progressDialog = new ProgressDialog(OCR_TTS.this); // 실시간 진행 상태 알림

                @Override // 작업시작
                protected void onPreExecute() {
                    progressDialog.show();
                } // progressdialog 생성

                @Override // 진행중
                protected String doInBackground(InputStream... inputStreams) {
                    tts.speakOutString("분석중입니다");
                    publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거

                    result = tesseract.processImage(changeBitmap);
                    return result;
                }

                @SuppressLint("StaticFieldLeak")
                @Override // 종료
                protected void onPostExecute(String s){

                    if(TextUtils.isEmpty(s)){
                        progressDialog.dismiss();
                        mTextResult.setText("인식할 수 없습니다");
                        tts.speakOut(mTextResult);
                    }
                    else {
                        progressDialog.dismiss();
                        mTextResult.setText(result);
                        tts.speakOut(mTextResult);
                    }
                }

                @Override
                protected void onProgressUpdate(String... values){
                    progressDialog.setMessage(values[0]);
                }
            };
            ocrTask.execute();
        }

        if (originBitmap != null) {
            opencv.detectEdgeUsingJNI(originBitmap, changeBitmap);

            // 사진 회전
            requestOptions.transform(new RotateTransform(degree));
            Glide.with(this).load(originBitmap).apply(requestOptions).into(originImageView);
        }
    }

    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();

        if (changeBitmap != null) {
            changeBitmap = null;
        }

        if (tts != null){
            tts.ttsDestory();
        }
    }
}