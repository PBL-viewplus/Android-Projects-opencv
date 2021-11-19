package com.pbl.viewplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.crypto.SecretKey;

public class historyDetail extends AppCompatActivity {
    private String date;
    private TextView hd_text_result;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        hd_text_result=findViewById(R.id.hd_text_result);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        date = intent.getStringExtra("date");

        DocumentReference docRef = db.collection("ooo").document(date);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //문서안의 필드를 가져와서 복호화 시작.
                    //hDataitem item=
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                        String text=document.get("text").toString();

                        hd_text_result.setText(text);
                        String k=document.get("k").toString();
                        String iv2=document.get("iv2").toString();
                        Log.d("No  document", k );
                        try {
                            if (AES.isExistKey(alias)) {//있음.
                                SecretKey secretKey = AES.getKeyStoreKey(alias);
                                String enc = AES.decByKeyStoreKey(secretKey, k, iv2); //keystore키로 복호화한 우리키
                                //k=enc[0].substring(0,32); //16, 32개로 맞추기
                                Log.d("No  document", iv2 );
                                Log.d("No  document", String.valueOf(enc.getBytes()));
                                Log.d("No  document", String.valueOf(enc.length()));

                                //우리키로 암호문 복호화 진행
                                //. enc 길이가 16, 32 안맞춰져서..?
                                String decText = AES.decByKey(enc, text,document.get("iv1").toString());
                                Log.d("No  document", document.get("text").toString() );
                                Log.d("No  document", document.get("iv1").toString() );


                                hd_text_result.setText(decText);
                                Log.d("No such document", decText);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //.d(TAG, "get failed with ", task.getException());
                }
            }
        });


//        db.collection("ooo")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                //Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            //Log.w(TAG, "Error getting documents.", task.getException());
//                        }
//                    }
//                });




    }
}