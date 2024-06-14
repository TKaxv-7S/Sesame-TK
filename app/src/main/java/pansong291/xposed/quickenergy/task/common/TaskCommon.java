package pansong291.xposed.quickenergy.task.common;

import pansong291.xposed.quickenergy.util.TimeUtil;

public class TaskCommon {

    public static volatile Boolean IS_MORNING = false;

    public static volatile Boolean IS_AFTER_8AM = false;

    public static void update() {
        String timeStr = TimeUtil.getTimeStr();
        IS_MORNING = timeStr.compareTo("0700") >= 0 && timeStr.compareTo("0730") <= 0;
        IS_AFTER_8AM = timeStr.compareTo("0800") >= 0;
    }

}
