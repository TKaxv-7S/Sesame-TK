package tkaxv7s.xposed.sesame.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Constanline
 * @since 2023/07/17
 */
public class TimeUtil {

    public static Boolean checkNowInTimeRange(String timeRange) {
        return checkInTimeRange(System.currentTimeMillis(), timeRange);
    }

    public static Boolean checkInTimeRange(Long timeMillis, List<String> timeRangeList) {
        for (String timeRange : timeRangeList) {
            if (checkInTimeRange(timeMillis, timeRange)) {
                return true;
            }
        }
        return false;
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
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTimeInMillis(timeMillis);
            Calendar compareCalendar = getTodayCalendarByTimeStr(compareTimeStr);
            if (compareCalendar != null) {
                return timeCalendar.compareTo(compareCalendar);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static Calendar getTodayCalendarByTimeStr(String timeStr) {
        return getCalendarByTimeStr((Long) null, timeStr);
    }

    public static Calendar getCalendarByTimeStr(Long timeMillis, String timeStr) {
        try {
            Calendar timeCalendar = getCalendarByTimeMillis(timeMillis);
            return getCalendarByTimeStr(timeCalendar, timeStr);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static Calendar getCalendarByTimeStr(Calendar timeCalendar, String timeStr) {
        try {
            int length = timeStr.length();
            switch (length) {
                case 6:
                    timeCalendar.set(Calendar.SECOND, Integer.parseInt(timeStr.substring(4)));
                    timeCalendar.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    break;
                case 4:
                    timeCalendar.set(Calendar.SECOND, 0);
                    timeCalendar.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    break;
                case 2:
                    timeCalendar.set(Calendar.SECOND, 0);
                    timeCalendar.set(Calendar.MINUTE, 0);
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    break;
                default:
                    return null;
            }
            timeCalendar.set(Calendar.MILLISECOND, 0);
            return timeCalendar;
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static Calendar getCalendarByTimeMillis(Long timeMillis) {
        Calendar timeCalendar = Calendar.getInstance();
        if (timeMillis != null) {
            timeCalendar.setTimeInMillis(timeMillis);
        }
        return timeCalendar;
    }

    public static String getTimeStr(long ts) {
        return DateFormat.getTimeInstance().format(new java.util.Date(ts));
    }

    public static String getDateStr() {
        return getDateStr(0);
    }

    public static String getDateStr(int plusDay) {
        Calendar c = Calendar.getInstance();
        if (plusDay != 0) {
            c.add(Calendar.DATE, plusDay);
        }
        return DateFormat.getDateInstance().format(c.getTime());
    }

    public static Calendar getToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static Calendar getNow() {
        return Calendar.getInstance();
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Log.i("sleep error:");
            Log.printStackTrace(e);
        }
    }

    /**
     * 获取指定时间的周数
     * @param dateTime 时间
     * @return 当前年的第几周
     */
    public static int getWeekNumber(Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        // 设置周的第一天为周一
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 比较第一个时间戳的天数是否小于第二个时间戳的天数
     * @param firstdTimestamp 第一个时间戳
     * @param secondTimestamp 第二个时间戳
     * @return boolean 如果小于，则为true，否则为false
     */
    public static boolean isLessThanSecondOfDays(Long firstdTimestamp, Long secondTimestamp) {
        final long gmt8 = 8 * 60 * 60 * 1000;
        final long day = 24 * 60 * 60 * 1000;
        firstdTimestamp = firstdTimestamp + gmt8;
        secondTimestamp = secondTimestamp + gmt8;
        return firstdTimestamp / day < secondTimestamp / day;
    }

    /**
     * 通过时间戳比较传入的时间戳的天数是否小于当前时间戳的天数
     * @param timestamp 时间戳
     * @return boolean 如果小于当前时间戳所计算的天数，则为true，否则为false
     */
    public static boolean isLessThanNowOfDays(Long timestamp) {
        return isLessThanSecondOfDays(timestamp, System.currentTimeMillis());
    }

    @SuppressLint("SimpleDateFormat")
    public static DateFormat getCommonDateFormat() {
        return new SimpleDateFormat("dd日HH:mm:ss");
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCommonDate(Long timestamp) {
        return getCommonDateFormat().format(timestamp);
    }

}
