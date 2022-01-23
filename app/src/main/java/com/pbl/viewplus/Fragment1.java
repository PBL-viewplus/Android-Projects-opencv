package com.pbl.viewplus;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment {

    View v;
    TTS_controller tts = new TTS_controller();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_1, container, false);

        //위 버튼 누르면 안내 듣기
        Button btn1= v.findViewById(R.id.btn_fragment1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ex1="안녕하세요";

                tts.initTTS(v.getContext(), ex1);
            }
        });


        return v;
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