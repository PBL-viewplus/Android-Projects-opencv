package com.pbl.viewplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class historyAdapter extends RecyclerView.Adapter<historyAdapter.ViewHolder>{

    private ArrayList<hDataitem> hData = null;
    public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭

    private String userEmail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    //아이템 뷰 저장하는 뷰 홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout hDataLinearLayout;
        ImageView itemImg;
        Button delButton;

        ViewHolder(View itemView){
            super(itemView);
            hDataLinearLayout = itemView.findViewById(R.id.hdata_linearLayout);
            itemImg = itemView.findViewById(R.id.itemImg);
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

        //사용자 구분
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmail= userEmail.split("@")[0];


        db.collection(userEmail).document(date).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // 복호화를 위한 준비 과정
                    String k = item.getK();
                    String iv2 = item.getIv2();
                    String piciv = item.getPiciv();

                    try {
                        if (AES.isExistKey(alias)) {
                            SecretKey secretKey = AES.getKeyStoreKey(alias);
                            String enc = AES.decByKeyStoreKey(secretKey, k, iv2);

                            // 스토리지에서 사진 가져오기
                            StorageReference storageRef = storage.getReference();
                            StorageReference pathReference = storageRef.child(userEmail+"/"+ date +".txt");

                            final long ONE_MEGABYTE = 2048 * 2048; // 약 4.1MB
                            pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // 이미지 복호화
                                    try {
                                        String decPic = AES.decByKey(enc, Base64.encodeToString(bytes,0), piciv);
                                        Bitmap bitmap= AES.StringToBitmap(decPic);
                                        holder.itemImg.setImageBitmap(bitmap);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // item 의 이미지
        holder.itemImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // imageview 에 있는 사진을 bitmap 으로 변경
                BitmapDrawable drawable = (BitmapDrawable) holder.itemImg.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File storage = v.getContext().getCacheDir(); // 내부 저장소의 캐시 경로 // /data/user/0/com.pbl.viewplus/cache
                File tempFile = new File(storage, date); // 파일 객체 생성
                String filePath = storage + "/" + date; // 저장할 파일 이름
                try{
                    tempFile.createNewFile(); // 자동으로 빈 파일 생성
                    FileOutputStream stream = new FileOutputStream(tempFile); // 파일 쓸 수 있는 스트림 준비
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // compress 함수를 사용해 스트림에 bitmap 저장
                    stream.close(); // 스트림 사용 후 닫음
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(v.getContext(), historyDetail.class);
                intent.putExtra("filePath", filePath);
                intent.putExtra("date", date);
                v.getContext().startActivity(intent);
            }
        });

        // item 의 삭제 버튼
        holder.delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection(userEmail).document(date)
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
                notifyItemRangeChanged(position, hData.size());

                //스토리지 사진파일 삭제
                StorageReference desertRef = storageRef.child(userEmail+"/"+ date +".txt");
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
