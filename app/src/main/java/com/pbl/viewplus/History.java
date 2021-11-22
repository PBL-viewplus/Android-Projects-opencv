package com.pbl.viewplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class History extends AppCompatActivity {
    public RecyclerView mRecyclerView;
    historyAdapter mAdapter = null ;
    ArrayList<hDataitem> mList = new ArrayList<>(); //오늘
    ArrayList<hDataitem> mList1 = new ArrayList<>(); //1일전
    ArrayList<hDataitem> mList2 = new ArrayList<>(); //2일전
    ArrayList<hDataitem> mList3 = new ArrayList<>(); //3일전
    ArrayList<hDataitem> mList4 = new ArrayList<>(); //4일전


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Button del_hbutton;
    private String date;

    //삭제
    ArrayList<String> docName = new ArrayList<>();

    //이전결과 볼 수 있는 액티비티
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent intent = getIntent();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 표시 포맷
        Calendar cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime()); //현재 날짜
        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 1일전
        String date1 = sdf.format(cal.getTime());
        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 2일전
        String date2 = sdf.format(cal.getTime());
        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 3일전
        String date3 = sdf.format(cal.getTime());
        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 4일전
        String date4 = sdf.format(cal.getTime());

        //컬렉션이름은 유저 정보로 수정
        db.collection("ooo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //오늘
                                if (document.getId().startsWith(today)) {
                                    mList.add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hdata_recycler);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                                    mAdapter = new historyAdapter(mList);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //1일전
                                else if (document.getId().startsWith(date1)) {
                                    mList1.add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hdata_recycler1);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                                    mAdapter = new historyAdapter(mList1);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //2일전
                                else if (document.getId().startsWith(date2)) {
                                    mList2.add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hdata_recycler2);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                                    mAdapter = new historyAdapter(mList2);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //3일전
                                else if (document.getId().startsWith(date3)) {
                                    mList3.add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hdata_recycler3);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                                    mAdapter = new historyAdapter(mList3);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //4일전
                                else if (document.getId().startsWith(date4)) {
                                    mList4.add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hdata_recycler4);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                                    mAdapter = new historyAdapter(mList4);
                                    mRecyclerView.setAdapter(mAdapter);

                                }

                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    //해당날짜 자동으로 삭제
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

                                //스토리지 삭제
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference desertRef = storageRef.child("ooo/"+ name +".txt");
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
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