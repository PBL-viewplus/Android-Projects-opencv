package com.pbl.viewplus;

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
    ArrayList<hDataitem> mList[];
    private String date[]= new String[5];
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userEmail;
    private FirebaseAuth mAuth;
    private Button del_hbutton;

    //삭제
    ArrayList<String> docName = new ArrayList<>();

    //이전결과 볼 수 있는 액티비티
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmail= userEmail.split("@")[0];

        mList=new ArrayList[5]; //필드 정보 담을 배열
        for(int i=0;i<5;i++){
            mList[i]=new ArrayList<>();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 표시 포맷
        Calendar cal = Calendar.getInstance();
        for(int i=0;i<5;i++){//현재 날짜, 1일전, 2일전...
            date[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
        }
//        date[0] = sdf.format(cal.getTime()); //현재 날짜
//        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 1일전
//        date[1] = sdf.format(cal.getTime());
//        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 2일전
//        date[2] = sdf.format(cal.getTime());
//        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 3일전
//        date[3] = sdf.format(cal.getTime());
//        cal.add(Calendar.DATE, -1); // 현재 날짜로부터 4일전
//        date[4] = sdf.format(cal.getTime());

        //컬렉션이름은 유저 정보로 수정
        db.collection(userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //오늘
                                if (document.getId().startsWith(date[0])) {
                                    mList[0].add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hDataRecycler0);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                    mAdapter = new historyAdapter(mList[0]);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //1일전
                                else if (document.getId().startsWith(date[1])) {
                                    mList[1].add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hDataRecycler1);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                    mAdapter = new historyAdapter(mList[1]);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //2일전
                                else if (document.getId().startsWith(date[2])) {
                                    mList[2].add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hDataRecycler2);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                    mAdapter = new historyAdapter(mList[2]);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //3일전
                                else if (document.getId().startsWith(date[3])) {
                                    mList[3].add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hDataRecycler3);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                    mAdapter = new historyAdapter(mList[3]);
                                    mRecyclerView.setAdapter(mAdapter);

                                }
                                //4일전
                                else if (document.getId().startsWith(date[4])) {
                                    mList[4].add(new hDataitem(
                                            document.get("date").toString(),
                                            document.get("text").toString(),
                                            document.get("iv1").toString(),
                                            document.get("iv2").toString(),
                                            document.get("k").toString(),
                                            document.get("piciv").toString()
                                    ));

                                    mRecyclerView = findViewById(R.id.hDataRecycler4);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                    mAdapter = new historyAdapter(mList[4]);
                                    mRecyclerView.setAdapter(mAdapter);

                                }

                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    // 해당 날짜 자동으로 삭제
    public void deleteDocument() {
        // 삭제할 날짜 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 표시 포맷
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -4);
        String delDate = sdf.format(cal.getTime()) + " 00:00:00"; // 현재 날짜로부터 4일전 00:00:00 로 삭제 날짜 지정

        // 해당 user의 문서 이름 가져옴
        db.collection(userEmail)
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
                            if (name.compareTo(delDate) <= 0){ // delDate 보다 지난 날짜면 문서 삭제
                                db.collection(userEmail).document(name)
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
                                StorageReference desertRef = storageRef.child(userEmail+"/"+ name +".txt");
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