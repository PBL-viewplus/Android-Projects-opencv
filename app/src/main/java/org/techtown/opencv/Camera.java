package org.techtown.opencv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;


public class Camera {

    public String imageFilePath;
    private Uri photoUri;
    public int exifOrientation;
    public float exifDegree;
    private MediaScanner mMediaScanner;

    // 카메라 시작 함수
    public void cameraStart(Context context, Intent intent){
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(context);
            } catch (IOException e) {

            }
            if (photoFile != null) {
                // ***이전: context.getPackageName()
                photoUri = FileProvider.getUriForFile(context, "org.techtown.opencv.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            }
        }
    }

    // 이미지의 정보 추출 함수
    public void exifInterface(){
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegress(exifOrientation);
        } else {
            exifDegree = 0;
        }
    }

    public void fileOpen(Context context, Bitmap bitmap){
        mMediaScanner = MediaScanner.getInstance(context);
        String result = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "HONGDROID" + File.separator;
        File file = new File(strFolderName);
        if(!file.exists()){
            file.mkdirs();
        }

        File f = new File(strFolderName + "/" + filename + ".png");
        result = f.getPath();

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = "Save Error fOut";
        }

        // 비트맵 사진 폴더 경로에 저장
        rotate(bitmap,exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
            // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
            mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".png");
        } catch (IOException e) {
            e.printStackTrace();
            result = "File close Error";
        }
    }

    // 카메라로 찍은 사진 저장하는 파일 생성 함수
    public File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    // 사진 회전할 각도 가늠 함수
    public int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    // 사진 회전 함수
    public Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 해상도 조절 함수
    public Bitmap getResizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) 960) / width;
        // create a matrix for the manipulation
        float scaleHeight = ((float) 720) / height;
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false );
        image.recycle();
        return resizedBitmap;
    }
}
