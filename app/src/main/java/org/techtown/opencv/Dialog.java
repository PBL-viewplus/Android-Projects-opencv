package org.techtown.opencv;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Dialog extends AppCompatActivity {
    private TextView title;
    private TextView content;

    TTS_controller tts = new TTS_controller();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setTitle("");

        title = findViewById(R.id.title);
        content = findViewById(R.id.content);

        tts.initTTS(this, 1);
    }

    //확인 버튼 클릭
    public void mOnClose(View v) {
        Intent intent = new Intent();
        intent.putExtra("result", "확인");
        setResult(RESULT_OK, intent);
        finish(); //액티비티(팝업) 닫기
    }

    // 다시보지 않기 버튼
    public void mOnCloseForever(View v) {
        Intent intent = new Intent();
        intent.putExtra("result", "다시보지않기");
        setResult(RESULT_OK, intent);
        finish(); //액티비티(팝업) 닫기
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (tts != null){
            tts.ttsDestory();
        }
    }
}