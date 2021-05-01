package com.example.taller3;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsManager {

    public static final int IMAGE_GALLERY_PERMISSION_ID = 0;
    public static final int CAMERA_PERMISSION_ID = 1;
    public static final int CONTACTS_PERMISSION_ID = 2;
    public static final int LOCATION_PERMISSION_ID = 3;
    public static final String FINE_LOCATION_PERMISSION_NAME = Manifest.permission.ACCESS_FINE_LOCATION;

    public static boolean askForPermission(Activity context, String permission, String message, int idPermissionCode){
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permission}, idPermissionCode);
        } else {
            return true;
        }
        return false;
    }

    public static boolean onRequestPermissionsResult(@NonNull int[] grantResults, Activity context, String message) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermission(Activity context, String permission, String justification, int idCode){
        context.requestPermissions(new String[]{permission},idCode);
    }
}
