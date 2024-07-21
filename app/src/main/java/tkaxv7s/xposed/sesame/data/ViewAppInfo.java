package tkaxv7s.xposed.sesame.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import lombok.Getter;
import lombok.Setter;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.util.Log;

public final class ViewAppInfo {

    @Getter
    private static Context context = null;

    @Getter
    private static String appTitle = "";

    @Getter
    private static String appVersion = "";

    @Setter
    @Getter
    private static RunType runType = RunType.DISABLE;

    public static void init(Context context) {
        if (ViewAppInfo.context == null) {
            ViewAppInfo.context = context;
            appTitle = context.getString(R.string.app_name);
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = packageInfo.versionName;
                appTitle = appTitle + " " + appVersion;
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    public static RunType checkRunType() {
        if (runType != null) {
            return runType;
        }
        try {
            if (context == null) {
                return runType = RunType.DISABLE;
            }
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = null;
            try {
                result = contentResolver.call(uri, "active", null, null);
            } catch (RuntimeException e) {
                // TaiChi is killed, try invoke
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable e1) {
                    return runType = RunType.DISABLE;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }

            if (result == null) {
                return runType = RunType.DISABLE;
            }
            if (result.getBoolean("active", false)) {
                return runType = RunType.MODEL;
            }
            return runType = RunType.DISABLE;
        } catch (Throwable ignored) {
        }
        return runType = RunType.DISABLE;
    }

    public static void setRunTypeByCode(Integer runTypeCode) {
        RunType newRunType = RunType.getByCode(runTypeCode);
        if (newRunType == null) {
            newRunType = RunType.DISABLE;
        }
        ViewAppInfo.runType = newRunType;
    }

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
