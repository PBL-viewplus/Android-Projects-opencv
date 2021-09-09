package org.techtown.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import java.io.InputStream;

public class Clickcamera extends AppCompatActivity {
    TextView txtResult;
    Button btnCamera;
    Bitmap imgBitmap;
    ImageView imageView2;

    private Bitmap changeBitmap;
    private Bitmap originBitmap;
    private ImageView changeImageView;
    private ImageView originImageView;

    // 카메라 객체 생성
    Camera camera = new Camera();

    // opencv 객체 생성
    OpenCV opencv = new OpenCV();

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickcamera);

         changeImageView = findViewById(R.id.imageView2);
         originImageView = findViewById(R.id.imageView3);

        txtResult = (TextView) findViewById(R.id.txtResult);
        btnCamera = (Button) findViewById(R.id.btnCamera);

        // 카메라 권한 체크
         Permission permission = new Permission();
         permission.permissioncheck(getApplicationContext());

        // 카메라 버튼
         btnCamera.setOnClickListener(new View.OnClickListener(){
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 camera.cameraStart(getApplicationContext(), intent);
                 startActivityForResult(intent, 2);
                 txtResult.setText("");
             }
         });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            opencv.mIsOpenCVReady = true;
        }
    }

    // onActivityResult(): sub activity에서 main activity로 넘어갈 때
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

 //       if (requestCode == 1 && resultCode == RESULT_OK && intent != null) { // 갤러리
//            try {
//                InputStream in = getContentResolver().openInputStream(intent.getData());
//                imgBitmap = BitmapFactory.decodeStream(in);
//                in.close();
//
//                imageView2.setImageBitmap(imgBitmap);
//            } catch (Exception e) {}
//        }
//        else
            if (requestCode == 2 && resultCode == RESULT_OK) { // 카메라
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                String path = camera.imageFilePath;

                originBitmap = BitmapFactory.decodeFile(path, options);
                changeBitmap = BitmapFactory.decodeFile(path, options);

                camera.exifInterface();
                originBitmap = camera.rotate(originBitmap,camera.exifDegree);
                changeBitmap = camera.rotate(changeBitmap,camera.exifDegree);

                camera.fileOpen(getApplicationContext(), originBitmap);

                if (originBitmap != null) {
                    opencv.detectEdgeUsingJNI(originBitmap, originBitmap);
                }

                //opencv.java에서 이미지뷰에 넣어주므로 필요없음
                //imageView2.setImageBitmap(camera.rotate(imgBitmap, camera.exifDegree));
            }
        }

}