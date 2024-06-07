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
import android.widget.Toast;

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
import pansong291.xposed.quickenergy.entity.Task;
import pansong291.xposed.quickenergy.model.AncientTree;
import pansong291.xposed.quickenergy.model.AntCooperate;
import pansong291.xposed.quickenergy.model.AntFarm;
import pansong291.xposed.quickenergy.model.AntForest;
import pansong291.xposed.quickenergy.model.AntForestNotification;
import pansong291.xposed.quickenergy.model.AntForestToast;
import pansong291.xposed.quickenergy.model.AntMember;
import pansong291.xposed.quickenergy.model.AntOcean;
import pansong291.xposed.quickenergy.model.AntOrchard;
import pansong291.xposed.quickenergy.model.AntSports;
import pansong291.xposed.quickenergy.model.AntStall;
import pansong291.xposed.quickenergy.model.GreenFinance;
import pansong291.xposed.quickenergy.model.Reserve;
import pansong291.xposed.quickenergy.rpc.NewRpcBridge;
import pansong291.xposed.quickenergy.rpc.OldRpcBridge;
import pansong291.xposed.quickenergy.rpc.RpcBridge;
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

    @Getter
    private static Task antForestTask;

    private static Task antCooperateTask;

    private static Task antFarmTask;

    private static Task reserveTask;

    private static Task ancientTreeTask;

    //private static Task antBookReadTask;

    private static Task antSportsTask;

    private static Task antMemberTask;

    private static Task antOceanTask;

    private static Task antOrchardTask;

    private static Task antStallTask;

    private static Task greenFinanceTask;

    //private static Task omegakoiTownTask;

    //private static Task consumeGoldTask;

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
                        AntForestNotification.setContentText("支付宝前台服务被销毁");
                        stopHandler(true);
                        Log.record("支付宝前台服务被销毁");
                        alarmHook(3000, false);
                    }
                });
                Log.i(TAG, "hook service onDestroy successfully");
            } catch (Throwable t) {
                Log.i(TAG, "hook service onDestroy err:");
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
                            Toast.makeText(context, "请授予支付宝使用闹钟权限", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                    return;
                }
                /*if (!init && !PermissionUtil.checkBatteryPermissions()) {
                    Log.record("支付宝无始终在后台运行权限");
                    mainHandler.postDelayed(() -> {
                        if (!PermissionUtil.checkOrRequestBatteryPermissions(context)) {
                            Toast.makeText(context, "请授予支付宝终在后台运行权限", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }*/
                Log.record("开始加载");
                Config config = Config.load();
                if (config.isNewRpc()) {
                    rpcBridge = new NewRpcBridge();
                } else {
                    rpcBridge = new OldRpcBridge();
                }
                rpcBridge.load();
                if (config.isStartAt7()) {
                    try {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent it = new Intent();
                        it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
                        int piFlag;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            piFlag = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
                        } else {
                            piFlag = PendingIntent.FLAG_UPDATE_CURRENT;
                        }
                        alarm7Pi = PendingIntent.getActivity(context, 999, it, piFlag);
                        Calendar calendar = Calendar.getInstance();
                        //calendar.add(Calendar.SECOND, 10);
                        if (calendar.get(Calendar.HOUR_OF_DAY) >= 7) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 7);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                        }*/
                        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), alarm7Pi);
                    } catch (Throwable th) {
                        Log.printStackTrace("alarm7", th);
                    }
                }
                if (config.isStayAwake()) {
                    PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
                    wakeLock.acquire();
                    restartHook(Config.INSTANCE.getStayAwakeType(), 30 * 60 * 1000, false);
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
                    /*Map<Long, Boolean> callMap = new ConcurrentHashMap<>();
                    try {
                        XposedHelpers.findAndHookMethod(
                                "com.alibaba.xriver.android.bridge.CRVNativeBridge", classLoader
                                , "callJavaBridgeExtensionWithJson"
                                , classLoader.loadClass("com.alibaba.ariver.kernel.api.node.Node"), String.class, classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME), long.class, String.class
                                , new XC_MethodHook() {
        
                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        if ("rpc".equals(param.args[1])) {
                                            Object[] args = param.args;
                                            Long id = (Long) args[3];
                                            Log.debug("CallBridge id[" + id + "]: " + Arrays.toString(args));
                                            callMap.put(id, true);
                                        }
                                    }
        
                                });
                        Log.i(TAG, "hook callJavaBridgeExtensionWithJson successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook callJavaBridgeExtensionWithJson err:");
                        Log.printStackTrace(TAG, t);
                    }*/
                    /*try {
                        XposedHelpers.findAndHookMethod(
                                "com.alibaba.xriver.android.bridge.CRVNativeBridge", loader
                                , "nativeCallBridge"
                                , long.class, int.class, String.class, String.class, String.class, String.class
                                , new XC_MethodHook() {
        
                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        Object[] args = param.args;
                                        Log.debug("NativeCallBridge: " + Arrays.toString(args));
                                    }
        
                                });
                        Log.i(TAG, "hook nativeCallBridge successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook nativeCallBridge err:");
                        Log.printStackTrace(TAG, t);
                    }*/
                    /*try {
                        XposedHelpers.findAndHookMethod(
                                "com.alibaba.xriver.android.bridge.CRVNativeBridge", classLoader
                                , "nativeInvokeCallback"
                                , long.class, Object.class, boolean.class
                                , new XC_MethodHook() {
        
                                    @SuppressLint("WakelockTimeout")
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        Object[] args = param.args;
                                        Long id = (Long) args[0];
                                        if (Boolean.TRUE.equals(callMap.remove(id))) {
                                            Log.debug("Callback id[" + id + "]: " + Arrays.toString(args));
                                        }
                                    }
        
                                });
                        Log.i(TAG, "hook nativeInvokeCallback successfully");
                    } catch (Throwable t) {
                        Log.i(TAG, "hook nativeInvokeCallback err:");
                        Log.printStackTrace(TAG, t);
                    }*/
                }
                Statistics.load();
                Task.removeAllTask();
                Task.putTask(antForestTask = AntForest.init());
                Task.putTask(antCooperateTask = AntCooperate.init());
                Task.putTask(antFarmTask = AntFarm.init());
                Task.putTask(reserveTask = Reserve.init());
                Task.putTask(ancientTreeTask = AncientTree.init());
                //Task.putTask(antBookReadTask = AntBookRead.init());
                Task.putTask(antSportsTask = AntSports.init());
                Task.putTask(antMemberTask = AntMember.init());
                Task.putTask(antOceanTask = AntOcean.init());
                Task.putTask(antOrchardTask = AntOrchard.init());
                Task.putTask(antStallTask = AntStall.init());
                Task.putTask(greenFinanceTask = GreenFinance.init());
                //Task.putTask(omegakoiTownTask = OmegakoiTown.init());
                //Task.putTask(consumeGoldTask = ConsumeGold.init());
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
                                AntForestNotification.setContentTextExec();
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
                                        mainHandler.postDelayed(this, config.getCheckInterval());
                                        return;
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "check err:");
                                    Log.printStackTrace(TAG, e);
                                    mainHandler.postDelayed(this, config.getCheckInterval());
                                    return;
                                }
                                antForestTask.startTask();
                                if (TimeUtil.getTimeStr().compareTo("0700") < 0
                                        || TimeUtil.getTimeStr().compareTo("0730") > 0) {
                                    TimeUtil.sleep(50);
                                    antCooperateTask.startTask();
                                    TimeUtil.sleep(50);
                                    antFarmTask.startTask();
                                    TimeUtil.sleep(50);
                                    reserveTask.startTask();
                                    if (TimeUtil.getTimeStr().compareTo("0800") >= 0) {
                                        TimeUtil.sleep(60);
                                        ancientTreeTask.startTask();
                                        //TimeUtil.sleep(60);
                                        //antBookReadTask.startTask();
                                    }
                                    TimeUtil.sleep(60);
                                    antSportsTask.startTask();
                                    TimeUtil.sleep(60);
                                    antMemberTask.startTask();
                                    TimeUtil.sleep(60);
                                    antOceanTask.startTask();
                                    TimeUtil.sleep(60);
                                    antOrchardTask.startTask();
                                    TimeUtil.sleep(60);
                                    antStallTask.startTask();
                                    TimeUtil.sleep(60);
                                    greenFinanceTask.startTask();
                                    //TimeUtil.sleep(60);
                                    //omegakoiTownTask.startTask();
                                    //TimeUtil.sleep(60);
                                    //consumeGoldTask.startTask();
                                }
                            }
                            int checkInterval = config.getCheckInterval();
                            AntForestNotification.setNextScanTime(System.currentTimeMillis() + checkInterval);
                            AntForestNotification.setContentTextIdle();
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
                AntForestToast.show("芝麻粒加载成功");
                init = true;
            }
            offline = false;
            AntForestNotification.start(service);
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
                AntForestNotification.stop(service, false);
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

    public static String request(String method, String data) {
        return rpcBridge.requestJson(method, data);
    }

    public static void restartHook(int stayAwakeType, long delayTime, boolean force) {
        if (stayAwakeType == StayAwakeType.ALARM) {
            alarmHook(delayTime, force);
        } else {
            alarmBroadcast(delayTime, force);
        }
    }

    public static void restartHook(Context context, boolean force) {
        try {
            Intent intent;
            if (force || Config.INSTANCE.getStayAwakeTarget() == StayAwakeTarget.ACTIVITY) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (force) {
                    offline = true;
                }
                context.startActivity(intent);
            } else {
                intent = new Intent();
                intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_SERVICE);
                context.startService(intent);
            }
        } catch (Throwable t) {
            Log.i(TAG, "restartHook err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public static void alarmBroadcast(long delayTime, boolean force) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("com.eg.android.AlipayGphone.xqe.broadcast");
            intent.putExtra("force", force);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, getPendingIntentFlag());
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, pi);
        } catch (Throwable th) {
            Log.printStackTrace(TAG, th);
        }
    }

    public static void alarmHook(long delayTime, boolean force) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi;
            if (force || Config.INSTANCE.getStayAwakeTarget() == StayAwakeTarget.ACTIVITY) {
                Intent it = new Intent();
                it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
                pi = PendingIntent.getActivity(context, 1, it, getPendingIntentFlag());
                if (force) {
                    offline = true;
                }
            } else {
                Intent it = new Intent();
                it.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_SERVICE);
                pi = PendingIntent.getService(context, 2, it, getPendingIntentFlag());
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, pi);
        } catch (Throwable th) {
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

    private static PendingIntent getAlarm7Pi() {
        if (alarm7Pi == null) {
        }
        return alarm7Pi;
    }

    public static Object getMicroApplicationContext() {
        return XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication", classLoader), "getInstance"), "getMicroApplicationContext");
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

    private static class AlipayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.eg.android.AlipayGphone.xqe.broadcast".equals(action)) {
                boolean force = intent.getBooleanExtra("force", false);
                restartHook(context, force);
            } else if ("com.eg.android.AlipayGphone.xqe.test".equals(action)) {
                Log.record("收到测试消息");
//                alarmHook(context, 3000, true);
            } else if ("com.eg.android.AlipayGphone.xqe.reload".equals(action)) {
                startHandler(true);
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadcastReceiver(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.broadcast");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.test");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.reload");
            context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
            Log.record("注册广播接收器成功 " + context);
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
