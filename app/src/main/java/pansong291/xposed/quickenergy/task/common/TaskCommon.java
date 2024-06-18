package pansong291.xposed.quickenergy.task.common;

import pansong291.xposed.quickenergy.util.TimeUtil;

public class TaskCommon {

    public static volatile Boolean IS_MORNING = false;

    public static volatile Boolean IS_AFTER_8AM = false;

    public static void update() {
        long currentTimeMillis = System.currentTimeMillis();
        IS_MORNING = TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0700") && TimeUtil.isBeforeOrCompareTimeStr(currentTimeMillis, "0730");
        IS_AFTER_8AM = TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0800");
    }

}
