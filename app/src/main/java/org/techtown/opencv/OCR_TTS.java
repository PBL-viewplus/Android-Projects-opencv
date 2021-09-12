package org.techtown.opencv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.opencv.android.OpenCVLoader;

import java.io.File;


import com.bumptech.glide.request.RequestOptions;


public class OCR_TTS extends AppCompatActivity {

    private static final int REQ_CODE_SELECT_IMAGE = 100;
    private Bitmap originBitmap;
    private Bitmap changeBitmap;
    private ImageView originImageView;
    private Button mLoadImageBtn;
    private TextView mTextResult;
    private Button load_camera_btn;
    String dataPath = "";

    // tesseract 객체 생성
    Tesseract tesseract = new Tesseract();

    // 갤러리 객체 생성
    Gallery gallery = new Gallery();

    // opencv 객체 생성
    OpenCV opencv = new OpenCV();

    // 카메라 객체 생성
    Camera camera = new Camera();

    // tts 객체 생성
    TTS_controller tts = new TTS_controller();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String value = intent.getExtras().getString("value");
        if (value=="camera"){
            System.out.println("camera");
        }
        if (value == "gallery"){
            System.out.println("gallery");
        }

        setContentView(R.layout.ocr_tts);
        originImageView = findViewById(R.id.origin_iv);
        mLoadImageBtn = findViewById(R.id.load_image_btn);
        load_camera_btn = findViewById(R.id.load_camera_btn);
        mTextResult = findViewById(R.id.textview);

        // 카메라 권한 체크
        Permission permission = new Permission();
        permission.permissioncheck(getApplicationContext());

        // tesseract 언어 데이터 경로
        dataPath = getFilesDir()+ "/tesseract/";
        tesseract.checkFile(new File(dataPath+"tessdata/"),"kor", dataPath, getApplicationContext());
        tesseract.checkFile(new File(dataPath+"tessdata/"),"eng", dataPath, getApplicationContext());

        // tesseract 객체 초기화
        tesseract.tessInit(dataPath);

        // TTS 객체 초기화
        tts.initTTS(getApplicationContext());

        // 사진 찍기 버튼. 버튼 수정 필요 -이름겹침
        load_camera_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.cameraStart(getApplicationContext(), intent);
                startActivityForResult(intent, 2);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!gallery.hasPermissions(gallery.PERMISSIONS,getApplicationContext())) {
                requestPermissions(gallery.PERMISSIONS, gallery.PERMISSIONS_REQUEST_CODE);
            }
        }

        // 이미지 업로드 버튼
        mLoadImageBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
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
        requestOptions = requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);//캐시비우기

        float degree=0; //회전필요한 각도

        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {//카메라 실행
             try {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 String path = camera.imageFilePath;
                 options.inSampleSize = 4;

                 originBitmap = BitmapFactory.decodeFile(path, options);
                 changeBitmap = BitmapFactory.decodeFile(path, options);

                 camera.exifInterface();
//               originBitmap = camera.rotate(originBitmap,camera.exifDegree);
//               changeBitmap = camera.rotate(changeBitmap,camera.exifDegree);

                 degree=camera.exifDegree;
                 camera.fileOpen(getApplicationContext(), originBitmap);

             } catch (Exception e) {
                 e.printStackTrace();
             }
        }

        if (requestCode == REQ_CODE_SELECT_IMAGE&& resultCode == Activity.RESULT_OK){//갤러리 실행
            try {
                String path = gallery.getImagePathFromURI(data.getData(), getApplicationContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                originBitmap = BitmapFactory.decodeFile(path, options);
                changeBitmap = BitmapFactory.decodeFile(path, options);

                gallery.exifInterface();
                degree=gallery.exifDegree;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (originImageView != null) {
            opencv.detectEdgeUsingJNI(originBitmap, changeBitmap);
        }

        //사진 회전
        requestOptions.transform(new RotateTransform(degree));
        Glide.with(this).load(originBitmap).apply(requestOptions).into(originImageView);

        tesseract.processImage(changeBitmap, mTextResult);
        tts.speakOut(mTextResult);
    }

    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();

//        changeBitmap.recycle();
        if (changeBitmap != null) {
            changeBitmap = null;
        }

        if (tts != null){
            tts.ttsDestory();
        }
    }
}
