package com.pbl.viewplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class IntroSliderScreen extends AppCompatActivity {
    ViewPager pager;
    int pageCount = 3;
    Button startBtn;
    ImageView imageView1, imageView2, imageView3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider_screen);


        startBtn = findViewById(R.id.startBtn);
        //startBtn.setVisibility(View.GONE);//처음엔 안보임
        startBtn.setVisibility(View.INVISIBLE);

        imageView1 =findViewById(R.id.imageView1);
        imageView2 =findViewById(R.id.imageView2);
        imageView3 =findViewById(R.id.imageView3);


        pager = findViewById(R.id.pager);
        pager.setOffscreenPageLimit(pageCount);//로딩할 화면 개수

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        Fragment1 fragment1 = new Fragment1();//1
        adapter.addItem(fragment1);

        Fragment2 fragment2 = new Fragment2();//2
        adapter.addItem(fragment2);

        Fragment3 fragment3 = new Fragment3();//3
        adapter.addItem(fragment3);

        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){//페이지 바뀔 때 설정함
            //https://javaexpert.tistory.com/496 //현재 페이지 보기
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int sequence = pager.getCurrentItem();

                if(sequence == pageCount -3) {//첫 페이지
                    //startBtn.setVisibility(View.GONE);
                    startBtn.setVisibility(View.INVISIBLE);
                    imageView1.setImageResource(R.drawable.white_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                }
                else if(sequence == pageCount -2) {//두번째 페이지
                    //startBtn.setVisibility(View.GONE);
                    startBtn.setVisibility(View.INVISIBLE);
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.white_circle);
                    imageView3.setImageResource(R.drawable.gray_circle);
                }
                else if(sequence == pageCount -1) {//마지막 페이지
                    startBtn.setVisibility(View.VISIBLE);
                    imageView1.setImageResource(R.drawable.gray_circle);
                    imageView2.setImageResource(R.drawable.gray_circle);
                    imageView3.setImageResource(R.drawable.white_circle);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IntroSliderScreen.this, MainActivity.class));
                finish();
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

}