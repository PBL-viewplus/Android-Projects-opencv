package com.pbl.viewplus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;


public class Permission {

    public Context context;
    String[] car_PERMISSIONS = {"android.permission.CAMERA"};
    String[] gal_PERMISSIONS = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    // 권한 체크 함수
    public void permissioncheck(Context c) {
        context = c;
        if (!hasPermissions(car_PERMISSIONS, c)) {
            if (!hasPermissions(gal_PERMISSIONS, c)) {//카메라, 갤러리 둘다 권한 없으면
                TedPermission.with(c)
                        .setPermissionListener(permissionListener)
                        //.setRationaleMessage("카메라 권한이 필요합니다.")
                        .setDeniedMessage("설정에서 카메라와 저장공간 권한을 허용해주세요.")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .check();
            } else {//카메라 권한만 없으면 -----오류남..
                TedPermission.with(c)
                        .setPermissionListener(permissionListener)
                        //.setRationaleMessage("카메라 권한이 필요합니다.")
                        .setDeniedMessage("설정에서 카메라 권한을 허용해주세요.")
                        .setPermissions(Manifest.permission.CAMERA)
                        .check();
            }
        }
        else if(!hasPermissions(gal_PERMISSIONS, c)){//카메라 권한이 있고 갤러리 권한이 없으면 (2번째로 권한 물어보는 if문)
            TedPermission.with(c)
                    .setPermissionListener(permissionListener)
                    //.setRationaleMessage("카메라 권한이 필요합니다.")
                    .setDeniedMessage("설정에서 저장공간 권한을 허용해주세요.")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        }
    }

    public void car_permissioncheck(Context c){
        context = c;
        TedPermission.with(c)
                .setPermissionListener(permissionListener)
                //.setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("설정에서 카메라와 저장공간 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }

    // 권한 체크 여부를 듣는 함수
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(context, "권한이 허용됨",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(context, "권한이 거부됨",Toast.LENGTH_SHORT).show();
        }
    };

    // 권한 있는지 확인 함수
    public boolean hasPermissions(String[] permissions, Context context) {
        int result;
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(context, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}
