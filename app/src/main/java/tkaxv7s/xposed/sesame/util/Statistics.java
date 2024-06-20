package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import tkaxv7s.xposed.sesame.task.model.antCooperate.AntCooperate;
import tkaxv7s.xposed.sesame.task.model.antForest.AntForestV2;

@Data
public class Statistics {

    private static final String TAG = Statistics.class.getSimpleName();

    public static final Statistics INSTANCE = new Statistics();

    private TimeStatistics year;
    private TimeStatistics month;
    private TimeStatistics day;

    // forest
    private ArrayList<WaterFriendLog> waterFriendLogList = new ArrayList<>();
    private ArrayList<String> cooperateWaterList = new ArrayList<>();
    private ArrayList<String> syncStepList = new ArrayList<>();
    private ArrayList<ReserveLog> reserveLogList = new ArrayList<>();
    private ArrayList<BeachLog> beachLogList = new ArrayList<>();
    private ArrayList<String> beachTodayList = new ArrayList<>();
    private ArrayList<String> ancientTreeCityCodeList = new ArrayList<>();
    private ArrayList<String> exchangeList = new ArrayList<>();
    private ArrayList<String> protectBubbleList = new ArrayList<>();
    private int exchangeDoubleCard = 0;
    private int exchangeTimes = 0;
    private int doubleTimes = 0;

    // farm
    private ArrayList<String> answerQuestionList = new ArrayList<>();
    private String questionHint;
    private ArrayList<FeedFriendLog> feedFriendLogList = new ArrayList<>();
    private ArrayList<VisitFriendLog> visitFriendLogList = new ArrayList<>();
    private ArrayList<StallShareIdLog> stallShareIdLogList = new ArrayList<>();
    private ArrayList<StallHelpedCountLog> stallHelpedCountLogList = new ArrayList<>();
    private Set<String> dailyAnswerList = new HashSet<>();
    private ArrayList<String> donationEggList = new ArrayList<>();
    private ArrayList<String> spreadManureList = new ArrayList<>();
    private ArrayList<String> stallP2PHelpedList = new ArrayList<>();

    // other
    private ArrayList<String> memberSignInList = new ArrayList<>();
    private int kbSignIn = 0;

    public Statistics() {
        Calendar calendar = Calendar.getInstance();
        year = new TimeStatistics(calendar.get(Calendar.YEAR));
        month = new TimeStatistics(calendar.get(Calendar.MONTH));
        day = new TimeStatistics(calendar.get(Calendar.DAY_OF_YEAR));
    }

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
        Statistics stat = INSTANCE;
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
        if (stat.questionHint != null && !stat.questionHint.isEmpty()) {
            sb.append("\nquestion hint : ").append(stat.questionHint);
        }
        return sb.toString();
    }

    public static boolean canWaterFriendToday(String id, int count) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.waterFriendLogList.size(); i++)
            if (stat.waterFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return true;
        WaterFriendLog wfl = stat.waterFriendLogList.get(index);
        return wfl.waterCount < count;
    }

    public static void waterFriendToday(String id, int count) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Statistics stat = INSTANCE;
        WaterFriendLog wfl;
        int index = -1;
        for (int i = 0; i < stat.waterFriendLogList.size(); i++)
            if (stat.waterFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            wfl = new WaterFriendLog(id);
            stat.waterFriendLogList.add(wfl);
        } else {
            wfl = stat.waterFriendLogList.get(index);
        }
        wfl.waterCount = count;
        save();
    }

    public static int getReserveTimes(String id) {
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.reserveLogList.size(); i++)
            if (stat.reserveLogList.get(i).projectId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return 0;
        ReserveLog rl = stat.reserveLogList.get(index);
        return rl.applyCount;
    }

    public static boolean canReserveToday(String id, int count) {
        return getReserveTimes(id) < count;
    }

    public static void reserveToday(String id, int count) {
        Statistics stat = INSTANCE;
        ReserveLog rl;
        int index = -1;
        for (int i = 0; i < stat.reserveLogList.size(); i++)
            if (stat.reserveLogList.get(i).projectId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            rl = new ReserveLog(id);
            stat.reserveLogList.add(rl);
        } else {
            rl = stat.reserveLogList.get(index);
        }
        rl.applyCount += count;
        save();
    }

    public static boolean canCooperateWaterToday(String uid, String coopId) {
        return !AntCooperate.cooperateWaterList.getValue().getKey().containsKey(uid + "_" + coopId);
    }

    public static void cooperateWaterToday(String uid, String coopId) {
        Statistics stat = INSTANCE;
        String v = uid + "_" + coopId;
        if (!stat.cooperateWaterList.contains(v)) {
            stat.cooperateWaterList.add(v);
            save();
        }
    }

    public static boolean canAncientTreeToday(String cityCode) {
        return !INSTANCE.ancientTreeCityCodeList.contains(cityCode);
    }

    public static void ancientTreeToday(String cityCode) {
        Statistics stat = INSTANCE;
        if (!stat.ancientTreeCityCodeList.contains(cityCode)) {
            stat.ancientTreeCityCodeList.add(cityCode);
            save();
        }
    }

    public static boolean canAnswerQuestionToday(String uid) {
        return !INSTANCE.answerQuestionList.contains(uid);
    }

    public static void answerQuestionToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.answerQuestionList.contains(uid)) {
            stat.answerQuestionList.add(uid);
            save();
        }
    }

    public static void setQuestionHint(String s) {
        Statistics stat = INSTANCE;
        if (stat.questionHint == null) {
            stat.questionHint = s;
            save();
        }
    }

    public static boolean canFeedFriendToday(String id, int count) {
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.feedFriendLogList.size(); i++)
            if (stat.feedFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return true;
        FeedFriendLog ffl = stat.feedFriendLogList.get(index);
        return ffl.feedCount < count;
    }

    public static void feedFriendToday(String id) {
        Statistics stat = INSTANCE;
        FeedFriendLog ffl;
        int index = -1;
        for (int i = 0; i < stat.feedFriendLogList.size(); i++)
            if (stat.feedFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            ffl = new FeedFriendLog(id);
            stat.feedFriendLogList.add(ffl);
        } else {
            ffl = stat.feedFriendLogList.get(index);
        }
        ffl.feedCount++;
        save();
    }

    public static boolean canVisitFriendToday(String id, int count) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.visitFriendLogList.size(); i++)
            if (stat.visitFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return true;
        VisitFriendLog vfl = stat.visitFriendLogList.get(index);
        return vfl.visitCount < count;
    }

    public static void visitFriendToday(String id, int count) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Statistics stat = INSTANCE;
        VisitFriendLog vfl;
        int index = -1;
        for (int i = 0; i < stat.visitFriendLogList.size(); i++)
            if (stat.visitFriendLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            vfl = new VisitFriendLog(id);
            stat.visitFriendLogList.add(vfl);
        } else {
            vfl = stat.visitFriendLogList.get(index);
        }
        vfl.visitCount = count;
        save();
    }

    public static List<String> stallP2PUserIdList(String uid) {
        List<String> p2pUserIdList = new ArrayList<>();
        Statistics stat = INSTANCE;
        for (int i = 0; i < stat.stallShareIdLogList.size(); i++)
            if (!stat.stallShareIdLogList.get(i).userId.equals(uid)) {
                p2pUserIdList.add(stat.stallShareIdLogList.get(i).userId);
            }
        return p2pUserIdList;
    }

    public static void stallShareIdToday(String uid, String sid) {
        Statistics stat = INSTANCE;
        StallShareIdLog ssil;
        int index = -1;
        for (int i = 0; i < stat.stallShareIdLogList.size(); i++)
            if (stat.stallShareIdLogList.get(i).userId.equals(uid)) {
                index = i;
                break;
            }
        if (index < 0) {
            ssil = new StallShareIdLog(uid, sid);
            stat.stallShareIdLogList.add(ssil);
        } else {
            ssil = stat.stallShareIdLogList.get(index);
        }
        ssil.shareId = sid;
        save();
    }

    public static String getStallShareId(String uid) {
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.stallShareIdLogList.size(); i++)
            if (stat.stallShareIdLogList.get(i).userId.equals(uid)) {
                index = i;
                break;
            }
        if (index < 0) {
            return null;
        } else {
            return stat.stallShareIdLogList.get(index).shareId;
        }
    }

    public static boolean canStallHelpToday(String id) {
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.stallHelpedCountLogList.size(); i++)
            if (stat.stallHelpedCountLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return true;
        StallHelpedCountLog shcl = stat.stallHelpedCountLogList.get(index);
        return shcl.helpedCount < 3;
    }

    public static void stallHelpToday(String id, boolean limited) {
        Statistics stat = INSTANCE;
        StallHelpedCountLog shcl;
        int index = -1;
        for (int i = 0; i < stat.stallHelpedCountLogList.size(); i++)
            if (stat.stallHelpedCountLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            shcl = new StallHelpedCountLog(id);
            stat.stallHelpedCountLogList.add(shcl);
        } else {
            shcl = stat.stallHelpedCountLogList.get(index);
        }
        if (limited) {
            shcl.helpedCount = 3;
        } else {
            shcl.helpedCount += 1;
        }
        save();
    }

    public static boolean canStallBeHelpToday(String id) {
        Statistics stat = INSTANCE;
        int index = -1;
        for (int i = 0; i < stat.stallHelpedCountLogList.size(); i++)
            if (stat.stallHelpedCountLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0)
            return true;
        StallHelpedCountLog shcl = stat.stallHelpedCountLogList.get(index);
        return shcl.beHelpedCount < 3;
    }

    public static void stallBeHelpToday(String id, boolean limited) {
        Statistics stat = INSTANCE;
        StallHelpedCountLog shcl;
        int index = -1;
        for (int i = 0; i < stat.stallHelpedCountLogList.size(); i++)
            if (stat.stallHelpedCountLogList.get(i).userId.equals(id)) {
                index = i;
                break;
            }
        if (index < 0) {
            shcl = new StallHelpedCountLog(id);
            stat.stallHelpedCountLogList.add(shcl);
        } else {
            shcl = stat.stallHelpedCountLogList.get(index);
        }
        if (limited) {
            shcl.beHelpedCount = 3;
        } else {
            shcl.beHelpedCount += 1;
        }
        save();
    }

    public static boolean canMemberSignInToday(String uid) {
        return !INSTANCE.memberSignInList.contains(uid);
    }

    public static void memberSignInToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.memberSignInList.contains(uid)) {
            stat.memberSignInList.add(uid);
            save();
        }
    }

    public static boolean canDonationEgg(String uid) {
        return !INSTANCE.donationEggList.contains(uid);
    }

    public static void donationEgg(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.donationEggList.contains(uid)) {
            stat.donationEggList.add(uid);
            save();
        }
    }

    public static boolean canSpreadManureToday(String uid) {
        return !INSTANCE.spreadManureList.contains(uid);
    }

    public static void spreadManureToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.spreadManureList.contains(uid)) {
            stat.spreadManureList.add(uid);
            save();
        }
    }

    public static boolean canStallP2PHelpToday(String uid) {
        uid = UserIdMap.getCurrentUid() + "-" + uid;
        return !INSTANCE.stallP2PHelpedList.contains(uid);
    }

    public static void stallP2PHelpeToday(String uid) {
        uid = UserIdMap.getCurrentUid() + "-" + uid;
        Statistics stat = INSTANCE;
        if (!stat.stallP2PHelpedList.contains(uid)) {
            stat.stallP2PHelpedList.add(uid);
            save();
        }
    }

    public static boolean canExchangeToday(String uid) {
        return !INSTANCE.exchangeList.contains(uid);
    }

    public static void exchangeToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.exchangeList.contains(uid)) {
            stat.exchangeList.add(uid);
            save();
        }
    }

    public static boolean canProtectBubbleToday(String uid) {
        return !INSTANCE.protectBubbleList.contains(uid);
    }

    public static void protectBubbleToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.protectBubbleList.contains(uid)) {
            stat.protectBubbleList.add(uid);
            save();
        }
    }

    public static boolean canExchangeDoubleCardToday() {
        Statistics stat = INSTANCE;
        if (stat.exchangeDoubleCard < stat.day.time) {
            return true;
        } else return stat.exchangeTimes < AntForestV2.exchangeEnergyDoubleClickCount.getValue();
    }

    public static void exchangeDoubleCardToday(boolean isSuccess) {
        Statistics stat = INSTANCE;
        if (stat.exchangeDoubleCard != stat.day.time) {
            stat.exchangeDoubleCard = stat.day.time;
        }
        if (isSuccess) {
            stat.exchangeTimes += 1;
        } else {
            stat.exchangeTimes = AntForestV2.exchangeEnergyDoubleClickCount.getValue();
        }
        save();
    }

    public static boolean canDoubleToday() {
        return INSTANCE.doubleTimes < AntForestV2.doubleCountLimit.getValue();
    }

    public static void DoubleToday() {
        INSTANCE.doubleTimes += 1;
        save();
    }

    public static boolean canKbSignInToday() {
        Statistics stat = INSTANCE;
        return stat.kbSignIn < stat.day.time;
    }

    public static void KbSignInToday() {
        Statistics stat = INSTANCE;
        if (stat.kbSignIn != stat.day.time) {
            stat.kbSignIn = stat.day.time;
            save();
        }
    }

    public static Set<String> getDadaDailySet() {
        return INSTANCE.dailyAnswerList;
    }

    public static void setDadaDailySet(Set<String> dailyAnswerList) {
        INSTANCE.dailyAnswerList = dailyAnswerList;
        save();
    }

    public static boolean canSyncStepToday(String uid) {
        Statistics stat = INSTANCE;
        return !stat.syncStepList.contains(uid);
    }

    public static void SyncStepToday(String uid) {
        Statistics stat = INSTANCE;
        if (!stat.syncStepList.contains(uid)) {
            stat.syncStepList.add(uid);
            save();
        }
    }

    public Boolean resetByCalendar(Calendar calendar) {
        int ye = calendar.get(Calendar.YEAR);
        int mo = calendar.get(Calendar.MONTH);
        int da = calendar.get(Calendar.DAY_OF_MONTH);
        if (ye > year.time) {
            year.reset(ye);
            month.reset(mo);
            day.reset(da);
        } else if (mo > month.time) {
            month.reset(mo);
            day.reset(da);
        } else if (da > day.time) {
            day.reset(da);
        } else {
            return false;
        }
        dayClear();
        return true;
    }

    private static void dayClear() {
        Log.system(TAG, "重置 statistics.json");
        Statistics stat = INSTANCE;
        stat.waterFriendLogList.clear();
        stat.cooperateWaterList.clear();
        stat.syncStepList.clear();
        stat.exchangeList.clear();
        stat.protectBubbleList.clear();
        stat.reserveLogList.clear();
        stat.beachTodayList.clear();
        stat.ancientTreeCityCodeList.clear();
        stat.answerQuestionList.clear();
        stat.feedFriendLogList.clear();
        stat.visitFriendLogList.clear();
        stat.stallHelpedCountLogList.clear();
        stat.questionHint = null;
        stat.donationEggList.clear();
        stat.spreadManureList.clear();
        stat.stallP2PHelpedList.clear();
        stat.memberSignInList.clear();
        stat.kbSignIn = 0;
        stat.exchangeDoubleCard = 0;
        stat.exchangeTimes = 0;
        stat.doubleTimes = 0;
        save();
    }

    public static synchronized Statistics load() {
        String json = null;
        try {
            File statisticsFile = FileUtil.getStatisticsFile();
            if (statisticsFile.exists()) {
                json = FileUtil.readFromFile(statisticsFile);
            }
            JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
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
        String formatted = JsonUtil.toJsonString(INSTANCE);
        if (formatted != null && !formatted.equals(json)) {
            Log.i(TAG, "重新格式化 statistics.json");
            Log.system(TAG, "重新格式化 statistics.json");
            FileUtil.write2File(formatted, FileUtil.getStatisticsFile());
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
    private static class TimeStatistics {
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

    @Data
    private static class WaterFriendLog {
        String userId;
        int waterCount = 0;

        public WaterFriendLog() {
        }

        public WaterFriendLog(String id) {
            userId = id;
        }
    }

    @Data
    private static class ReserveLog {
        String projectId;
        int applyCount = 0;

        public ReserveLog() {
        }

        public ReserveLog(String id) {
            projectId = id;
        }
    }

    @Data
    private static class BeachLog {
        String cultivationCode;
        int applyCount = 0;

        public BeachLog() {
        }

        public BeachLog(String id) {
            cultivationCode = id;
        }
    }

    @Data
    private static class FeedFriendLog {
        String userId;
        int feedCount = 0;

        public FeedFriendLog() {
        }

        public FeedFriendLog(String id) {
            userId = id;
        }
    }

    @Data
    private static class VisitFriendLog {
        String userId;
        int visitCount = 0;

        public VisitFriendLog() {
        }

        public VisitFriendLog(String id) {
            userId = id;
        }
    }

    @Data
    private static class StallShareIdLog {
        String userId;
        String shareId;

        public StallShareIdLog() {
        }

        public StallShareIdLog(String uid, String sid) {
            userId = uid;
            shareId = sid;
        }
    }

    @Data
    private static class StallHelpedCountLog {
        String userId;
        int helpedCount = 0;
        int beHelpedCount = 0;

        public StallHelpedCountLog() {
        }

        public StallHelpedCountLog(String id) {
            userId = id;
        }
    }

}
