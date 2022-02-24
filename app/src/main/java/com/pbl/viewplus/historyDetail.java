package com.pbl.viewplus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

import javax.crypto.SecretKey;

public class historyDetail extends AppCompatActivity {
    private String date;
    private TextView textResult;
    private ImageView imageView;
    private ImageButton backButton;
    private ImageButton plusButton;
    private ImageButton minusButton;
    private ImageButton againButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userEmail;
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭

    FirebaseStorage storage = FirebaseStorage.getInstance();
    TTS_controller tts = new TTS_controller();


    private String choiceResult="";
    private String decText="";
    Regex regex = new Regex();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        textResult = findViewById(R.id.hd_text_result);
        imageView = findViewById(R.id.hd_origin_iv);
        backButton = findViewById(R.id.hd_btn_back);
        plusButton = (ImageButton) findViewById(R.id.hd_btn_plus);
        againButton = findViewById(R.id.hd_btn_again);
        minusButton = (ImageButton) findViewById(R.id.hd_btn_minus);

        db = FirebaseFirestore.getInstance();

        //롱클릭시 복사
        textResult.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("분석 결과 복사", textResult.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(historyDetail.this, "복사되었습니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        minusButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (textResult.getTextSize() > 70) {
                    textResult.setTextSize(textResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
                }
            }
        });

        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textResult != null) {
                    tts.speakOut(textResult.getText().toString());
                }
            }
        });

        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textResult.getTextSize() < 320) {
                    textResult.setTextSize(textResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density + 10);
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

        // intent 로 해당 사진의 날짜와 사진을 받음
        Intent intent = getIntent();
        date = intent.getStringExtra("date");
        String filePath = intent.getStringExtra("filePath");
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        File file = new File(filePath);
        file.delete();
        imageView.setImageBitmap(bitmap);

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmail = userEmail.split("@")[0];

        DocumentReference docRef = db.collection(userEmail).document(date);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // 문서안의 필드를 가져와서 복호화 시작
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String text=document.get("text").toString();
                        String k=document.get("k").toString();
                        String iv2=document.get("iv2").toString();

                        try {
                            if (AES.isExistKey(alias)) {
                                SecretKey secretKey = AES.getKeyStoreKey(alias);
                                String enc = AES.decByKeyStoreKey(secretKey, k, iv2); // keystore키로 복호화한 우리키
                                decText = AES.decByKey(enc, text,document.get("iv1").toString()); // 우리키로 암호문 복호화 진행

                                //텍스트에 마스킹할 부분이 있다면
                                if(regex.isRegex(decText)){
                                    //검열 묻는 팝업창
                                    Intent regexDialogIntent = new Intent(getApplicationContext(), RegexDialog.class);
                                    startActivityForResult(regexDialogIntent, 3);
                                    //다음 분석을 위해 셋팅
                                    regex.hasRegex= false;
                                }else{
                                    //아니면 바로 보여줌
                                    textResult.setText(decText);
                                    tts.initTTS(getApplicationContext(), decText);
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE); // 캐시비우기

        if (requestCode == 3 && resultCode == RESULT_OK) { //검열 팝업창 결과 받는 곳
            choiceResult = data.getStringExtra("result");

            if(choiceResult.equals("검열해서 보기")){
                decText=regex.doMasking(decText);
            }

            textResult.setText(decText);
            tts.initTTS(getApplicationContext(), decText);
        }
    }

    public void onStop() {
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.ttsDestory();
        }
    }
}