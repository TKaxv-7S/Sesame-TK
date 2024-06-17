package pansong291.xposed.quickenergy.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import lombok.Getter;
import lombok.Setter;
import pansong291.xposed.quickenergy.R;

public final class ViewAppInfo {

    @Getter
    private static Context context = null;

    @Getter
    private static String appTitle = "";

    @Getter
    private static String appVersion = "";

    @Setter
    @Getter
    private static ModelType modelType = null;

    public static void init(Context context) {
        ViewAppInfo.context = context;
        appTitle = context.getString(R.string.app_name);
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = " v" + packageInfo.versionName;
            appTitle += appVersion;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        checkModleType();
    }

    public static ModelType checkModleType() {
        if (modelType != null) {
            return modelType;
        }
        try {
            if (context == null) {
                return modelType = ModelType.DISABLE;
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
                    return modelType = ModelType.DISABLE;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }

            if (result == null) {
                return modelType = ModelType.DISABLE;
            }
            if (result.getBoolean("active", false)) {
                return modelType = ModelType.MODEL;
            }
            return modelType = ModelType.DISABLE;
        } catch (Throwable ignored) {
        }
        return modelType = ModelType.DISABLE;
    }

    public static void setModelTypeByCode(Integer modelTypeCode) {
        ViewAppInfo.modelType = ModelType.getByCode(modelTypeCode);
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
