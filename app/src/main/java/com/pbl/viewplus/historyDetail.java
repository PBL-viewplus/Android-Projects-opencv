package com.pbl.viewplus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.crypto.SecretKey;

public class historyDetail extends AppCompatActivity {
    private String date;
    private TextView hd_text_result;
    private ImageView hd_origin_iv;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        hd_text_result=findViewById(R.id.hd_text_result);
        hd_origin_iv=findViewById(R.id.hd_origin_iv);
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
                                String decText = AES.decByKey(enc, text,document.get("iv1").toString());
                                Log.d("No  document", document.get("text").toString() );
                                Log.d("No  document", document.get("iv1").toString() );

                                hd_text_result.setText(decText);

                                //비트맵 복호화
//                                String decPic = AES.decByKey(enc, document.get("pic").toString(),document.get("piciv").toString());
//                                Bitmap bb= AES.StringToBitmap(decPic);
//
//                                hd_origin_iv.setImageBitmap(bb);

                                //스토리지에서 사진 가져오기
                                StorageReference storageRef = storage.getReference();
                                StorageReference pathReference = storageRef.child("ooo/"+ date +".txt");

                                final long ONE_MEGABYTE = 2048 * 2048; //약 4.1MB
                                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Data for "images/island.jpg" is returns, use this as needed

                                        try {
                                            String decPic = AES.decByKey(enc, Base64.encodeToString(bytes,0),document.get("piciv").toString());
                                            Bitmap bb= AES.StringToBitmap(decPic);
                                            hd_origin_iv.setImageBitmap(bb);

                                            Log.d("No  document", "mpppppp222222" );
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                        Log.d("No  document", "mpppppp" );

                                    }
                                });


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