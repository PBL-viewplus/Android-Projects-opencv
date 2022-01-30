package com.pbl.viewplus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;


@RequiresApi(api = Build.VERSION_CODES.O)
public class AzureImage extends AppCompatActivity {
    private ImageView imageView;
    private TextView mTextResult;
    private Bitmap imgBitmap;
    private ImageButton minusButton;
    private ImageButton againButton;
    private ImageButton plusButton;
    private ImageButton backButton;
    private ImageButton cameraButton_az;
    private ImageButton galleryButton_az;
    private final String API_KEY = "d4e5bcc8873949e88fd2a12c19a5bcc5";
    private final String API_LINK = "https://westus.api.cognitive.microsoft.com/vision/v1.0";

    //암호화
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭
    public byte[] key = AES.generateRandomBase64Token(16);

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public String userEmail;
    public Map<String, Object> user = new HashMap<>();;
    public String[] encText = null;
    public String[] pic = null;

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);
    TTS_controller tts = new TTS_controller();
    Camera camera = new Camera();
    Gallery gallery = new Gallery();

    //현재 날짜로 문서 생성
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Date date;
    public String getTime;

    //효과음
    SoundPool soundPool;
    int bellSoundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyze_picture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        imageView = (ImageView) findViewById(R.id.origin_iv);
        mTextResult = (TextView) findViewById(R.id.text_result);
        minusButton = (ImageButton) findViewById(R.id.btn_minus);
        againButton = findViewById(R.id.btn_again);
        plusButton = (ImageButton) findViewById(R.id.btn_plus);
        backButton = (ImageButton) findViewById(R.id.btn_back);

        cameraButton_az = findViewById(R.id.btn_camera);
        galleryButton_az = findViewById(R.id.btn_gallery);

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmail = userEmail.split("@")[0];

        // 카메라 권한 체크
        Permission permission = new Permission();
        permission.permissioncheck(getApplicationContext());

        // TTS 객체 초기화
        tts.initTTS(this, null);

        //효과음 설정
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            soundPool = new SoundPool.Builder().setMaxStreams(1).build(); //setMaxStreams 현재 시점에 재생할 최대음원개수
        }
        else{
            soundPool = new SoundPool(1, AudioManager.STREAM_RING,0);
        }
        bellSoundID = soundPool.load(this,R.raw.bell,1);

        //인텐트 받기
        Intent intent = getIntent();
        int value = intent.getExtras().getInt("value");
//        if (value == 3){
//            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.camera_btn));
//        }
//        if (value == 4){
//            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_image));
//        }

        // 사진 찍기 버튼
        cameraButton_az.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (permission.hasPermissions(permission.car_PERMISSIONS, getApplicationContext())) {//권한이 있어야 활성화함
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    camera.cameraStart(getApplicationContext(), intent);
                    startActivityForResult(intent, 3);

                }
                else {//권한이 없으면
                    permission.permissioncheck(getApplicationContext());
                }
            }
        });

        // 갤러리 버튼
        galleryButton_az.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (gallery.hasPermissions(gallery.PERMISSIONS, getApplicationContext())) {//권한이 있어야 활성화함
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 4);

                }
                else {//권한이 없으면
                    permission.permissioncheck(getApplicationContext());
                }
            }
        });

        // 돋보기 -버튼
        minusButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (imgBitmap != null) {
                    if (mTextResult.getTextSize() > 70) {
                        mTextResult.setTextSize(mTextResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
                    }
                }
            }
        });

        // TTS 다시 듣기 버튼
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTextResult != null) {
                    tts.speakOut(mTextResult.getText().toString());
                }
            }
        });

        // 돋보기 +버튼
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgBitmap != null) {
                    if (mTextResult.getTextSize() < 320) {
                        mTextResult.setTextSize(mTextResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density + 10);
                    }
                }
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
                //camera.exifInterface();
                //degree = camera.exifDegree;

                Bitmap rotatedBitmap = null;

                // 회전된 사진을 원래대로 돌려 표시한다.
                if (imgBitmap != null) {
                    ExifInterface ei = new ExifInterface(path);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(imgBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(imgBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(imgBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            rotatedBitmap = imgBitmap;
                    }
                    imgBitmap=rotatedBitmap;
                    imageView.setImageBitmap(imgBitmap);

                    camera.fileOpen(getApplicationContext(), imgBitmap);
                }


                //imgBitmap = camera.getResizedBitmap(imgBitmap); // 해상도 조절
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                //비트맵 암호화
                pic= AES.encByKey(key, AES.BitmapToString(imgBitmap));
//                user.put("piciv", pic[1]);//비트맵의 벡터
//                //스토리지에 보내기
//                uploadStream(pic[0],getTime);

                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                    ProgressDialog progressDialog = new ProgressDialog(AzureImage.this); // 실시간 진행 상태 알림

                    @Override // 작업시작
                    protected void onPreExecute() {
                        progressDialog.show();
                    } // progressdialog 생성

                    @Override // 진행중
                    protected String doInBackground(InputStream... inputStreams) {
                        progressDialog.setCanceledOnTouchOutside(false);

                        try
                        {
                            tts.speakOut("분석중입니다");
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
                        progressDialog.dismiss();

                        if(TextUtils.isEmpty(s)){ // s가 null일때
                            mTextResult.setText("인식할 수 없습니다");
                            Toast.makeText(AzureImage.this,"인식할 수 없습니다",Toast.LENGTH_SHORT).show();
                            tts.speakOut(mTextResult.getText().toString());
                        }
                        else {
                            //완료 효과음
                            soundPool.play(bellSoundID,1f,1f,0,0,1f);

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
//                requestOptions.transform(new RotateTransform(degree));
//                Glide.with(this).load(imgBitmap).apply(requestOptions).into(imageView);

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

//                gallery.exifInterface();
//                degree = gallery.exifDegree;

                Bitmap rotatedBitmap = null;

                // 회전된 사진을 원래대로 돌려 표시한다.
                if (imgBitmap != null) {
                    ExifInterface ei = new ExifInterface(path);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(imgBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(imgBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(imgBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            rotatedBitmap = imgBitmap;
                    }
                    imgBitmap=rotatedBitmap;
                    imageView.setImageBitmap(imgBitmap);
                }

//                InputStream in = getContentResolver().openInputStream(data.getData());
//                imgBitmap = BitmapFactory.decodeStream(in);
//                in.close();

                //imgBitmap = gallery.getResizedBitmap(imgBitmap); // 해상도 조절
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());


                // 사진 회전
//                requestOptions.transform(new RotateTransform(degree));
//                Glide.with(this).load(imgBitmap).apply(requestOptions).into(imageView);

                //imageView.setImageBitmap(imgBitmap);

                //비트맵 암호화
                pic= AES.encByKey(key, AES.BitmapToString(imgBitmap));
//                user.put("piciv", pic[1]);//비트맵의 벡터
//                //스토리지에 보내기
//                uploadStream(pic[0],getTime);


                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                    ProgressDialog progressDialog = new ProgressDialog(AzureImage.this); // 실시간 진행 상태 알림

                    @Override // 작업시작
                    protected void onPreExecute() {
                        progressDialog.show();
                    } // progressdialog 생성

                    @Override // 진행중
                    protected String doInBackground(InputStream... inputStreams) {
                        progressDialog.setCanceledOnTouchOutside(false);

                        try
                        {
                            tts.speakOut("분석중입니다");
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
                        progressDialog.dismiss();

                        if(TextUtils.isEmpty(s)){ // s가 null일때
                            mTextResult.setText("인식할 수 없습니다");
                            Toast.makeText(AzureImage.this,"API Return Empty Result",Toast.LENGTH_SHORT).show();
                            tts.speakOut(mTextResult.getText().toString());
                        }
                        else {
                            //완료 효과음
                            soundPool.play(bellSoundID,1f,1f,0,0,1f);

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

            // 암호화 실행
            try{
                //우리키로 평문 암호화
                encText= AES.encByKey(key, resultWord);
                user.put("text", encText[0]);//암호화 된 평문
                user.put("iv1", encText[1]);//평문의 벡터


                //암호화 완료했으면 keystore키로 우리키 암호화하기
                if (!AES.isExistKey(alias)) {
                    AES.generateKey(alias);
                }
                SecretKey secretKey = AES.getKeyStoreKey(alias);
                String[] enc = AES.encByKeyStoreKey(secretKey, key);

                user.put("k", enc[0]); //암호화된 키를 보낸다.
                user.put("iv2", enc[1]); //암호화된 키의 벡터를 보낸다.


            } catch (Exception e) {
                e.printStackTrace();

            }

            date= new Date();
            getTime = sdf.format(date);

            user.put("piciv", pic[1]);//비트맵의 벡터
            //스토리지에 보내기
            uploadStream(pic[0],getTime);

            user.put("date", getTime);

            //서버로 보내기
            db.collection(userEmail).document(getTime).set(user);

            tts.speakOut(mTextResult.getText().toString());
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };

    //스토리지에 보내기
    public void uploadStream(String pic, String getTime){
        //경로, 이름 지정
        StorageReference mountainImagesRef = storageRef.child(userEmail+"/"+ getTime +".txt");
        byte[] data = Base64.decode(pic,0);

        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    public void onStop() {
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if(tts != null){
            tts.ttsDestory();
        }
        soundPool.release();
    }

}