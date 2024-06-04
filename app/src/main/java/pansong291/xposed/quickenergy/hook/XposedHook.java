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
import java.util.concurrent.FutureTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import pansong291.xposed.quickenergy.AncientTree;
import pansong291.xposed.quickenergy.AntBookRead;
import pansong291.xposed.quickenergy.AntCooperate;
import pansong291.xposed.quickenergy.AntFarm;
import pansong291.xposed.quickenergy.AntForest;
import pansong291.xposed.quickenergy.AntForestNotification;
import pansong291.xposed.quickenergy.AntForestToast;
import pansong291.xposed.quickenergy.AntMember;
import pansong291.xposed.quickenergy.AntOcean;
import pansong291.xposed.quickenergy.AntOrchard;
import pansong291.xposed.quickenergy.AntSports;
import pansong291.xposed.quickenergy.AntStall;
import pansong291.xposed.quickenergy.ConsumeGold;
import pansong291.xposed.quickenergy.GreenFinance;
import pansong291.xposed.quickenergy.OmegakoiTown;
import pansong291.xposed.quickenergy.Reserve;
import pansong291.xposed.quickenergy.data.RuntimeInfo;
import pansong291.xposed.quickenergy.entity.Task;
import pansong291.xposed.quickenergy.ui.MainActivity;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.FriendIdMap;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.PluginUtils;
import pansong291.xposed.quickenergy.util.Statistics;
import pansong291.xposed.quickenergy.util.TimeUtil;

public class XposedHook implements IXposedHookLoadPackage {

    private static final String TAG = XposedHook.class.getCanonicalName();

    private static volatile boolean isInit = false;

    private static volatile boolean isHooked = false;

    private static volatile boolean isOffline = true;

    @SuppressLint("StaticFieldLeak")
    private static volatile Context context;

    @SuppressLint("StaticFieldLeak")
    private static volatile Service service;

    private static volatile ClassLoader classLoader;

    private static PowerManager.WakeLock wakeLock;

    private static Handler mainHandler;

    private static Runnable mainRunner;

    private static Task antForestTask;

    private static Task antCooperateTask;

    private static Task antFarmTask;

    private static Task reserveTask;

    private static Task ancientTreeTask;

    private static Task antBookReadTask;

    private static Task antSportsTask;

    private static Task antMemberTask;

    private static Task antOceanTask;

    private static Task antOrchardTask;

    private static Task antStallTask;

    private static Task greenFinanceTask;

    private static Task omegakoiTownTask;

    private static Task consumeGoldTask;

    public static Boolean getIsOffline() {
        return isOffline;
    }

    public static void setIsOffline(boolean isOffline) {
        XposedHook.isOffline = isOffline;
    }

    public static Context getContext() {
        return context;
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public static Handler getMainHandler() {
        return mainHandler;
    }

    public static Task getAntForestTask() {
        return antForestTask;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("pansong291.xposed.quickenergy.repair".equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader, "setModuleActive",
                    boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.args[0] = true;
                        }
                    });
        }

        if (ClassMember.PACKAGE_NAME.equals(lpparam.processName) && ClassMember.PACKAGE_NAME.equals(lpparam.packageName)) {
            if (!isHooked) {
                isHooked = true;
                RuntimeInfo.process = lpparam.packageName;
                classLoader = lpparam.classLoader;
                Log.i(TAG, lpparam.packageName);
                hookRpcCall();
                hookStep();
                hookService();
                PluginUtils.invoke(XposedHook.class, PluginUtils.PluginAction.INIT);
            }
        }
    }

    private static void restartHandler() {
        mainHandler.removeCallbacks(mainRunner);
        AntForestNotification.stop(service, false);
        Task.stopAllTask();

        AntForestNotification.start(service);
        mainHandler.post(mainRunner);
    }

    private static void initHandler() {
        try {
            if (context != null) {
                if (!isInit) {
                    Log.recordLog("尝试初始化");
                    if (Config.startAt7()) {
                        Config.setAlarm7(context);
                    }
                    if (mainHandler != null && mainRunner != null) {
                        mainHandler.removeCallbacks(mainRunner);
                        AntForestNotification.stop(service, false);
                    }
                    Task.removeAllTask();
                    Task.putTask(antForestTask = AntForest.init());
                    Task.putTask(antCooperateTask = AntCooperate.init());
                    Task.putTask(antFarmTask = AntFarm.init());
                    Task.putTask(reserveTask = Reserve.init());
                    Task.putTask(ancientTreeTask = AncientTree.init());
                    Task.putTask(antBookReadTask = AntBookRead.init());
                    Task.putTask(antSportsTask = AntSports.init());
                    Task.putTask(antMemberTask = AntMember.init());
                    Task.putTask(antOceanTask = AntOcean.init());
                    Task.putTask(antOrchardTask = AntOrchard.init());
                    Task.putTask(antStallTask = AntStall.init());
                    Task.putTask(greenFinanceTask = GreenFinance.init());
                    Task.putTask(omegakoiTownTask = OmegakoiTown.init());
                    Task.putTask(consumeGoldTask = ConsumeGold.init());
                    mainHandler = new Handler();
                    mainRunner = new Runnable() {
                        @Override
                        public void run() {
                            if (!isInit) {
                                return;
                            }
                            PluginUtils.invoke(XposedHook.class, PluginUtils.PluginAction.START);
                            String targetUid = RpcUtil.getUserId();
                            if (targetUid != null) {
                                FriendIdMap.setCurrentUid(targetUid);
                                Config.shouldReload = true;
                                Statistics.resetToday();

                                try {
                                    FutureTask<Boolean> checkTask = new FutureTask<>(AntMemberRpcCall::check);
                                    Thread checkThread = new Thread(checkTask);
                                    checkThread.start();
                                    if (!checkTask.get()) {
                                        mainHandler.postDelayed(this, Config.checkInterval());
                                        return;
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "check err:");
                                    Log.printStackTrace(TAG, e);
                                    mainHandler.postDelayed(this, Config.checkInterval());
                                    return;
                                }
                                antForestTask.startTask();
                                if (TimeUtil.getTimeStr().compareTo("0700") < 0
                                        || TimeUtil.getTimeStr().compareTo("0730") > 0) {
                                    antCooperateTask.startTask();
                                    antFarmTask.startTask();
                                    reserveTask.startTask();
                                    if (TimeUtil.getTimeStr().compareTo("0800") >= 0) {
                                        ancientTreeTask.startTask();
                                        antBookReadTask.startTask();
                                    }
                                    antSportsTask.startTask();
                                    antMemberTask.startTask();
                                    antOceanTask.startTask();
                                    antOrchardTask.startTask();
                                    antStallTask.startTask();
                                    greenFinanceTask.startTask();
                                    omegakoiTownTask.startTask();
                                    consumeGoldTask.startTask();
                                }
                            }
                            if (Config.collectEnergy() || Config.enableFarm()) {
                                AntForestNotification.setNextScanTime(System.currentTimeMillis() + Config.checkInterval());
                                mainHandler.postDelayed(this, Config.checkInterval());
                            } else {
                                AntForestNotification.stop(service, false);
                            }

                            PluginUtils.invoke(XposedHook.class, PluginUtils.PluginAction.STOP);
                        }
                    };
                    isInit = true;
                }
                isOffline = false;
                restartHandler();
                AntForestToast.show("芝麻粒加载成功");
            }
        } catch (Throwable th) {
            Log.i(TAG, "initHandler err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void hookService() {
        try {
            XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", classLoader,
                    "onResume", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Log.i(TAG, "Activity onResume");
                            //PermissionUtil.requestPermissions((Activity) param.thisObject);
                            AntForestNotification.setContentText("运行中...");
                            String targetUid = RpcUtil.getUserId();
                            if (targetUid == null) {
                                return;
                            }
                            String currentUid = FriendIdMap.getCurrentUid();
                            if (!targetUid.equals(currentUid)) {
                                FriendIdMap.setCurrentUid(targetUid);
                                if (currentUid != null) {
                                    initHandler();
                                    Log.i(TAG, "Activity changeUser");
                                    return;
                                }
                                isOffline = true;
                            }
                            if (isOffline) {
                                isOffline = false;
                                restartHandler();
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
                            if (!ClassMember.CURRENT_USING_SERVICE.equals(appService.getClass().getCanonicalName())) {
                                return;
                            }
                            Log.i(TAG, "Service onCreate");
                            AntForestNotification.setContentText("运行中...");
                            registerBroadcastReceiver(appService);
                            service = appService;
                            context = appService.getApplicationContext();
                            RpcUtil.init(classLoader);
                            if (Config.stayAwake()) {
                                PowerManager pm = (PowerManager) appService.getSystemService(Context.POWER_SERVICE);
                                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, appService.getClass().getName());
                                wakeLock.acquire();
                                restartHook(Config.stayAwakeType(), 30 * 60 * 1000, false);
                            }
                            initHandler();
                        }
                    });
            Log.i(TAG, "hook onCreate successfully");
        } catch (Throwable t) {
            Log.i(TAG, "hook onCreate err:");
            Log.printStackTrace(TAG, t);
        }
        try {
            XposedHelpers.findAndHookMethod("android.app.Service", classLoader, "onDestroy", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Service service = (Service) param.thisObject;
                    if (!ClassMember.CURRENT_USING_SERVICE.equals(service.getClass().getCanonicalName())) {
                        return;
                    }
                    if (wakeLock != null) {
                        wakeLock.release();
                        wakeLock = null;
                    }
                    Task.stopAllTask();
                    AntForestNotification.stop(service, false);
                    AntForestNotification.setContentText("支付宝前台服务被销毁");
                    Log.recordLog("支付宝前台服务被销毁", "");
                    alarmHook(3000, false);
                }
            });
            Log.i(TAG, "hook onDestroy successfully");
        } catch (Throwable t) {
            Log.i(TAG, "hook onDestroy err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void hookRpcCall() {
        try {
            Class<?> clazz = classLoader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5AppRpcUpdate);
            Class<?> H5PageClazz = classLoader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
            XposedHelpers.findAndHookMethod(
                    clazz, ClassMember.matchVersion, H5PageClazz, Map.class, String.class,
                    XC_MethodReplacement.returnConstant(false));
            Log.i(TAG, "hook " + ClassMember.matchVersion + " successfully");
        } catch (Throwable t) {
            Log.i(TAG, "hook " + ClassMember.matchVersion + " err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void hookStep() {
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
        } catch (Throwable t) {
            Log.i(TAG, "hookStep err:");
            Log.printStackTrace(TAG, t);
        }

    }

    public static void restartHook(StayAwakeType stayAwakeType, long delayTime, boolean force) {
        if (stayAwakeType == StayAwakeType.ALARM) {
            alarmHook(delayTime, force);
        } else {
            alarmBroadcast(delayTime, force);
        }
    }

    public static void restartHook(Context context, boolean force) {
        try {
            Intent intent;
            if (force || Config.stayAwakeTarget() == StayAwakeTarget.ACTIVITY) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(ClassMember.PACKAGE_NAME, ClassMember.CURRENT_USING_ACTIVITY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (force) {
                    isOffline = true;
                }
                context.startActivity(intent);
            } else {
                intent = new Intent();
                intent.setClassName(ClassMember.PACKAGE_NAME, ClassMember.CURRENT_USING_SERVICE);
                context.startService(intent);
            }
        } catch (Throwable t) {
            Log.i(TAG, "restartHook err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int getPendingIntentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
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
            if (force || Config.stayAwakeTarget() == StayAwakeTarget.ACTIVITY) {
                Intent it = new Intent();
                it.setClassName(ClassMember.PACKAGE_NAME, ClassMember.CURRENT_USING_ACTIVITY);
                pi = PendingIntent.getActivity(context, 1, it, getPendingIntentFlag());
                if (force) {
                    isOffline = true;
                }
            } else {
                Intent it = new Intent();
                it.setClassName(ClassMember.PACKAGE_NAME, ClassMember.CURRENT_USING_SERVICE);
                pi = PendingIntent.getService(context, 2, it, getPendingIntentFlag());
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, pi);
        } catch (Throwable th) {
            Log.printStackTrace(TAG, th);
        }
    }

    private static class AlipayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.eg.android.AlipayGphone.xqe.broadcast".equals(action)) {
                boolean force = intent.getBooleanExtra("force", false);
                restartHook(context, force);
            } else if ("com.eg.android.AlipayGphone.xqe.test".equals(action)) {
                Log.recordLog("收到测试消息");
//                alarmHook(context, 3000, true);
            } else if ("com.eg.android.AlipayGphone.xqe.cancelAlarm7".equals(action)) {
                Config.cancelAlarm7(context, false);
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadcastReceiver(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.broadcast");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.test");
            intentFilter.addAction("com.eg.android.AlipayGphone.xqe.cancelAlarm7");
            context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
            Log.recordLog("注册广播接收器成功", context.toString());
        } catch (Throwable th) {
            Log.i(TAG, "hook registerBroadcastReceiver err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public enum StayAwakeType {
        BROADCAST, ALARM, NONE;

        public static final CharSequence[] nickNames = {"广播", "闹钟", "不重启"};
    }

    public enum StayAwakeTarget {
        SERVICE, ACTIVITY;

        public static final CharSequence[] nickNames = {"Service", "Activity"};
    }

}
