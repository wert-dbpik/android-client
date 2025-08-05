package ru.wert.tubus_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class AppPermissions {

    public static int MY_PERMISSIONS_REQUEST_CAMERA;
    public static int MY_PERMISSIONS_REQUEST_INTERNET;
    public static int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE;

    private final Activity activity;

    public AppPermissions(Activity activity) {
        this.activity = activity;
    }

    public void checkUp(){

        if (activity.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_INTERNET);
        }
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        }

    }
}
