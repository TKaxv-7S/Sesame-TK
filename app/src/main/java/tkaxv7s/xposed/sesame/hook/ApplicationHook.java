package tkaxv7s.xposed.sesame.hook;

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
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.Getter;
import tkaxv7s.xposed.sesame.data.ConfigV2;
import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.data.RunType;
import tkaxv7s.xposed.sesame.data.ViewAppInfo;
import tkaxv7s.xposed.sesame.data.task.BaseTask;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.FriendWatch;
import tkaxv7s.xposed.sesame.entity.RpcEntity;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.model.task.antMember.AntMemberRpcCall;
import tkaxv7s.xposed.sesame.rpc.NewRpcBridge;
import tkaxv7s.xposed.sesame.rpc.OldRpcBridge;
import tkaxv7s.xposed.sesame.rpc.RpcBridge;
import tkaxv7s.xposed.sesame.util.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationHook implements IXposedHookLoadPackage {

    private static final String TAG = ApplicationHook.class.getSimpleName();

    private static final Map<Object, Object[]> rpcHookMap = new ConcurrentHashMap<>();

    private static final Map<String, PendingIntent> wakenAtTimeAlarmMap = new ConcurrentHashMap<>();

    @Getter
    private static volatile boolean hooked = false;

    private static volatile boolean canInit = false;

    private static volatile boolean init = false;

    private static volatile Calendar dayCalendar;

    @Getter
    private static volatile boolean offline = false;

    @Getter
    private static final AtomicInteger reLoginCount = new AtomicInteger(0);

    @Getter
    private static volatile ClassLoader classLoader;

    @Getter
    @SuppressLint("StaticFieldLeak")
    private static volatile Context context = null;

    @SuppressLint("StaticFieldLeak")
    private static volatile Service service;

    @Getter
    private static Handler mainHandler;

    private static BaseTask mainTask;

    private static RpcBridge rpcBridge;

    private static PowerManager.WakeLock wakeLock;

    private static PendingIntent alarm0Pi;

    private static XC_MethodHook.Unhook rpcRequestUnhook;

    private static XC_MethodHook.Unhook rpcResponseUnhook;

    public static void setOffline(boolean offline) {
        ApplicationHook.offline = offline;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("tkaxv7s.xposed.sesame".equals(lpparam.packageName)) {
            try {
                XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass(ViewAppInfo.class.getName()), "setRunTypeByCode", RunType.MODEL.getCode());
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
                XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", classLoader,
                        "onResume", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Log.i(TAG, "Activity onResume");
                                String targetUid = getUserId();
                                if (targetUid == null) {
                                    Log.record("用户未登录");
                                    Toast.show("用户未登录");
                                    return;
                                }
                                if (!init) {
                                    if (canInit) {
                                        if (initHandler(true)) {
                                            init = true;
                                        }
                                    }
                                    return;
                                }
                                String currentUid = UserIdMap.getCurrentUid();
                                if (!targetUid.equals(currentUid)) {
                                    if (currentUid != null) {
                                        initHandler(true);
                                        Log.record("用户已切换");
                                        Toast.show("用户已切换");
                                        return;
                                    }
                                    UserIdMap.initUser(targetUid);
                                }
                                if (offline) {
                                    offline = false;
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
                                mainTask = BaseTask.newInstance("MAIN_TASK", new Runnable() {

                                    private volatile long lastExecTime = 0;

                                    @Override
                                    public void run() {
                                        if (!init) {
                                            return;
                                        }
                                        Log.record("开始执行");
                                        try {
                                            int checkInterval = BaseModel.getCheckInterval().getValue();
                                            if (lastExecTime + 5000 > System.currentTimeMillis()) {
                                                Log.record("执行间隔较短，跳过执行");
                                                execDelayedHandler(checkInterval);
                                                return;
                                            }
                                            updateDay();
                                            String targetUid = getUserId();
                                            String currentUid = UserIdMap.getCurrentUid();
                                            if (targetUid == null || currentUid == null) {
                                                Log.record("用户为空，放弃执行");
                                                reLogin();
                                                return;
                                            }
                                            if (!targetUid.equals(currentUid)) {
                                                Log.record("开始切换用户");
                                                Toast.show("开始切换用户");
                                                reLogin();
                                                return;
                                            }
                                            lastExecTime = System.currentTimeMillis();
                                            try {
                                                FutureTask<Boolean> checkTask = new FutureTask<>(AntMemberRpcCall::check);
                                                Thread checkThread = new Thread(checkTask);
                                                checkThread.start();
                                                if (!checkTask.get(10, TimeUnit.SECONDS)) {
                                                    long waitTime = 10000 - System.currentTimeMillis() + lastExecTime;
                                                    if (waitTime > 0) {
                                                        Thread.sleep(waitTime);
                                                    }
                                                    Log.record("执行失败：检查超时");
                                                    reLogin();
                                                    return;
                                                }
                                                reLoginCount.set(0);
                                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                                Log.record("执行失败：检查中断");
                                                reLogin();
                                                return;
                                            } catch (Exception e) {
                                                Log.record("执行失败：检查异常");
                                                reLogin();
                                                Log.printStackTrace(TAG, e);
                                                return;
                                            }
                                            TaskCommon.update();
                                            ModelTask.startAllTask(false);
                                            lastExecTime = System.currentTimeMillis();

                                            try {
                                                List<String> execAtTimeList = BaseModel.getExecAtTimeList().getValue();
                                                if (execAtTimeList != null) {
                                                    Calendar lastExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(lastExecTime);
                                                    Calendar nextExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(lastExecTime + checkInterval);
                                                    for (String execAtTime : execAtTimeList) {
                                                        Calendar execAtTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(execAtTime);
                                                        if (execAtTimeCalendar != null && lastExecTimeCalendar.compareTo(execAtTimeCalendar) < 0 && nextExecTimeCalendar.compareTo(execAtTimeCalendar) > 0) {
                                                            Log.record("设置定时执行:" + execAtTime);
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
                                });
                                registerBroadcastReceiver(appService);
                                dayCalendar = Calendar.getInstance();
                                canInit = true;
                                if (getUserId() != null) {
                                    Statistics.load();
                                    FriendWatch.load();
                                    if (initHandler(true)) {
                                        init = true;
                                    }
                                }
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
                        Log.record("支付宝前台服务被销毁");
                        NotificationUtil.updateStatusText("支付宝前台服务被销毁");
                        destroyHandler(true);
                        //FriendWatch.unload();
                        //Statistics.unload();
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

    private static void setWakenAtTimeAlarm() {
        try {
            unsetWakenAtTimeAlarm();
            try {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.eg.android.AlipayGphone.sesame.execute"), getPendingIntentFlag());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                if (setAlarmTask(calendar.getTimeInMillis(), pendingIntent)) {
                    alarm0Pi = pendingIntent;
                    Log.record("设置定时唤醒:0|000000");
                }
            } catch (Exception e) {
                Log.i(TAG, "setWakenAt0 err:");
                Log.printStackTrace(TAG, e);
            }
            List<String> wakenAtTimeList = BaseModel.getWakenAtTimeList().getValue();
            if (wakenAtTimeList != null && !wakenAtTimeList.isEmpty()) {
                Calendar nowCalendar = Calendar.getInstance();
                for (int i = 1, len = wakenAtTimeList.size(); i < len; i++) {
                    try {
                        String wakenAtTime = wakenAtTimeList.get(i);
                        Calendar wakenAtTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(wakenAtTime);
                        if (wakenAtTimeCalendar != null) {
                            if (wakenAtTimeCalendar.compareTo(nowCalendar) > 0) {
                                PendingIntent wakenAtTimePendingIntent = PendingIntent.getBroadcast(context, i, new Intent("com.eg.android.AlipayGphone.sesame.execute"), getPendingIntentFlag());
                                if (setAlarmTask(wakenAtTimeCalendar.getTimeInMillis(), wakenAtTimePendingIntent)) {
                                    String wakenAtTimeKey = i + "|" + wakenAtTime;
                                    wakenAtTimeAlarmMap.put(wakenAtTimeKey, wakenAtTimePendingIntent);
                                    Log.record("设置定时唤醒:" + wakenAtTimeKey);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "setWakenAtTime err:");
                        Log.printStackTrace(TAG, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "setWakenAtTimeAlarm err:");
            Log.printStackTrace(TAG, e);
        }
    }

    private static void unsetWakenAtTimeAlarm() {
        try {
            for (Map.Entry<String, PendingIntent> entry : wakenAtTimeAlarmMap.entrySet()) {
                try {
                    String wakenAtTimeKey = entry.getKey();
                    PendingIntent wakenAtTimePendingIntent = entry.getValue();
                    if (unsetAlarmTask(wakenAtTimePendingIntent)) {
                        wakenAtTimeAlarmMap.remove(wakenAtTimeKey);
                        Log.record("取消定时唤醒:" + wakenAtTimeKey);
                    }
                } catch (Exception e) {
                    Log.i(TAG, "unsetWakenAtTime err:");
                    Log.printStackTrace(TAG, e);
                }
            }
            try {
                if (unsetAlarmTask(alarm0Pi)) {
                    alarm0Pi = null;
                    Log.record("取消定时唤醒:0|000000");
                }
            } catch (Exception e) {
                Log.i(TAG, "unsetWakenAt0 err:");
                Log.printStackTrace(TAG, e);
            }
        } catch (Exception e) {
            Log.i(TAG, "unsetWakenAtTimeAlarm err:");
            Log.printStackTrace(TAG, e);
        }
    }

    @SuppressLint("WakelockTimeout")
    private Boolean initHandler(Boolean force) {
        if (context == null) {
            return false;
        }
        destroyHandler(force);
        try {
            if (force) {
                String userId = getUserId();
                if (userId == null) {
                    Log.record("用户未登录");
                    Toast.show("用户未登录");
                    return false;
                }
                UserIdMap.initUser(userId);
                if (!PermissionUtil.checkAlarmPermissions()) {
                    Log.record("支付宝无闹钟权限");
                    mainHandler.postDelayed(() -> {
                        if (!PermissionUtil.checkOrRequestAlarmPermissions(context)) {
                            android.widget.Toast.makeText(context, "请授予支付宝使用闹钟权限", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                    return false;
                }
                Model.initAllModel();
                Log.record("开始加载");
                ConfigV2.load(UserIdMap.getCurrentUid());
                if (!Model.getModel(BaseModel.class).getEnableField().getValue()) {
                    Log.record("芝麻粒已禁用");
                    Toast.show("芝麻粒已禁用");
                    return false;
                }
                if (BaseModel.getBatteryPerm().getValue() && !init && !PermissionUtil.checkBatteryPermissions()) {
                    Log.record("支付宝无始终在后台运行权限");
                    mainHandler.postDelayed(() -> {
                        if (!PermissionUtil.checkOrRequestBatteryPermissions(context)) {
                            android.widget.Toast.makeText(context, "请授予支付宝终在后台运行权限", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }
                if (BaseModel.getNewRpc().getValue()) {
                    rpcBridge = new NewRpcBridge();
                } else {
                    rpcBridge = new OldRpcBridge();
                }
                rpcBridge.load();
                if (BaseModel.getStayAwake().getValue()) {
                    try {
                        PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
                        wakeLock.acquire();
                    } catch (Throwable t) {
                        Log.printStackTrace(t);
                    }
                }
                //setWakenAtTimeAlarm();
                if (BaseModel.getNewRpc().getValue() && BaseModel.getDebugMode().getValue()) {
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
                                        Object object = args[15];
                                        Object[] recordArray = new Object[4];
                                        recordArray[0] = System.currentTimeMillis();
                                        recordArray[1] = args[0];
                                        recordArray[2] = args[4];
                                        rpcHookMap.put(object, recordArray);
                                    }

                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        Object object = param.args[15];
                                        Object[] recordArray = rpcHookMap.remove(object);
                                        if (recordArray != null) {
                                            Log.debug("记录\n时间: " + recordArray[0] + "\n方法: " + recordArray[1] + "\n参数: " + recordArray[2] + "\n数据: " + recordArray[3] + "\n");
                                        } else {
                                            Log.debug("删除记录ID: " + object.hashCode());
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
                                        Object object = param.thisObject;
                                        Object[] recordArray = rpcHookMap.get(object);
                                        if (recordArray != null) {
                                            recordArray[3] = String.valueOf(param.args[0]);
                                        }
                                    }

                                });
                        Log.i(TAG, "hook record response successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook record response err:");
                        Log.printStackTrace(TAG, t);
                    }
                }
                NotificationUtil.start(service);
                Model.bootAllModel(classLoader);
                Status.load();
                updateDay();
                BaseModel.initData();
                Log.record("加载完成");
                Toast.show("芝麻粒加载成功");
            }
            offline = false;
            execHandler();
            return true;
        } catch (Throwable th) {
            Log.i(TAG, "startHandler err:");
            Log.printStackTrace(TAG, th);
            Toast.show("芝麻粒加载失败");
            return false;
        }
    }

    private static void destroyHandler(Boolean force) {
        try {
            if (force) {
                if (context != null) {
                    stopHandler();
                    BaseModel.destroyData();
                    Status.unload();
                    NotificationUtil.stop();
                    ConfigV2.unload();
                    ModelTask.destroyAllModel();
                }
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
                if (rpcBridge != null) {
                    rpcBridge.unload();
                    rpcBridge = null;
                }
            } else {
                ModelTask.stopAllTask();
            }
        } catch (Throwable th) {
            Log.i(TAG, "stopHandler err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void execHandler() {
        if (context != null) {
            mainTask.startTask(false);
        }
    }

    private static void execDelayedHandler(long delayMillis) {
        if (context != null) {
            mainHandler.postDelayed(() -> mainTask.startTask(false), delayMillis);
            try {
                NotificationUtil.updateNextExecText(System.currentTimeMillis() + delayMillis);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void stopHandler() {
        if (context != null) {
            mainTask.stopTask();
        }
    }

    public static void updateDay() {
        Calendar nowCalendar = Calendar.getInstance();
        try {
            int nowYear = nowCalendar.get(Calendar.YEAR);
            int nowMonth = nowCalendar.get(Calendar.MONTH);
            int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
            if (dayCalendar.get(Calendar.YEAR) != nowYear || dayCalendar.get(Calendar.MONTH) != nowMonth || dayCalendar.get(Calendar.DAY_OF_MONTH) != nowDay) {
                dayCalendar = (Calendar) nowCalendar.clone();
                dayCalendar.set(Calendar.HOUR_OF_DAY, 0);
                dayCalendar.set(Calendar.MINUTE, 0);
                dayCalendar.set(Calendar.SECOND, 0);
                Log.record("日期更新为：" + nowYear + "-" + (nowMonth + 1) + "-" + nowDay);
                setWakenAtTimeAlarm();
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        try {
            Statistics.save(nowCalendar);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        try {
            Status.save(nowCalendar);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        try {
            FriendWatch.updateDay();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    private static Boolean setAlarmTask(long triggerAtMillis, PendingIntent operation) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            }
            Log.i("setAlarmTask triggerAtMillis:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerAtMillis) + " operation:" + (operation == null ? "" : operation.toString()));
            return true;
        } catch (Throwable th) {
            Log.i(TAG, "setAlarmTask err:");
            Log.printStackTrace(TAG, th);
        }
        return false;
    }

    private static Boolean unsetAlarmTask(PendingIntent operation) {
        try {
            if (operation != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(operation);
            }
            return true;
        } catch (Throwable th) {
            Log.i(TAG, "unsetAlarmTask err:");
            Log.printStackTrace(TAG, th);
        }
        return false;
    }

    public static String requestString(RpcEntity rpcEntity) {
        return rpcBridge.requestString(rpcEntity, 3, -1);
    }

    public static String requestString(RpcEntity rpcEntity, int tryCount, int retryInterval) {
        return rpcBridge.requestString(rpcEntity, tryCount, retryInterval);
    }

    public static String requestString(String method, String data) {
        return rpcBridge.requestString(method, data);
    }

    public static String requestString(String method, String data, String relation) {
        return rpcBridge.requestString(method, data, relation);
    }

    public static String requestString(String method, String data, int tryCount, int retryInterval) {
        return rpcBridge.requestString(method, data, tryCount, retryInterval);
    }

    public static String requestString(String method, String data, String relation, int tryCount, int retryInterval) {
        return rpcBridge.requestString(method, data, relation, tryCount, retryInterval);
    }

    public static RpcEntity requestObject(RpcEntity rpcEntity) {
        return rpcBridge.requestObject(rpcEntity, 3, -1);
    }

    public static RpcEntity requestObject(RpcEntity rpcEntity, int tryCount, int retryInterval) {
        return rpcBridge.requestObject(rpcEntity, tryCount, retryInterval);
    }

    public static RpcEntity requestObject(String method, String data) {
        return rpcBridge.requestObject(method, data);
    }

    public static RpcEntity requestObject(String method, String data, String relation) {
        return rpcBridge.requestObject(method, data, relation);
    }

    public static RpcEntity requestObject(String method, String data, int tryCount, int retryInterval) {
        return rpcBridge.requestObject(method, data, tryCount, retryInterval);
    }

    public static RpcEntity requestObject(String method, String data, String relation, int tryCount, int retryInterval) {
        return rpcBridge.requestObject(method, data, relation, tryCount, retryInterval);
    }

    public static void reLoginByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.reLogin"));
        } catch (Throwable th) {
            Log.i(TAG, "sesame sendBroadcast reLogin err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public static void restartByBroadcast() {
        try {
            context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.restart"));
        } catch (Throwable th) {
            Log.i(TAG, "sesame sendBroadcast restart err:");
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
            Object userObject = getUserObject();
            if (userObject != null) {
                return (String) XposedHelpers.getObjectField(userObject, "userId");
            }
        } catch (Throwable th) {
            Log.i(TAG, "getUserId err");
            Log.printStackTrace(TAG, th);
        }
        return null;
    }

    public static Object getUserObject() {
        try {
            return XposedHelpers.callMethod(XposedHelpers.callMethod(getMicroApplicationContext(), "findServiceByInterface", XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader).getName()), "getMyAccountInfoModelByLocal");
        } catch (Throwable th) {
            Log.i(TAG, "getUserObject err");
            Log.printStackTrace(TAG, th);
        }
        return null;
    }

    public static void reLogin() {
        if (context != null) {
            mainHandler.post(() -> {
                if (reLoginCount.get() < 5) {
                    execDelayedHandler(reLoginCount.getAndIncrement() * 5000L);
                } else {
                    execDelayedHandler(Math.max(BaseModel.getCheckInterval().getValue(), 180_000));
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                offline = true;
                context.startActivity(intent);
            });
        }
    }

    /*public static Boolean reLogin() {
        Object authService = getExtServiceByInterface("com.alipay.mobile.framework.service.ext.security.AuthService");
        if ((Boolean) XposedHelpers.callMethod(authService, "rpcAuth")) {
            return true;
        }
        Log.record("重新登录失败");
        return false;
    }*/

    private class AlipayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("sesame broadcast action:" + action + " intent:" + intent);
            if (action != null) {
                switch (action) {
                    case "com.eg.android.AlipayGphone.sesame.restart":
                        String userId = intent.getStringExtra("userId");
                        if (StringUtil.isEmpty(userId)) {
                            initHandler(true);
                        } else if (Objects.equals(UserIdMap.getCurrentUid(), userId)) {
                            initHandler(true);
                        }
                        break;
                    case "com.eg.android.AlipayGphone.sesame.execute":
                        initHandler(false);
                        break;
                    case "com.eg.android.AlipayGphone.sesame.reLogin":
                        reLogin();
                        break;
                    case "com.eg.android.AlipayGphone.sesame.status":
                        try {
                            context.sendBroadcast(new Intent("tkaxv7s.xposed.sesame.status"));
                        } catch (Throwable th) {
                            Log.i(TAG, "sesame sendBroadcast status err:");
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
            intentFilter.addAction("com.eg.android.AlipayGphone.sesame.restart");
            intentFilter.addAction("com.eg.android.AlipayGphone.sesame.execute");
            intentFilter.addAction("com.eg.android.AlipayGphone.sesame.reLogin");
            intentFilter.addAction("com.eg.android.AlipayGphone.sesame.status");
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
