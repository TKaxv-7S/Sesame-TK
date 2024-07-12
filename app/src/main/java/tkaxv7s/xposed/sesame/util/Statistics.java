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

    public static synchronized Statistics load() {
        try {
            File statisticsFile = FileUtil.getStatisticsFile();
            if (statisticsFile.exists()) {
                String json = FileUtil.readFromFile(statisticsFile);
                JsonUtil.copyMapper().readerForUpdating(INSTANCE).readValue(json);
                String formatted = JsonUtil.toFormatJsonString(INSTANCE);
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "重新格式化 statistics.json");
                    Log.system(TAG, "重新格式化 statistics.json");
                    FileUtil.write2File(formatted, statisticsFile);
                }
            } else {
                JsonUtil.copyMapper().updateValue(INSTANCE, new Statistics());
                Log.i(TAG, "初始化 statistics.json");
                Log.system(TAG, "初始化 statistics.json");
                FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), statisticsFile);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "统计文件格式有误，已重置统计文件");
            Log.system(TAG, "统计文件格式有误，已重置统计文件");
            try {
                JsonUtil.copyMapper().updateValue(INSTANCE, new Statistics());
                FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), FileUtil.getStatisticsFile());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, e);
            }
        }
        return INSTANCE;
    }

    public static synchronized void unload() {
        try {
            JsonUtil.copyMapper().updateValue(INSTANCE, new Statistics());
        } catch (JsonMappingException e) {
            Log.printStackTrace(TAG, e);
        }
    }

    public static synchronized void save() {
        save(Calendar.getInstance());
    }

    public static synchronized void save(Calendar nowCalendar) {
        if (updateDay(nowCalendar)) {
            Log.system(TAG, "重置 statistics.json");
        } else {
            Log.system(TAG, "保存 statistics.json");
        }
        FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), FileUtil.getStatisticsFile());
    }

    public static Boolean updateDay(Calendar nowCalendar) {
        int ye = nowCalendar.get(Calendar.YEAR);
        int mo = nowCalendar.get(Calendar.MONTH) + 1;
        int da = nowCalendar.get(Calendar.DAY_OF_MONTH);
        if (ye != INSTANCE.year.time) {
            INSTANCE.year.reset(ye);
            INSTANCE.month.reset(mo);
            INSTANCE.day.reset(da);
        } else if (mo != INSTANCE.month.time) {
            INSTANCE.month.reset(mo);
            INSTANCE.day.reset(da);
        } else if (da != INSTANCE.day.time) {
            INSTANCE.day.reset(da);
        } else {
            return false;
        }
        return true;
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
