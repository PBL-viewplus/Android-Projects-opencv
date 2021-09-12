package org.techtown.opencv;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class TTS_controller {

    private final Bundle params = new Bundle();
    private TextToSpeech tts;


    // tts 객체 초기화
    public void initTTS(Context context) {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int state) {
                if (state == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                } else {
                    Toast.makeText(context, "TTS 객체 초기화 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String utteranceId) {

            }

            @Override
            public void onError(String s) {
                Toast.makeText(context, "재생 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }

        });
    }

    // tts 실행 함수
    public void speakOut(TextView t) {
        String text = t.getText().toString();
        tts.setSpeechRate((float) 0.9); // 속도.
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    // tts 종료 함수
    public void ttsDestory(){
        tts.stop();
        tts.shutdown();
        tts = null;
    }

    public void ttsStop(){
        if(tts.isSpeaking()==true){
            tts.stop();
        }
    }
}

// 돋보기 기능 -> 나중에 추가
//ImageButton b; (사진찍기 버튼)
//
////이미지버튼 설정
//        b=findViewById(R.id.imageButton);
//                b.setBackground(ContextCompat.getDrawable(this, R.drawable.backarrow));
////돋보기 기능. textSize는 public으로 선언-xml 연결 수정필요
//                Button plusButton= findViewById(R.id.plusButton);
//                plusButton.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        TextView textview= findViewById(R.id.textView);
//        int size= 16;
//        textSize+=5;
//        textview.setTextSize(size+textSize);
//        }
//        });
