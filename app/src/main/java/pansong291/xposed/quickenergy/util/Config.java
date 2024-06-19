package pansong291.xposed.quickenergy.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import pansong291.xposed.quickenergy.task.model.antFarm.AntFarm.SendType;

@Data
public class Config {

    private static final String TAG = Config.class.getSimpleName();

    public static final Config INSTANCE = defInit();

    @Getter
    private static volatile boolean init;

    /* application */
    private boolean immediateEffect = true;
    private boolean recordLog;
    private boolean showToast;
    private int toastOffsetY;
    private int checkInterval;
    private boolean stayAwake;
    private boolean timeoutRestart;
    private boolean startAt0 = true;
    private boolean startAt7;
    private boolean enableOnGoing;
    private boolean batteryPerm = true;
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
    private int collectInterval = 350;
    private int collectTimeout;
    private int returnWater33;
    private int returnWater18;
    private int returnWater10;
    private boolean helpFriendCollect;
    private Set<String> dontCollectSet = new HashSet<>();
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
    private boolean enableChouchoule = true;
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

    public static Config defInit() {
        Config c = new Config();

        c.immediateEffect = true;
        c.recordLog = true;
        c.showToast = true;
        c.toastOffsetY = 0;
        c.stayAwake = true;
        c.timeoutRestart = true;
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
        c.collectInterval = 350;
        c.collectTimeout = 2_000;
        c.returnWater33 = 0;
        c.returnWater18 = 0;
        c.returnWater10 = 0;
        c.helpFriendCollect = true;
        if (c.dontCollectSet == null)
            c.dontCollectSet = new HashSet<>();
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
        c.rewardFriend = false;
        c.sendBackAnimal = true;
        c.sendType = SendType.NORMAL;
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

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        CharSequence[] nickNames = {"始终召回", "偷吃时召回", "饥饿时召回", "不召回"};
    }

}
