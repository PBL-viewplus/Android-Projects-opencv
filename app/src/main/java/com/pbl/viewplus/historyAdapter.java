package com.pbl.viewplus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class historyAdapter extends RecyclerView.Adapter<historyAdapter.ViewHolder>{

    private ArrayList<hDataitem> hData = null ;
    private Intent intent;

    //아이템 뷰 저장하는 뷰 홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout hdata_linearLayout;
        TextView text;
        ViewHolder(View itemView){
            super(itemView);
            hdata_linearLayout=itemView.findViewById(R.id.hdata_linearLayout);
            text=itemView.findViewById(R.id.itemtext);
        }
    }
    // 생성자에서 데이터 리스트 객체를 전달받음.
    historyAdapter(ArrayList<hDataitem> list) {
        hData = list ;
    }

    //onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴. viewType 형태의 아이템 뷰를 위한 뷰홀더 객체 생성.
    @Override
    public historyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.hdata_item, parent, false) ;
        historyAdapter.ViewHolder vh = new historyAdapter.ViewHolder(view) ;

        return vh;
    }

    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(historyAdapter.ViewHolder holder, int position) {
        hDataitem item = hData.get(position) ;

        String date= item.getDate();
        holder.text.setText(item.getText());

        holder.hdata_linearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //버튼을 쓰레기로 바꿀거면 분석과정 안일어나게 하던지 아님 새로 액티비티 파던지. -간단하게 파자..
                //결과 창으로 이동
                intent= new Intent(v.getContext(), historyDetail.class);
                intent.putExtra("date", date);
                v.getContext().startActivity(intent);
            }
        });

    }



    @Override
    public int getItemCount() {
        return hData.size();
    }
}
