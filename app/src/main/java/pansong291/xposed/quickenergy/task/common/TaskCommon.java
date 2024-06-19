package pansong291.xposed.quickenergy.task.common;

import pansong291.xposed.quickenergy.util.TimeUtil;

public class TaskCommon {

    public static volatile Boolean IS_ENERGY_TIME = false;

    public static volatile Boolean IS_AFTER_8AM = false;

    public static void update() {
        long currentTimeMillis = System.currentTimeMillis();
        IS_ENERGY_TIME = (TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "00") && TimeUtil.isBeforeOrCompareTimeStr(currentTimeMillis, "0030")) || (TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0700") && TimeUtil.isBeforeOrCompareTimeStr(currentTimeMillis, "0730"));
        IS_AFTER_8AM = TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0800");
    }

}
