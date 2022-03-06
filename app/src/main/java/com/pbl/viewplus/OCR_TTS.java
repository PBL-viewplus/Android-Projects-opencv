package com.pbl.viewplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

import android.content.ClipData;
import android.content.ClipboardManager;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OCR_TTS extends AppCompatActivity {

    private Bitmap originBitmap;
    private Bitmap changeBitmap;
    private Bitmap showBitmap;
    private ImageButton backButton;
    private ImageView originImageView;
    private ImageButton minusButton;
    private ImageButton againButton;
    private ImageButton plusButton;
    private TextView mTextResult;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private String dataPath = "";
    private String mCurrentPhotoPath; // 사진 경로
    private String choiceResult="";
    private String result="";

    //암호화
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭
    public String TAG="hello";
    public byte[] key = AES.generateRandomBase64Token(16);

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private FirebaseAuth mAuth;
    private String userEmail;
    private String uid;

//    private boolean flag = false;

    Tesseract tesseract = new Tesseract();
    Gallery gallery = new Gallery();
    OpenCV opencv = new OpenCV();
    Camera camera = new Camera();
    TTS_controller tts = new TTS_controller();
    Regex regex = new Regex();

    //현재 날짜로 문서 생성
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Date date= new Date();
    public String getTime = sdf.format(date);

    //효과음
    SoundPool soundPool;
    int bellSoundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyze_picture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        originImageView = findViewById(R.id.origin_iv);
        mTextResult = findViewById(R.id.text_result);
        minusButton = findViewById(R.id.btn_minus);
        plusButton = findViewById(R.id.btn_plus);
        againButton = findViewById(R.id.btn_again);
        backButton = findViewById(R.id.btn_back);
        cameraButton = findViewById(R.id.btn_camera);
        galleryButton = findViewById(R.id.btn_gallery);

        mAuth = FirebaseAuth.getInstance();

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmail= userEmail.split("@")[0];

        //효과음 설정
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            soundPool = new SoundPool.Builder().setMaxStreams(1).build(); //setMaxStreams 현재 시점에 재생할 최대음원개수
        }
        else{
            soundPool = new SoundPool(1, AudioManager.STREAM_RING,0);
        }
        bellSoundID = soundPool.load(this,R.raw.bell,1);

        // Mainactivity의 intent value값 받아 버튼 종류 결정
        Intent intent = getIntent();
        int value = intent.getExtras().getInt("value");
//        if (value == 1) {
//            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.camera_btn));
//        }
//        if (value == 2) {
//            pictureButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_image));
//        }

        // 카메라 권한 체크
        Permission permission = new Permission();
        //permission.permissioncheck(getApplicationContext());

        // 갤러리 권한 체크
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!gallery.hasPermissions(gallery.PERMISSIONS, getApplicationContext())) {
//                requestPermissions(gallery.PERMISSIONS, gallery.PERMISSIONS_REQUEST_CODE);
//            }
//        }

        // tesseract 언어 데이터 경로
        dataPath = getFilesDir() + "/tesseract/";
        tesseract.checkFile(new File(dataPath + "tessdata/"), "kor", dataPath, getApplicationContext());
        tesseract.checkFile(new File(dataPath + "tessdata/"), "eng", dataPath, getApplicationContext());

        // tesseract 객체 초기화
        tesseract.tessInit(dataPath);

        // TTS 객체 초기화
        tts.initTTS(this, null);

        // 사진 찍기 버튼
        cameraButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {//1/4일 수정
                if (permission.hasPermissions(permission.car_PERMISSIONS, getApplicationContext())) {//권한이 있어야 활성화함
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    camera.cameraStart(getApplicationContext(), intent);
                    startActivityForResult(intent, 1);

                    //클릭하면 drawable, src 변화
                    cameraButton.setBackgroundResource(R.drawable.border_white);
                    cameraButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.mipmap.camera_white7));

                    galleryButton.setBackgroundResource(R.drawable.border_puple);
                    galleryButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.mipmap.gallery_pupple7));


//                    if (value == 1) {
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        camera.cameraStart(getApplicationContext(), intent);
//                        startActivityForResult(intent, 1);
//                    }
//                    if (value == 2) {
//                        Intent intent = new Intent(Intent.ACTION_PICK);
//                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(intent, 2);
//                    }
                }
                else {//권한이 없으면
                    //Toast.makeText(OCR_TTS.this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    permission.permissioncheck(getApplicationContext()); //갤러리만 허용되면 튕김
                }
            }
        });

        // 갤러리 버튼
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gallery.hasPermissions(gallery.PERMISSIONS, getApplicationContext())) {//권한이 있어야 활성화함
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                    //클릭하면 drawable, src 변화
                    galleryButton.setBackgroundResource(R.drawable.border_white);
                    galleryButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.mipmap.gallery_white));

                    cameraButton.setBackgroundResource(R.drawable.border_puple);
                    cameraButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.mipmap.camera_pupple));

                    //galleryButton.setBackground(ContextCompat.getDrawable(this, R.drawable.camera_btn));
                }
                else {//권한이 없으면
                    permission.permissioncheck(getApplicationContext());
                }

            }
        });

        //롱클릭시 복사
        mTextResult.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("분석 결과 복사", mTextResult.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(OCR_TTS.this, "복사되었습니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        // 돋보기 -버튼
        minusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeBitmap != null) {
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
                if (mTextResult != null){
                    tts.speakOut(mTextResult.getText().toString());
                }
            }
        });

        // 돋보기 +버튼
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeBitmap != null) {
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

    @Override
    public void onPause() {
        super.onPause();
        soundPool.autoPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            opencv.mIsOpenCVReady = true;
        }
        soundPool.autoResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE); // 캐시비우기

        float degree = 0; // 회전에 필요한 각도

        // 카메라 실행
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                //경로 변경**  --> 오류: 분석완전히 안끝내고 다른 사진찍으면 분석중입니다 뜨지 않음
//                File file = new File(camera.imageFilePath);
//                Bitmap rotatedBitmap = null;
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
//                        FileProvider.getUriForFile(OCR_TTS.this,
//                                getApplicationContext().getPackageName() + ".fileprovider", file));

                String path = camera.imageFilePath;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                Bitmap rotatedBitmap = null;

                // 회전된 사진을 원래대로 돌려 표시한다.
                if (bitmap != null) {
                    ExifInterface ei = new ExifInterface(camera.imageFilePath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(bitmap, 90);
                            showBitmap = rotateImage(bitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(bitmap, 180);
                            showBitmap = rotateImage(bitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(bitmap, 270);
                            showBitmap = rotateImage(bitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = bitmap;
                            showBitmap = bitmap;
                    }
                    //originImageView.setImageBitmap(rotatedBitmap);
                    changeBitmap = rotatedBitmap;

                    originImageView.setImageBitmap(showBitmap);

                    camera.fileOpen(getApplicationContext(),showBitmap);
                    opencv.cvtColor(changeBitmap);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            try {
//                String path = camera.imageFilePath;
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 4;
//
//                originBitmap = BitmapFactory.decodeFile(path, options);
//                changeBitmap = BitmapFactory.decodeFile(path, options);
//
//                camera.exifInterface();
//                degree = camera.exifDegree;
//                camera.fileOpen(getApplicationContext(), originBitmap);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            AsyncTask<InputStream, String, String> ocrTask = new AsyncTask<InputStream, String, String>() {
                //String result;
                // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                ProgressDialog progressDialog = new ProgressDialog(OCR_TTS.this, R.style.DialogStyle); // 실시간 진행 상태 알림

                @Override // 작업시작
                protected void onPreExecute() {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("분석중입니다");
                    progressDialog.show();
                } // progressdialog 생성

                @Override // 진행중
                protected String doInBackground(InputStream... inputStreams) {
                    tts.speakOut("분석중입니다");
                    //publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거

                    try {
                        for (int i = 0; i <= 5; i++) {
                            progressDialog.setProgress(i * 20);
                            Thread.sleep(600);
                        }
                        //창 터치시 중지 방지
                        //progressDialog.setCancelable(false);//뒤로가기도 막음
                        progressDialog.setCanceledOnTouchOutside(false);

                        result = tesseract.processImage(changeBitmap);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return result;
                }

                @SuppressLint("StaticFieldLeak")
                @Override // 종료
                protected void onPostExecute(String s) {
                    progressDialog.dismiss();

                    if (TextUtils.isEmpty(s)) {
                        mTextResult.setText("인식할 수 없습니다");
                        Toast.makeText(OCR_TTS.this,"인식할 수 없습니다",Toast.LENGTH_SHORT).show();
                        tts.speakOut(mTextResult.getText().toString());
                    } else {
                        //완료 효과음
                        soundPool.play(bellSoundID,1f,1f,0,0,1f);

                        //텍스트에 마스킹할 부분이 있다면
                        if(regex.isRegex(result)){
                            //검열 묻는 팝업창
                            Intent regexDialogIntent = new Intent(getApplicationContext(), RegexDialog.class);
                            startActivityForResult(regexDialogIntent, 3);
                            //분석결과 지우기
                            mTextResult.setText("");
                            //다음 분석을 위해 셋팅
                            regex.hasRegex= false;
                        }else{
                            //분석결과 지우기
                            mTextResult.setText("");
                            //아니면 바로 보여줌
                            mTextResult.setText(result);
                            tts.speakOut(mTextResult.getText().toString());
                        }

                        //서버에 보낼 데이터들을 담을 맵
                        Map<String, Object> user = new HashMap<>();
                        String[] encText = null;
                        //암호화한 사진 담을곳
                        String[] pic=null;

                        // 암호화 실행
                        try{
                            //우리키로 평문 암호화
                            encText= AES.encByKey(key, result);
                            user.put("text", encText[0]);//암호화 된 평문
                            user.put("iv1", encText[1]);//평문의 벡터

                            //비트맵 암호화
                            pic= AES.encByKey(key, AES.BitmapToString(showBitmap));
                            user.put("piciv", pic[1]);//비트맵의 벡터

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
                        getTime=sdf.format(date);

                        user.put("date", getTime);

                        //스토리지에 보내기
                        uploadStream(pic[0],getTime);

                        //서버로 보내기
                        db.collection(userEmail).document(getTime).set(user);
                    }


                }

                @Override
                protected void onProgressUpdate(String... values) {
                    progressDialog.setMessage(values[0]);
                }
            };
            ocrTask.execute();
        }

        // 갤러리 실행
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            try {
                String path = gallery.getImagePathFromURI(data.getData(), getApplicationContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                originBitmap = BitmapFactory.decodeFile(path, options);
                changeBitmap = BitmapFactory.decodeFile(path, options);
                Bitmap tempBitmap = BitmapFactory.decodeFile(path, options);

//                gallery.exifInterface();
//                degree = gallery.exifDegree;

                Bitmap rotatedBitmap = null;
                //Bitmap showBitmap = null;

                // 회전된 사진을 원래대로 돌려 표시한다.
                if (originBitmap != null) {
                    ExifInterface ei = new ExifInterface(path);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(originBitmap, 90);
                            showBitmap = rotateImage(tempBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(originBitmap, 180);
                            showBitmap = rotateImage(tempBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(originBitmap, 270);
                            showBitmap = rotateImage(tempBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            rotatedBitmap = originBitmap;
                            showBitmap = tempBitmap;
                    }
                    //originImageView.setImageBitmap(rotatedBitmap);
                    //originBitmap = rotatedBitmap;
                    changeBitmap = rotatedBitmap;
                    //opencv.detectEdgeUsingJNI(originBitmap, changeBitmap);

                    originImageView.setImageBitmap(showBitmap);
                    opencv.cvtColor(changeBitmap);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }



            AsyncTask<InputStream, String, String> ocrTask = new AsyncTask<InputStream, String, String>() {
                //String result;
                // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
                ProgressDialog progressDialog = new ProgressDialog(OCR_TTS.this); // 실시간 진행 상태 알림

                @Override // 작업시작
                protected void onPreExecute() {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("분석중입니다");
                    progressDialog.show();
                } // progressdialog 생성

                //버전 23까지 가야됨 -26암호화
                @SuppressLint("StaticFieldLeak")
                @Override // 진행중
                protected String doInBackground(InputStream... inputStreams) {

                    tts.speakOut("분석중입니다");
                    //publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거

                    try {
                        for (int i = 0; i <= 5; i++) {
                            progressDialog.setProgress(i * 20);
                            Thread.sleep(600);
                        }
                        //창 터치시 중지 방지
                        //progressDialog.setCancelable(false);//뒤로가기도 막음
                        progressDialog.setCanceledOnTouchOutside(false);

                        result = tesseract.processImage(changeBitmap);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return result;
                }

                @SuppressLint("StaticFieldLeak")
                @Override // 종료
                protected void onPostExecute(String s) {
                    progressDialog.dismiss();

                    if (TextUtils.isEmpty(s)) {
                        mTextResult.setText("인식할 수 없습니다");
                        Toast.makeText(OCR_TTS.this,"인식할 수 없습니다",Toast.LENGTH_SHORT).show();
                        tts.speakOut(mTextResult.getText().toString());
                    } else {
                        //완료 효과음
                        soundPool.play(bellSoundID,1f,1f,0,0,1f);

                        //1. result 보고 검열 할게 있으면 팝업창 뜨고.
                        //2. 네/ 아니오로 검열 여부를 정해야됨.- 그에 맞는 결과 출력

                        //텍스트에 마스킹할 부분이 있다면
                        if(regex.isRegex(result)){
                            //검열 묻는 팝업창
                            Intent regexDialogIntent = new Intent(getApplicationContext(), RegexDialog.class);
                            startActivityForResult(regexDialogIntent, 3);
                            //분석결과 지우기
                            mTextResult.setText("");
                            //다음 분석을 위해 셋팅
                            regex.hasRegex= false;
                        }else{
                            //분석결과 지우기
                            mTextResult.setText("");
                            //아니면 바로 보여줌
                            mTextResult.setText(result);
                            tts.speakOut(mTextResult.getText().toString());
                        }

                        //서버에 보낼 데이터들을 담을 맵
                        Map<String, Object> user = new HashMap<>();
                        String[] encText = null;
                        //암호화한 사진 담을곳
                        String[] pic=null;

                        // 암호화 실행
                        try{
                            //우리키로 평문 암호화
                            encText= AES.encByKey(key, result);
                            user.put("text", encText[0]);//암호화 된 평문
                            user.put("iv1", encText[1]);//평문의 벡터

                            //비트맵 암호화
                            pic= AES.encByKey(key, AES.BitmapToString(showBitmap));
                            user.put("piciv", pic[1]);//비트맵의 벡터

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


                        // 지난 날짜로 문서 생성 (테스트용)
//                        Calendar cal = Calendar.getInstance();
//                        cal.add(Calendar.DATE, -5);
//                        getTime = sdf.format(cal.getTime());

//                        String collection=getTime.substring(0,10);
//                        String document=getTime.substring(11,19);

                        date= new Date();
                        getTime=sdf.format(date);

                        user.put("date", getTime);

                        //스토리지에 보내기
                        uploadStream(pic[0],getTime);

                        //서버로 보내기
                        db.collection(userEmail).document(getTime).set(user);

                    }
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    progressDialog.setMessage(values[0]);
                }

            };
            ocrTask.execute();

        }

//        if (originBitmap != null) {
//            //opencv.detectEdgeUsingJNI(originBitmap, changeBitmap);
//
//            // 사진 회전
////            requestOptions.transform(new RotateTransform(degree));
////            Glide.with(this).load(originBitmap).apply(requestOptions).into(originImageView);
//        }


        if (requestCode == 3 && resultCode == RESULT_OK) { //검열 팝업창 결과 받는 곳
            choiceResult = data.getStringExtra("result");
            System.out.println("hooo33333333"+ choiceResult);

            if(choiceResult.equals("검열해서 보기")){
                result=regex.doMasking(result);
            }

            mTextResult.setText(result);
            tts.speakOut(mTextResult.getText().toString());
        }
    }



    public void onStop() {
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();

        if (changeBitmap != null) {
            changeBitmap = null;
        }

        if (tts != null) {
            tts.ttsDestory();
        }

        soundPool.release();
    }


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

}