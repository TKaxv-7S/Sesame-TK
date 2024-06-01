package pansong291.xposed.quickenergy;

import android.content.Context;
import android.widget.Toast;

import pansong291.xposed.quickenergy.hook.XposedHook;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;

public class AntForestToast {
    private static final String TAG = AntForestToast.class.getCanonicalName();

    public static void show(CharSequence cs) {
        show(cs, false);
    }

    public static void show(CharSequence cs, boolean force) {
        try {
            Context context = XposedHook.getContext();
            if (context != null && (force || Config.showToast())) {
                XposedHook.getMainHandler().post(() -> {
                    try {
                        Toast toast = Toast.makeText(context, cs, Toast.LENGTH_SHORT);
                        toast.setGravity(toast.getGravity(), toast.getXOffset(), Config.toastOffsetY());
                        toast.show();
                    } catch (Throwable t) {
                        Log.i(TAG, "show.run err:");
                        Log.printStackTrace(TAG, t);
                    }
                });
            }
        } catch (Throwable t) {
            Log.i(TAG, "show err:");
            Log.printStackTrace(TAG, t);
        }
    }
}
