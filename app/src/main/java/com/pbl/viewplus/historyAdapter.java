package com.pbl.viewplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class historyAdapter extends RecyclerView.Adapter<historyAdapter.ViewHolder>{

    private ArrayList<hDataitem> hData = null ;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    historyAdapter(ArrayList<hDataitem> list) {
        hData = list ;
    }

    @Override
    public historyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.hdata_item, parent, false) ;
        historyAdapter.ViewHolder vh = new historyAdapter.ViewHolder(view) ;

        return vh;
    }

    @Override
    public void onBindViewHolder(historyAdapter.ViewHolder holder, int position) {
        hDataitem item = hData.get(position) ;
        holder.text.setText(item.getText());

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        ViewHolder(View itemView){
            super(itemView);
            text=itemView.findViewById(R.id.itemtext);
        }
    }

    @Override
    public int getItemCount() {
        return hData.size();
    }
}
