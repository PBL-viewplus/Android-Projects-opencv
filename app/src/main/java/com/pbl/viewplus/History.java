package com.pbl.viewplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class History extends AppCompatActivity {
    public RecyclerView mRecyclerView;
    historyAdapter mAdapter = null ;
    ArrayList<hDataitem> mList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    //삭제
    ArrayList<String> docName = new ArrayList<String>();

    //이전결과 볼 수 있는 액티비티
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent intent = getIntent();

        //데이터 까진 나왔는데 - 컬렉션이름은 유저 정보로 수정
        db.collection("ooo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mList.add(new hDataitem(
                                        document.get("date").toString(),
                                        document.get("text").toString(),
                                        document.get("iv1").toString(),
                                        document.get("iv2").toString(),
                                        document.get("k").toString()
                                ));

                                mRecyclerView = findViewById(R.id.hdata_recycler) ;
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false)) ;

                                mAdapter=new historyAdapter(mList);
                                mRecyclerView.setAdapter(mAdapter);

                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void addItem(String date, String text,String iv1,String iv2,String k ) {
        hDataitem item = new hDataitem();

        item.setText(text);
        item.setDate(date);
        item.setIv1(iv1);
        item.setIv2(iv2);
        item.setK(k);

        mList.add(item);
    }

    //삭제
    public void deleteDocument() {
        // 삭제할 날짜 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 표시 포맷
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -6); // 현재 날짜로부터 6일전
        String delDate = sdf.format(cal.getTime());

        // 해당 user의 문서 이름 가져옴
        db.collection("ooo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 문서 이름 가져와 docName에 저장
                                docName.add(document.getId());
                            }
                        }

                        // 삭제할 날짜에 해당하는 문서 삭제
                        for (String name : docName){
                            if (name.startsWith(delDate)){ // delName으로 시작하면 문서 삭제
                                db.collection("ooo").document(name)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("hello", "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("hello", "Error deleting document", e);
                                            }
                                        });
                            }else{ // delName으로 시작하지 않으면 for문 break
                                break;
                            }
                        }
                    }
                });
    }

}