package com.pbl.viewplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class IntroSliderScreen extends AppCompatActivity {
    ViewPager pager;
    int pageCount = 7;

    //tts 객체 생성
    View v;
    TTS_controller tts = new TTS_controller();

    Button startBtn, preBtn, nextBtn;
    ImageButton noticeBtn;
    ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6, imageView7;
    Fragment1 fragment1;Fragment2 fragment2;
    Fragment3 fragment3;Fragment4 fragment4;
    Fragment5 fragment5;Fragment6 fragment6;
    Fragment7 fragment7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider_screen);

        //tts 초기화
        tts.initTTS(getApplicationContext(), null);

        startBtn = findViewById(R.id.startBtn);
        preBtn = findViewById(R.id.preBtn);
        nextBtn = findViewById(R.id.nextBtn);
        startBtn.setVisibility(View.VISIBLE);
        noticeBtn = findViewById(R.id.noticeBtn);

        imageView1 =findViewById(R.id.imageView1);
        imageView2 =findViewById(R.id.imageView2);
        imageView3 =findViewById(R.id.imageView3);
        imageView4 =findViewById(R.id.imageView4);
        imageView5 =findViewById(R.id.imageView5);
        imageView6 =findViewById(R.id.imageView6);
        imageView7 =findViewById(R.id.imageView7);

        String p1= "튜토리얼 안내\n 왼쪽 상단 버튼을 통해 튜토리얼을 다시 볼 수 있습니다\n 오른쪽 상단에는 로그아웃 버튼이 있습니다";
        String p2= "문자 인식과 이미지 묘사 기능\n 문자 인식은 사진 속 문자를 분석하여 읽어주는 기능입니다\n" +
                "이미지 묘사는 사진 속 상황을 파악하여 설명해주는 기능입니다";
        String p3= "해당 기능을 사용하시려면 1 상단의 카메라 버튼을 눌러 사진을 촬영하거나 갤러리 버튼을 눌러 원하는 사진을 불러옵니다\n" +
                "2 분석이 완료되면 분석 결과가 아래에 나타나고 음성으로 글자를 읽어줍니다\n"+
                "3 양 옆 돋보기 버튼으로 글자 크기를 조정할 수 있으며 가운데 다시 듣기 버튼으로 결과를 다시 재생할 수 있습니다";
        String p4= "웹 브라우저 기능\n 웹 브라우저는 인터넷 사용이 가능하고 인터넷 사용 도중 원하는 이미지를 분석해주는 기능입니다.\n";
        String p5= "검색창에 직접 주소를 입력하거나 붙여넣기 버튼을 사용해\n" +
                "1 주소를 붙여 넣어 원하는 사이트를 검색할 수 있습니다.\n" +
                "2 웹 브라우저 사용 도중, 이미지를 길게 눌러 해당 이미지를 복사합니다.\n" +
                "3 문자 인식이나 이미지 묘사 버튼을 눌러 분석을 진행합니다\n";
        String p6= "과거 기록\n 과거 기록은 사용자의 과거 분석 결과를 다시 볼 수 있는 기능입니다.\n";
        String p7= "1 오늘을 포함한 5일 간의 기록이 보관됩니다.\n" +
                "2 사진을 클릭하여 과거 분석 결과를 확인할 수 있습니다.\n" +
                "3 사진 옆의 X 버튼을 눌러 해당 정보를 삭제할 수 있습니다.\n";

        //tts 내용 작성
        String ex[] = {p1,p2,"3","4","5","6","7"};


        //페이저 연결 작업
        pager = findViewById(R.id.pager);
        pager.setOffscreenPageLimit(pageCount);//로딩할 화면 개수

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        fragment1 = new Fragment1();//1
        adapter.addItem(fragment1);

        fragment2 = new Fragment2();//2
        adapter.addItem(fragment2);

        fragment3 = new Fragment3();//3
        adapter.addItem(fragment3);

        fragment4 = new Fragment4();//4
        adapter.addItem(fragment4);

        fragment5 = new Fragment5();//5
        adapter.addItem(fragment5);

        fragment6 = new Fragment6();//6
        adapter.addItem(fragment6);

        fragment7 = new Fragment7();//7
        adapter.addItem(fragment7);

        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){//페이지 바뀔 때 설정함
            //https://javaexpert.tistory.com/496 //현재 페이지 보기
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int sequence = pager.getCurrentItem();

                if(sequence == 0) {//첫 페이지
                    imageView1.setImageResource(R.drawable.white_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }
                else if(sequence == 1) {//두번째 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.white_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }
                else if(sequence == 2) {//세번째 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.white_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }
                else if(sequence == 3) {//네번째 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.white_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }
                else if(sequence == 4) {//다섯번째 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.white_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }
                else if(sequence == 5) {//여섯번째 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.white_circle);
                    imageView7.setImageResource(R.drawable.gray_circle);

                    tts.speakOut(ex[sequence]);
                }

                else if(sequence == 6) {//마지막 페이지
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                    imageView4.setImageResource(R.drawable.gray_circle);
                    imageView5.setImageResource(R.drawable.gray_circle);
                    imageView6.setImageResource(R.drawable.gray_circle);
                    imageView7.setImageResource(R.drawable.white_circle);

                    tts.speakOut(ex[sequence]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speakOut(ex[pager.getCurrentItem()]);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {//skip button
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IntroSliderScreen.this, MainActivity.class));
                finish();
            }
        });

        preBtn.setOnClickListener(new View.OnClickListener() {//이전 버튼
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(pager.getCurrentItem()-1);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {//이전 버튼
            @Override
            public void onClick(View view) {

                if(pager.getCurrentItem()==6){
                    startActivity(new Intent(IntroSliderScreen.this, MainActivity.class));
                    finish();
                } else {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                }
            }
        });

    }


    class MyPagerAdapter extends FragmentStatePagerAdapter{

        ArrayList<Fragment> items = new ArrayList<>();

        public MyPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public void addItem(Fragment item){//배열에 프래그먼트 추가
            items.add(item);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

    }

    //tts 종료
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