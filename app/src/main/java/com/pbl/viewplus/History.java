package com.pbl.viewplus;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private GoogleSignInClient mGoogleSignInClient;
    //private Button btn_logout;
    private TextView userEmailText;
    private ImageView userProfile;


    //삭제
    ArrayList<String> docName = new ArrayList<>();

    //이전결과 볼 수 있는 액티비티
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        //btn_logout=findViewById(R.id.btn_logout);
        userEmailText=findViewById(R.id.userEmailText);
        userProfile=findViewById(R.id.userProfile);

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        //사용자 이메일 먼저 텍스트뷰에 지정
        userEmailText.setText(userEmail);
        userEmail= userEmail.split("@")[0];
        //사용자 프로필 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Uri photoUrl = user.getPhotoUrl();
            Glide.with(this).load(photoUrl).into(userProfile);
        }

        mAuth=FirebaseAuth.getInstance();

        // 히스토리 자동 삭제
        deleteDocument();

//        //로그아웃
//        btn_logout.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
//                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
//
//                signOut();
//                finish();
//
//                //로그인화면으로 이동
//                Intent intent = new Intent(getApplicationContext(), Login.class);
//                MainActivity.MainAct.finish();
//                startActivity(intent);
//            }
//        });

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
                                    //히스토리에서 이미지 깜빡임 방지
                                    mRecyclerView.setItemViewCacheSize(mList[0].size());
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
                                    //히스토리에서 이미지 깜빡임 방지
                                    mRecyclerView.setItemViewCacheSize(mList[1].size());
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
                                    //히스토리에서 이미지 깜빡임 방지
                                    mRecyclerView.setItemViewCacheSize(mList[2].size());
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
                                    //히스토리에서 이미지 깜빡임 방지
                                    mRecyclerView.setItemViewCacheSize(mList[3].size());
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
                                    //히스토리에서 이미지 깜빡임 방지
                                    mRecyclerView.setItemViewCacheSize(mList[4].size());
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

//    private void updateUI(FirebaseUser user) { //update ui code here
//        if (user != null) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }
//    private void signOut() {
//        mAuth.signOut();
//        mGoogleSignInClient.signOut().addOnCompleteListener(this,
//                new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        updateUI(null);
//                    }
//                });
//    }


}