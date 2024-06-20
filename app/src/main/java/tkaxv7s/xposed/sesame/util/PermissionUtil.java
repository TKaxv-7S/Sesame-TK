package tkaxv7s.xposed.sesame.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.task.model.antForest.AntForestRpcCall;

public class PermissionUtil {
    private static final String TAG = AntForestRpcCall.class.getSimpleName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    public static Boolean checkOrRequestAllPermissions(Activity activity) {
        return checkOrRequestFilePermissions(activity) && checkOrRequestAlarmPermissions(activity);
    }

    public static boolean checkFilePermissions(Context context) {
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

    public static Boolean checkOrRequestFilePermissions(Activity activity) {
        try {
            if (checkFilePermissions(activity)) {
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
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG, e);
        }
        return false;
    }

    public static boolean checkAlarmPermissions() {
        Context context;
        try {
            if (!ApplicationHook.isHooked()) {
                return false;
            }
            context = ApplicationHook.getContext();
            if (context == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //判断是否有使用闹钟的权限
            AlarmManager systemService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (systemService != null) {
                return systemService.canScheduleExactAlarms();
            }
            return true;
        }
        return true;
    }

    public static Boolean checkOrRequestAlarmPermissions(Context context) {
        try {
            if (checkAlarmPermissions()) {
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //跳转到权限页，请求权限
                Intent appIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appIntent.setData(Uri.parse("package:" + ClassUtil.PACKAGE_NAME));
                //appIntent.setData(Uri.fromParts("package", ClassUtil.PACKAGE_NAME, null));
                try {
                    context.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG, e);
        }
        return false;
    }

    public static boolean checkBatteryPermissions() {
        Context context;
        try {
            if (!ApplicationHook.isHooked()) {
                return false;
            }
            context = ApplicationHook.getContext();
            if (context == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断是否有始终在后台运行的权限
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                return powerManager.isIgnoringBatteryOptimizations(ClassUtil.PACKAGE_NAME);
            }
            return true;
        }
        return true;
    }

    public static Boolean checkOrRequestBatteryPermissions(Context context) {
        try {
            if (checkBatteryPermissions()) {
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //跳转到权限页，请求权限
                Intent appIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appIntent.setData(Uri.parse("package:" + ClassUtil.PACKAGE_NAME));
                //appIntent.setData(Uri.fromParts("package", ClassUtil.PACKAGE_NAME, null));
                try {
                    context.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG, e);
        }
        return false;
    }
}
