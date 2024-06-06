package pansong291.xposed.quickenergy.util;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.NeverCleanStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {

    static {
        XLog.init(LogLevel.ALL);
    }

    private static final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

    };

    private static final ThreadLocal<SimpleDateFormat> dateTimeFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }

    };

    private static final ThreadLocal<SimpleDateFormat> otherDateTimeFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        }

    };

    private static final Logger runtimeLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("runtime"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS} {l}/{t}: {m}"))
                    .build()).build();

    private static final Logger recordLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("record"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS}: {m}"))
                    .build()).build();

    private static final Logger systemLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("system"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS} {l}/{t}: {m}"))
                    .build()).build();

    private static final Logger debugLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("debug"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS} {l}/{t}: {m}"))
                    .build()).build();

    private static final Logger forestLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("forest"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS}: {m}"))
                    .build()).build();

    private static final Logger farmLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("farm"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS}: {m}"))
                    .build()).build();

    private static final Logger otherLogger = XLog.printers(
            new FilePrinter.Builder(FileUtils.LOG_DIRECTORY_FILE.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("other"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new NeverCleanStrategy())
                    .flattener(new PatternFlattener("{d HH:mm:ss.SSS}: {m}"))
                    .build()).build();

    public static void i(String s) {
        runtimeLogger.i(s);
    }

    public static void i(String tag, String s) {
        i(tag + ", " + s);
    }

    public static void record(String str) {
        record(str, "");
    }

    public static void record(String str, String str2) {
        runtimeLogger.i(str + str2);
        if (!Config.INSTANCE.isRecordLog()) {
            return;
        }
        recordLogger.i(str);
    }

    public static void system(String tag, String s) {
        systemLogger.i(tag + ", " + s);
    }

    public static void debug(String s) {
        debugLogger.d(s);
    }

    public static void forest(String s) {
        record(s, "");
        forestLogger.i(s);
    }

    public static void farm(String s) {
        record(s, "");
        farmLogger.i(s);
    }

    public static void other(String s) {
        record(s, "");
        otherLogger.i(s);
    }

    public static void printStackTrace(Throwable t) {
        i(android.util.Log.getStackTraceString(t));
    }

    public static void printStackTrace(String tag, Throwable t) {
        i(tag, android.util.Log.getStackTraceString(t));
    }

    public static String getLogFileName(String logName) {
        SimpleDateFormat sdf = dateFormatThreadLocal.get();
        if (sdf == null) {
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
        return logName + "." + sdf.format(new Date()) + ".log";
    }

    public static String getFormatDateTime() {
        SimpleDateFormat simpleDateFormat = dateTimeFormatThreadLocal.get();
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
        return simpleDateFormat.format(new Date());
    }

    public static String getFormatDate() {
        return getFormatDateTime().split(" ")[0];
    }

    public static String getFormatTime() {
        return getFormatDateTime().split(" ")[1];
    }

    /* //日期转换为时间戳 */
    public static long timeToStamp(String timers) {
        Date d = new Date();
        long timeStemp;
        try {
            SimpleDateFormat simpleDateFormat = otherDateTimeFormatThreadLocal.get();
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
            }
            Date newD = simpleDateFormat.parse(timers);
            if (newD != null) {
                d = newD;
            }
        } catch (ParseException ignored) {
        }
        timeStemp = d.getTime();
        return timeStemp;
    }

    public static class CustomDateFileNameGenerator implements FileNameGenerator {

        ThreadLocal<SimpleDateFormat> mLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {

            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }

        };

        private final String name;

        public CustomDateFileNameGenerator(String name) {
            this.name = name;
        }

        @Override
        public boolean isFileNameChangeable() {
            return true;
        }

        /**
         * Generate a file name which represent a specific date.
         */
        @Override
        public String generateFileName(int logLevel, long timestamp) {
            SimpleDateFormat sdf = mLocalDateFormat.get();
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            return name + "." + sdf.format(new Date(timestamp)) + ".log";
        }
    }

}
