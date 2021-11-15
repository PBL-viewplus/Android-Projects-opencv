package com.pbl.viewplus;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class TTS_controller {

    private final Bundle params = new Bundle();
    private TextToSpeech tts;
    // 팝업창 tts 내용
    private String dialogText = "주의사항 사진은 최대한 가까이하고 정중앙으로 찍어주세요 그림자가 지지 않게 밝은 곳에서 찍어주세요";
    // 검열 팝업창 내용
    private String maskingDialogText= "개인정보가 포함될 수도 있습니다 열람하시겠습니까?";

    // tts 객체 초기화
    public void initTTS(Context context, int num) {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int state) {
                if (state == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                    if (num==1){
                        speakOutString(dialogText);
                    }
                    if (num==2){
                        speakOutString(maskingDialogText);
                    }
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

    // tts 실행 함수(String)
    public void speakOutString(String s) {
        tts.setSpeechRate((float) 0.9); // 속도
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }

    // tts 실행 함수(TextView)
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