package tkaxv7s.xposed.sesame.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import tkaxv7s.xposed.sesame.data.ConfigV2;


public class LanguageUtil {
    // 存储 Context 对象
    private static Context mContext;

    // 初始化 Utils 类，传入 Context 对象
    public static void initialize(Context context) {
        mContext = context;
    }


    public static void setLocale(Context context) {
        if (ConfigV2.INSTANCE.isLanguageSimplifiedChinese()) {
            // 忽略系统语言，强制使用简体中文
            Locale locale = new Locale("zh", "CN"); // 简体中文的区域代码
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    // 获取字符串资源
    public static String getString(int resId) {
        // 检查 Context 是否为空
        if (mContext == null) {
            // 抛出异常，或者你可以选择返回一个默认字符串
            throw new IllegalStateException("Utils 类尚未初始化！");
        }
        return mContext.getString(resId);
    }
}
