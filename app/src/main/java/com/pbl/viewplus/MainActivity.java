package com.pbl.viewplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private ImageButton camera_text;
    private ImageButton gallery_text;
    private ImageButton camera_image;
    private ImageButton gallery_image;
    private ImageButton web_image;
    private ImageButton btn_history;
    public String PopDialog;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public static Activity MainAct;


    //1/3 수정
    public SharedPreferences prefs;//어플 실행 확인 변수 선언


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정


        //1/3 수정
        prefs = getSharedPreferences("Pref", MODE_PRIVATE); //어플 최초 실행 확인 생성하기
        checkFirstRun();//어플 최초 실행 확인


        camera_text = findViewById(R.id.button1);
        gallery_text = findViewById(R.id.button2);
        camera_image = findViewById(R.id.button3);
        gallery_image = findViewById(R.id.button4);
        web_image = findViewById(R.id.button5);
        btn_history = findViewById(R.id.btn_history);

        PopDialog = "";

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        //로그아웃 시 종료를 위함
        MainAct = this;

        // 히스토리 버튼
        btn_history.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(v.getContext(), History.class);
                startActivity(intent);
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

}
