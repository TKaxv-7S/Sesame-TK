package tkaxv7s.xposed.sesame.util;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import lombok.Getter;
import tkaxv7s.xposed.sesame.data.RuntimeInfo;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;

import java.text.DateFormat;

public class NotificationUtil {
    private static Context context;
    private static final int NOTIFICATION_ID = 99;
    private static final String CHANNEL_ID = "tkaxv7s.xposed.sesame.ANTFOREST_NOTIFY_CHANNEL";
    private static NotificationManager mNotifyManager;
    private static Notification.Builder builder;

    @Getter
    private static volatile long lastNoticeTime = 0;
    private static String titleText = "";
    private static String contentText = "";

    public static void start(Context context) {
        try {
            NotificationUtil.context = context;
            NotificationUtil.stop();
            titleText = "启动中";
            contentText = "暂无消息";
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setData(Uri.parse("alipays://platformapi/startapp?appId="));
            PendingIntent pi = PendingIntent.getActivity(context, 0, it,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "芝麻粒能量提醒",
                        NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setShowBadge(false);
                mNotifyManager.createNotificationChannel(notificationChannel);
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context).setPriority(Notification.PRIORITY_LOW);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setCategory(Notification.CATEGORY_NAVIGATION);
            }
            builder
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.sym_def_app_icon))
                    .setSubText("芝麻粒")
                    .setAutoCancel(false)
                    .setContentIntent(pi);
            if (BaseModel.getEnableOnGoing().getValue()) {
                builder.setOngoing(true);
            }
            Notification mNotification = builder.build();
            if (context instanceof Service) {
                ((Service) context).startForeground(NOTIFICATION_ID, mNotification);
            } else {
                mNotifyManager.notify(NOTIFICATION_ID, mNotification);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void stop() {
        try {
            if (context instanceof Service) {
                ((Service) context).stopForeground(true);
            } else {
                if (mNotifyManager != null) {
                    mNotifyManager.cancel(NOTIFICATION_ID);
                } else {
                    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
                }
            }
            mNotifyManager = null;
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateStatusText(String status) {
        try {
            long forestPauseTime = RuntimeInfo.getInstance().getLong(RuntimeInfo.RuntimeInfoKey.ForestPauseTime);
            if (forestPauseTime > System.currentTimeMillis()) {
                status = "触发异常，等待至" + TimeUtil.getCommonDate(forestPauseTime);
            }
            titleText = status;
            lastNoticeTime = System.currentTimeMillis();
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateNextExecText(long nextExecTime) {
        try {
            titleText = nextExecTime > 0 ? "下次执行 " + TimeUtil.getTimeStr(nextExecTime) : "";
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateLastExecText(String content) {
        try {
            contentText = "上次执行  " + TimeUtil.getTimeStr(System.currentTimeMillis()) + " " + content;
            lastNoticeTime = System.currentTimeMillis();
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void setStatusTextExec() {
        updateStatusText("执行中");
    }

    private static void sendText() {
        try {
            builder.setContentTitle(titleText);
            if (!StringUtil.isEmpty(contentText)) {
                builder.setContentText(contentText);
            }
            //Notification.BigTextStyle style = new Notification.BigTextStyle();
            //builder.setStyle(style);
            /*Notification.InboxStyle style = new Notification.InboxStyle();
            if (hasStatus) {
                if (hasNextExecText) {
                    style.addLine(statusText + "，" + nextExecText);
                } else {
                    style.addLine(statusText);
                }
            } else if (hasNextExecText) {
                style.addLine(nextExecText);
            }
            if (!StringUtil.isEmpty(lastExecText)) {
                style.addLine(lastExecText);
            }*/
            mNotifyManager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

}
