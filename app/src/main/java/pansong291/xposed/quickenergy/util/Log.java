package pansong291.xposed.quickenergy.util;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Log {

    static {
        XLog.init(LogLevel.ALL);
    }

    private static final File mainDirectoryFile = FileUtils.getMainDirectoryFile();

    private static final Logger runtimeLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("runtime"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger simpleLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("simple"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger debugLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("debug"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger infoChangedLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("infoChangedFile"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger forestLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("forest"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger farmLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("farm"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();

    private static final Logger otherLogger = XLog.printers(
            new FilePrinter.Builder(mainDirectoryFile.getPath())
                    .fileNameGenerator(new CustomDateFileNameGenerator("other"))
                    .backupStrategy(new NeverBackupStrategy())
                    .cleanStrategy(new FileLastModifiedCleanStrategy(2 * 86_400_000))
                    .flattener(new ClassicFlattener())
                    .build()).build();
    private static SimpleDateFormat sdf;

    public static void i(String tag, String s) {
        runtimeLogger.i(tag + ", " + s);
    }

    public static void debug(String s) {
        debugLogger.d(s);
    }

    public static void recordLog(String str) {
        recordLog(str, "");
    }

    public static void recordLog(String str, String str2) {
        runtimeLogger.i(str + str2);
        if (!Config.recordLog())
            return;
        simpleLogger.i(str);
    }

    public static void infoChanged(String tag, String s) {
        infoChangedLogger.i(tag + ", " + s);
    }

    public static void forest(String s) {
        recordLog(s, "");
        forestLogger.i(s);
    }

    public static void farm(String s) {
        recordLog(s, "");
        farmLogger.i(s);
    }

    public static void other(String s) {
        recordLog(s, "");
        otherLogger.i(s);
    }

    public static void printStackTrace(String tag, Throwable t) {
        i(tag, android.util.Log.getStackTraceString(t));
    }

    public static String getFormatDateTime() {
        if (sdf == null)
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
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
        long timeStemp = 0;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
            d = sf.parse(timers);// 日期转换为时间戳
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timeStemp = d.getTime();
        return timeStemp;
    }

    public static class CustomDateFileNameGenerator implements FileNameGenerator {

        ThreadLocal<SimpleDateFormat> mLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {

            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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
            sdf.setTimeZone(TimeZone.getDefault());
            return name + "." + sdf.format(new Date(timestamp)) + ".log";
        }
    }

}
