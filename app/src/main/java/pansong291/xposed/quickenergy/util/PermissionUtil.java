package pansong291.xposed.quickenergy.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import pansong291.xposed.quickenergy.hook.AntForestRpcCall;

public class PermissionUtil {
    private static final String TAG = AntForestRpcCall.class.getSimpleName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    public static boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //判断是否有管理外部存储的权限
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS_STORAGE) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static Boolean checkOrRequestPermissions(Activity activity) {
        try {
            if (checkPermissions(activity)) {
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //跳转到权限页，请求权限
                Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                appIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                //appIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                try {
                    activity.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    allFileIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(allFileIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG, e);
        }
        return false;
    }
}
