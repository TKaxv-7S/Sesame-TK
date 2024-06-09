package pansong291.xposed.quickenergy.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.Getter;
import pansong291.xposed.quickenergy.entity.RpcEntity;
import pansong291.xposed.quickenergy.rpc.NewRpcBridge;
import pansong291.xposed.quickenergy.rpc.OldRpcBridge;
import pansong291.xposed.quickenergy.rpc.RpcBridge;
import pansong291.xposed.quickenergy.task.common.Task;
import pansong291.xposed.quickenergy.task.common.TaskCommon;
import pansong291.xposed.quickenergy.task.model.antMember.AntMemberRpcCall;
import pansong291.xposed.quickenergy.ui.MainActivity;
import pansong291.xposed.quickenergy.util.ClassUtil;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.FileUtils;
import pansong291.xposed.quickenergy.util.FriendIdMap;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.PermissionUtil;
import pansong291.xposed.quickenergy.util.Statistics;
import pansong291.xposed.quickenergy.util.TimeUtil;

public class ApplicationHook implements IXposedHookLoadPackage {

    private static final String TAG = ApplicationHook.class.getSimpleName();

    private static final Map<Object, Boolean> rpcHookMap = new ConcurrentHashMap<>();

    @Getter
    private static volatile boolean hooked = false;

    private static volatile boolean init = false;

    @Getter
    private static volatile boolean offline = true;

    @Getter
    private static volatile ClassLoader classLoader;

    @Getter
    @SuppressLint("StaticFieldLeak")
    private static volatile Context context = null;

    @SuppressLint("StaticFieldLeak")
    private static volatile Service service;

    @Getter
    private static Handler mainHandler;

    private static Runnable mainRunner;

    private static RpcBridge rpcBridge;

    private static PendingIntent alarm7Pi;

    private static PowerManager.WakeLock wakeLock;

    private static XC_MethodHook.Unhook rpcRequestUnhook;

    private static XC_MethodHook.Unhook rpcResponseUnhook;

    public static void setOffline(boolean offline) {
        ApplicationHook.offline = offline;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("pansong291.xposed.quickenergy.repair".equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader, "setModuleActive", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = true;
                }
            });
        } else if (ClassUtil.PACKAGE_NAME.equals(lpparam.packageName) && ClassUtil.PACKAGE_NAME.equals(lpparam.processName)) {
            if (hooked) {
                return;
            }
            classLoader = lpparam.classLoader;
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.nebulaappproxy.api.rpc.H5AppRpcUpdate", classLoader, "matchVersion", classLoader.loadClass(ClassUtil.H5PAGE_NAME), Map.class, String.class, XC_MethodReplacement.returnConstant(false));
                Log.i(TAG, "hook matchVersion successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook matchVersion err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alibaba.health.pedometer.core.datasource.PedometerAgent", classLoader,
                        "readDailyStep", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                int originStep = (Integer) param.getResult();
                                int step = Config.tmpStepCount();
                                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || originStep >= step) {
                                    return;
                                }
                                param.setResult(step);

                            }
                        });
                Log.i(TAG, "hook readDailyStep successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook readDailyStep err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", classLoader,
                        "onResume", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Log.i(TAG, "Activity onResume");
                                if (!init) {
                                    startHandler(false);
                                    return;
                                }
                                String targetUid = getUserId();
                                if (targetUid == null) {
                                    return;
                                }
                                String currentUid = FriendIdMap.getCurrentUid();
                                if (!targetUid.equals(currentUid)) {
                                    FriendIdMap.setCurrentUid(targetUid);
                                    if (currentUid != null) {
                                        startHandler(true);
                                        Log.i(TAG, "Activity changeUser");
                                        return;
                                    }
                                    offline = true;
                                }
                                if (offline) {
                                    startHandler(false);
                                    ((Activity) param.thisObject).finish();
                                    Log.i(TAG, "Activity reLogin");
                                }
                            }
                        });
                Log.i(TAG, "hook login successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook login err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Service", classLoader, "onCreate", new XC_MethodHook() {

                            @SuppressLint("WakelockTimeout")
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Service appService = (Service) param.thisObject;
                                if (!ClassUtil.CURRENT_USING_SERVICE.equals(appService.getClass().getCanonicalName())) {
                                    return;
                                }
                                Log.i(TAG, "Service onCreate");
                                context = appService.getApplicationContext();
                                service = appService;
                                mainHandler = new Handler();
                                registerBroadcastReceiver(appService);
                                startHandler(true);
                            }
                        });
                Log.i(TAG, "hook service onCreate successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook service onCreate err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("android.app.Service", classLoader, "onDestroy", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Service service = (Service) param.thisObject;
                        if (!ClassUtil.CURRENT_USING_SERVICE.equals(service.getClass().getCanonicalName())) {
                            return;
                        }
                        Notification.setContentText("支付宝前台服务被销毁");
                        stopHandler(true);
                        Log.record("支付宝前台服务被销毁");
                        restartByBroadcast();
                    }
                });
                Log.i(TAG, "hook service onDestroy successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook service onDestroy err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackground", XC_MethodReplacement.returnConstant(false));
                Log.i(TAG, "hook FgBgMonitorImpl method 1 successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook FgBgMonitorImpl method 1 err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackground", boolean.class, XC_MethodReplacement.returnConstant(false));
                Log.i(TAG, "hook FgBgMonitorImpl method 2 successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook FgBgMonitorImpl method 2 err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackgroundV2", XC_MethodReplacement.returnConstant(false));
                Log.i(TAG, "hook FgBgMonitorImpl method 3 successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook FgBgMonitorImpl method 3 err:");
                Log.printStackTrace(TAG, t);
            }
            try {
                XposedHelpers.findAndHookMethod("com.alipay.mobile.common.transport.utils.MiscUtils", classLoader, "isAtFrontDesk", classLoader.loadClass("android.content.Context"), XC_MethodReplacement.returnConstant(true));
                Log.i(TAG, "hook MiscUtils successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook MiscUtils err:");
                Log.printStackTrace(TAG, t);
            }
            hooked = true;
            Log.i(TAG, "load success: " + lpparam.packageName);
        }
    }

    @SuppressLint("WakelockTimeout")
    private static void startHandler(Boolean force) {
        if (context == null) {
            return;
        }
        stopHandler(force);
        try {
            if (!init || force) {
                if (!PermissionUtil.checkAlarmPermissions()) {
                    Log.record("支付宝无闹钟权限");
                    mainHandler.postDelayed(() -> {
                        if (!PermissionUtil.checkOrRequestAlarmPermissions(context)) {
                            android.widget.Toast.makeText(context, "请授予支付宝使用闹钟权限", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                    return;
                }
                Log.record("开始加载");
                Config config = Config.load();
                if (config.isBatteryPerm() && !init && !PermissionUtil.checkBatteryPermissions()) {
                    Log.record("支付宝无始终在后台运行权限");
                    mainHandler.postDelayed(() -> {
                        if (!PermissionUtil.checkOrRequestBatteryPermissions(context)) {
                            android.widget.Toast.makeText(context, "请授予支付宝终在后台运行权限", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }
                if (config.isNewRpc()) {
                    rpcBridge = new NewRpcBridge();
                } else {
                    rpcBridge = new OldRpcBridge();
                }
                rpcBridge.load();
                if (config.isStartAt7()) {
                    try {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.eg.android.AlipayGphone.xqe.restart"), getPendingIntentFlag());
                        Calendar calendar = Calendar.getInstance();
                        if (calendar.get(Calendar.HOUR_OF_DAY) >= 7) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 6);
                        calendar.set(Calendar.MINUTE, 55);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                        }*/
                        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), pendingIntent);
                        alarm7Pi = pendingIntent;
                    } catch (Throwable th) {
                        Log.printStackTrace("alarm7", th);
                    }
                }
                if (config.isStayAwake()) {
                    PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
                    wakeLock.acquire();
                    holdByAlarm(30 * 60 * 1000, false);
                }
                if (config.isNewRpc() && config.isDebugMode()) {
                    try {
                        rpcRequestUnhook = XposedHelpers.findAndHookMethod(
                                "com.alibaba.ariver.commonability.network.rpc.RpcBridgeExtension", classLoader
                                , "rpc"
                                , String.class, boolean.class, boolean.class, String.class, classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME), String.class, classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME), boolean.class, boolean.class, int.class, boolean.class, String.class, classLoader.loadClass("com.alibaba.ariver.app.api.App"), classLoader.loadClass("com.alibaba.ariver.app.api.Page"), classLoader.loadClass("com.alibaba.ariver.engine.api.bridge.model.ApiContext"), classLoader.loadClass("com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback")
                                , new XC_MethodHook() {

                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        Object[] args = param.args;
                                        rpcHookMap.put(args[15], true);
                                        Log.debug("record new rpc request: " + Arrays.toString(args));
                                    }

                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        if (rpcHookMap.containsKey(param.args[15])) {
                                            rpcHookMap.remove(param.args[15]);
                                            Log.debug("record new rpc request removed");
                                        }
                                    }

                                });
                        Log.i(TAG, "hook record new rpc request successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook record new rpc request err:");
                        Log.printStackTrace(TAG, t);
                    }
                    try {
                        rpcResponseUnhook = XposedHelpers.findAndHookMethod(
                                "com.alibaba.ariver.engine.common.bridge.internal.DefaultBridgeCallback", classLoader
                                , "sendJSONResponse"
                                , classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME)
                                , new XC_MethodHook() {

                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        if (Boolean.TRUE.equals(rpcHookMap.remove(param.thisObject))) {
                                            Log.debug("record new rpc response: " + Arrays.toString(param.args));
                                        }
                                    }

                                });
                        Log.i(TAG, "hook record new rpc response successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook record new rpc response err:");
                        Log.printStackTrace(TAG, t);
                    }
                }
                Statistics.load();
                Task.initAllTask();
                mainRunner = new Runnable() {
                    @Override
                    public void run() {
                        if (!init) {
                            return;
                        }
                        Log.record("开始执行");
                        try {
                            Config config = Config.INSTANCE;
                            String targetUid = getUserId();
                            if (targetUid != null) {
                                FriendIdMap.setCurrentUid(targetUid);
                                Notification.setContentTextExec();
                                try {
                                    Statistics.resetToday();
                                } catch (Exception e) {
                                    Log.i(TAG, "statistics err:");
                                    Log.printStackTrace(TAG, e);
                                }

                                try {
                                    FutureTask<Boolean> checkTask = new FutureTask<>(AntMemberRpcCall::check);
                                    Thread checkThread = new Thread(checkTask);
                                    checkThread.start();
                                    if (!checkTask.get()) {
                                        if (!reLogin()) {
                                            mainHandler.postDelayed(this, config.getCheckInterval());
                                            return;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "check err:");
                                    Log.printStackTrace(TAG, e);
                                    mainHandler.postDelayed(this, config.getCheckInterval());
                                    return;
                                }
                                TaskCommon.IS_MORNING = TimeUtil.getTimeStr().compareTo("0700") >= 0 && TimeUtil.getTimeStr().compareTo("0730") <= 0;
                                TaskCommon.IS_AFTER_8AM = TimeUtil.getTimeStr().compareTo("0800") >= 0;

                                Task.startAllTask(false);
                            }
                            int checkInterval = config.getCheckInterval();
                            Notification.setNextScanTime(System.currentTimeMillis() + checkInterval);
                            Notification.setContentTextIdle();
                            mainHandler.postDelayed(this, checkInterval);
                            FileUtils.clearLog(2);
                        } catch (Exception e){
                            Log.record("执行异常:");
                            Log.printStackTrace(e);
                        } finally {
                            Log.record("执行完成");
                        }
                    }
                };
                Log.record("加载完成");
                Toast.show("芝麻粒加载成功");
                init = true;
            }
            offline = false;
            Notification.start(service);
            mainHandler.post(mainRunner);
        } catch (Throwable th) {
            Log.i(TAG, "startHandler err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void stopHandler(Boolean force) {
        try {
            if (mainHandler != null && mainRunner != null) {
                mainHandler.removeCallbacks(mainRunner);
                Notification.stop(service, false);
            }
            if (force) {
                if (rpcResponseUnhook != null) {
                    rpcResponseUnhook.unhook();
                }
                if (rpcRequestUnhook != null) {
                    rpcRequestUnhook.unhook();
                }
                if (wakeLock != null) {
                    wakeLock.release();
                    wakeLock = null;
                }
                try {
                    if (alarm7Pi != null) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(alarm7Pi);
                    }
                } catch (Throwable th) {
                    Log.printStackTrace("alarm7", th);
                } finally {
                    alarm7Pi = null;
                }
                if (rpcBridge != null) {
                    rpcBridge.unload();
                    rpcBridge = null;
                }
            }
            Task.stopAllTask();
        } catch (Throwable th) {
            Log.i(TAG, "stopHandler err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static String requestString(String method, String data) {
        return rpcBridge.requestString(method, data);
    }

    public static String requestString(String method, String data, int retryCount) {
        return rpcBridge.requestString(method, data, retryCount);
    }

    public static RpcEntity requestObject(String method, String data) {
        return rpcBridge.requestObject(method, data);
    }

    public static RpcEntity requestObject(String method, String data, int retryCount) {
        return rpcBridge.requestObject(method, data, retryCount);
    }

    public static void holdByAlarm(long delayTime, boolean force) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent it = new Intent();
            it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_SERVICE);
            PendingIntent pi = PendingIntent.getService(context, 2, it, getPendingIntentFlag());
            if (force) {
                offline = true;
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, pi);
        } catch (Throwable th) {
            Log.i(TAG, "holdByAlarm err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static void reLoginByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.reLogin"));
        } catch (Throwable th) {
            Log.i(TAG, "reLoginByBroadcast err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static void restartByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.restart"));
        } catch (Throwable th) {
            Log.i(TAG, "restartByBroadcast err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static int getPendingIntentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    public static Object getMicroApplicationContext() {
        return XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication", classLoader), "getInstance"), "getMicroApplicationContext");
    }

    public static Object getExtServiceByInterface(String interfaceName) {
        return XposedHelpers.callMethod(getMicroApplicationContext(), interfaceName);
    }

    public static String getUserId() {
        try {
            Object callMethod = XposedHelpers.callMethod(XposedHelpers.callMethod(getMicroApplicationContext(), "findServiceByInterface", XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader).getName()), "getMyAccountInfoModelByLocal");
            if  (callMethod != null) {
                return (String) XposedHelpers.getObjectField(callMethod, "userId");
            }
        } catch (Throwable th) {
            Log.i(TAG, "getUserId err");
            Log.printStackTrace(TAG, th);
        }
        return null;
    }

    public static Boolean reLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        offline = true;
        context.startActivity(intent);
        return true;
        /*Object authService = getExtServiceByInterface("com.alipay.mobile.framework.service.ext.security.AuthService");
        if ((Boolean) XposedHelpers.callMethod(authService, "rpcAuth")) {
            return true;
        }
        Log.record("重新登录失败");
        return false;*/
    }

    private static class AlipayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.eg.android.AlipayGphone.xqe.restart".equals(action)) {
                startHandler(true);
            } else if ("com.eg.android.AlipayGphone.xqe.reLogin".equals(action)) {
                reLogin();
                startHandler(false);
            } else if ("com.eg.android.AlipayGphone.xqe.test".equals(action)) {
                Log.record("收到测试消息");
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadcastReceiver(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.restart");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.reLogin");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.test");
            context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
            Log.i(TAG, "hook registerBroadcastReceiver successfully");
        } catch (Throwable th) {
            Log.i(TAG, "hook registerBroadcastReceiver err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public interface StayAwakeType {

        int BROADCAST = 0;
        int ALARM = 1;
        int NONE = 2;

        String[] nickNames = {"广播", "闹钟", "不重启"};
    }

    public interface StayAwakeTarget {

        int SERVICE = 0;
        int ACTIVITY = 1;

        String[] nickNames = {"Service", "Activity"};
    }

}
