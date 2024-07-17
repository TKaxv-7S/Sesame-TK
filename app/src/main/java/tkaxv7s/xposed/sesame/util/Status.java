package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.Data;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.model.task.antForest.AntForestV2;

import java.io.File;
import java.util.*;

@Data
public class Status {

    private static final String TAG = Status.class.getSimpleName();

    public static final Status INSTANCE = new Status();

    // forest
    private Map<String, Integer> waterFriendLogList = new HashMap<>();
    private Set<String> cooperateWaterList = new HashSet<>();
    private Map<String, Integer> reserveLogList = new HashMap<>();
    private Set<String> ancientTreeCityCodeList = new HashSet<>();
    private Set<String> protectBubbleList = new HashSet<>();
    private int exchangeDoubleCard = 0;
    private int exchangeTimes = 0;
    private int exchangeTimesLongTime = 0;
    private int doubleTimes = 0;
    private boolean exchangeEnergyShield = false;
    private boolean exchangeCollectHistoryAnimal7Days = false;
    private boolean exchangeCollectToFriendTimes7Days = false;

    // farm
    private Boolean answerQuestion = false;
    private Map<String, Integer> feedFriendLogList = new HashMap<>();
    private Map<String, Integer> visitFriendLogList = new HashMap<>();
    private Set<String> dailyAnswerList = new HashSet<>();
    private Set<String> donationEggList = new HashSet<>();
    private int useAccelerateToolCount = 0;
    private Boolean canOrnament = true;
    private Boolean animalSleep = false;

    // stall
    private Map<String, Integer> stallHelpedCountLogList = new HashMap<>();
    private Set<String> spreadManureList = new HashSet<>();
    private Set<String> stallP2PHelpedList = new HashSet<>();
    private Boolean canStallDonate = true;

    // sport
    private Set<String> syncStepList = new HashSet<>();
    private Set<String> exchangeList = new HashSet<>();
    private boolean donateCharityCoin = false;

    // other
    private Set<String> memberSignInList = new HashSet<>();
    private int kbSignIn = 0;

    // 保存时间
    private Long saveTime = 0L;

    /**
     * 新村助力好友，已上限的用户
     */
    private Set<String> antStallAssistFriend = new HashSet<>();
    /**
     * 新村-罚单已贴完的用户
     */
    private Set<String> canPasteTicketTime = new HashSet<>();

    /**
     * 绿色经营，收取好友金币已完成用户
     */
    private Set<String> greenFinancePointFriend = new HashSet<>();

    /**
     * 绿色经营，评级领奖已完成用户
     */
    private Map<String, Integer> greenFinancePrizesMap = new HashMap<>();

    public static boolean canExchangeEnergyShield() {
        return !INSTANCE.exchangeEnergyShield;
    }

    public static void exchangeEnergyShield() {
        Status stat = INSTANCE;
        if (!stat.exchangeEnergyShield) {
            stat.exchangeEnergyShield = true;
            save();
        }
    }

    public static boolean canExchangeCollectHistoryAnimal7Days() {
        return !INSTANCE.exchangeCollectHistoryAnimal7Days;
    }

    public static void exchangeCollectHistoryAnimal7Days() {
        Status stat = INSTANCE;
        if (!stat.exchangeCollectHistoryAnimal7Days) {
            stat.exchangeCollectHistoryAnimal7Days = true;
            save();
        }
    }

    public static boolean canExchangeCollectToFriendTimes7Days() {
        return !INSTANCE.exchangeCollectToFriendTimes7Days;
    }

    public static void exchangeCollectToFriendTimes7Days() {
        Status stat = INSTANCE;
        if (!stat.exchangeCollectToFriendTimes7Days) {
            stat.exchangeCollectToFriendTimes7Days = true;
            save();
        }
    }

    public static boolean canAnimalSleep() {
        return !INSTANCE.animalSleep;
    }

    public static void animalSleep() {
        Status stat = INSTANCE;
        if (!stat.animalSleep) {
            stat.animalSleep = true;
            save();
        }
    }

    public static boolean canWaterFriendToday(String id, int newCount) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Integer count = INSTANCE.waterFriendLogList.get(id);
        if (count == null) {
            return true;
        }
        return count < newCount;
    }

    public static void waterFriendToday(String id, int count) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        INSTANCE.waterFriendLogList.put(id, count);
        save();
    }

    public static int getReserveTimes(String id) {
        Integer count = INSTANCE.reserveLogList.get(id);
        if (count == null) {
            return 0;
        }
        return count;
    }

    public static boolean canReserveToday(String id, int count) {
        return getReserveTimes(id) < count;
    }

    public static void reserveToday(String id, int newCount) {
        Integer count = INSTANCE.reserveLogList.get(id);
        if (count == null) {
            count = 0;
        }
        INSTANCE.reserveLogList.put(id, count + newCount);
        save();
    }

    public static boolean canCooperateWaterToday(String uid, String coopId) {
        return !INSTANCE.cooperateWaterList.contains(uid + "_" + coopId);
    }

    public static void cooperateWaterToday(String uid, String coopId) {
        Status stat = INSTANCE;
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
        Status stat = INSTANCE;
        if (!stat.ancientTreeCityCodeList.contains(cityCode)) {
            stat.ancientTreeCityCodeList.add(cityCode);
            save();
        }
    }

    public static boolean canAnswerQuestionToday() {
        return !INSTANCE.answerQuestion;
    }

    public static void answerQuestionToday() {
        Status stat = INSTANCE;
        if (!stat.answerQuestion) {
            stat.answerQuestion = true;
            save();
        }
    }

    public static boolean canFeedFriendToday(String id, int newCount) {
        Integer count = INSTANCE.feedFriendLogList.get(id);
        if (count == null) {
            return true;
        }
        return count < newCount;
    }

    public static void feedFriendToday(String id) {
        Integer count = INSTANCE.feedFriendLogList.get(id);
        if (count == null) {
            count = 0;
        }
        INSTANCE.feedFriendLogList.put(id, count + 1);
        save();
    }

    public static boolean canVisitFriendToday(String id, int newCount) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        Integer count = INSTANCE.visitFriendLogList.get(id);
        if (count == null) {
            return true;
        }
        return count < newCount;
    }

    public static void visitFriendToday(String id, int newCount) {
        id = UserIdMap.getCurrentUid() + "-" + id;
        INSTANCE.visitFriendLogList.put(id, newCount);
        save();
    }

    public static boolean canStallHelpToday(String id) {
        Integer count = INSTANCE.stallHelpedCountLogList.get(id);
        if (count == null) {
            return true;
        }
        return count < 3;
    }

    public static void stallHelpToday(String id, boolean limited) {
        Integer count = INSTANCE.stallHelpedCountLogList.get(id);
        if (count == null) {
            count = 0;
        }
        if (limited) {
            count = 3;
        } else {
            count += 1;
        }
        INSTANCE.stallHelpedCountLogList.put(id, count);
        save();
    }

    public static boolean canMemberSignInToday(String uid) {
        return !INSTANCE.memberSignInList.contains(uid);
    }

    public static void memberSignInToday(String uid) {
        Status stat = INSTANCE;
        if (!stat.memberSignInList.contains(uid)) {
            stat.memberSignInList.add(uid);
            save();
        }
    }

    public static boolean canUseAccelerateTool() {
        return INSTANCE.useAccelerateToolCount < 8;
    }

    public static void useAccelerateTool() {
        INSTANCE.useAccelerateToolCount += 1;
        save();
    }

    public static boolean canDonationEgg(String uid) {
        return !INSTANCE.donationEggList.contains(uid);
    }

    public static void donationEgg(String uid) {
        Status stat = INSTANCE;
        if (!stat.donationEggList.contains(uid)) {
            stat.donationEggList.add(uid);
            save();
        }
    }

    public static boolean canSpreadManureToday(String uid) {
        return !INSTANCE.spreadManureList.contains(uid);
    }

    public static void spreadManureToday(String uid) {
        Status stat = INSTANCE;
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
        Status stat = INSTANCE;
        if (!stat.stallP2PHelpedList.contains(uid)) {
            stat.stallP2PHelpedList.add(uid);
            save();
        }
    }

    /**
     * 是否可以新村助力
     *
     * @return true是，false否
     */
    public static boolean canAntStallAssistFriendToday() {
        return !INSTANCE.antStallAssistFriend.contains(UserIdMap.getCurrentUid());
    }

    /**
     * 设置新村助力已到上限
     */
    public static void antStallAssistFriendToday() {
        Status stat = INSTANCE;
        String uid = UserIdMap.getCurrentUid();
        if (!stat.antStallAssistFriend.contains(uid)) {
            stat.antStallAssistFriend.add(uid);
            save();
        }
    }

    public static boolean canProtectBubbleToday(String uid) {
        return !INSTANCE.protectBubbleList.contains(uid);
    }

    public static void protectBubbleToday(String uid) {
        Status stat = INSTANCE;
        if (!stat.protectBubbleList.contains(uid)) {
            stat.protectBubbleList.add(uid);
            save();
        }
    }

    public static boolean canExchangeDoubleCardToday() {
        Status stat = INSTANCE;
        if (stat.exchangeDoubleCard < Statistics.INSTANCE.getDay().time) {
            return true;
        }
        AntForestV2 task = ModelTask.getModel(AntForestV2.class);
        if (task == null) {
            return false;
        }
        return stat.exchangeTimes < task.getExchangeEnergyDoubleClickCount().getValue();
    }

    public static void exchangeDoubleCardToday(boolean isSuccess) {
        Status stat = INSTANCE;
        if (stat.exchangeDoubleCard != Statistics.INSTANCE.getDay().time) {
            stat.exchangeDoubleCard = Statistics.INSTANCE.getDay().time;
        }
        if (isSuccess) {
            stat.exchangeTimes += 1;
        } else {
            AntForestV2 task = ModelTask.getModel(AntForestV2.class);
            if (task == null) {
                stat.exchangeTimes = 0;
            } else {
                stat.exchangeTimes = task.getExchangeEnergyDoubleClickCount().getValue();
            }
        }
        save();
    }

    public static boolean canExchangeDoubleCardTodayLongTime() {
        Status stat = INSTANCE;
        if (stat.exchangeDoubleCard < Statistics.INSTANCE.getDay().time) {
            return true;
        }
        AntForestV2 task = ModelTask.getModel(AntForestV2.class);
        if (task == null) {
            return false;
        }
        return stat.exchangeTimesLongTime < task.getExchangeEnergyDoubleClickCountLongTime().getValue();
    }

    public static void exchangeDoubleCardTodayLongTime(boolean isSuccess) {
        Status stat = INSTANCE;
        if (stat.exchangeDoubleCard != Statistics.INSTANCE.getDay().time) {
            stat.exchangeDoubleCard = Statistics.INSTANCE.getDay().time;
        }
        if (isSuccess) {
            stat.exchangeTimesLongTime += 1;
        } /*else {
            stat.exchangeTimesLongTime = AntForestV2.exchangeEnergyDoubleClickCountLongTime.getValue();
        }*/
        save();
    }

    /**
     * 是否可以贴罚单
     *
     * @return true是，false否
     */
    public static boolean canPasteTicketTime() {
        return !INSTANCE.canPasteTicketTime.contains(UserIdMap.getCurrentUid());
    }

    /**
     * 罚单贴完了
     */
    public static void pasteTicketTime() {
        if (INSTANCE.canPasteTicketTime.contains(UserIdMap.getCurrentUid())) {
            return;
        }
        INSTANCE.canPasteTicketTime.add(UserIdMap.getCurrentUid());
        save();
    }

    public static boolean canDoubleToday() {
        AntForestV2 task = ModelTask.getModel(AntForestV2.class);
        if (task == null) {
            return false;
        }
        return INSTANCE.doubleTimes < task.getDoubleCountLimit().getValue();
    }

    public static void DoubleToday() {
        INSTANCE.doubleTimes += 1;
        save();
    }

    public static boolean canKbSignInToday() {
        Status stat = INSTANCE;
        return stat.kbSignIn < Statistics.INSTANCE.getDay().time;
    }

    public static void KbSignInToday() {
        Status stat = INSTANCE;
        if (stat.kbSignIn != Statistics.INSTANCE.getDay().time) {
            stat.kbSignIn = Statistics.INSTANCE.getDay().time;
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

    public static boolean canDonateCharityCoin() {
        return !INSTANCE.donateCharityCoin;
    }

    public static void donateCharityCoin() {
        Status stat = INSTANCE;
        if (!stat.donateCharityCoin) {
            stat.donateCharityCoin = true;
            save();
        }
    }

    public static boolean canSyncStepToday(String uid) {
        Status stat = INSTANCE;
        return !stat.syncStepList.contains(uid);
    }

    public static void SyncStepToday(String uid) {
        Status stat = INSTANCE;
        if (!stat.syncStepList.contains(uid)) {
            stat.syncStepList.add(uid);
            save();
        }
    }

    public static boolean canExchangeToday(String uid) {
        return !INSTANCE.exchangeList.contains(uid);
    }

    public static void exchangeToday(String uid) {
        Status stat = INSTANCE;
        if (!stat.exchangeList.contains(uid)) {
            stat.exchangeList.add(uid);
            save();
        }
    }

    /**
     * 绿色经营-是否可以收好友金币
     *
     * @return true是，false否
     */
    public static boolean canGreenFinancePointFriend() {
        return !INSTANCE.greenFinancePointFriend.contains(UserIdMap.getCurrentUid());
    }

    /**
     * 绿色经营-收好友金币完了
     */
    public static void greenFinancePointFriend() {
        if (!canGreenFinancePointFriend()) {
            return;
        }
        INSTANCE.greenFinancePointFriend.add(UserIdMap.getCurrentUid());
        save();
    }

    /**
     * 绿色经营-是否可以做评级任务
     *
     * @return true是，false否
     */
    public static boolean canGreenFinancePrizesMap() {
        int week = TimeUtil.getWeekNumber(new Date());
        String currentUid = UserIdMap.getCurrentUid();
        if (INSTANCE.greenFinancePrizesMap.containsKey(currentUid)) {
            Integer storedWeek = INSTANCE.greenFinancePrizesMap.get(currentUid);
            return storedWeek == null || storedWeek != week;
        }
        return true;
    }

    /**
     * 绿色经营-评级任务完了
     */
    public static void greenFinancePrizesMap() {
        if (!canGreenFinancePrizesMap()) {
            return;
        }
        INSTANCE.greenFinancePrizesMap.put(UserIdMap.getCurrentUid(), TimeUtil.getWeekNumber(new Date()));
        save();
    }

    public static synchronized Status load() {
        String currentUid = UserIdMap.getCurrentUid();
        try {
            if (StringUtil.isEmpty(currentUid)) {
                Log.i(TAG, "用户为空，状态加载失败");
                throw new RuntimeException("用户为空，状态加载失败");
            }
            File statusFile = FileUtil.getStatusFile(currentUid);
            if (statusFile.exists()) {
                String json = FileUtil.readFromFile(statusFile);
                JsonUtil.copyMapper().readerForUpdating(INSTANCE).readValue(json);
                String formatted = JsonUtil.toFormatJsonString(INSTANCE);
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "重新格式化 status.json");
                    Log.system(TAG, "重新格式化 status.json");
                    FileUtil.write2File(formatted, FileUtil.getStatusFile(currentUid));
                }
            } else {
                JsonUtil.copyMapper().updateValue(INSTANCE, new Status());
                Log.i(TAG, "初始化 status.json");
                Log.system(TAG, "初始化 status.json");
                FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), FileUtil.getStatusFile(currentUid));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "状态文件格式有误，已重置");
            Log.system(TAG, "状态文件格式有误，已重置");
            try {
                JsonUtil.copyMapper().updateValue(INSTANCE, new Status());
                FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), FileUtil.getStatusFile(currentUid));
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, e);
            }
        }
        if (INSTANCE.saveTime == 0) {
            INSTANCE.saveTime = System.currentTimeMillis();
        }
        return INSTANCE;
    }

    public static synchronized void unload() {
        try {
            JsonUtil.copyMapper().updateValue(INSTANCE, new Status());
        } catch (JsonMappingException e) {
            Log.printStackTrace(TAG, e);
        }
    }

    public static synchronized void save() {
        save(Calendar.getInstance());
    }

    public static synchronized void save(Calendar nowCalendar) {
        String currentUid = UserIdMap.getCurrentUid();
        if (StringUtil.isEmpty(currentUid)) {
            Log.record("用户为空，状态保存失败");
            throw new RuntimeException("用户为空，状态保存失败");
        }
        if (updateDay(nowCalendar)) {
            Log.system(TAG, "重置 statistics.json");
        } else {
            Log.system(TAG, "保存 status.json");
        }
        long lastSaveTime = INSTANCE.saveTime;
        try {
            INSTANCE.saveTime = System.currentTimeMillis();
            FileUtil.write2File(JsonUtil.toFormatJsonString(INSTANCE), FileUtil.getStatusFile(currentUid));
        } catch (Exception e){
            INSTANCE.saveTime = lastSaveTime;
            throw e;
        }
    }

    public static Boolean updateDay(Calendar nowCalendar) {
        if (TimeUtil.isLessThanSecondOfDays(INSTANCE.saveTime, nowCalendar.getTimeInMillis())) {
            Status.unload();
            return true;
        } else {
            return false;
        }
    }

    public static boolean canOrnamentToday() {
        return INSTANCE.canOrnament;
    }

    public static void setOrnamentToday() {
        if (INSTANCE.canOrnament) {
            INSTANCE.canOrnament = false;
            save();
        }
    }

    // 新村捐赠
    public static boolean canStallDonateToday() {
        return INSTANCE.canStallDonate;
    }

    public static void setStallDonateToday() {
        if (INSTANCE.canStallDonate) {
            INSTANCE.canStallDonate = false;
            save();
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