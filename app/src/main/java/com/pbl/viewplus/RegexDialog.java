package com.pbl.viewplus;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegexDialog extends AppCompatActivity {
    private TextView maskingDialogText;

    TTS_controller tts = new TTS_controller();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_regex_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setTitle("");

        maskingDialogText = findViewById(R.id.txtText);
        tts.initTTS(this, maskingDialogText.getText().toString());
    }

    public void yesButton(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "네");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    public void noButton(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "검열해서 보기");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
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