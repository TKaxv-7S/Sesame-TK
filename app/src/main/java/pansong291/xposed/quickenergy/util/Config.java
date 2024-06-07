package pansong291.xposed.quickenergy.util;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.Data;
import lombok.Getter;
import pansong291.xposed.quickenergy.hook.ApplicationHook;
import pansong291.xposed.quickenergy.model.AntFarm.SendType;

@Data
public class Config {

    private static final String TAG = Config.class.getSimpleName();

    public static final Config INSTANCE = defInit();

    @Getter
    private static volatile boolean init;

    /* application */
    private boolean immediateEffect;
    private boolean recordLog;
    private boolean showToast;
    private int toastOffsetY;
    private int checkInterval;
    private boolean stayAwake;
    private int stayAwakeType;
    private int stayAwakeTarget;
    private boolean timeoutRestart;
    private int timeoutType;
    private boolean startAt7;
    private boolean enableOnGoing;
    private boolean newRpc = true;
    private boolean debugMode = false;
    private boolean languageSimplifiedChinese;


    /* forest */
    private boolean collectEnergy;

    private boolean collectWateringBubble;

    private boolean batchRobEnergy;
    private boolean collectProp;
    private boolean limitCollect;
    private int limitCount;
    private boolean doubleCard;
    private List<String> doubleCardTime;
    private int doubleCountLimit;
    private int advanceTime;
    private int collectInterval;
    private int collectTimeout;
    private int returnWater33;
    private int returnWater18;
    private int returnWater10;
    private boolean helpFriendCollect;
    private List<String> dontCollectList;
    private List<String> dontHelpCollectList;
    private boolean receiveForestTaskAward;
    private List<String> waterFriendList;
    private int waterFriendCount;
    private List<Integer> waterCountList;
    private boolean cooperateWater;
    private List<String> cooperateWaterList;
    private List<Integer> cooperateWaterNumList;
    private boolean ancientTree;
    private List<String> ancientTreeCityCodeList;
    private boolean energyRain;
    private boolean reserve;
    private List<String> reserveList;
    private List<Integer> reserveCountList;
    private boolean beach;
    private List<String> beachList;
    private List<Integer> beachCountList;
    private boolean ancientTreeOnlyWeek;

    private List<String> giveEnergyRainList;

    private int waitWhenException;

    private boolean exchangeEnergyDoubleClick;
    private int exchangeEnergyDoubleClickCount;
    private boolean antdodoCollect;
    private boolean antOcean;
    private boolean userPatrol;
    private boolean animalConsumeProp;
    private boolean collectGiftBox;
    private boolean totalCertCount;

    /* farm */
    private boolean enableFarm;
    private boolean rewardFriend;
    private boolean sendBackAnimal;
    private int sendType;
    private List<String> dontSendFriendList;
    private int recallAnimalType;
    private boolean receiveFarmToolReward;
    private boolean recordFarmGame;
    private boolean kitchen;
    private boolean useSpecialFood;
    private boolean useNewEggTool;
    private boolean harvestProduce;
    private boolean donation;
    private boolean answerQuestion;
    private boolean receiveFarmTaskAward;
    private boolean feedAnimal;
    private boolean useAccelerateTool;
    private List<String> feedFriendAnimalList;
    private List<Integer> feedFriendCountList;

    private List<String> farmGameTime;
    private List<String> animalSleepTime;
    private boolean notifyFriend;
    private List<String> dontNotifyFriendList;
    private List<String> whoYouWantGiveTo;
    private List<String> sendFriendCard;
    private boolean acceptGift;
    private List<String> visitFriendList;
    private List<Integer> visitFriendCountList;
    private boolean chickenDiary;
    private boolean antOrchard;
    private boolean receiveOrchardTaskAward;
    private int orchardSpreadManureCount;

    private boolean enableStall;
    private boolean stallAutoClose;
    private boolean stallAutoOpen;
    private boolean stallAutoTask;
    private boolean stallReceiveAward;
    private boolean stallOpenType;
    private List<String> stallOpenList;
    private List<String> stallWhiteList;
    private List<String> stallBlackList;
    private int stallAllowOpenTime;
    private int stallSelfOpenTime;
    private boolean stallDonate;
    private boolean stallInviteRegister;
    private boolean stallThrowManure;
    private List<String> stallInviteShopList;

    /* other */
    private boolean receivePoint;
    private boolean openTreasureBox;
    private boolean receiveCoinAsset;
    private boolean donateCharityCoin;
    private int minExchangeCount;
    private int latestExchangeTime;
    private int syncStepCount;
    private boolean kbSignIn;
    private boolean ecoLifeTick;
    private boolean tiyubiz;
    private boolean insBlueBeanExchange;
    private boolean collectSesame;
    private boolean zcjSignIn;
    private boolean merchantKmdk;
    private boolean greenFinance;
    private boolean antBookRead;
    private boolean consumeGold;
    private boolean omegakoiTown;

    /* forest */
    public void setDoubleCardTime(String i) {
        doubleCardTime = Arrays.asList(i.split(","));
    }

    public String getDoubleCardTime() {
        return String.join(",", doubleCardTime);
    }

    public void setFarmGameTime(String i) {
        farmGameTime = Arrays.asList(i.split(","));
    }

    public String getFarmGameTime() {
        return String.join(",", farmGameTime);
    }

    public boolean hasAnimalSleepTime() {
        for (String doubleTime : animalSleepTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public boolean hasFarmGameTime() {
        for (String doubleTime : farmGameTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public static Config defInit() {
        Config c = new Config();

        c.immediateEffect = true;
        c.recordLog = true;
        c.showToast = true;
        c.toastOffsetY = 0;
        c.stayAwake = true;
        c.stayAwakeType = ApplicationHook.StayAwakeType.ALARM;
        c.stayAwakeTarget = ApplicationHook.StayAwakeTarget.SERVICE;
        c.timeoutRestart = true;
        c.timeoutType = ApplicationHook.StayAwakeType.ALARM;
        c.startAt7 = false;
        c.enableOnGoing = false;
        c.languageSimplifiedChinese = false;

        c.collectEnergy = false;
        c.collectWateringBubble = true;
        c.batchRobEnergy = false;
        c.collectProp = true;
        c.checkInterval = 1800_000;
        c.waitWhenException = 60 * 60 * 1000;
        c.limitCollect = true;
        c.limitCount = 50;
        c.doubleCard = false;
        c.doubleCardTime = new ArrayList<>();
        c.doubleCardTime.add("0700-0730");
        c.doubleCountLimit = 6;
        c.advanceTime = 0;
        c.collectInterval = 100;
        c.collectTimeout = 2_000;
        c.returnWater33 = 0;
        c.returnWater18 = 0;
        c.returnWater10 = 0;
        c.helpFriendCollect = true;
        if (c.dontCollectList == null)
            c.dontCollectList = new ArrayList<>();
        if (c.dontHelpCollectList == null)
            c.dontHelpCollectList = new ArrayList<>();
        c.receiveForestTaskAward = true;
        if (c.waterFriendList == null)
            c.waterFriendList = new ArrayList<>();
        if (c.waterCountList == null)
            c.waterCountList = new ArrayList<>();
        c.waterFriendCount = 66;
        c.cooperateWater = true;
        if (c.cooperateWaterList == null)
            c.cooperateWaterList = new ArrayList<>();
        if (c.cooperateWaterNumList == null)
            c.cooperateWaterNumList = new ArrayList<>();
        if (c.ancientTreeCityCodeList == null)
            c.ancientTreeCityCodeList = new ArrayList<>();
        c.ancientTree = false;
        c.reserve = true;
        if (c.reserveList == null)
            c.reserveList = new ArrayList<>();
        if (c.reserveCountList == null)
            c.reserveCountList = new ArrayList<>();
        c.beach = true;
        if (c.beachList == null)
            c.beachList = new ArrayList<>();
        if (c.beachCountList == null)
            c.beachCountList = new ArrayList<>();
        c.energyRain = true;
        if (c.giveEnergyRainList == null)
            c.giveEnergyRainList = new ArrayList<>();
        c.exchangeEnergyDoubleClick = false;
        c.exchangeEnergyDoubleClickCount = 6;
        c.ancientTreeOnlyWeek = true;
        c.antdodoCollect = true;
        c.antOcean = true;
        c.userPatrol = true;
        c.animalConsumeProp = true;
        c.collectGiftBox = true;
        c.totalCertCount = false;

        c.enableFarm = true;
        c.rewardFriend = true;
        c.sendBackAnimal = true;
        c.sendType = SendType.HIT;
        if (c.dontSendFriendList == null)
            c.dontSendFriendList = new ArrayList<>();
        c.recallAnimalType = RecallAnimalType.ALWAYS;
        c.receiveFarmToolReward = true;
        c.recordFarmGame = true;
        c.kitchen = true;
        c.useSpecialFood = false;
        c.useNewEggTool = true;
        c.harvestProduce = true;
        c.donation = true;
        c.answerQuestion = true;
        c.receiveFarmTaskAward = true;
        c.feedAnimal = true;
        c.useAccelerateTool = true;
        if (c.feedFriendAnimalList == null)
            c.feedFriendAnimalList = new ArrayList<>();
        if (c.feedFriendCountList == null)
            c.feedFriendCountList = new ArrayList<>();
        c.farmGameTime = new ArrayList<>();
        c.farmGameTime.add("2200-2400");
        c.animalSleepTime = new ArrayList<>();
        c.animalSleepTime.add("2300-2400");
        c.animalSleepTime.add("0000-0559");
        c.notifyFriend = false;
        if (c.dontNotifyFriendList == null)
            c.dontNotifyFriendList = new ArrayList<>();
        c.whoYouWantGiveTo = new ArrayList<>();
        c.sendFriendCard = new ArrayList<>();
        c.acceptGift = true;
        if (c.visitFriendList == null)
            c.visitFriendList = new ArrayList<>();
        if (c.visitFriendCountList == null)
            c.visitFriendCountList = new ArrayList<>();
        c.chickenDiary = true;
        c.antOrchard = true;
        c.receiveOrchardTaskAward = true;
        c.orchardSpreadManureCount = 0;

        c.enableStall = false;
        c.stallAutoClose = false;
        c.stallAutoOpen = false;
        c.stallAutoTask = true;
        c.stallReceiveAward = false;
        c.stallOpenType = true;
        c.stallOpenList = new ArrayList<>();
        c.stallWhiteList = new ArrayList<>();
        c.stallBlackList = new ArrayList<>();
        c.stallAllowOpenTime = 121;
        c.stallSelfOpenTime = 120;
        c.stallDonate = false;
        c.stallInviteRegister = false;
        c.stallThrowManure = false;
        c.stallInviteShopList = new ArrayList<>();

        c.receivePoint = true;
        c.openTreasureBox = true;
        c.receiveCoinAsset = true;
        c.donateCharityCoin = false;
        c.kbSignIn = true;
        c.syncStepCount = 22000;
        c.ecoLifeTick = true;
        c.tiyubiz = true;
        c.insBlueBeanExchange = true;
        c.collectSesame = false;
        c.zcjSignIn = false;
        c.merchantKmdk = false;
        c.greenFinance = false;
        c.antBookRead = false;
        c.consumeGold = false;
        c.omegakoiTown = false;
        return c;
    }

    public static Boolean isModify() {
        String json = null;
        if (FileUtils.getConfigFile(FriendIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigFile(FriendIdMap.getCurrentUid()));
        }
        if (json != null) {
            String formatted = JsonUtil.toJsonString(INSTANCE);
            return formatted == null || !formatted.equals(json);
        }
        return true;
    }

    public static Boolean save(Boolean force) {
        if (!force) {
            if (!isModify()) {
                return true;
            }
        }
        String json = JsonUtil.toJsonString(INSTANCE);
        Log.system(TAG, "保存 config.json: " + json);
        return FileUtils.write2File(json, FileUtils.getConfigFile());
    }

    /* base */
    public static synchronized Config load() {
        Log.i(TAG, "load config");
        String json = null;
        if (FileUtils.getConfigFile(FriendIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigFile(FriendIdMap.getCurrentUid()));
        }
        try {
            JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "配置文件格式有误，已重置配置文件");
            Log.system(TAG, "配置文件格式有误，已重置配置文件");
            try {
                JsonUtil.MAPPER.updateValue(INSTANCE, defInit());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, t);
            }
        }
        String formatted = JsonUtil.toJsonString(INSTANCE);
        if (formatted != null && !formatted.equals(json)) {
            Log.i(TAG, "重新格式化 config.json");
            Log.system(TAG, "重新格式化 config.json");
            FileUtils.write2File(formatted, FileUtils.getConfigFile());
        }
        init = true;
        return INSTANCE;
    }

    public void setAnimalSleepTime(String i) {
        animalSleepTime = Arrays.asList(i.split(","));
    }

    public String getAnimalSleepTime() {
        return String.join(",", animalSleepTime);
    }

    public boolean hasDoubleCardTime() {
        for (String doubleTime : doubleCardTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public static boolean hasAncientTreeWeek() {
        if (!INSTANCE.isAncientTreeOnlyWeek()) {
            return true;
        }
        SimpleDateFormat sdf_week = new SimpleDateFormat("EEEE", Locale.getDefault());
        String week = sdf_week.format(new Date());
        return "星期一".equals(week) || "星期三".equals(week) || "星期五".equals(week);
    }

    private static int tmpStepCount = -1;

    public static int tmpStepCount() {
        if (tmpStepCount >= 0) {
            return tmpStepCount;
        }
        tmpStepCount = Config.INSTANCE.getSyncStepCount();
        if (tmpStepCount > 0) {
            tmpStepCount = RandomUtils.nextInt(tmpStepCount, tmpStepCount + 2000);
            if (tmpStepCount > 100000) {
                tmpStepCount = 100000;
            }
        }
        return tmpStepCount;
    }

    private static boolean checkInTimeSpan(String timeStr) {
        if (timeStr.contains("-")) {
            String[] arr = timeStr.split("-");
            String min = arr[0];
            String max = arr[1];
            String now = TimeUtil.getTimeStr();
            return min.compareTo(now) <= 0 && max.compareTo(now) >= 0;
        } else {
            return TimeUtil.checkInTime(-INSTANCE.checkInterval, timeStr);
        }
    }

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        CharSequence[] nickNames = { "始终召回", "偷吃时召回", "饥饿时召回", "不召回" };
    }

}
