package tkaxv7s.xposed.sesame.util;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import lombok.Getter;
import tkaxv7s.xposed.sesame.data.RuntimeInfo;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;

import java.text.DateFormat;

public class NotificationUtil {
    private static final int NOTIFICATION_ID = 99;
    private static final String CHANNEL_ID = "tkaxv7s.xposed.sesame.ANTFOREST_NOTIFY_CHANNEL";
    private static NotificationManager mNotifyManager;
    private static Notification mNotification;
    private static Notification.Builder builder;

    @Getter
    private static volatile long lastNoticeTime = 0;
    private static String statusText = "";
    private static String nextExecText = "";
    private static String lastExecText = "";

    public static void start(Context context) {
        try {
            statusText = "加载";
            nextExecText = "待执行";
            lastExecText = "";
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
                builder = new android.app.Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new android.app.Notification.Builder(context).setPriority(android.app.Notification.PRIORITY_LOW);
            }
            builder.setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle("芝麻粒")
                    .setAutoCancel(false)
                    .setContentIntent(pi);
            if (BaseModel.getEnableOnGoing().getValue()) {
                builder.setOngoing(true);
            }
            mNotification = builder.build();
            if (context instanceof Service) {
                ((Service) context).startForeground(NOTIFICATION_ID, mNotification);
            } else {
                mNotifyManager.notify(NOTIFICATION_ID, mNotification);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void stop(Context context, boolean remove) {
        try {
            if (context instanceof Service) {
                ((Service) context).stopForeground(remove);
            } else {
                mNotifyManager.cancel(NOTIFICATION_ID);
            }
            mNotification = null;
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateStatusText(String status) {
        try {
            long forestPauseTime = RuntimeInfo.getInstance().getLong(RuntimeInfo.RuntimeInfoKey.ForestPauseTime);
            if (forestPauseTime > System.currentTimeMillis()) {
                status = "触发异常，等待至" + DateFormat.getDateTimeInstance().format(forestPauseTime);
            }
            statusText = status;
            lastNoticeTime = System.currentTimeMillis();
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateNextExecText(long nextExecTime) {
        try {
            nextExecText = nextExecTime > 0 ? "下次执行 " + TimeUtil.getTimeStr(nextExecTime) : "";
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void updateLastExecText(String content) {
        try {
            lastExecText = "上次执行 " + TimeUtil.getTimeStr(System.currentTimeMillis()) + " " + content;
            lastNoticeTime = System.currentTimeMillis();
            sendText();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public static void setStatusTextIdle() {
        updateStatusText("");
    }

    public static void setStatusTextExec() {
        updateStatusText("执行中");
    }

    private static void sendText() {
        try {
            Notification.BigTextStyle style = new Notification.BigTextStyle();
            style.bigText((StringUtil.isEmpty(statusText) ? "" : statusText + "，") + nextExecText + (StringUtil.isEmpty(lastExecText) ? "" : "\n" + lastExecText));
//          Notification.InboxStyle style = new Notification.InboxStyle();
//          style.addLine(preContent);
//          style.addLine(contentText);
            builder.setStyle(style);
            mNotification = builder.build();
            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

}
