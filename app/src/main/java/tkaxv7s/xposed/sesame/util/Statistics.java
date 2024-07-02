package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.Data;

import java.io.File;
import java.util.Calendar;

@Data
public class Statistics {

    private static final String TAG = Statistics.class.getSimpleName();

    public static final Statistics INSTANCE = new Statistics();

    private TimeStatistics year = new TimeStatistics();
    private TimeStatistics month = new TimeStatistics();
    private TimeStatistics day = new TimeStatistics();

    public static void addData(DataType dt, int i) {
        Statistics stat = INSTANCE;
        //resetToday();
        switch (dt) {
            case COLLECTED:
                stat.day.collected += i;
                stat.month.collected += i;
                stat.year.collected += i;
                break;
            case HELPED:
                stat.day.helped += i;
                stat.month.helped += i;
                stat.year.helped += i;
                break;
            case WATERED:
                stat.day.watered += i;
                stat.month.watered += i;
                stat.year.watered += i;
                break;
        }
        save();
    }

    public static int getData(TimeType tt, DataType dt) {
        Statistics stat = INSTANCE;
        int data = 0;
        TimeStatistics ts = null;
        switch (tt) {
            case YEAR:
                ts = stat.year;
                break;
            case MONTH:
                ts = stat.month;
                break;
            case DAY:
                ts = stat.day;
                break;
        }
        if (ts != null)
            switch (dt) {
                case TIME:
                    data = ts.time;
                    break;
                case COLLECTED:
                    data = ts.collected;
                    break;
                case HELPED:
                    data = ts.helped;
                    break;
                case WATERED:
                    data = ts.watered;
                    break;
            }
        return data;
    }

    public static String getText() {
        StringBuilder sb = new StringBuilder(getData(TimeType.YEAR, DataType.TIME) + "年 : 收 ");
        sb.append(getData(TimeType.YEAR, DataType.COLLECTED));
        sb.append(",   帮 ").append(getData(TimeType.YEAR, DataType.HELPED));
        sb.append(",   浇 ").append(getData(TimeType.YEAR, DataType.WATERED));
        sb.append("\n").append(getData(TimeType.MONTH, DataType.TIME)).append("月 : 收 ");
        sb.append(getData(TimeType.MONTH, DataType.COLLECTED));
        sb.append(",   帮 ").append(getData(TimeType.MONTH, DataType.HELPED));
        sb.append(",   浇 ").append(getData(TimeType.MONTH, DataType.WATERED));
        sb.append("\n").append(getData(TimeType.DAY, DataType.TIME)).append("日 : 收 ");
        sb.append(getData(TimeType.DAY, DataType.COLLECTED));
        sb.append(",   帮 ").append(getData(TimeType.DAY, DataType.HELPED));
        sb.append(",   浇 ").append(getData(TimeType.DAY, DataType.WATERED));
        return sb.toString();
    }

    public Boolean resetByCalendar(Calendar calendar) {
        int ye = calendar.get(Calendar.YEAR);
        int mo = calendar.get(Calendar.MONTH) + 1;
        int da = calendar.get(Calendar.DAY_OF_MONTH);
        if (ye != year.time) {
            year.reset(ye);
            month.reset(mo);
            day.reset(da);
        } else if (mo != month.time) {
            month.reset(mo);
            day.reset(da);
        } else if (da != day.time) {
            day.reset(da);
        } else {
            return false;
        }
        Log.system(TAG, "重置 statistics.json");
        save();
        Status.dayClear();
        return true;
    }

    public static synchronized Statistics load() {
        try {
            File statisticsFile = FileUtil.getStatisticsFile();
            if (statisticsFile.exists()) {
                String json = FileUtil.readFromFile(statisticsFile);
                JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
                if (INSTANCE.resetByCalendar(Calendar.getInstance())) {
                    return INSTANCE;
                }
                String formatted = JsonUtil.toJsonString(INSTANCE);
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "重新格式化 statistics.json");
                    Log.system(TAG, "重新格式化 statistics.json");
                    FileUtil.write2File(formatted, statisticsFile);
                }
            } else {
                JsonUtil.MAPPER.updateValue(INSTANCE, new Statistics());
                String formatted = JsonUtil.toJsonString(INSTANCE);
                Log.i(TAG, "初始化 statistics.json");
                Log.system(TAG, "初始化 statistics.json");
                FileUtil.write2File(formatted, statisticsFile);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "统计文件格式有误，已重置统计文件");
            Log.system(TAG, "统计文件格式有误，已重置统计文件");
            try {
                JsonUtil.MAPPER.updateValue(INSTANCE, new Statistics());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, t);
            }
        }
        return INSTANCE;
    }

    private static void save() {
        String json = JsonUtil.toJsonString(INSTANCE);
        Log.system(TAG, "保存 statistics.json");
        FileUtil.write2File(json, FileUtil.getStatisticsFile());
    }

    public enum TimeType {
        YEAR, MONTH, DAY
    }

    public enum DataType {
        TIME, COLLECTED, HELPED, WATERED
    }

    @Data
    public static class TimeStatistics {
        int time;
        int collected, helped, watered;

        public TimeStatistics() {
        }

        TimeStatistics(int i) {
            reset(i);
        }

        public void reset(int i) {
            time = i;
            collected = 0;
            helped = 0;
            watered = 0;
        }
    }

}
