package com.pbl.viewplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainActivity extends AppCompatActivity {

    private ImageButton camera_text;
    private ImageButton gallery_text;
    private ImageButton camera_image;
    private ImageButton gallery_image;
    private ImageButton web_image;
    public String PopDialog;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //구글 연동
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        camera_text = findViewById(R.id.button1);
        gallery_text = findViewById(R.id.button2);
        camera_image = findViewById(R.id.button3);
        gallery_image = findViewById(R.id.button4);
        web_image = findViewById(R.id.button5);
        PopDialog = "";

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        //구글 연동 ->버튼생성해서 안에 넣기
//        mAuth = FirebaseAuth.getInstance();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);

        //로그아웃
        //FirebaseAuth.getInstance().signOut();

        // 카메라 문자 읽기
        camera_text.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OCR_TTS.class);
                intent.putExtra("value", 1);
                startActivity(intent);

                PopDialog = pref.getString("result","");

                if (PopDialog.equals("") || PopDialog.equals("확인")){
                    //데이터 담아서 팝업(액티비티) 호출
                    Intent intent2 = new Intent(getApplicationContext(), Dialog.class);
                    startActivityForResult(intent2, 1);
                }

            }
        });

        // 갤러리 문자 읽기
        gallery_text.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OCR_TTS.class);
                intent.putExtra("value", 2);
                startActivity(intent);
            }
        });

        // 카메라 이미지 분석
        camera_image.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AzureImage.class);
                intent.putExtra("value", 3);
                startActivity(intent);
            }
        });

        // 갤러리 이미지 분석
        gallery_image.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AzureImage.class);
                intent.putExtra("value", 4);
                startActivity(intent);
            }
        });

        // 웹브라우저 기능
        web_image.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebBrowser.class);
                startActivity(intent);
            }
        });
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // 데이터 받기
                PopDialog = data.getStringExtra("result");
                editor.putString("result", PopDialog);
                editor.apply();
            }
        }

        //구글 연동
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                }
            }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //로그인 성공
                            user = mAuth.getCurrentUser();

                            if (user!=null){
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } else { //로그인 실패
                            Toast.makeText(getApplicationContext(), "로그인 오류", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 뒤로가기 눌렀을 때 앱 종료 묻기
    public void onBackPressed() {
        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("앱을 종료하시겠습니까?");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                // 긍정 이벤트일 경우 다이얼로그 종료
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }



}
