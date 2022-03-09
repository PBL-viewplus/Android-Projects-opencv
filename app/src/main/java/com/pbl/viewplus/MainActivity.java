package com.pbl.viewplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private ImageButton camera_text;
    private ImageButton gallery_text;
    private ImageButton camera_image;
    private ImageButton gallery_image;
    private ImageButton web_image;
    private ImageButton btn_history;
    private ImageButton btn_tutorial;
    public String PopDialog;
    private Button btn_logout;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public static Activity MainAct;


    //1/3 수정
    public SharedPreferences prefs;//어플 실행 확인 변수 선언


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정


        //1/3 수정
        prefs = getSharedPreferences("Pref", MODE_PRIVATE); //어플 최초 실행 확인 생성하기
        checkFirstRun();//어플 최초 실행 확인


        camera_text = findViewById(R.id.button1);
        //gallery_text = findViewById(R.id.button2);
        camera_image = findViewById(R.id.button3);
        //gallery_image = findViewById(R.id.button4);
        web_image = findViewById(R.id.button5);
        btn_history = findViewById(R.id.btn_history);
        btn_logout=findViewById(R.id.btn_logout);
        btn_tutorial = findViewById(R.id.btn_tutorial);

        PopDialog = "";

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        //로그아웃 시 종료를 위함
        MainAct = this;

        mAuth = FirebaseAuth.getInstance();

        //tutorial 다시보기 //2/21
        btn_tutorial.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(v.getContext(), IntroSliderScreen.class);
                intent.putExtra("Tutorial",0); //튜토리얼 신호용
                startActivity(intent);
            }
        });

        //로그아웃
        btn_logout.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                signOut();
                finish();

                //로그인화면으로 이동
                Intent intent = new Intent(getApplicationContext(), Login.class);
                MainActivity.MainAct.finish();
                startActivity(intent);
            }
        });


        // 히스토리 버튼
        btn_history.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(v.getContext(), History.class);
                startActivity(intent);
            }
        });
        // 버튼 누르고 있을때 효과
        btn_history.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                    btn_history.setBackground(getDrawable(R.drawable.graycontent));
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn_history.setBackground(getDrawable(R.drawable.puppleborder));
                }
                return false;
            }
        });

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
        // 버튼 누르고 있을때 효과
        camera_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                    camera_text.setBackground(getDrawable(R.drawable.graycontent));
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    camera_text.setBackground(getDrawable(R.drawable.puppleborder));
                }
                return false;
            }
        });

        // 갤러리 문자 읽기
//        gallery_text.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), OCR_TTS.class);
//                intent.putExtra("value", 2);
//                startActivity(intent);
//            }
//        });

        // 카메라 이미지 분석
        camera_image.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AzureImage.class);
                intent.putExtra("value", 3);
                startActivity(intent);
            }
        });
        // 버튼 누르고 있을때 효과
        camera_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                    camera_image.setBackground(getDrawable(R.drawable.graycontent));
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    camera_image.setBackground(getDrawable(R.drawable.puppleborder));
                }
                return false;
            }
        });

        // 갤러리 이미지 분석
//        gallery_image.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), AzureImage.class);
//                intent.putExtra("value", 4);
//                startActivity(intent);
//            }
//        });

        // 웹브라우저 기능
        web_image.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebBrowser.class);
                startActivity(intent);
            }
        });
        // 버튼 누르고 있을때 효과
        web_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                    web_image.setBackground(getDrawable(R.drawable.graycontent));
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    web_image.setBackground(getDrawable(R.drawable.puppleborder));
                }
                return false;
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


    //1/3 수정
    public void checkFirstRun() {
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            Intent newIntent = new Intent(MainActivity.this, IntroSliderScreen.class);
            startActivity(newIntent);

            prefs.edit().putBoolean("isFirstRun", false).apply();
        }

    }

    private void updateUI(FirebaseUser user) { //update ui code here
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

}
