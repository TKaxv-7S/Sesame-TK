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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.Getter;
import pansong291.xposed.quickenergy.data.ConfigV2;
import pansong291.xposed.quickenergy.data.ModelType;
import pansong291.xposed.quickenergy.data.ViewAppInfo;
import pansong291.xposed.quickenergy.entity.RpcEntity;
import pansong291.xposed.quickenergy.rpc.NewRpcBridge;
import pansong291.xposed.quickenergy.rpc.OldRpcBridge;
import pansong291.xposed.quickenergy.rpc.RpcBridge;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.task.common.TaskCommon;
import pansong291.xposed.quickenergy.task.model.antMember.AntMemberRpcCall;
import pansong291.xposed.quickenergy.util.ClassUtil;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.FileUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.PermissionUtil;
import pansong291.xposed.quickenergy.util.Statistics;
import pansong291.xposed.quickenergy.util.TimeUtil;
import pansong291.xposed.quickenergy.util.UserIdMap;

public class ApplicationHook implements IXposedHookLoadPackage {

    private static final String TAG = ApplicationHook.class.getSimpleName();

    private static final Map<Object, Boolean> rpcHookMap = new ConcurrentHashMap<>();

    @Getter
    private static volatile boolean hooked = false;

    private static volatile boolean init = false;

    @Getter
    private static volatile boolean offline = false;

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

    private static PowerManager.WakeLock wakeLock;

    private static PendingIntent alarmLastPi;

    private static PendingIntent alarm0Pi;

    private static PendingIntent alarm7Pi;

    private static XC_MethodHook.Unhook rpcRequestUnhook;

    private static XC_MethodHook.Unhook rpcResponseUnhook;

    public static void setOffline(boolean offline) {
        ApplicationHook.offline = offline;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("pansong291.xposed.quickenergy.repair".equals(lpparam.packageName)) {
            try {
                XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass(ViewAppInfo.class.getName()), "setModelTypeByCode", ModelType.MODEL.getCode());
            } catch (ClassNotFoundException e) {
                Log.printStackTrace(e);
            }
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
                                String currentUid = UserIdMap.getCurrentUid();
                                if (!targetUid.equals(currentUid)) {
                                    UserIdMap.setCurrentUid(targetUid);
                                    if (currentUid != null) {
                                        Toast.show("芝麻粒切换用户");
                                        initHandler(true);
                                        Log.i(TAG, "Activity changeUser");
                                        return;
                                    }
                                }
                                if (offline) {
                                    execHandler();
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
                                mainRunner = new Runnable() {

                                    private volatile long lastExecTime = 0;

                                    @Override
                                    public void run() {
                                        if (!init) {
                                            return;
                                        }
                                        Log.record("开始执行");
                                        try {
                                            ConfigV2 config = ConfigV2.INSTANCE;
                                            if (lastExecTime + 5000 > System.currentTimeMillis()) {
                                                Log.record("执行间隔较短，跳过执行");
                                                execDelayedHandler(config.getCheckInterval());
                                                return;
                                            }

                                            String targetUid = getUserId();
                                            if (targetUid == null) {
                                                Log.record("用户为空，放弃执行");
                                                return;
                                            }
                                            String currentUid = UserIdMap.getCurrentUid();
                                            if (!targetUid.equals(currentUid)) {
                                                if (currentUid != null) {
                                                    reLogin();
                                                    return;
                                                }
                                            }
                                            UserIdMap.setCurrentUid(targetUid);
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
                                                if (!checkTask.get(2, TimeUnit.SECONDS)) {
                                                    reLogin();
                                                    return;
                                                }
                                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                                Log.i(TAG, "check timeout");
                                                execDelayedHandler(config.getCheckInterval());
                                                return;
                                            } catch (Exception e) {
                                                Log.i(TAG, "check err:");
                                                Log.printStackTrace(TAG, e);
                                                execDelayedHandler(config.getCheckInterval());
                                                return;
                                            }
                                            TaskCommon.update();
                                            ModelTask.startAllTask(false);
                                            lastExecTime = System.currentTimeMillis();
                                            int checkInterval = config.getCheckInterval();

                                            try {
                                                List<String> execAtTimeList = config.getExecAtTimeList();
                                                if (execAtTimeList != null) {
                                                    long nextExecTime = lastExecTime + checkInterval;
                                                    Calendar lastExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(lastExecTime);
                                                    Calendar nextExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(nextExecTime);
                                                    for (String execAtTime : execAtTimeList) {
                                                        Calendar execAtTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(execAtTime);
                                                        if (lastExecTimeCalendar.compareTo(execAtTimeCalendar) < 0 && nextExecTimeCalendar.compareTo(execAtTimeCalendar) > 0) {
                                                            Log.record("下次检测为定时执行:" + execAtTime);
                                                            execDelayedHandler(execAtTimeCalendar.getTimeInMillis() - lastExecTime);
                                                            FileUtil.clearLog();
                                                            return;
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.i(TAG, "execAtTime err:");
                                                Log.printStackTrace(TAG, e);
                                            }

                                            execDelayedHandler(checkInterval);
                                            FileUtil.clearLog();
                                        } catch (Exception e) {
                                            Log.record("执行异常:");
                                            Log.printStackTrace(e);
                                        }
                                    }
                                };
                                registerBroadcastReceiver(appService);
                                initHandler(true);
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
                        destroyHandler(true);
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

    private static void execHandler() {
        if (context != null) {
            mainHandler.removeCallbacks(mainRunner);
            mainHandler.post(mainRunner);
        }
    }

    private static void execDelayedHandler(long delayMillis) {
        if (context != null) {
            mainHandler.postDelayed(mainRunner, delayMillis);
            try {
                Notification.setNextExecTime(System.currentTimeMillis() + delayMillis);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    @SuppressLint("WakelockTimeout")
    private static void initHandler(Boolean force) {
        if (context == null) {
            return;
        }
        destroyHandler(force);
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
                ConfigV2 config = ConfigV2.load();
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
                if (config.isStayAwake()) {
                    try {
                        PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
                        wakeLock.acquire();
                        Log.record("stayAwake 已设置");
                    } catch (Throwable th) {
                        Log.i(TAG, "stayAwake err:");
                        Log.printStackTrace(TAG, th);
                    }
                    try {
                        Intent it = new Intent();
                        it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_SERVICE);
                        PendingIntent pi = PendingIntent.getService(context, 0, it, getPendingIntentFlag());
                        setAlarmTask(System.currentTimeMillis() + 30 * 60 * 1000, pi);
                        Log.record("stayAwakeAlarm 已设置");
                    } catch (Throwable th) {
                        Log.i(TAG, "stayAwakeAlarm err:");
                        Log.printStackTrace(TAG, th);
                    }
                }
                try {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("com.eg.android.AlipayGphone.xqe.execute"), getPendingIntentFlag());
                    Calendar calendar = Calendar.getInstance();
                    if (calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) == 57) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 57);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    setAlarmTask(calendar.getTimeInMillis(), pendingIntent);
                    alarmLastPi = pendingIntent;
                    Log.record("alarmLast 已设置");
                } catch (Throwable th) {
                    Log.printStackTrace("alarmLast", th);
                }
                if (config.isStartAt0()) {
                    try {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, new Intent("com.eg.android.AlipayGphone.xqe.execute"), getPendingIntentFlag());
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        setAlarmTask(calendar.getTimeInMillis(), pendingIntent);
                        alarm0Pi = pendingIntent;
                        Log.record("alarm0 已设置");
                    } catch (Throwable th) {
                        Log.printStackTrace("alarm0", th);
                    }
                }
                if (config.isStartAt7()) {
                    try {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 3, new Intent("com.eg.android.AlipayGphone.xqe.restart"), getPendingIntentFlag());
                        Calendar calendar = Calendar.getInstance();
                        if (calendar.get(Calendar.HOUR_OF_DAY) >= 7 || (calendar.get(Calendar.HOUR_OF_DAY) >= 6 && calendar.get(Calendar.MINUTE) >= 55)) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 6);
                        calendar.set(Calendar.MINUTE, 55);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        setAlarmTask(calendar.getTimeInMillis(), pendingIntent);
                        alarm7Pi = pendingIntent;
                        Log.record("alarm7 已设置");
                    } catch (Throwable th) {
                        Log.printStackTrace("alarm7", th);
                    }
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
                Statistics.load();
                Notification.start(service);
                Log.record("加载完成");
                Toast.show("芝麻粒加载成功");
                init = true;
            }
            offline = false;
            execHandler();
        } catch (Throwable th) {
            Log.i(TAG, "startHandler err:");
            Log.printStackTrace(TAG, th);
            Toast.show("芝麻粒加载失败");
        }
    }

    private static void destroyHandler(Boolean force) {
        try {
            if (force) {
                if (context != null) {
                    Notification.stop(service, false);
                    mainHandler.removeCallbacks(mainRunner);
                }
                if (rpcResponseUnhook != null) {
                    rpcResponseUnhook.unhook();
                }
                if (rpcRequestUnhook != null) {
                    rpcRequestUnhook.unhook();
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
                try {
                    if (alarmLastPi != null) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(alarmLastPi);
                    }
                } catch (Throwable th) {
                    Log.printStackTrace("alarmLast", th);
                } finally {
                    alarmLastPi = null;
                }
                if (wakeLock != null) {
                    wakeLock.release();
                    wakeLock = null;
                }
                if (rpcBridge != null) {
                    rpcBridge.unload();
                    rpcBridge = null;
                }
                ModelTask.destroyAllTask();
            } else {
                ModelTask.stopAllTask();
            }
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
            Log.i("setAlarmTask triggerAtMillis:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerAtMillis) + " operation:" + (operation == null ? "" : operation.toString()));
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

    public static void reLoginByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.reLogin"));
        } catch (Throwable th) {
            Log.i(TAG, "xqe sendBroadcast reLogin err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static void restartByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.restart"));
        } catch (Throwable th) {
            Log.i(TAG, "xqe sendBroadcast restart err:");
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

    public static void reLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        offline = true;
        context.startActivity(intent);
    }

    /*public static Boolean reLogin() {
        Object authService = getExtServiceByInterface("com.alipay.mobile.framework.service.ext.security.AuthService");
        if ((Boolean) XposedHelpers.callMethod(authService, "rpcAuth")) {
            return true;
        }
        Log.record("重新登录失败");
        return false;
    }*/

    private static class AlipayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("xqe broadcast action:" + action + " intent:" + intent);
            if (action != null) {
                switch (action) {
                    case "com.eg.android.AlipayGphone.xqe.restart":
                        initHandler(true);
                        break;
                    case "com.eg.android.AlipayGphone.xqe.execute":
                        initHandler(false);
                        break;
                    case "com.eg.android.AlipayGphone.xqe.reLogin":
                        reLogin();
                        break;
                    case "com.eg.android.AlipayGphone.xqe.status":
                        try {
                            context.sendBroadcast(new Intent("pansong291.xposed.quickenergy.status"));
                        } catch (Throwable th) {
                            Log.i(TAG, "xqe sendBroadcast status err:");
                            Log.printStackTrace(TAG, th);
                        }
                        break;
                }
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
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.status");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
            }
            Log.i(TAG, "hook registerBroadcastReceiver successfully");
        } catch (Throwable th) {
            Log.i(TAG, "hook registerBroadcastReceiver err:");
            Log.printStackTrace(TAG, th);
        }
    }

}
