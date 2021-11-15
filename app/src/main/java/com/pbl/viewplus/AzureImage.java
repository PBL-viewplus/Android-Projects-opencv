package com.pbl.viewplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;


public class AzureImage extends AppCompatActivity {
    private ImageView imageView;
    private TextView mTextResult;
    private ImageButton pictureButton;
    private Bitmap imgBitmap;
    private ImageButton minusButton;
    private ImageButton plusButton;
    private ImageButton backButton;
    private final String API_KEY = "d4e5bcc8873949e88fd2a12c19a5bcc5";
    private final String API_LINK = "https://westus.api.cognitive.microsoft.com/vision/v1.0";

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);
    TTS_controller tts = new TTS_controller();
    Camera camera = new Camera();
    Gallery gallery = new Gallery();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyze_picture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        imageView = (ImageView) findViewById(R.id.origin_iv);
        mTextResult = (TextView) findViewById(R.id.text_result);
        pictureButton = (ImageButton) findViewById(R.id.btn_picture);
        minusButton=(ImageButton) findViewById(R.id.btn_minus);
        plusButton=(ImageButton) findViewById(R.id.btn_plus);
        backButton=(ImageButton) findViewById(R.id.btn_back);

        // 카메라 권한 체크
        Permission permission = new Permission();
        permission.permissioncheck(getApplicationContext());

        // TTS 객체 초기화
        tts.initTTS(this, 0);

        // 초기 imageView 설정
        Bitmap sample = BitmapFactory.decodeResource(getResources(),R.drawable.sample);
        imageView.setImageBitmap(sample);

        //인텐트 받기
        Intent intent = getIntent();
        int value = intent.getExtras().getInt("value");
        if (value == 3){
            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.picturebutton));
        }
        if (value == 4){
            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.gallerybutton));
        }

        // 사진 찍기, 갤러리 버튼
        pictureButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (value == 3){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    camera.cameraStart(getApplicationContext(), intent);
                    startActivityForResult(intent, 3);
                }
                if (value == 4){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 4);
                }
            }
        });

        minusButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTextResult.setTextSize(mTextResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
            }
        });

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


    // onActivityResult(): sub activity에서 main activity로 넘어갈 때
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        RequestOptions requestOptions= new RequestOptions();
        requestOptions = requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE); // 캐시비우기

        float degree = 0; // 회전에 필요한 각도

        // 카메라 실행
        if (requestCode ==3  && resultCode == RESULT_OK) {
            try{
                String path = camera.imageFilePath;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                imgBitmap = BitmapFactory.decodeFile(path, options);
                camera.exifInterface();
                degree = camera.exifDegree;
                camera.fileOpen(getApplicationContext(), imgBitmap);

                imgBitmap = camera.getResizedBitmap(imgBitmap); // 해상도 조절
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                    ProgressDialog progressDialog = new ProgressDialog(AzureImage.this); // 실시간 진행 상태 알림

                    @Override // 작업시작
                    protected void onPreExecute() {
                        progressDialog.show();
                    } // progressdialog 생성

                    @Override // 진행중
                    protected String doInBackground(InputStream... inputStreams) {
                        try
                        {
                            tts.speakOutString("분석중입니다");
                            publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거
                            String[] features = {"Description"};
                            String[] details = {};

                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0],features,details);

                            String jsonResult = new Gson().toJson(result);
                            return jsonResult;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (VisionServiceException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    @SuppressLint("StaticFieldLeak")
                    @Override // 종료
                    protected void onPostExecute(String s){

                        if(TextUtils.isEmpty(s)){ // s가 null일때

                            mTextResult.setText("인식할 수 없습니다");
                            Toast.makeText(AzureImage.this,"API Return Empty Result",Toast.LENGTH_SHORT).show();
                            tts.speakOut(mTextResult);
                        }
                        else {
                            progressDialog.dismiss();
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                            StringBuilder result_Text = new StringBuilder();
                            for (Caption caption : result.description.captions)
                                result_Text.append(caption.text);

                            //파파고 번역
                            new Thread() {
                                @Override
                                public void run() {
                                    String word = result_Text.toString();
                                    Papago_translate papago = new Papago_translate();
                                    String resultWord = papago.getTranslation(word, "en", "ko");

                                    Bundle papagoBundle = new Bundle();
                                    papagoBundle.putString("resultWord", resultWord);

                                    Message msg = papago_handler.obtainMessage();
                                    msg.setData(papagoBundle);
                                    papago_handler.sendMessage(msg);
                                }
                            }.start();
                        }
                    }

                    @Override
                    protected void onProgressUpdate(String... values){
                        progressDialog.setMessage(values[0]);

                    }
                };
                visionTask.execute(inputStream);

                // 사진 회전
                requestOptions.transform(new RotateTransform(degree));
                Glide.with(this).load(imgBitmap).apply(requestOptions).into(imageView);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 갤러리 실행
        else if (requestCode == 4 && resultCode == RESULT_OK ) {
            try {
                String path = gallery.getImagePathFromURI(data.getData(), getApplicationContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                imgBitmap = BitmapFactory.decodeFile(path, options);

                gallery.exifInterface();
                degree = gallery.exifDegree;

//                InputStream in = getContentResolver().openInputStream(data.getData());
//                imgBitmap = BitmapFactory.decodeStream(in);
//                in.close();

                imgBitmap = gallery.getResizedBitmap(imgBitmap); // 해상도 조절
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                    ProgressDialog progressDialog = new ProgressDialog(AzureImage.this); // 실시간 진행 상태 알림

                    @Override // 작업시작
                    protected void onPreExecute() {
                        progressDialog.show();
                    } // progressdialog 생성

                    @Override // 진행중
                    protected String doInBackground(InputStream... inputStreams) {
                        try
                        {
                            tts.speakOutString("분석중입니다");
                            publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거
                            String[] features = {"Description"};
                            String[] details = {};

                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0],features,details);

                            String jsonResult = new Gson().toJson(result);
                            return jsonResult;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (VisionServiceException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    @SuppressLint("StaticFieldLeak")
                    @Override // 종료
                    protected void onPostExecute(String s){

                        if(TextUtils.isEmpty(s)){ // s가 null일때

                            mTextResult.setText("인식할 수 없습니다");
                            Toast.makeText(AzureImage.this,"API Return Empty Result",Toast.LENGTH_SHORT).show();
                            tts.speakOut(mTextResult);
                        }
                        else {
                            progressDialog.dismiss();
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                            StringBuilder result_Text = new StringBuilder();
                            for (Caption caption : result.description.captions)
                                result_Text.append(caption.text);

                            //파파고 번역
                            new Thread() {
                                @Override
                                public void run() {
                                    String word = result_Text.toString();
                                    Papago_translate papago = new Papago_translate();
                                    String resultWord = papago.getTranslation(word, "en", "ko");

                                    Bundle papagoBundle = new Bundle();
                                    papagoBundle.putString("resultWord", resultWord);

                                    Message msg = papago_handler.obtainMessage();
                                    msg.setData(papagoBundle);
                                    papago_handler.sendMessage(msg);
                                }
                            }.start();
                        }
                    }

                    @Override
                    protected void onProgressUpdate(String... values){
                        progressDialog.setMessage(values[0]);

                    }
                };
                visionTask.execute(inputStream);

                // 사진 회전
                requestOptions.transform(new RotateTransform(degree));
                Glide.with(this).load(imgBitmap).apply(requestOptions).into(imageView);

                //imageView.setImageBitmap(imgBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 갤러리 호출 함수
    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*"); // 갤러리타입 형식 호출
        intent.setAction(Intent.ACTION_GET_CONTENT); // 인텐트 작업 지정. 앨범 호출
        startActivityForResult(intent,1); // 새 액티비티를 열어주고 결과값 전달
    }

    //파파고 번역에 필요한 핸들러
    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            mTextResult.setText(resultWord);
            tts.speakOut(mTextResult);
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.ttsDestory();
        }
    }

}