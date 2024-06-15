package pansong291.xposed.quickenergy.hook;

import android.content.Context;
import android.os.Handler;

import pansong291.xposed.quickenergy.data.ConfigV2;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;

public class Toast {
    private static final String TAG = Toast.class.getSimpleName();

    public static void show(CharSequence cs) {
        show(cs, false);
    }

    public static void show(CharSequence cs, boolean force) {
        Context context = ApplicationHook.getContext();
        if (context != null && (force || ConfigV2.INSTANCE.isShowToast())) {
            show(context, ApplicationHook.getMainHandler(), cs);
        }
    }

    public static void show(Context context, Handler handler, CharSequence cs) {
        try {
            handler.post(() -> {
                try {
                    android.widget.Toast toast = android.widget.Toast.makeText(context, cs, android.widget.Toast.LENGTH_SHORT);
                    toast.setGravity(toast.getGravity(), toast.getXOffset(), ConfigV2.INSTANCE.getToastOffsetY());
                    toast.show();
                } catch (Throwable t) {
                    Log.i(TAG, "show.run err:");
                    Log.printStackTrace(TAG, t);
                }
            });
        } catch (Throwable t) {
            Log.i(TAG, "show err:");
            Log.printStackTrace(TAG, t);
        }
    }
}
