package com.pbl.viewplus;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userEmail;
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭

    FirebaseStorage storage = FirebaseStorage.getInstance();
    TTS_controller tts = new TTS_controller();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        textResult = findViewById(R.id.hd_text_result);
        imageView = findViewById(R.id.hd_origin_iv);
        backButton = findViewById(R.id.hd_btn_back);
        plusButton = (ImageButton) findViewById(R.id.hd_btn_plus);
        minusButton = (ImageButton) findViewById(R.id.hd_btn_minus);
        db = FirebaseFirestore.getInstance();

        minusButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                textResult.setTextSize(textResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
            }
        });

        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                textResult.setTextSize(textResult.getTextSize() / Resources.getSystem().getDisplayMetrics().density + 10);
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
                                String decText = AES.decByKey(enc, text,document.get("iv1").toString()); // 우리키로 암호문 복호화 진행
                                textResult.setText(decText);
                                tts.initTTS(getApplicationContext(), decText);
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

    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.ttsDestory();
        }
    }
}