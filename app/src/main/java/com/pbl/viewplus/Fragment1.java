package com.pbl.viewplus;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment {

    View v;
    TTS_controller tts = new TTS_controller();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_1, container, false);

        //프래그먼트 이동했을때 바로 tts되야됨
        // -> 처음 한번만 들려줌. 전환될때마다 다시 들려줘야됨.
        // 프랙2까지 한번에 생성돼서 tts가 바로 이어져버림.
        //생명주기?



        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        String ex1="안녕하세요";

        tts.initTTS(v.getContext(), ex1);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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