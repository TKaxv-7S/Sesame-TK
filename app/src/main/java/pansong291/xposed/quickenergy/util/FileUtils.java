package pansong291.xposed.quickenergy.util;

import android.os.Environment;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import pansong291.xposed.quickenergy.hook.Toast;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static final File MAIN_DIRECTORY_FILE = getMainDirectoryFile();
    public static final File CONFIG_DIRECTORY_FILE = getConfigDirectoryFile();
    public static final File LOG_DIRECTORY_FILE = getLogDirectoryFile();
    private static final Map<String, File> configFileMap = new HashMap<>();
    private static File runtimeInfoFile;
    private static File friendIdMapFile;
    private static File cooperationIdMapFile;
    private static File reserveIdMapFile;
    private static File beachIdMapFile;
    private static File statisticsFile;
    private static File exportedStatisticsFile;
    private static File systemLogFile;
    private static File forestLogFile;
    private static File farmLogFile;
    private static File otherLogFile;
    private static File recordLogFile;
    private static File debugLogFile;
    private static File runtimeLogFile;
    private static File errorLogFile;
    private static File cityCodeFile;
    private static File friendWatchFile;
    private static File wuaFile;
    private static File certCountDirectory;
    private static File certCountFile;

    @SuppressWarnings("deprecation")
    private static File getMainDirectoryFile() {
        String storageDirStr = Environment.getExternalStorageDirectory() + File.separator + "Android" +
                File.separator + "media" + File.separator + ClassUtil.PACKAGE_NAME;
        File storageDir = new File(storageDirStr);
        File mainDir = new File(storageDir, "xqe");
        if (mainDir.exists()) {
            if (mainDir.isFile()) {
                mainDir.delete();
                mainDir.mkdirs();
            }
        } else {
            mainDir.mkdirs();
            /*File oldDirectory = new File(Environment.getExternalStorageDirectory(), "xqe");
            if (oldDirectory.exists()) {
                File deprecatedFile = new File(oldDirectory, "deprecated");
                if (!deprecatedFile.exists()) {
                    copyFile(oldDirectory, mainDirectory, "config.json");
                    copyFile(oldDirectory, mainDirectory, "friendId.list");
                    copyFile(oldDirectory, mainDirectory, "cooperationId.list");
                    copyFile(oldDirectory, mainDirectory, "reserveId.list");
                    copyFile(oldDirectory, mainDirectory, "statistics.json");
                    copyFile(oldDirectory, mainDirectory, "cityCode.json");
                    try {
                        deprecatedFile.createNewFile();
                    } catch (Throwable ignored) {
                    }
                }
            }*/
        }
        return mainDir;
    }

    private static File getConfigDirectoryFile() {
        File configDir = new File(MAIN_DIRECTORY_FILE, "config");
        if (configDir.exists()) {
            if (configDir.isFile()) {
                configDir.delete();
                configDir.mkdirs();
            }
        } else {
            configDir.mkdirs();
        }
        return configDir;
    }

    private static File getLogDirectoryFile() {
        File logDir = new File(MAIN_DIRECTORY_FILE, "log");
        if (logDir.exists()) {
            if (logDir.isFile()) {
                logDir.delete();
                logDir.mkdirs();
            }
        } else {
            logDir.mkdirs();
        }
        return logDir;
    }

    public static File getConfigFile() {
        return getConfigFile(null);
    }

    public static File getConfigFile(String userId) {
        if (!configFileMap.containsKey("Default")) {
            File configFile = new File(MAIN_DIRECTORY_FILE, "config.json");
            if (configFile.exists()) {
                Log.i(TAG, "[config]读:" + configFile.canRead() + ";写:" + configFile.canWrite());
            } else {
                Log.i(TAG, "config.json文件不存在");
            }
            configFileMap.put("Default", configFile);
        }
        if (!StringUtil.isEmpty(userId)) {
            if (!configFileMap.containsKey(userId)) {
                File configFile = new File(CONFIG_DIRECTORY_FILE, "config-" + userId + ".json");
                if (configFile.exists()) {
                    configFileMap.put(userId, configFile);
                    return configFile;
                }
            } else {
                return configFileMap.get(userId);
            }
        }
        return configFileMap.get("Default");
    }

    public static File getFriendWatchFile() {
        if (friendWatchFile == null) {
            friendWatchFile = new File(MAIN_DIRECTORY_FILE, "friendWatch.json");
            if (friendWatchFile.exists() && friendWatchFile.isDirectory())
                friendWatchFile.delete();
        }
        return friendWatchFile;
    }

    public static File getWuaFile() {
        if (wuaFile == null) {
            wuaFile = new File(MAIN_DIRECTORY_FILE, "wua.list");
        }
        return wuaFile;
    }

    public static File getFriendIdMapFile() {
        if (friendIdMapFile == null) {
            friendIdMapFile = new File(MAIN_DIRECTORY_FILE, "friendId.list");
            if (friendIdMapFile.exists() && friendIdMapFile.isDirectory())
                friendIdMapFile.delete();
        }
        return friendIdMapFile;
    }

    public static File runtimeInfoFile() {
        if (runtimeInfoFile == null) {
            runtimeInfoFile = new File(MAIN_DIRECTORY_FILE, "runtimeInfo.json");
            if (!runtimeInfoFile.exists()) {
                try {
                    runtimeInfoFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return runtimeInfoFile;
    }

    public static File getCooperationIdMapFile() {
        if (cooperationIdMapFile == null) {
            cooperationIdMapFile = new File(MAIN_DIRECTORY_FILE, "cooperationId.list");
            if (cooperationIdMapFile.exists() && cooperationIdMapFile.isDirectory())
                cooperationIdMapFile.delete();
        }
        return cooperationIdMapFile;
    }

    public static File getReserveIdMapFile() {
        if (reserveIdMapFile == null) {
            reserveIdMapFile = new File(MAIN_DIRECTORY_FILE, "reserveId.list");
            if (reserveIdMapFile.exists() && reserveIdMapFile.isDirectory())
                reserveIdMapFile.delete();
        }
        return reserveIdMapFile;
    }

    public static File getBeachIdMapFile() {
        if (beachIdMapFile == null) {
            beachIdMapFile = new File(MAIN_DIRECTORY_FILE, "beachId.list");
            if (beachIdMapFile.exists() && beachIdMapFile.isDirectory())
                beachIdMapFile.delete();
        }
        return beachIdMapFile;
    }

    public static File getStatisticsFile() {
        if (statisticsFile == null) {
            statisticsFile = new File(MAIN_DIRECTORY_FILE, "statistics.json");
            if (statisticsFile.exists() && statisticsFile.isDirectory()) {
                statisticsFile.delete();
            }
            if (statisticsFile.exists()) {
                Log.i(TAG, "[statistics]读:" + statisticsFile.canRead() + ";写:" + statisticsFile.canWrite());
            } else {
                Log.i(TAG, "statisticsFile.json文件不存在");
            }
        }
        return statisticsFile;
    }

    public static File getExportedStatisticsFile() {
        if (exportedStatisticsFile == null) {
            String storageDirStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "xqe";
            File storageDir = new File(storageDirStr);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            exportedStatisticsFile = new File(storageDir, "statistics.json");
            if (exportedStatisticsFile.exists() && exportedStatisticsFile.isDirectory()) {
                exportedStatisticsFile.delete();
            }
        }
        return exportedStatisticsFile;
    }

    public static File exportFile(File file) {
        String exportDirStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "xqe";
        File exportDir = new File(exportDirStr);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File exportFile = new File(exportDir, file.getName());
        if (exportFile.exists() && exportFile.isDirectory()) {
            exportFile.delete();
        }
        if (FileUtils.copyTo(file, exportFile)) {
            return exportFile;
        }
        return null;
    }

    public static File getCityCodeFile() {
        if (cityCodeFile == null) {
            cityCodeFile = new File(MAIN_DIRECTORY_FILE, "cityCode.json");
            if(cityCodeFile.exists() && cityCodeFile.isDirectory())
                cityCodeFile.delete();
        }
        return cityCodeFile;
    }

    public static File getRuntimeLogFile() {
        if (runtimeLogFile == null) {
            runtimeLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("runtime"));
            if (runtimeLogFile.exists() && runtimeLogFile.isDirectory()) {
                runtimeLogFile.delete();
            }
            if (!runtimeLogFile.exists()) {
                try {
                    runtimeLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return runtimeLogFile;
    }

    public static File getRecordLogFile() {
        if (recordLogFile == null) {
            recordLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("record"));
            if (recordLogFile.exists() && recordLogFile.isDirectory()) {
                recordLogFile.delete();
            }
            if (!recordLogFile.exists()) {
                try {
                    recordLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return recordLogFile;
    }

    public static File getSystemLogFile() {
        if (systemLogFile == null) {
            systemLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("system"));
            if (systemLogFile.exists() && systemLogFile.isDirectory()) {
                systemLogFile.delete();
            }
            if (!systemLogFile.exists()) {
                try {
                    systemLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return systemLogFile;
    }

    public static File getDebugLogFile() {
        if (debugLogFile == null) {
            debugLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("debug"));
            if (debugLogFile.exists() && debugLogFile.isDirectory()) {
                debugLogFile.delete();
            }
            if (!debugLogFile.exists()) {
                try {
                    debugLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return debugLogFile;
    }

    public static File getForestLogFile() {
        if (forestLogFile == null) {
            forestLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("forest"));
            if (forestLogFile.exists() && forestLogFile.isDirectory()) {
                forestLogFile.delete();
            }
            if (!forestLogFile.exists()) {
                try {
                    forestLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return forestLogFile;
    }

    public static File getFarmLogFile() {
        if (farmLogFile == null) {
            farmLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("farm"));
            if (farmLogFile.exists() && farmLogFile.isDirectory()) {
                farmLogFile.delete();
            }
            if (!farmLogFile.exists()) {
                try {
                    farmLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return farmLogFile;
    }

    public static File getOtherLogFile() {
        if (otherLogFile == null) {
            otherLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("other"));
            if (otherLogFile.exists() && otherLogFile.isDirectory()) {
                otherLogFile.delete();
            }
            if (!otherLogFile.exists()) {
                try {
                    otherLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return otherLogFile;
    }

    public static File getErrorLogFile() {
        if (errorLogFile == null) {
            errorLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("error"));
            if (errorLogFile.exists() && errorLogFile.isDirectory()) {
                errorLogFile.delete();
            }
            if (!errorLogFile.exists()) {
                try {
                    errorLogFile.createNewFile();
                } catch (Throwable ignored) {
                }
            }
        }
        return errorLogFile;
    }

    public static void clearLog(int day) {
        File[] files = LOG_DIRECTORY_FILE.listFiles();
        if (files == null) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        for (File file : files) {
            if (file.getName().endsWith(".log")) {
                if (currentTimeMillis - file.lastModified() > day * 86_400_000L) {
                    file.delete();
                }
            }
        }
    }

    public static File getCertCountFile(String userId) {
        File certCountFile = new File(getCertCountDirectoryFile(), "certCount-" + userId + ".json");
        if (!certCountFile.exists()) {
            JSONObject jo_certCount = new JSONObject();
            write2File(jo_certCount.toString(), certCountFile);
        }
        return certCountFile;
    }

    public static File getCertCountDirectoryFile() {
        if (certCountDirectory == null) {
            certCountDirectory = new File(MAIN_DIRECTORY_FILE, "certCount");
            if (certCountDirectory.exists()) {
                if (certCountDirectory.isFile()) {
                    certCountDirectory.delete();
                    certCountDirectory.mkdirs();
                }
            } else {
                certCountDirectory.mkdirs();
            }
        }
        return certCountDirectory;
    }

    private static void copyFile(File srcDir, File dstDir, String filename) {
        File file = new File(srcDir, filename);
        if (!file.exists()) {
            return;
        }
        String content = readFromFile(file);
        file = new File(dstDir, filename);
        write2File(content, file);
    }

    public static void setCertCount(String userId, String dateString, int certCount) {
        try {
            File certCountFile = getCertCountFile(userId);
            JSONObject jo_certCount = new JSONObject(readFromFile(certCountFile));
            jo_certCount.put(dateString, Integer.toString(certCount));
            write2File(JsonUtil.toJsonString(jo_certCount), certCountFile);
        } catch (Throwable ignored) {
        }
    }

    public static String readFromFile(File f) {
        if (!f.exists()) {
            return "";
        }
        if (!f.canRead()) {
            Toast.show(f.getName() + "没有读取权限！", true);
            return "";
        }
        StringBuilder result = new StringBuilder();
        FileReader fr = null;
        try {
            fr = new FileReader(f);
            char[] chs = new char[1024];
            int len;
            while ((len = fr.read(chs)) >= 0) {
                result.append(chs, 0, len);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            close(fr, f);
        }
        return result.toString();
    }

    public static boolean write2File(String s, File f) {
        if (f.exists() && !f.canWrite()) {
            Toast.show(f.getAbsoluteFile() + "没有写入权限！", true);
            return false;
        }
        boolean success = false;
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            success = true;
        } catch (Throwable t) {
            if (!f.equals(getRuntimeLogFile()))
                Log.printStackTrace(TAG, t);
        }
        close(fw, f);
        return success;
    }

    public static boolean append2File(String s, File f) {
        if (f.exists() && !f.canWrite()) {
            Toast.show(f.getAbsoluteFile() + "没有写入权限！", true);
            return false;
        }
        boolean success = false;
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
            fw.append(s);
            fw.flush();
            success = true;
        } catch (Throwable t) {
            if (!f.equals(getRuntimeLogFile()))
                Log.printStackTrace(TAG, t);
        }
        close(fw, f);
        return success;
    }

    public static boolean copyTo(File f1, File f2) {
        return write2File(readFromFile(f1), f2);
    }

    public static void close(Closeable c, File f) {
        try {
            if (c != null)
                c.close();
        } catch (Throwable t) {
            if (!f.equals(getRuntimeLogFile()))
                Log.printStackTrace(TAG, t);
        }
    }

}
