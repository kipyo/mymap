package com.work.kipyo.mymap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by q on 2016-09-25.
 */

public class PrevActivity extends Activity {
    private final static int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //현위치 정보를 가져오기 위한 퍼미션 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ArrayList<String> permissionList = new ArrayList<String>();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                String[] permissionSet = new String[permissionList.size()];
                Iterator<String> iter = permissionList.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    permissionSet[i++] = iter.next();
                }
                requestPermissions(permissionSet, LOCATION_PERMISSION);
            }
        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent. FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION:
                if (allPermissionGranted(grantResults)) {
                    startMainActivity();
                } else {
                    Toast.makeText(this, "이 앱은 위치 정보를 얻기위한 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private boolean allPermissionGranted(int[] grantResults) {
        if (grantResults.length > 0) {
            for(int i = 0; i < grantResults.length ; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
