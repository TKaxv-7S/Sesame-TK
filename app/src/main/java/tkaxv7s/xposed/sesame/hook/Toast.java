package tkaxv7s.xposed.sesame.hook;

import android.content.Context;
import android.os.Handler;

import tkaxv7s.xposed.sesame.data.ConfigV2;
import tkaxv7s.xposed.sesame.util.Log;

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
