package com.pbl.viewplus;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class RotateTransform extends BitmapTransformation {
    private float rotateRotationAngle=0f;

    public RotateTransform(float rotateRotationAngle) {
        this.rotateRotationAngle = rotateRotationAngle;
    }

    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(("rotate$rotateRotationAngle").getBytes());

    }

    public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateRotationAngle);
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
    }

}
