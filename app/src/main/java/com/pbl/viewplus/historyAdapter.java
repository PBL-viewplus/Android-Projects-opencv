package com.pbl.viewplus;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class historyAdapter extends RecyclerView.Adapter<historyAdapter.ViewHolder>{

    private ArrayList<hDataitem> hData = null;
    private Intent intent;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    //아이템 뷰 저장하는 뷰 홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout hDataLinearLayout;
        TextView itemText;
        Button delButton;

        ViewHolder(View itemView){
            super(itemView);
            hDataLinearLayout = itemView.findViewById(R.id.hdata_linearLayout);
            itemText = itemView.findViewById(R.id.itemtext);
            delButton = itemView.findViewById(R.id.delButton);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    historyAdapter(ArrayList<hDataitem> list) {
        hData = list ;
    }

    // onCreateViewHolder() - ViewHolder 를 새로 만들어야 할 때 호출되는 메소드.
    // 각 아이템을 위한 XML 레이아웃을 활용한 뷰 객체를 생성하고 이를 뷰 홀더 객체에 담아 리턴.
    // ViewHolder 가 아직 어떠한 데이터에 바인딩된 상태가 아니기 때문에 각 뷰의 내용은 채우지 않음.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.hdata_item, parent, false) ;
        ViewHolder vh = new ViewHolder(view) ;

        return vh;
    }

    // position 에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        hDataitem item = hData.get(position) ;
        String date = item.getDate();
        holder.itemText.setText(item.getIv1());

        // item 의 text
        holder.itemText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //결과 창으로 이동
                intent= new Intent(v.getContext(), historyDetail.class);
                intent.putExtra("date", date);
                v.getContext().startActivity(intent);
            }
        });

        // item 의 삭제 버튼
        holder.delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("ooo").document(date)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("hello", "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("hello", "Error deleting document", e);
                            }
                        });

                hData.remove(position);
                notifyItemRemoved(position);

                //스토리지 사진파일 삭제
                StorageReference desertRef = storageRef.child("ooo/"+ date +".txt");
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return hData.size();
    }
}
