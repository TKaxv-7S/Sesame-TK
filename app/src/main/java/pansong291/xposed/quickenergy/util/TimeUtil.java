package pansong291.xposed.quickenergy.util;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * @author Constanline
 * @since 2023/07/17
 */
public class TimeUtil {

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();
    public static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance();

    public static Boolean checkNowInTimeRange(String timeRange) {
        return checkInTimeRange(System.currentTimeMillis(), timeRange);
    }

    public static Boolean checkInTimeRange(Long timeMillis, String timeRange) {
        try {
            String[] timeRangeArray = timeRange.split("-");
            if (timeRangeArray.length == 2) {
                String min = timeRangeArray[0];
                String max = timeRangeArray[1];
                return isAfterOrCompareTimeStr(timeMillis, min) && isBeforeOrCompareTimeStr(timeMillis, max);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return false;
    }

    public static Boolean isNowBeforeTimeStr(String beforeTimeStr) {
        return isBeforeTimeStr(System.currentTimeMillis(), beforeTimeStr);
    }

    public static Boolean isNowAfterTimeStr(String afterTimeStr) {
        return isAfterTimeStr(System.currentTimeMillis(), afterTimeStr);
    }

    public static Boolean isNowBeforeOrCompareTimeStr(String beforeTimeStr) {
        return isBeforeOrCompareTimeStr(System.currentTimeMillis(), beforeTimeStr);
    }

    public static Boolean isNowAfterOrCompareTimeStr(String afterTimeStr) {
        return isAfterOrCompareTimeStr(System.currentTimeMillis(), afterTimeStr);
    }

    public static Boolean isBeforeTimeStr(Long timeMillis, String beforeTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, beforeTimeStr);
        if (compared != null) {
            return compared < 0;
        }
        return false;
    }

    public static Boolean isAfterTimeStr(Long timeMillis, String afterTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, afterTimeStr);
        if (compared != null) {
            return compared > 0;
        }
        return false;
    }

    public static Boolean isBeforeOrCompareTimeStr(Long timeMillis, String beforeTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, beforeTimeStr);
        if (compared != null) {
            return compared <= 0;
        }
        return false;
    }

    public static Boolean isAfterOrCompareTimeStr(Long timeMillis, String afterTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, afterTimeStr);
        if (compared != null) {
            return compared >= 0;
        }
        return false;
    }

    public static Integer isCompareTimeStr(Long timeMillis, String compareTimeStr) {
        try {
            if (compareTimeStr.length() == 4) {
                int compareHour = Integer.parseInt(compareTimeStr.substring(0, 2));
                int compareMinute = Integer.parseInt(compareTimeStr.substring(2));
                Calendar timeCalendar = Calendar.getInstance();
                Calendar compareCalendar = (Calendar) timeCalendar.clone();
                timeCalendar.setTimeInMillis(timeMillis);
                compareCalendar.set(Calendar.HOUR_OF_DAY, compareHour);
                compareCalendar.set(Calendar.MINUTE, compareMinute);
                compareCalendar.set(Calendar.SECOND, 0);
                return timeCalendar.compareTo(compareCalendar);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static String getTimeStr(long ts) {
        return TIME_FORMAT.format(new java.util.Date(ts));
    }

    public static String getDateStr() {
        return getDateStr(0);
    }

    public static String getDateStr(int plusDay) {
        Calendar c = Calendar.getInstance();
        if (plusDay != 0) {
            c.add(Calendar.DATE, plusDay);
        }
        return DATE_FORMAT.format(c.getTime());
    }

    public static Calendar getToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Log.i("sleep error:");
            Log.printStackTrace(e);
        }
    }
}
