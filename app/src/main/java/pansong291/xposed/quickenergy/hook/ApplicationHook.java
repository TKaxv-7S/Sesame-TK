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

    private static PendingIntent alarm0Pi;

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
                if (!config.isImmediateEffect()) {
                    Log.record("芝麻粒已禁用");
                    Toast.show("芝麻粒已禁用");
                    return;
                }
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
                if (config.isStartAt0()) {
                    try {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.eg.android.AlipayGphone.xqe.execute"), getPendingIntentFlag());
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        setAlarmTask(calendar.getTimeInMillis(), pendingIntent);
                        alarm0Pi = pendingIntent;
                    } catch (Throwable th) {
                        Log.printStackTrace("alarm0", th);
                    }
                }
                if (config.isStartAt7()) {
                    try {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("com.eg.android.AlipayGphone.xqe.restart"), getPendingIntentFlag());
                        Calendar calendar = Calendar.getInstance();
                        if (calendar.get(Calendar.HOUR_OF_DAY) >= 6 && calendar.get(Calendar.MINUTE) >= 55) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 6);
                        calendar.set(Calendar.MINUTE, 55);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        setAlarmTask(calendar.getTimeInMillis(), pendingIntent);
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
                                        Log.debug("record request | method: " + args[0] + " | args: " + args[4]);
                                    }

                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        if (rpcHookMap.containsKey(param.args[15])) {
                                            rpcHookMap.remove(param.args[15]);
                                            Log.debug("record request removed");
                                        }
                                    }

                                });
                        Log.i(TAG, "hook record request successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook record request err:");
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
                                            Log.debug("record response | data: " + param.args[0]);
                                        }
                                    }

                                });
                        Log.i(TAG, "hook record response successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook record response err:");
                        Log.printStackTrace(TAG, t);
                    }
                }
                Task.initAllTask();
                Statistics.load();
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
                            if (targetUid == null) {
                                Log.record("用户为空，放弃执行");
                                return;
                            }
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
                            int checkInterval = config.getCheckInterval();
                            mainHandler.postDelayed(this, checkInterval);
                            Notification.setNextScanTime(System.currentTimeMillis() + checkInterval);
                            FileUtils.clearLog();
                        } catch (Exception e) {
                            Log.record("执行异常:");
                            Log.printStackTrace(e);
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
                try {
                    if (alarm0Pi != null) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(alarm0Pi);
                    }
                } catch (Throwable th) {
                    Log.printStackTrace("alarm0", th);
                } finally {
                    alarm0Pi = null;
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

    private static void setAlarmTask(long triggerAtMillis, PendingIntent operation) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            }
            Log.i("setAlarmTask triggerAtMillis:" + triggerAtMillis + " operation:" + operation.toString());
        } catch (Throwable th) {
            Log.i(TAG, "setAlarmTask err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static String requestString(RpcEntity rpcEntity) {
        return rpcBridge.requestString(rpcEntity, 3);
    }

    public static String requestString(RpcEntity rpcEntity, int retryCount) {
        return rpcBridge.requestString(rpcEntity, retryCount);
    }

    public static String requestString(String method, String data) {
        return rpcBridge.requestString(method, data);
    }

    public static String requestString(String method, String data, int retryCount) {
        return rpcBridge.requestString(method, data, retryCount);
    }

    public static RpcEntity requestObject(RpcEntity rpcEntity) {
        return rpcBridge.requestObject(rpcEntity, 3);
    }

    public static RpcEntity requestObject(RpcEntity rpcEntity, int retryCount) {
        return rpcBridge.requestObject(rpcEntity, retryCount);
    }

    public static RpcEntity requestObject(String method, String data) {
        return rpcBridge.requestObject(method, data);
    }

    public static RpcEntity requestObject(String method, String data, int retryCount) {
        return rpcBridge.requestObject(method, data, retryCount);
    }

    public static void holdByAlarm(long delayTime, boolean force) {
        try {
            Intent it = new Intent();
            it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_SERVICE);
            PendingIntent pi = PendingIntent.getService(context, 2, it, getPendingIntentFlag());
            if (force) {
                offline = true;
            }
            setAlarmTask(System.currentTimeMillis() + delayTime, pi);
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
            if (callMethod != null) {
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
            Log.i("broadcast action:" + action + " intent:" + intent);
            if ("com.eg.android.AlipayGphone.xqe.restart".equals(action)) {
                startHandler(true);
            } else if ("com.eg.android.AlipayGphone.xqe.execute".equals(action)) {
                startHandler(false);
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
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.execute");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.reLogin");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.test");
            context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
            Log.i(TAG, "hook registerBroadcastReceiver successfully");
        } catch (Throwable th) {
            Log.i(TAG, "hook registerBroadcastReceiver err:");
            Log.printStackTrace(TAG, th);
        }
    }

}
