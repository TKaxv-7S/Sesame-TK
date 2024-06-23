package tkaxv7s.xposed.sesame.task.base;

import tkaxv7s.xposed.sesame.util.TimeUtil;

public class TaskCommon {

    public static volatile Boolean IS_ENERGY_TIME = false;

    public static volatile Boolean IS_AFTER_8AM = false;

    public static void update() {
        long currentTimeMillis = System.currentTimeMillis();
        IS_ENERGY_TIME = (TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "00") && TimeUtil.isBeforeOrCompareTimeStr(currentTimeMillis, "0005"))
                || (TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0700") && TimeUtil.isBeforeOrCompareTimeStr(currentTimeMillis, "0730"));
        IS_AFTER_8AM = TimeUtil.isAfterOrCompareTimeStr(currentTimeMillis, "0800");
    }

}
