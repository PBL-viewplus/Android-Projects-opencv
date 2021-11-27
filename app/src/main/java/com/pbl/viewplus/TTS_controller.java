package com.pbl.viewplus;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.Locale;


public class TTS_controller {

    private final Bundle params = new Bundle();
    private TextToSpeech tts;

    // tts 객체 초기화
    public void initTTS(Context context, String text) {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int state) {
                if (state == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                    if (text != null){
                        speakOut(text);
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
    public void speakOut(String s) {
        tts.setSpeechRate((float) 0.9); // 속도
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
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