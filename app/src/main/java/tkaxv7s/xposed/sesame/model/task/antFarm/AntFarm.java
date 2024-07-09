package tkaxv7s.xposed.sesame.model.task.antFarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ListModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.model.normal.answerAI.AnswerAI;
import tkaxv7s.xposed.sesame.util.*;

import java.util.*;

public class AntFarm extends ModelTask {
    private static final String TAG = AntFarm.class.getSimpleName();

    private String ownerFarmId;
    private String userId;
    private Animal[] animals;
    private Animal ownerAnimal = new Animal();
    private int foodStock;
    private int foodStockLimit;
    private String rewardProductNum;
    private RewardFriend[] rewardList;
    private double benevolenceScore;
    private double harvestBenevolenceScore;
    private int unreceiveTaskAward = 0;

    private FarmTool[] farmTools;

    private static final List<String> bizKeyList;

    static {
        bizKeyList = new ArrayList<>();
        bizKeyList.add("ADD_GONGGE_NEW");
        bizKeyList.add("USER_STARVE_PUSH");
        bizKeyList.add("YEB_PURCHASE");
        bizKeyList.add("WIDGET_addzujian");//添加庄园小组件
        bizKeyList.add("HIRE_LOW_ACTIVITY");//雇佣小鸡拿饲料
        bizKeyList.add("HEART_DONATION_ADVANCED_FOOD_V2");//爱心美食任务
        bizKeyList.add("DIANTAOHUANDUAN");//去点淘逛一逛
        bizKeyList.add("TAO_GOLDEN_V2");//去逛一逛淘金币小镇
        bizKeyList.add("SHANGYEHUA_90_1");//去杂货铺逛一逛
        bizKeyList.add("TAOBAO_tab2gzy");// 去逛一逛淘宝视频
        bizKeyList.add("YITAO_appgyg");// 去一淘APP逛逛
        bizKeyList.add("ANTFARM_chouchoule");// 【抽抽乐】好运装扮来袭！
        bizKeyList.add("TB_qiandao2023");// 去淘宝签到逛一逛
        bizKeyList.add("BABAFARM_TB");// 去逛一逛淘宝芭芭农场
        bizKeyList.add("TB_chongzhi");// 逛一逛小羊农场
        bizKeyList.add("ALIPAIMAI_gygzy");// 逛一逛淘花岛
        bizKeyList.add("BABA_FARM_SPREAD_MANURE");// 去芭芭农场给果树施肥
        bizKeyList.add("ELM_hudong2024");// 去饿了么游乐园逛一逛
        bizKeyList.add("2024XIANYU_huanduan");// 去闲鱼逛一逛
        bizKeyList.add("JINGTAN_FEED_FISH");// 去鲸探喂鱼集福气
        bizKeyList.add("UC_gygzy");// 逛一逛UC浏览器
    }

    @Override
    public String getName() {
        return "庄园";
    }

    private ListModelField.ListJoinCommaToStringModelField wakeUpAtTime;
    private ListModelField.ListJoinCommaToStringModelField sleepAtTime;
    private BooleanModelField feedAnimal;
    private BooleanModelField rewardFriend;
    private BooleanModelField sendBackAnimal;
    private ChoiceModelField sendBackAnimalWay;
    private ChoiceModelField sendBackAnimalType;
    private SelectModelField sendBackAnimalList;
    private ChoiceModelField recallAnimalType;
    private BooleanModelField receiveFarmToolReward;
    private BooleanModelField recordFarmGame;
    private ListModelField.ListJoinCommaToStringModelField farmGameTime;
    private BooleanModelField kitchen;
    private BooleanModelField useSpecialFood;
    private BooleanModelField useNewEggTool;
    private BooleanModelField harvestProduce;
    private BooleanModelField donation;
    private ChoiceModelField donationCount;
    private BooleanModelField answerQuestion;
    private BooleanModelField receiveFarmTaskAward;
    private BooleanModelField useAccelerateTool;
    private SelectModelField feedFriendAnimalList;
    private BooleanModelField notifyFriend;
    private ChoiceModelField notifyFriendType;
    private SelectModelField notifyFriendList;
    private BooleanModelField acceptGift;
    private SelectModelField visitFriendList;
    private BooleanModelField chickenDiary;
    private BooleanModelField enableChouchoule;
    private BooleanModelField listOrnaments;
    private BooleanModelField hireAnimal;
    private ChoiceModelField hireAnimalType;
    private SelectModelField hireAnimalList;
    private BooleanModelField enableDdrawGameCenterAward;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(wakeUpAtTime = new ListModelField.ListJoinCommaToStringModelField("wakeUpAtTime", "小鸡起床时间(关闭:-1)", ListUtil.newArrayList("0555")));
        modelFields.addField(sleepAtTime = new ListModelField.ListJoinCommaToStringModelField("sleepAtTime", "小鸡睡眠时间(关闭:-1)", ListUtil.newArrayList("2000")));
        modelFields.addField(recallAnimalType = new ChoiceModelField("recallAnimalType", "召回小鸡", RecallAnimalType.ALWAYS, RecallAnimalType.nickNames));
        modelFields.addField(rewardFriend = new BooleanModelField("rewardFriend", "打赏好友", false));
        modelFields.addField(feedAnimal = new BooleanModelField("feedAnimal", "自动喂小鸡", false));
        modelFields.addField(feedFriendAnimalList = new SelectModelField("feedFriendAnimalList", "喂小鸡好友列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayUser::getList));
        modelFields.addField(acceptGift = new BooleanModelField("acceptGift", "收麦子", false));
        modelFields.addField(visitFriendList = new SelectModelField("visitFriendList", "送麦子好友列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayUser::getList));
        modelFields.addField(hireAnimal = new BooleanModelField("hireAnimal", "雇佣小鸡 | 开启", false));
        modelFields.addField(hireAnimalType = new ChoiceModelField("hireAnimalType", "雇佣小鸡 | 动作", HireAnimalType.DONT_HIRE, HireAnimalType.nickNames));
        modelFields.addField(hireAnimalList = new SelectModelField("hireAnimalList", "雇佣小鸡 | 好友列表", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser::getList));
        modelFields.addField(sendBackAnimal = new BooleanModelField("sendBackAnimal", "遣返 | 开启", false));
        modelFields.addField(sendBackAnimalWay = new ChoiceModelField("sendBackAnimalWay", "遣返 | 方式", SendBackAnimalWay.NORMAL, SendBackAnimalWay.nickNames));
        modelFields.addField(sendBackAnimalType = new ChoiceModelField("sendBackAnimalType", "遣返 | 动作", SendBackAnimalType.NOT_BACK, SendBackAnimalType.nickNames));
        modelFields.addField(sendBackAnimalList = new SelectModelField("dontSendFriendList", "遣返 | 好友列表", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser::getList));
        modelFields.addField(notifyFriend = new BooleanModelField("notifyFriend", "通知赶鸡 | 开启", false));
        modelFields.addField(notifyFriendType = new ChoiceModelField("notifyFriendType", "通知赶鸡 | 动作", NotifyFriendType.NOTIFY, NotifyFriendType.nickNames));
        modelFields.addField(notifyFriendList = new SelectModelField("notifyFriendList", "通知赶鸡 | 好友列表", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser::getList));
        modelFields.addField(donation = new BooleanModelField("donation", "每日捐蛋 | 开启", false));
        modelFields.addField(donationCount = new ChoiceModelField("donationCount", "每日捐蛋 | 次数", DonationCount.ONE, DonationCount.nickNames));
        modelFields.addField(useAccelerateTool = new BooleanModelField("useAccelerateTool", "使用加速卡", false));
        modelFields.addField(useSpecialFood = new BooleanModelField("useSpecialFood", "使用特殊食品", false));
        modelFields.addField(useNewEggTool = new BooleanModelField("useNewEggTool", "使用新蛋卡", false));
        modelFields.addField(receiveFarmTaskAward = new BooleanModelField("receiveFarmTaskAward", "收取饲料奖励", false));
        modelFields.addField(receiveFarmToolReward = new BooleanModelField("receiveFarmToolReward", "收取道具奖励", false));
        modelFields.addField(harvestProduce = new BooleanModelField("harvestProduce", "收获爱心鸡蛋", false));
        modelFields.addField(kitchen = new BooleanModelField("kitchen", "小鸡厨房", false));
        modelFields.addField(chickenDiary = new BooleanModelField("chickenDiary", "小鸡日记", false));
        modelFields.addField(enableChouchoule = new BooleanModelField("enableChouchoule", "开启小鸡抽抽乐", false));
        modelFields.addField(listOrnaments = new BooleanModelField("listOrnaments", "小鸡每日换装", false));
        modelFields.addField(enableDdrawGameCenterAward = new BooleanModelField("enableDdrawGameCenterAward", "开宝箱", false));
        modelFields.addField(answerQuestion = new BooleanModelField("answerQuestion", "开启答题", false));
        modelFields.addField(recordFarmGame = new BooleanModelField("recordFarmGame", "游戏改分(星星球、登山赛、飞行赛、揍小鸡)", false));
        List<String> farmGameTimeList = new ArrayList<>();
        farmGameTimeList.add("2200-2400");
        modelFields.addField(farmGameTime = new ListModelField.ListJoinCommaToStringModelField("farmGameTime", "小鸡游戏时间(范围)", farmGameTimeList));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (!enterFarm()) {
                return;
            }

            listFarmTool();

            if (rewardFriend.getValue()) {
                rewardFriend();
            }

            if (sendBackAnimal.getValue()) {
                sendBackAnimal();
            }

            if (!AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus)) {
                if ("ORCHARD".equals(ownerAnimal.locationType)) {
                    Log.farm("庄园通知📣[你家的小鸡给拉去除草了！]");
                    JSONObject joRecallAnimal = new JSONObject(AntFarmRpcCall
                            .orchardRecallAnimal(ownerAnimal.animalId, ownerAnimal.currentFarmMasterUserId));
                    int manureCount = joRecallAnimal.getInt("manureCount");
                    Log.farm("召回小鸡📣[收获:肥料" + manureCount + "g]");
                } else {
                    syncAnimalStatusAtOtherFarm(ownerAnimal.currentFarmId);
                    boolean guest = false;
                    switch (SubAnimalType.valueOf(ownerAnimal.subAnimalType)) {
                        case GUEST:
                            guest = true;
                            Log.record("小鸡到好友家去做客了");
                            break;
                        case NORMAL:
                            Log.record("小鸡太饿，离家出走了");
                            break;
                        case PIRATE:
                            Log.record("小鸡外出探险了");
                            break;
                        case WORK:
                            Log.record("小鸡出去工作啦");
                            break;
                        default:
                            Log.record("小鸡不在庄园" + " " + ownerAnimal.subAnimalType);
                    }

                    boolean hungry = false;
                    String userName = UserIdMap
                            .getMaskName(AntFarmRpcCall.farmId2UserId(ownerAnimal.currentFarmId));
                    switch (AnimalFeedStatus.valueOf(ownerAnimal.animalFeedStatus)) {
                        case HUNGRY:
                            hungry = true;
                            Log.record("小鸡在[" + userName + "]的庄园里挨饿");
                            break;

                        case EATING:
                            Log.record("小鸡在[" + userName + "]的庄园里吃得津津有味");
                            break;
                    }

                    boolean recall = false;
                    switch (recallAnimalType.getValue()) {
                        case RecallAnimalType.ALWAYS:
                            recall = true;
                            break;
                        case RecallAnimalType.WHEN_THIEF:
                            recall = !guest;
                            break;
                        case RecallAnimalType.WHEN_HUNGRY:
                            recall = hungry;
                            break;
                    }
                    if (recall) {
                        recallAnimal(ownerAnimal.animalId, ownerAnimal.currentFarmId, ownerFarmId, userName);
                        syncAnimalStatus(ownerFarmId);
                    }
                }

            }

            if (receiveFarmToolReward.getValue()) {
                receiveToolTaskReward();
            }

            if (recordFarmGame.getValue()) {
                long currentTimeMillis = System.currentTimeMillis();
                for (String time : farmGameTime.getValue()) {
                    if (TimeUtil.checkInTimeRange(currentTimeMillis, time)) {
                        recordFarmGame(GameType.starGame);
                        recordFarmGame(GameType.jumpGame);
                        recordFarmGame(GameType.flyGame);
                        recordFarmGame(GameType.hitGame);
                        break;
                    }
                }
            }

            if (kitchen.getValue()) {
                collectDailyFoodMaterial(userId);
                collectDailyLimitedFoodMaterial();
                cook(userId);
            }

            if (chickenDiary.getValue()) {
                queryChickenDiaryList();
            }

            if (useNewEggTool.getValue()) {
                useFarmTool(ownerFarmId, ToolType.NEWEGGTOOL);
                syncAnimalStatus(ownerFarmId);
            }

            if (harvestProduce.getValue() && benevolenceScore >= 1) {
                Log.record("有可收取的爱心鸡蛋");
                harvestProduce(ownerFarmId);
            }

            if (donation.getValue() && Status.canDonationEgg(userId) && harvestBenevolenceScore >= 1) {
                handleDonation(donationCount.getValue());
            }

            if (answerQuestion.getValue() && Status.canAnswerQuestionToday(UserIdMap.getCurrentUid())) {
                answerQuestion();
            }

            if (receiveFarmTaskAward.getValue()) {
                doFarmDailyTask();
                receiveFarmTaskAward();
            }

            if (AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus)) {
                boolean needReload = false;
                if (feedAnimal.getValue() && AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus)) {
                    Log.record("小鸡在挨饿");
                    if (feedAnimal(ownerFarmId)) {
                        needReload = true;
                    }
                }

                if (AnimalBuff.ACCELERATING.name().equals(ownerAnimal.animalBuff)) {
                    Log.record("小鸡在加速吃饭");
                } else if (useAccelerateTool.getValue() && !AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus)) {
                    // 加速卡
                    if (useFarmTool(ownerFarmId, ToolType.ACCELERATETOOL)) {
                        needReload = true;
                    }
                }

                // 小鸡换装
                if (listOrnaments.getValue() && Status.canOrnamentToday()) {
                    listOrnaments();
                }

                if (needReload) {
                    enterFarm();
                }

                if (feedAnimal.getValue()) {
                    try {
                        Long startEatTime = ownerAnimal.startEatTime;
                        double allFoodHaveEatten = 0d;
                        double allConsumeSpeed = 0d;
                        for (Animal animal : animals) {
                            allFoodHaveEatten += animal.foodHaveEatten;
                            allConsumeSpeed += animal.consumeSpeed;
                        }
                        long nextFeedTime = startEatTime + (long) ((180 - (allFoodHaveEatten)) / (allConsumeSpeed)) * 1000;
                        String taskId = "FA|" + ownerFarmId;
                        if (!hasChildTask(taskId)) {
                            addChildTask(new ChildModelTask(taskId, "FA", () -> feedAnimal(ownerFarmId), nextFeedTime));
                            Log.record("添加蹲点投喂🥣[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(nextFeedTime) + "]执行");
                        } else {
                            addChildTask(new ChildModelTask(taskId, "FA", () -> feedAnimal(ownerFarmId), nextFeedTime));
                        }
                    } catch (Exception e) {
                        Log.printStackTrace(e);
                    }
                }

                if (unreceiveTaskAward > 0) {
                    Log.record("还有待领取的饲料");
                    receiveFarmTaskAward();
                }

            }

            // 到访小鸡送礼
            visitAnimal();

            // 送麦子
            visit();

            // 帮好友喂鸡
            feedFriend();

            // 通知好友赶鸡
            if (notifyFriend.getValue()) {
                notifyFriend();
            }

            // 抽抽乐
            if (enableChouchoule.getValue()) {
                chouchoule();
            }

            // 雇佣小鸡
            if (hireAnimal.getValue()) {
                hireAnimal();
            }

            // 开宝箱
            if (enableDdrawGameCenterAward.getValue()) {
                drawGameCenterAward();
            }

            //小鸡起床
            List<String> animalWakeUpTimes = wakeUpAtTime.getValue();
            if (animalWakeUpTimes != null && !animalWakeUpTimes.isEmpty()) {
                animalWake(animalWakeUpTimes);
            }

            //小鸡睡觉
            List<String> animalSleepTimes = sleepAtTime.getValue();
            if (animalSleepTimes != null && !animalSleepTimes.isEmpty()) {
                animalSleep(animalSleepTimes);
            }

        } catch (Throwable t) {
            Log.i(TAG, "AntFarm.start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void animalWake(List<String> animalWakeUpTimes) {
        for (String animalWakeUpTime : animalWakeUpTimes) {
            Calendar animalWakeUpTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(animalWakeUpTime);
            if (animalWakeUpTimeCalendar != null && animalWakeUpTimeCalendar.compareTo(TimeUtil.getNow()) > 0) {
                long animalWakeUpTimeInMillis = animalWakeUpTimeCalendar.getTimeInMillis();
                String taskId = "AW|" + animalWakeUpTime;
                if (!hasChildTask(taskId)) {
                    addChildTask(new ChildModelTask(taskId, "AW", this::animalWakeUpNow, animalWakeUpTimeInMillis));
                    Log.record("添加定时起床\uD83D\uDD06[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(animalWakeUpTimeInMillis) + "]执行");
                } else {
                    addChildTask(new ChildModelTask(taskId, "AW", this::animalWakeUpNow, animalWakeUpTimeInMillis));
                }
            }
        }
    }

    private void animalSleep(List<String> animalSleepTimes) {
        for (String animalSleepTime : animalSleepTimes) {
            Calendar animalSleepTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(animalSleepTime);
            if (animalSleepTimeCalendar != null && animalSleepTimeCalendar.compareTo(TimeUtil.getNow()) > 0) {
                long animalSleepTimeInMillis = animalSleepTimeCalendar.getTimeInMillis();
                String taskId = "AS|" + animalSleepTime;
                if (!hasChildTask(taskId)) {
                    addChildTask(new ChildModelTask(taskId, "AS", this::animalSleepNow, animalSleepTimeInMillis));
                    Log.record("添加定时睡觉🛌[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(animalSleepTimeInMillis) + "]执行");
                } else {
                    addChildTask(new ChildModelTask(taskId, "AS", this::animalSleepNow, animalSleepTimeInMillis));
                }
            }
        }
    }

    private Boolean enterFarm() {
        try {
            String s = AntFarmRpcCall.enterFarm("", UserIdMap.getCurrentUid());
            if (s == null) {
                throw new RuntimeException("庄园加载失败");
            }
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                rewardProductNum = jo.getJSONObject("dynamicGlobalConfig").getString("rewardProductNum");
                JSONObject joFarmVO = jo.getJSONObject("farmVO");
                foodStock = joFarmVO.getInt("foodStock");
                foodStockLimit = joFarmVO.getInt("foodStockLimit");
                harvestBenevolenceScore = joFarmVO.getDouble("harvestBenevolenceScore");
                parseSyncAnimalStatusResponse(joFarmVO.toString());
                userId = joFarmVO.getJSONObject("masterUserInfoVO").getString("userId");

                if (useSpecialFood.getValue()) {
                    JSONArray cuisineList = jo.getJSONArray("cuisineList");
                    if (!AnimalFeedStatus.SLEEPY.name().equals(ownerAnimal.animalFeedStatus))
                        useFarmFood(cuisineList);
                }

                if (jo.has("lotteryPlusInfo")) {
                    drawLotteryPlus(jo.getJSONObject("lotteryPlusInfo"));
                }
                if (acceptGift.getValue() && joFarmVO.getJSONObject("subFarmVO").has("giftRecord")
                        && foodStockLimit - foodStock >= 10) {
                    acceptGift();
                }
                return true;
            } else {
                Log.record(s);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        } finally {
            TimeUtil.sleep(1000);
        }
        return false;
    }

    private void animalWakeUpNow() {
        try {
            String s = AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid());
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONObject sleepNotifyInfo = jo.getJSONObject("sleepNotifyInfo");
                if (!sleepNotifyInfo.optBoolean("canSleep", true)) {
                    s = AntFarmRpcCall.wakeUp();
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        Log.farm("小鸡起床\uD83D\uDD06");
                    }
                } else {
                    Log.farm("小鸡已经起床\uD83D\uDD06");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "animalWakeUpNow err:");
            Log.printStackTrace(t);
        }
    }

    private void animalSleepNow() {
        try {
            String s = AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid());
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONObject sleepNotifyInfo = jo.getJSONObject("sleepNotifyInfo");
                if (sleepNotifyInfo.optBoolean("canSleep", false)) {
                    s = AntFarmRpcCall.sleep();
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        Log.farm("小鸡睡觉🛌");
                    }
                } else {
                    Log.farm("小鸡当前无法睡觉🛌");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "animalSleepNow err:");
            Log.printStackTrace(t);
        }
    }

    private void syncAnimalStatus(String farmId) {
        try {
            String s = AntFarmRpcCall.syncAnimalStatus(farmId);
            parseSyncAnimalStatusResponse(s);
        } catch (Throwable t) {
            Log.i(TAG, "syncAnimalStatus err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void syncAnimalStatusAtOtherFarm(String farmId) {
        try {
            String s = AntFarmRpcCall.enterFarm(farmId, "");
            JSONObject jo = new JSONObject(s);
            jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
            JSONArray jaAnimals = jo.getJSONArray("animals");
            for (int i = 0; i < jaAnimals.length(); i++) {
                jo = jaAnimals.getJSONObject(i);
                if (jo.getString("masterFarmId").equals(ownerFarmId)) {
                    Animal newOwnerAnimal = new Animal();
                    JSONObject animal = jaAnimals.getJSONObject(i);
                    newOwnerAnimal.animalId = animal.getString("animalId");
                    newOwnerAnimal.currentFarmId = animal.getString("currentFarmId");
                    newOwnerAnimal.currentFarmMasterUserId = animal.getString("currentFarmMasterUserId");
                    newOwnerAnimal.masterFarmId = ownerFarmId;
                    newOwnerAnimal.animalBuff = animal.getString("animalBuff");
                    newOwnerAnimal.locationType = animal.optString("locationType", "");
                    newOwnerAnimal.subAnimalType = animal.getString("subAnimalType");
                    animal = animal.getJSONObject("animalStatusVO");
                    newOwnerAnimal.animalFeedStatus = animal.getString("animalFeedStatus");
                    newOwnerAnimal.animalInteractStatus = animal.getString("animalInteractStatus");
                    ownerAnimal = newOwnerAnimal;
                    break;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "syncAnimalStatusAtOtherFarm err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void rewardFriend() {
        try {
            if (rewardList != null) {
                for (RewardFriend rewardFriend : rewardList) {
                    String s = AntFarmRpcCall.rewardFriend(rewardFriend.consistencyKey, rewardFriend.friendId,
                            rewardProductNum, rewardFriend.time);
                    JSONObject jo = new JSONObject(s);
                    String memo = jo.getString("memo");
                    if ("SUCCESS".equals(memo)) {
                        double rewardCount = benevolenceScore - jo.getDouble("farmProduct");
                        benevolenceScore -= rewardCount;
                        Log.farm("打赏好友💰[" + UserIdMap.getMaskName(rewardFriend.friendId) + "]#得" + rewardCount
                                + "颗爱心鸡蛋");
                    } else {
                        Log.record(memo);
                        Log.i(s);
                    }
                }
                rewardList = null;
            }
        } catch (Throwable t) {
            Log.i(TAG, "rewardFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void recallAnimal(String animalId, String currentFarmId, String masterFarmId, String user) {
        try {
            String s = AntFarmRpcCall.recallAnimal(animalId, currentFarmId, masterFarmId);
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                double foodHaveStolen = jo.getDouble("foodHaveStolen");
                Log.farm("召回小鸡📣，偷吃[" + user + "]#" + foodHaveStolen + "g");
                // 这里不需要加
                // add2FoodStock((int)foodHaveStolen);
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "recallAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void sendBackAnimal() {
        if (animals == null) {
            return;
        }
        try {
            for (Animal animal : animals) {
                if (AnimalInteractStatus.STEALING.name().equals(animal.animalInteractStatus)
                        && !SubAnimalType.GUEST.name().equals(animal.subAnimalType)
                        && !SubAnimalType.WORK.name().equals(animal.subAnimalType)) {
                    // 赶鸡
                    String user = AntFarmRpcCall.farmId2UserId(animal.masterFarmId);
                    boolean isSendBackAnimal = sendBackAnimalList.getValue().getKey().containsKey(user);
                    if (sendBackAnimalType.getValue() == SendBackAnimalType.BACK) {
                        isSendBackAnimal = !isSendBackAnimal;
                    }
                    if (isSendBackAnimal) {
                        continue;
                    }
                    int sendTypeInt = sendBackAnimalWay.getValue();
                    user = UserIdMap.getMaskName(user);
                    String s = AntFarmRpcCall.sendBackAnimal(
                            SendBackAnimalWay.nickNames[sendTypeInt], animal.animalId,
                            animal.currentFarmId, animal.masterFarmId);
                    JSONObject jo = new JSONObject(s);
                    String memo = jo.getString("memo");
                    if ("SUCCESS".equals(memo)) {
                        if (sendTypeInt == SendBackAnimalWay.HIT) {
                            if (jo.has("hitLossFood")) {
                                s = "胖揍小鸡🤺[" + user + "]，掉落[" + jo.getInt("hitLossFood") + "g]";
                                if (jo.has("finalFoodStorage"))
                                    foodStock = jo.getInt("finalFoodStorage");
                            } else
                                s = "[" + user + "]的小鸡躲开了攻击";
                        } else {
                            s = "驱赶小鸡🧶[" + user + "]";
                        }
                        Log.farm(s);
                    } else {
                        Log.record(memo);
                        Log.i(s);
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "sendBackAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveToolTaskReward() {
        try {
            String s = AntFarmRpcCall.listToolTaskDetails();
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONArray jaList = jo.getJSONArray("list");
                for (int i = 0; i < jaList.length(); i++) {
                    JSONObject joItem = jaList.getJSONObject(i);
                    if (joItem.has("taskStatus")
                            && TaskStatus.FINISHED.name().equals(joItem.getString("taskStatus"))) {
                        JSONObject bizInfo = new JSONObject(joItem.getString("bizInfo"));
                        String awardType = bizInfo.getString("awardType");
                        ToolType toolType = ToolType.valueOf(awardType);
                        boolean isFull = false;
                        for (FarmTool farmTool : farmTools) {
                            if (farmTool.toolType == toolType) {
                                if (farmTool.toolCount == farmTool.toolHoldLimit) {
                                    isFull = true;
                                }
                                break;
                            }
                        }
                        if (isFull) {
                            Log.record("领取道具🎖️[" + toolType.nickName() + "]#已满，暂不领取");
                            continue;
                        }
                        int awardCount = bizInfo.getInt("awardCount");
                        String taskType = joItem.getString("taskType");
                        String taskTitle = bizInfo.getString("taskTitle");
                        s = AntFarmRpcCall.receiveToolTaskReward(awardType, awardCount, taskType);
                        jo = new JSONObject(s);
                        memo = jo.getString("memo");
                        if ("SUCCESS".equals(memo)) {
                            Log.farm("领取道具🎖️[" + taskTitle + "-" + toolType.nickName() + "]#" + awardCount + "张");
                        } else {
                            memo = memo.replace("道具", toolType.nickName());
                            Log.record(memo);
                            Log.i(s);
                        }
                    }
                }
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveToolTaskReward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void harvestProduce(String farmId) {
        try {
            String s = AntFarmRpcCall.harvestProduce(farmId);
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                double harvest = jo.getDouble("harvestBenevolenceScore");
                harvestBenevolenceScore = jo.getDouble("finalBenevolenceScore");
                Log.farm("收取鸡蛋🥚[" + harvest + "颗]#剩余" + harvestBenevolenceScore + "颗");
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "harvestProduce err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 捐赠爱心鸡蛋 */
    private void handleDonation(int donationType) {
        try {
            String s = AntFarmRpcCall.listActivityInfo();
            TimeUtil.sleep(1000);
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONArray jaActivityInfos = jo.getJSONArray("activityInfos");
                String activityId = null, activityName = null;
                boolean isDonation = false;
                for (int i = 0; i < jaActivityInfos.length(); i++) {
                    jo = jaActivityInfos.getJSONObject(i);
                    if (!jo.get("donationTotal").equals(jo.get("donationLimit"))) {
                        activityId = jo.getString("activityId");
                        activityName = jo.optString("projectName", activityId);
                        if (performDonation(activityId, activityName)) {
                            isDonation = true;
                            if (donationType == DonationCount.ONE) {
                                break;
                            }
                        }
                    }
                }
                if (isDonation) {
                    Status.donationEgg(userId);
                }
                if (activityId == null) {
                    Log.record("今日已无可捐赠的活动");
                }
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "donation err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean performDonation(String activityId, String activityName) throws JSONException {
        try {
            String s = AntFarmRpcCall.donation(activityId, 1);
            TimeUtil.sleep(1000);
            JSONObject donationResponse = new JSONObject(s);
            String memo = donationResponse.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONObject donationDetails = donationResponse.getJSONObject("donation");
                harvestBenevolenceScore = donationDetails.getDouble("harvestBenevolenceScore");
                Log.farm("捐赠活动❤️[" + activityName + "]#累计捐赠" + donationDetails.getInt("donationTimesStat") + "次");
                return true;
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(t);
        }
        return false;
    }

    private void answerQuestion() {
        try {
            String s = AntFarmRpcCall.listFarmTask();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONArray jaFarmTaskList = jo.getJSONArray("farmTaskList");
                for (int i = 0; i < jaFarmTaskList.length(); i++) {
                    jo = jaFarmTaskList.getJSONObject(i);
                    if ("庄园小课堂".equals(jo.getString("title"))) {
                        switch (TaskStatus.valueOf((jo.getString("taskStatus")))) {
                            case TODO:
                                s = DadaDailyRpcCall.home("100");
                                jo = new JSONObject(s);
                                if (jo.getBoolean("success")) {
                                    JSONObject question = jo.getJSONObject("question");
                                    Log.i("题目:" + question, "");
                                    long questionId = question.getLong("questionId");
                                    JSONArray labels = question.getJSONArray("label");
                                    String answer = null;
                                    String anotherAnswer = null;
                                    boolean existsResult = false;
                                    Set<String> dadaDailySet = Status.getDadaDailySet();
                                    if (dadaDailySet.contains(TimeUtil.getDateStr() + labels.getString(0))) {
                                        answer = labels.getString(0);
                                        anotherAnswer = labels.getString(1);
                                        existsResult = true;
                                    } else if (dadaDailySet.contains(TimeUtil.getDateStr() + labels.getString(1))) {
                                        answer = labels.getString(1);
                                        anotherAnswer = labels.getString(0);
                                        existsResult = true;
                                    }
                                    if (!existsResult) {
                                        answer = AnswerAI.getAnswer(question.getString("title"), JsonUtil.jsonArrayToList(labels));
                                        if (answer == null || answer.isEmpty()) {
                                            answer = labels.getString(0);
                                        }
                                        anotherAnswer = labels.getString(1);
                                    }

                                    s = DadaDailyRpcCall.submit("100", answer, questionId);
                                    JSONObject joDailySubmit = new JSONObject(s);
                                    if (joDailySubmit.getBoolean("success")) {
                                        Log.record("提交完成");
                                        dadaDailySet = new HashSet<>();
                                        JSONObject extInfo = joDailySubmit.getJSONObject("extInfo");
                                        boolean correct = joDailySubmit.getBoolean("correct");
                                        if (!correct || !existsResult) {
                                            dadaDailySet.add(TimeUtil.getDateStr() + anotherAnswer);
                                        } else {
                                            dadaDailySet.add(TimeUtil.getDateStr() + answer);
                                        }
                                        Log.record("答题" + (correct ? "正确" : "错误") + "可领取［"
                                                + extInfo.getString("award") + "克］");
                                        Status.answerQuestionToday(UserIdMap.getCurrentUid());

                                        JSONArray operationConfigList = joDailySubmit
                                                .getJSONArray("operationConfigList");
                                        for (int j = 0; j < operationConfigList.length(); j++) {
                                            JSONObject operationConfig = operationConfigList.getJSONObject(j);
                                            if ("PREVIEW_QUESTION".equals(operationConfig.getString("type"))) {
                                                JSONArray actionTitle = new JSONArray(
                                                        operationConfig.getString("actionTitle"));
                                                for (int k = 0; k < actionTitle.length(); k++) {
                                                    JSONObject joActionTitle = actionTitle.getJSONObject(k);
                                                    if (joActionTitle.getBoolean("correct")) {
                                                        dadaDailySet.add(TimeUtil.getDateStr(1)
                                                                + joActionTitle.getString("title"));
                                                    }
                                                }
                                            }
                                        }
                                        Status.setDadaDailySet(dadaDailySet);
                                    } else {
                                        Log.i(s);
                                    }
                                    return;
                                } else {
                                    Log.i(s);
                                }
                                break;

                            case RECEIVED:
                                Log.record("今日答题已完成");
                                Status.answerQuestionToday(UserIdMap.getCurrentUid());
                                break;

                            case FINISHED:
                                Log.record("已经答过题了，饲料待领取");
                                Status.answerQuestionToday(UserIdMap.getCurrentUid());
                                break;
                        }
                        break;
                    }
                }
            } else {
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "answerQuestion err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void recordFarmGame(GameType gameType) {
        try {
            do {
                try {
                    JSONObject jo = new JSONObject(AntFarmRpcCall.initFarmGame(gameType.name()));
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        if (jo.getJSONObject("gameAward").getBoolean("level3Get")) {
                            return;
                        }
                        if (jo.optInt("remainingGameCount", 1) == 0) {
                            return;
                        }
                        jo = new JSONObject(AntFarmRpcCall.recordFarmGame(gameType.name()));
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            JSONArray awardInfos = jo.getJSONArray("awardInfos");
                            StringBuilder award = new StringBuilder();
                            for (int i = 0; i < awardInfos.length(); i++) {
                                JSONObject awardInfo = awardInfos.getJSONObject(i);
                                award.append(awardInfo.getString("awardName")).append("*").append(awardInfo.getInt("awardCount"));
                            }
                            if (jo.has("receiveFoodCount")) {
                                award.append(";肥料*").append(jo.getString("receiveFoodCount"));
                            }
                            Log.farm("庄园游戏🎮[" + gameType.gameName() + "]#" + award);
                            if (jo.optInt("remainingGameCount", 0) > 0) {
                                continue;
                            }
                        } else {
                            Log.i(TAG, jo.toString());
                        }
                    } else {
                        Log.i(TAG, jo.toString());
                    }
                    break;
                } finally {
                    TimeUtil.sleep(2000);
                }
            } while (true);
        } catch (Throwable t) {
            Log.i(TAG, "recordFarmGame err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void doFarmDailyTask() {
        try {
            String s = AntFarmRpcCall.listFarmTask();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONArray jaFarmTaskList = jo.getJSONArray("farmTaskList");
                for (int i = 0; i < jaFarmTaskList.length(); i++) {
                    jo = jaFarmTaskList.getJSONObject(i);
                    String title = null;
                    if (jo.has("title"))
                        title = jo.getString("title");
                    if ("TODO".equals(jo.getString("taskStatus"))) {
                        int awardCount = jo.optInt("awardCount");
                        String bizKey = jo.getString("bizKey");
                        if ("VIEW".equals(jo.optString("taskMode")) || bizKeyList.contains(bizKey)) {
                            jo = new JSONObject(AntFarmRpcCall.doFarmTask(bizKey));
                            if ("SUCCESS".equals(jo.getString("memo"))) {
                                Log.farm("庄园任务🧾[" + title + "]#获得饲料" + jo.optString("awardCount") + "g");
                            } else {
                                Log.record(jo.getString("memo"));
                                Log.i(jo.toString());
                            }
                        } else if ("庄园小视频".equals(title)) {
                            jo = new JSONObject(AntFarmRpcCall.queryTabVideoUrl());
                            if ("SUCCESS".equals(jo.getString("memo"))) {
                                String videoUrl = jo.getString("videoUrl");
                                String contentId = videoUrl.substring(videoUrl.indexOf("&contentId=") + 1,
                                        videoUrl.indexOf("&refer"));
                                jo = new JSONObject(AntFarmRpcCall.videoDeliverModule(contentId));
                                if (jo.getBoolean("success")) {
                                    Thread.sleep(15100);
                                    jo = new JSONObject(AntFarmRpcCall.videoTrigger(contentId));
                                    if (jo.getBoolean("success")) {
                                        Log.farm("庄园任务🧾[" + title + "]#获得饲料" + awardCount + "g");
                                    } else {
                                        Log.record(jo.getString("resultMsg"));
                                        Log.i(jo.toString());
                                    }
                                } else {
                                    Log.record(jo.getString("resultMsg"));
                                    Log.i(jo.toString());
                                }
                            } else {
                                Log.record(jo.getString("memo"));
                                Log.i(jo.toString());
                            }
                        }
                    }
                }
            } else {
                Log.record(jo.getString("memo"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "doFarmDailyTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveFarmTaskAward() {
        try {
            String s = AntFarmRpcCall.listFarmTask();
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONObject signList = jo.getJSONObject("signList");
                sign(signList);
                Thread.sleep(1000);
                JSONArray jaFarmTaskList = jo.getJSONArray("farmTaskList");
                for (int i = 0; i < jaFarmTaskList.length(); i++) {
                    jo = jaFarmTaskList.getJSONObject(i);
                    String taskTitle = null;
                    if (jo.has("title"))
                        taskTitle = jo.getString("title");
                    switch (TaskStatus.valueOf(jo.getString("taskStatus"))) {
                        case TODO:
                            break;
                        case FINISHED:
                            int awardCount = jo.getInt("awardCount");
                            if (Objects.equals(jo.optString("awardType"), "ALLPURPOSE")) {
                                if (awardCount + foodStock > foodStockLimit) {
                                    unreceiveTaskAward++;
                                    //Log.record("领取" + awardCount + "克饲料后将超过[" + foodStockLimit + "克]上限，终止领取");
                                    continue;
                                }
                            }
                            s = AntFarmRpcCall.receiveFarmTaskAward(jo.getString("taskId"));
                            Thread.sleep(1000);
                            jo = new JSONObject(s);
                            memo = jo.getString("memo");
                            if ("SUCCESS".equals(memo)) {
                                if (jo.has("foodStock")) {
                                    add2FoodStock(awardCount);
                                    Log.farm("领取奖励🎖️[" + taskTitle + "]#" + awardCount + "g");
                                }
                                if (unreceiveTaskAward > 0)
                                    unreceiveTaskAward--;
                            } else {
                                Log.record(memo);
                                Log.i(s);
                            }
                            break;
                        case RECEIVED:
                            break;
                    }
                }
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveFarmTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void sign(JSONObject signList) {
        try {
            JSONArray jaFarmsignList = signList.getJSONArray("signList");
            boolean signed = true;
            int awardCount = 0;
            for (int i = 0; i < jaFarmsignList.length(); i++) {
                JSONObject jo = jaFarmsignList.getJSONObject(i);
                if (Log.getFormatDate().equals(jo.getString("signKey"))) {
                    signed = jo.getBoolean("signed");
                    awardCount = jo.getInt("awardCount");
                    break;
                }
            }
            if (!signed) {
                JSONObject joSign = new JSONObject(AntFarmRpcCall.sign());
                if ("SUCCESS".equals(joSign.getString("memo"))) {
                    Log.farm("庄园签到📅获得饲料" + awardCount + "g");
                } else {
                    Log.i(TAG, joSign.toString());
                }
            } else {
                Log.record("庄园今日已签到");
            }
        } catch (Throwable t) {
            Log.i(TAG, "Farmsign err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean feedAnimal(String farmId) {
        try {
            if (foodStock < 180) {
                Log.record("喂鸡饲料不足");
            } else {
                String s = AntFarmRpcCall.feedAnimal(farmId);
                TimeUtil.sleep(1000);
                JSONObject jo = new JSONObject(s);
                String memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    int feedFood = foodStock - jo.getInt("foodStock");
                    add2FoodStock(-feedFood);
                    Log.farm("投喂小鸡🥣[" + feedFood + "g]#剩余" + foodStock + "g");
                    return true;
                } else {
                    Log.record(memo);
                    Log.i(s);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "feedAnimal err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void listFarmTool() {
        try {
            String s = AntFarmRpcCall.listFarmTool();
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONArray jaToolList = jo.getJSONArray("toolList");
                farmTools = new FarmTool[jaToolList.length()];
                for (int i = 0; i < jaToolList.length(); i++) {
                    jo = jaToolList.getJSONObject(i);
                    farmTools[i] = new FarmTool();
                    farmTools[i].toolId = jo.optString("toolId", "");
                    farmTools[i].toolType = ToolType.valueOf(jo.getString("toolType"));
                    farmTools[i].toolCount = jo.getInt("toolCount");
                    farmTools[i].toolHoldLimit = jo.optInt("toolHoldLimit", 20);
                }
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "listFarmTool err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean useFarmTool(String targetFarmId, ToolType toolType) {
        try {
            String s = AntFarmRpcCall.listFarmTool();
            JSONObject jo = new JSONObject(s);
            String memo = jo.getString("memo");
            if ("SUCCESS".equals(memo)) {
                JSONArray jaToolList = jo.getJSONArray("toolList");
                for (int i = 0; i < jaToolList.length(); i++) {
                    jo = jaToolList.getJSONObject(i);
                    if (toolType.name().equals(jo.getString("toolType"))) {
                        int toolCount = jo.getInt("toolCount");
                        if (toolCount > 0) {
                            String toolId = "";
                            if (jo.has("toolId"))
                                toolId = jo.getString("toolId");
                            s = AntFarmRpcCall.useFarmTool(targetFarmId, toolId, toolType.name());
                            jo = new JSONObject(s);
                            memo = jo.getString("memo");
                            if ("SUCCESS".equals(memo)) {
                                Log.farm("使用道具🎭[" + toolType.nickName() + "]#剩余" + (toolCount - 1) + "张");
                                return true;
                            } else {
                                Log.record(memo);
                            }
                            Log.i(s);
                        }
                        break;
                    }
                }
            } else {
                Log.record(memo);
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "useFarmTool err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void feedFriend() {
        try {
            String s, memo;
            JSONObject jo;
            Map<String, Integer> feedFriendAnimalMap = feedFriendAnimalList.getValue().getKey();
            for (Map.Entry<String, Integer> entry : feedFriendAnimalMap.entrySet()) {
                String userId = entry.getKey();
                if (userId.equals(UserIdMap.getCurrentUid()))
                    continue;
                if (!Status.canFeedFriendToday(userId, entry.getValue()))
                    continue;
                s = AntFarmRpcCall.enterFarm("", userId);
                jo = new JSONObject(s);
                memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
                    String friendFarmId = jo.getString("farmId");
                    JSONArray jaAnimals = jo.getJSONArray("animals");
                    for (int j = 0; j < jaAnimals.length(); j++) {
                        jo = jaAnimals.getJSONObject(j);
                        String masterFarmId = jo.getString("masterFarmId");
                        if (masterFarmId.equals(friendFarmId)) {
                            jo = jo.getJSONObject("animalStatusVO");
                            if (AnimalInteractStatus.HOME.name().equals(jo.getString("animalInteractStatus"))
                                    && AnimalFeedStatus.HUNGRY.name().equals(jo.getString("animalFeedStatus"))) {
                                feedFriendAnimal(friendFarmId, UserIdMap.getMaskName(userId));
                            }
                            break;
                        }
                    }
                } else {
                    Log.record(memo);
                    Log.i(s);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "feedFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void feedFriendAnimal(String friendFarmId, String user) {
        try {
            Log.record("[" + user + "]的小鸡在挨饿");
            if (foodStock < 180) {
                Log.record("喂鸡饲料不足");
                if (unreceiveTaskAward > 0) {
                    Log.record("还有待领取的饲料");
                    receiveFarmTaskAward();
                }
            }
            if (foodStock >= 180) {
                String s = AntFarmRpcCall.feedFriendAnimal(friendFarmId);
                JSONObject jo = new JSONObject(s);
                String memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    int feedFood = foodStock - jo.getInt("foodStock");
                    if (feedFood > 0) {
                        add2FoodStock(-feedFood);
                        Log.farm("帮喂好友🥣[" + user + "]的小鸡[" + feedFood + "g]#剩余" + foodStock + "g");
                        Status.feedFriendToday(AntFarmRpcCall.farmId2UserId(friendFarmId));
                    }
                } else {
                    Log.record(memo);
                    Log.i(s);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "feedFriendAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void notifyFriend() {
        if (foodStock >= foodStockLimit)
            return;
        try {
            boolean hasNext = false;
            int pageStartSum = 0;
            String s;
            JSONObject jo;
            do {
                s = AntFarmRpcCall.rankingList(pageStartSum);
                jo = new JSONObject(s);
                String memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    hasNext = jo.getBoolean("hasNext");
                    JSONArray jaRankingList = jo.getJSONArray("rankingList");
                    pageStartSum += jaRankingList.length();
                    for (int i = 0; i < jaRankingList.length(); i++) {
                        jo = jaRankingList.getJSONObject(i);
                        String userId = jo.getString("userId");
                        String userName = UserIdMap.getMaskName(userId);
                        boolean isNotifyFriend = notifyFriendList.getValue().getKey().containsKey(userId);
                        if (notifyFriendType.getValue() == NotifyFriendType.DONT_NOTIFY) {
                            isNotifyFriend = !isNotifyFriend;
                        }
                        if (!isNotifyFriend || userId.equals(UserIdMap.getCurrentUid())) {
                            continue;
                        }
                        boolean starve = jo.has("actionType") && "starve_action".equals(jo.getString("actionType"));
                        if (jo.getBoolean("stealingAnimal") && !starve) {
                            s = AntFarmRpcCall.enterFarm("", userId);
                            jo = new JSONObject(s);
                            memo = jo.getString("memo");
                            if ("SUCCESS".equals(memo)) {
                                jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
                                String friendFarmId = jo.getString("farmId");
                                JSONArray jaAnimals = jo.getJSONArray("animals");
                                boolean notified = !notifyFriend.getValue();
                                for (int j = 0; j < jaAnimals.length(); j++) {
                                    jo = jaAnimals.getJSONObject(j);
                                    String animalId = jo.getString("animalId");
                                    String masterFarmId = jo.getString("masterFarmId");
                                    if (!masterFarmId.equals(friendFarmId) && !masterFarmId.equals(ownerFarmId)) {
                                        if (notified)
                                            continue;
                                        jo = jo.getJSONObject("animalStatusVO");
                                        notified = notifyFriend(jo, friendFarmId, animalId, userName);
                                    }
                                }
                            } else {
                                Log.record(memo);
                                Log.i(s);
                            }
                        }
                    }
                } else {
                    Log.record(memo);
                    Log.i(s);
                }
            } while (hasNext);
            Log.record("饲料剩余[" + foodStock + "g]");
        } catch (Throwable t) {
            Log.i(TAG, "notifyFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private boolean notifyFriend(JSONObject joAnimalStatusVO, String friendFarmId, String animalId,
                                 String user) {
        try {
            if (AnimalInteractStatus.STEALING.name().equals(joAnimalStatusVO.getString("animalInteractStatus"))
                    && AnimalFeedStatus.EATING.name().equals(joAnimalStatusVO.getString("animalFeedStatus"))) {
                String s = AntFarmRpcCall.notifyFriend(animalId, friendFarmId);
                JSONObject jo = new JSONObject(s);
                String memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    double rewardCount = jo.getDouble("rewardCount");
                    if (jo.getBoolean("refreshFoodStock"))
                        foodStock = (int) jo.getDouble("finalFoodStock");
                    else
                        add2FoodStock((int) rewardCount);
                    Log.farm("通知好友📧[" + user + "]被偷吃#奖励" + rewardCount + "g");
                    return true;
                } else {
                    Log.record(memo);
                    Log.i(s);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "notifyFriend err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void parseSyncAnimalStatusResponse(String resp) {
        try {
            JSONObject jo = new JSONObject(resp);
            if (!jo.has("subFarmVO")) {
                return;
            }
            JSONObject subFarmVO = jo.getJSONObject("subFarmVO");
            if (subFarmVO.has("foodStock")) {
                foodStock = subFarmVO.getInt("foodStock");
            }
            if (subFarmVO.has("manureVO")) {
                JSONArray manurePotList = subFarmVO.getJSONObject("manureVO").getJSONArray("manurePotList");
                for (int i = 0; i < manurePotList.length(); i++) {
                    JSONObject manurePot = manurePotList.getJSONObject(i);
                    if (manurePot.getInt("manurePotNum") >= 100) {
                        JSONObject joManurePot = new JSONObject(
                                AntFarmRpcCall.collectManurePot(manurePot.getString("manurePotNO")));
                        if (joManurePot.getBoolean("success")) {
                            int collectManurePotNum = joManurePot.getInt("collectManurePotNum");
                            Log.farm("打扫鸡屎🧹[" + collectManurePotNum + "g]");
                        }
                    }
                }
            }
            ownerFarmId = subFarmVO.getString("farmId");
            JSONObject farmProduce = subFarmVO.getJSONObject("farmProduce");
            benevolenceScore = farmProduce.getDouble("benevolenceScore");
            if (subFarmVO.has("rewardList")) {
                JSONArray jaRewardList = subFarmVO.getJSONArray("rewardList");
                if (jaRewardList.length() > 0) {
                    rewardList = new RewardFriend[jaRewardList.length()];
                    for (int i = 0; i < rewardList.length; i++) {
                        JSONObject joRewardList = jaRewardList.getJSONObject(i);
                        if (rewardList[i] == null)
                            rewardList[i] = new RewardFriend();
                        rewardList[i].consistencyKey = joRewardList.getString("consistencyKey");
                        rewardList[i].friendId = joRewardList.getString("friendId");
                        rewardList[i].time = joRewardList.getString("time");
                    }
                }
            }
            JSONArray jaAnimals = subFarmVO.getJSONArray("animals");
            animals = new Animal[jaAnimals.length()];
            for (int i = 0; i < animals.length; i++) {
                Animal animal = new Animal();
                JSONObject animalJsonObject = jaAnimals.getJSONObject(i);
                animal.animalId = animalJsonObject.getString("animalId");
                animal.currentFarmId = animalJsonObject.getString("currentFarmId");
                animal.masterFarmId = animalJsonObject.getString("masterFarmId");
                animal.animalBuff = animalJsonObject.getString("animalBuff");
                animal.subAnimalType = animalJsonObject.getString("subAnimalType");
                animal.currentFarmMasterUserId = animalJsonObject.getString("currentFarmMasterUserId");
                animal.locationType = animalJsonObject.optString("locationType", "");
                JSONObject animalStatusVO = animalJsonObject.getJSONObject("animalStatusVO");
                animal.animalFeedStatus = animalStatusVO.getString("animalFeedStatus");
                animal.animalInteractStatus = animalStatusVO.getString("animalInteractStatus");
                animal.animalInteractStatus = animalStatusVO.getString("animalInteractStatus");
                animal.startEatTime = animalJsonObject.optLong("startEatTime");
                animal.consumeSpeed = animalJsonObject.optDouble("consumeSpeed");
                animal.foodHaveEatten = animalJsonObject.optDouble("foodHaveEatten");
                if (animal.masterFarmId.equals(ownerFarmId)) {
                    ownerAnimal = animal;
                }
                animals[i] = animal;
            }
        } catch (Throwable t) {
            Log.i(TAG, "parseSyncAnimalStatusResponse err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void add2FoodStock(int i) {
        foodStock += i;
        if (foodStock > foodStockLimit) {
            foodStock = foodStockLimit;
        }
        if (foodStock < 0) {
            foodStock = 0;
        }
    }

    private void collectDailyFoodMaterial(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterKitchen(userId));
            if ("SUCCESS".equals(jo.getString("memo"))) {
                boolean canCollectDailyFoodMaterial = jo.getBoolean("canCollectDailyFoodMaterial");
                int dailyFoodMaterialAmount = jo.getInt("dailyFoodMaterialAmount");
                int garbageAmount = jo.optInt("garbageAmount", 0);
                if (jo.has("orchardFoodMaterialStatus")) {
                    JSONObject orchardFoodMaterialStatus = jo.getJSONObject("orchardFoodMaterialStatus");
                    if ("FINISHED".equals(orchardFoodMaterialStatus.optString("foodStatus"))) {
                        jo = new JSONObject(AntFarmRpcCall.farmFoodMaterialCollect());
                        if ("100".equals(jo.getString("resultCode"))) {
                            Log.farm("小鸡厨房👨🏻‍🍳[领取农场食材]#" + jo.getInt("foodMaterialAddCount") + "g");
                        } else {
                            Log.i(TAG, jo.toString());
                        }
                    }
                }
                if (canCollectDailyFoodMaterial) {
                    jo = new JSONObject(AntFarmRpcCall.collectDailyFoodMaterial(dailyFoodMaterialAmount));
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        Log.farm("小鸡厨房👨🏻‍🍳[领取今日食材]#" + dailyFoodMaterialAmount + "g");
                    } else {
                        Log.i(TAG, jo.toString());
                    }
                }
                if (garbageAmount > 0) {
                    jo = new JSONObject(AntFarmRpcCall.collectKitchenGarbage());
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        Log.farm("小鸡厨房👨🏻‍🍳[领取肥料]#" + jo.getInt("recievedKitchenGarbageAmount") + "g");
                    } else {
                        Log.i(TAG, jo.toString());
                    }
                }
            } else {
                Log.i(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectDailyFoodMaterial err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectDailyLimitedFoodMaterial() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryFoodMaterialPack());
            if ("SUCCESS".equals(jo.getString("memo"))) {
                boolean canCollectDailyLimitedFoodMaterial = jo.getBoolean("canCollectDailyLimitedFoodMaterial");
                if (canCollectDailyLimitedFoodMaterial) {
                    int dailyLimitedFoodMaterialAmount = jo.getInt("dailyLimitedFoodMaterialAmount");
                    jo = new JSONObject(AntFarmRpcCall.collectDailyLimitedFoodMaterial(dailyLimitedFoodMaterialAmount));
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        Log.farm("小鸡厨房👨🏻‍🍳[领取爱心食材店食材]#" + dailyLimitedFoodMaterialAmount + "g");
                    } else {
                        Log.i(TAG, jo.toString());
                    }
                }
            } else {
                Log.i(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectDailyLimitedFoodMaterial err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void cook(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterKitchen(userId));
            if ("SUCCESS".equals(jo.getString("memo"))) {
                int cookTimesAllowed = jo.getInt("cookTimesAllowed");
                if (cookTimesAllowed > 0) {
                    for (int i = 0; i < cookTimesAllowed; i++) {
                        jo = new JSONObject(AntFarmRpcCall.cook(userId));
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            JSONObject cuisineVO = jo.getJSONObject("cuisineVO");
                            Log.farm("小鸡厨房👨🏻‍🍳[" + cuisineVO.getString("name") + "]制作成功");
                        } else {
                            Log.i(TAG, jo.toString());
                        }
                        Thread.sleep(RandomUtil.delay());
                    }
                }
            } else {
                Log.i(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "cook err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void useFarmFood(JSONArray cuisineList) {
        try {
            JSONObject jo = new JSONObject();
            String cookbookId = null;
            String cuisineId = null;
            String name = null;
            for (int i = 0; i < cuisineList.length(); i++) {
                jo = cuisineList.getJSONObject(i);
                if (jo.getInt("count") <= 0)
                    continue;
                cookbookId = jo.getString("cookbookId");
                cuisineId = jo.getString("cuisineId");
                name = jo.getString("name");
                jo = new JSONObject(AntFarmRpcCall.useFarmFood(cookbookId, cuisineId));
                if ("SUCCESS".equals(jo.getString("memo"))) {
                    double deltaProduce = jo.getJSONObject("foodEffect").getDouble("deltaProduce");
                    Log.farm("使用美食🍱[" + name + "]#加速" + deltaProduce + "颗爱心鸡蛋");
                } else {
                    Log.i(TAG, jo.toString());
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "useFarmFood err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void drawLotteryPlus(JSONObject lotteryPlusInfo) {
        try {
            if (!lotteryPlusInfo.has("userSevenDaysGiftsItem"))
                return;
            String itemId = lotteryPlusInfo.getString("itemId");
            JSONObject jo = lotteryPlusInfo.getJSONObject("userSevenDaysGiftsItem");
            JSONArray ja = jo.getJSONArray("userEverydayGiftItems");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (jo.getString("itemId").equals(itemId)) {
                    if (!jo.getBoolean("received")) {
                        String singleDesc = jo.getString("singleDesc");
                        int awardCount = jo.getInt("awardCount");
                        if (singleDesc.contains("饲料") && awardCount + foodStock > foodStockLimit) {
                            Log.record("暂停领取[" + awardCount + "]克饲料，上限为[" + foodStockLimit + "]克");
                            break;
                        }
                        jo = new JSONObject(AntFarmRpcCall.drawLotteryPlus());
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            Log.farm("惊喜礼包🎁[" + singleDesc + "*" + awardCount + "]");
                        } else {
                            Log.i(TAG, jo.getString("memo"));
                        }
                    } else {
                        Log.record("当日奖励已领取");
                    }
                    break;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "drawLotteryPlus err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void visit() {
        try {
            Map<String, Integer> map = visitFriendList.getValue().getKey();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String userId = entry.getKey();
                Integer count = entry.getValue();
                if (userId.equals(UserIdMap.getCurrentUid()))
                    continue;
                if (count <= 0)
                    continue;
                if (count > 3)
                    count = 3;
                if (Status.canVisitFriendToday(userId, count)) {
                    count = visitFriend(userId, count);
                    if (count > 0)
                        Status.visitFriendToday(userId, count);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "visit err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private int visitFriend(String userId, int count) {
        int visitedTimes = 0;
        try {
            String s = AntFarmRpcCall.enterFarm("", userId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONObject farmVO = jo.getJSONObject("farmVO");
                foodStock = farmVO.getInt("foodStock");
                JSONObject subFarmVO = farmVO.getJSONObject("subFarmVO");
                if (subFarmVO.optBoolean("visitedToday", true))
                    return 3;
                String farmId = subFarmVO.getString("farmId");
                for (int i = 0; i < count; i++) {
                    if (foodStock < 10)
                        break;
                    jo = new JSONObject(AntFarmRpcCall.visitFriend(farmId));
                    if ("SUCCESS".equals(jo.getString("memo"))) {
                        foodStock = jo.getInt("foodStock");
                        Log.farm("赠送麦子🌾[" + UserIdMap.getMaskName(userId) + "]#" + jo.getInt("giveFoodNum") + "g");
                        visitedTimes++;
                        if (jo.optBoolean("isReachLimit")) {
                            Log.record("今日给[" + UserIdMap.getMaskName(userId) + "]送麦子已达上限");
                            visitedTimes = 3;
                            break;
                        }
                    } else {
                        Log.record(jo.getString("memo"));
                        Log.i(jo.toString());
                    }
                    Thread.sleep(1000L);
                }
            } else {
                Log.record(jo.getString("memo"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "visitFriend err:");
            Log.printStackTrace(TAG, t);
        }
        return visitedTimes;
    }

    private void acceptGift() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.acceptGift());
            if ("SUCCESS".equals(jo.getString("memo"))) {
                int receiveFoodNum = jo.getInt("receiveFoodNum");
                Log.farm("收取麦子🌾[" + receiveFoodNum + "g]");
            } else {
                Log.i(TAG, jo.toString());
            }

        } catch (Throwable t) {
            Log.i(TAG, "acceptGift err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryChickenDiary(String queryDayStr) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryChickenDiary(queryDayStr));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject chickenDiary = data.getJSONObject("chickenDiary");
                String diaryDateStr = chickenDiary.getString("diaryDateStr");
                if (data.has("hasTietie")) {
                    if (!data.optBoolean("hasTietie", true)) {
                        jo = new JSONObject(AntFarmRpcCall.diaryTietie(diaryDateStr, "NEW"));
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            String prizeType = jo.getString("prizeType");
                            int prizeNum = jo.optInt("prizeNum", 0);
                            Log.farm("贴贴小鸡💞[" + prizeType + "*" + prizeNum + "]");
                        } else {
                            Log.i(jo.getString("memo"), jo.toString());
                        }
                        if (!chickenDiary.has("statisticsList"))
                            return;
                        JSONArray statisticsList = chickenDiary.getJSONArray("statisticsList");
                        if (statisticsList.length() > 0) {
                            for (int i = 0; i < statisticsList.length(); i++) {
                                JSONObject tietieStatus = statisticsList.getJSONObject(i);
                                String tietieRoleId = tietieStatus.getString("tietieRoleId");
                                jo = new JSONObject(AntFarmRpcCall.diaryTietie(diaryDateStr, tietieRoleId));
                                if ("SUCCESS".equals(jo.getString("memo"))) {
                                    String prizeType = jo.getString("prizeType");
                                    int prizeNum = jo.optInt("prizeNum", 0);
                                    Log.farm("贴贴小鸡💞[" + prizeType + "*" + prizeNum + "]");
                                } else {
                                    Log.i(jo.getString("memo"), jo.toString());
                                }
                            }
                        }
                    }
                }
            } else {
                Log.i(jo.getString("resultDesc"), jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryChickenDiary err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryChickenDiaryList() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryChickenDiaryList());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray chickenDiaryBriefList = jo.getJSONObject("data").optJSONArray("chickenDiaryBriefList");
                if (chickenDiaryBriefList != null && chickenDiaryBriefList.length() > 0) {
                    for (int i = 0; i < chickenDiaryBriefList.length(); i++) {
                        jo = chickenDiaryBriefList.getJSONObject(i);
                        if (!jo.optBoolean("read", true)) {
                            String dateStr = jo.getString("dateStr");
                            queryChickenDiary(dateStr);
                            Thread.sleep(300);
                        }
                    }
                }
            } else {
                Log.i(jo.getString("resultDesc"), jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryChickenDiaryList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void visitAnimal() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.visitAnimal());
            if ("SUCCESS".equals(jo.getString("memo"))) {
                if (!jo.has("talkConfigs"))
                    return;
                JSONArray talkConfigs = jo.getJSONArray("talkConfigs");
                JSONArray talkNodes = jo.getJSONArray("talkNodes");
                JSONObject data = talkConfigs.getJSONObject(0);
                String farmId = data.getString("farmId");
                jo = new JSONObject(AntFarmRpcCall.feedFriendAnimalVisit(farmId));
                if ("SUCCESS".equals(jo.getString("memo"))) {
                    for (int i = 0; i < talkNodes.length(); i++) {
                        jo = talkNodes.getJSONObject(i);
                        if (!"FEED".equals(jo.getString("type")))
                            continue;
                        String consistencyKey = jo.getString("consistencyKey");
                        jo = new JSONObject(AntFarmRpcCall.visitAnimalSendPrize(consistencyKey));
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            String prizeName = jo.getString("prizeName");
                            Log.farm("小鸡到访💞[" + prizeName + "]");
                        } else {
                            Log.i(jo.getString("memo"), jo.toString());
                        }
                    }
                } else {
                    Log.i(jo.getString("memo"), jo.toString());
                }
            } else {
                Log.i(jo.getString("resultDesc"), jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "visitAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 抽抽乐 */
    private void chouchoule() {
        boolean doubleCheck;
        do {
            doubleCheck = false;
            try {
                String s = AntFarmRpcCall.chouchouleListFarmTask();
                JSONObject jo = new JSONObject(s);
                if (jo.getBoolean("success")) {
                    JSONArray farmTaskList = jo.getJSONArray("farmTaskList");
                    for (int i = 0; i < farmTaskList.length(); i++) {
                        jo = farmTaskList.getJSONObject(i);
                        String taskStatus = jo.getString("taskStatus");
                        String title = jo.getString("title");
                        String taskId = jo.getString("bizKey");
                        int rightsTimes = jo.optInt("rightsTimes", 0);
                        int rightsTimesLimit = jo.optInt("rightsTimesLimit", 0);
                        if ("FINISHED".equals(taskStatus)) {
                            if (rightsTimes < rightsTimesLimit) {
                                chouchouleDoFarmTask(taskId, title, rightsTimesLimit - rightsTimes);
                            }
                            if (chouchouleReceiveFarmTaskAward(taskId)) {
                                doubleCheck = true;
                            }
                        } else if ("TODO".equals(taskStatus)) {
                            if (chouchouleDoFarmTask(taskId, title, rightsTimesLimit - rightsTimes)) {
                                doubleCheck = true;
                            }
                        }
                    }
                } else {
                    Log.record(jo.getString("memo"));
                    Log.i(s);
                }
            } catch (Throwable t) {
                Log.i(TAG, "chouchoule err:");
                Log.printStackTrace(TAG, t);
            }
        } while (doubleCheck);
        try {
            String s = AntFarmRpcCall.enterDrawMachine();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject userInfo = jo.getJSONObject("userInfo");
                int leftDrawTimes = userInfo.optInt("leftDrawTimes", 0);
                if (leftDrawTimes > 0) {
                    for (int i = 0; i < leftDrawTimes; i++) {
                        jo = new JSONObject(AntFarmRpcCall.DrawPrize());
                        if (jo.getBoolean("success")) {
                            String title = jo.getString("title");
                            int prizeNum = jo.optInt("prizeNum", 0);
                            Log.farm("庄园小鸡🎁[领取:抽抽乐" + title + "*" + prizeNum + "]");
                        }
                        Thread.sleep(1000L);
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "DrawPrize err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean chouchouleDoFarmTask(String bizKey, String name, int times) {
        try {
            for (int i = 0; i < times; i++) {
                String s = AntFarmRpcCall.chouchouleDoFarmTask(bizKey);
                JSONObject jo = new JSONObject(s);
                if (jo.optBoolean("success", false)) {
                    Log.farm("庄园小鸡🧾️[完成:抽抽乐" + name + "]");
                    return true;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "chouchouleDoFarmTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean chouchouleReceiveFarmTaskAward(String taskId) {
        try {
            String s = AntFarmRpcCall.chouchouleReceiveFarmTaskAward(taskId);
            JSONObject jo = new JSONObject(s);
            // Log.other("庄园小鸡🧾️[完成:心愿金" + name + "]" + amount);
            return jo.optBoolean("success", false);
        } catch (Throwable t) {
            Log.i(TAG, "chouchouleReceiveFarmTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    /* 雇佣好友小鸡 */
    private void hireAnimal() {
        JSONArray animals = null;
        try {
            String s = AntFarmRpcCall.enterFarm("", UserIdMap.getCurrentUid());
            JSONObject jsonObject = new JSONObject(s);
            if ("SUCCESS".equals(jsonObject.getString("memo"))) {
                JSONObject farmVO = jsonObject.getJSONObject("farmVO");
                JSONObject subFarmVO = farmVO.getJSONObject("subFarmVO");
                animals = subFarmVO.getJSONArray("animals");
            } else {
                Log.record(jsonObject.getString("memo"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "getAnimalCount err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
        if (animals == null) {
            return;
        }
        try {
            for (int i = 0, len = animals.length(); i < len; i++) {
                JSONObject joo = animals.getJSONObject(i);
                if (Objects.equals(joo.getString("subAnimalType"), "WORK")) {
                    String taskId = "HIRE|" + joo.getString("animalId");
                    long beHiredEndTime = joo.getLong("beHiredEndTime");
                    if (!hasChildTask(taskId)) {
                        addChildTask(new ChildModelTask(taskId, "HIRE", () -> {
                            if (hireAnimal.getValue()) {
                                hireAnimal();
                            }
                        }, beHiredEndTime));
                        Log.record("添加蹲点雇佣👷在[" + TimeUtil.getCommonDate(beHiredEndTime) + "]执行");
                    } else {
                        addChildTask(new ChildModelTask(taskId, "HIRE", () -> {
                            if (hireAnimal.getValue()) {
                                hireAnimal();
                            }
                        }, beHiredEndTime));
                    }
                }
            }
            int animalCount = animals.length();
            if (animalCount >= 3) {
                return;
            }
            Log.farm("雇佣小鸡👷[当前可雇佣小鸡数量:" + (3 - animalCount) + "只]");
            Map<String, Integer> hireAnimalMap = hireAnimalList.getValue().getKey();
            boolean hasNext;
            int pageStartSum = 0;
            String s;
            JSONObject jo;
            do {
                s = AntFarmRpcCall.rankingList(pageStartSum);
                TimeUtil.sleep(1000);
                jo = new JSONObject(s);
                String memo = jo.getString("memo");
                if ("SUCCESS".equals(memo)) {
                    hasNext = jo.getBoolean("hasNext");
                    JSONArray jaRankingList = jo.getJSONArray("rankingList");
                    pageStartSum += jaRankingList.length();
                    for (int i = 0; i < jaRankingList.length(); i++) {
                        JSONObject joo = jaRankingList.getJSONObject(i);
                        String userId = joo.getString("userId");
                        boolean isHireAnimal = hireAnimalMap.containsKey(userId);
                        if (hireAnimalType.getValue() == HireAnimalType.DONT_HIRE) {
                            isHireAnimal = !isHireAnimal;
                        }
                        if (!isHireAnimal || userId.equals(UserIdMap.getCurrentUid())) {
                            continue;
                        }
                        String actionTypeListStr = joo.getJSONArray("actionTypeList").toString();
                        if (actionTypeListStr.contains("can_hire_action")) {
                            if (hireAnimalAction(userId)) {
                                animalCount++;
                                break;
                            }
                        }
                    }
                } else {
                    Log.record(memo);
                    Log.i(s);
                    break;
                }
            } while (hasNext && animalCount < 3);
            if (animalCount < 3) {
                Log.farm("雇佣小鸡失败，没有足够的小鸡可以雇佣");
            }
        } catch (Throwable t) {
            Log.i(TAG, "hireAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private boolean hireAnimalAction(String userId) {
        try {
            String s = AntFarmRpcCall.enterFarm("", userId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("memo"))) {
                JSONObject farmVO = jo.getJSONObject("farmVO");
                JSONObject subFarmVO = farmVO.getJSONObject("subFarmVO");
                String farmId = subFarmVO.getString("farmId");
                JSONArray animals = subFarmVO.getJSONArray("animals");
                for (int i = 0, len = animals.length(); i < len; i++) {
                    JSONObject animal = animals.getJSONObject(i);
                    if (Objects.equals(animal.getJSONObject("masterUserInfoVO").getString("userId"), userId)) {
                        String animalId = animal.getString("animalId");
                        jo = new JSONObject(AntFarmRpcCall.hireAnimal(farmId, animalId));
                        if ("SUCCESS".equals(jo.getString("memo"))) {
                            Log.farm("雇佣小鸡👷[" + UserIdMap.getMaskName(userId) + "] 成功");
                            JSONArray newAnimals = jo.getJSONArray("animals");
                            for (int ii = 0, newLen = newAnimals.length(); ii < newLen; ii++) {
                                JSONObject joo = newAnimals.getJSONObject(ii);
                                if (Objects.equals(joo.getString("animalId"), animalId)) {
                                    long beHiredEndTime = joo.getLong("beHiredEndTime");
                                    addChildTask(new ChildModelTask("HIRE|" + animalId, "HIRE", () -> {
                                        if (hireAnimal.getValue()) {
                                            hireAnimal();
                                        }
                                    }, beHiredEndTime));
                                    Log.record("添加蹲点雇佣👷在[" + TimeUtil.getCommonDate(beHiredEndTime) + "]执行");
                                    break;
                                }
                            }
                            return true;
                        } else {
                            Log.record(jo.getString("memo"));
                            Log.i(s);
                        }
                        return false;
                    }
                }
            } else {
                Log.record(jo.getString("memo"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "hireAnimal err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
        return false;
    }

    private void drawGameCenterAward() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryGameList());
            TimeUtil.sleep(1000);
            if (jo.getBoolean("success")) {
                JSONObject gameDrawAwardActivity = jo.getJSONObject("gameDrawAwardActivity");
                int canUseTimes = gameDrawAwardActivity.getInt("canUseTimes");
                while (canUseTimes > 0) {
                    try {
                        jo = new JSONObject(AntFarmRpcCall.drawGameCenterAward());
                        if (jo.optBoolean("success")) {
                            canUseTimes = jo.getInt("drawRightsTimes");
                            JSONArray gameCenterDrawAwardList = jo.getJSONArray("gameCenterDrawAwardList");
                            ArrayList<String> awards = new ArrayList<String>();
                            for (int i = 0; i < gameCenterDrawAwardList.length(); i++) {
                                JSONObject gameCenterDrawAward = gameCenterDrawAwardList.getJSONObject(i);
                                int awardCount = gameCenterDrawAward.getInt("awardCount");
                                String awardName = gameCenterDrawAward.getString("awardName");
                                awards.add(awardName + "*" + awardCount);
                            }
                            Log.farm("庄园小鸡🎁[开宝箱:获得" + StringUtil.collectionJoinString(",", awards) + "]");
                        } else {
                            Log.i(TAG, "drawGameCenterAward falsed result: " + jo.toString());
                        }
                    } catch (Throwable t) {
                        Log.printStackTrace(TAG, t);
                    } finally {
                        TimeUtil.sleep(3000);
                    }
                }
            } else {
                Log.i(TAG, "queryGameList falsed result: " + jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryChickenDiaryList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 小鸡换装
    private void listOrnaments() {
        try {
            String s = AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid());
            JSONObject jsonObject = new JSONObject(s);
            if ("SUCCESS".equals(jsonObject.getString("memo"))) {
                JSONObject ownAnimal = jsonObject.getJSONObject("ownAnimal");
                String animalId = ownAnimal.getString("animalId");
                String farmId = ownAnimal.getString("farmId");
                String listResult = AntFarmRpcCall.listOrnaments();
                JSONObject jolistOrnaments = new JSONObject(listResult);
                // 检查是否有 achievementOrnaments 数组
                if (!jolistOrnaments.has("achievementOrnaments")) {
                    return; // 数组为空，直接返回
                }
                JSONArray achievementOrnaments = jolistOrnaments.getJSONArray("achievementOrnaments");
                Random random = new Random();
                List<String> possibleOrnaments = new ArrayList<>(); // 收集所有可保存的套装组合
                for (int i = 0; i < achievementOrnaments.length(); i++) {
                    JSONObject ornament = achievementOrnaments.getJSONObject(i);
                    if (ornament.getBoolean("acquired")) {
                        JSONArray sets = ornament.getJSONArray("sets");
                        List<JSONObject> availableSets = new ArrayList<>();
                        // 收集所有带有 cap 和 coat 的套装组合
                        for (int j = 0; j < sets.length(); j++) {
                            JSONObject set = sets.getJSONObject(j);
                            if ("cap".equals(set.getString("subType")) || "coat".equals(set.getString("subType"))) {
                                availableSets.add(set);
                            }
                        }
                        // 如果有可用的帽子和外套套装组合
                        if (availableSets.size() >= 2) {
                            // 将所有可保存的套装组合添加到 possibleOrnaments 列表中
                            for (int j = 0; j < availableSets.size() - 1; j++) {
                                JSONObject selectedCoat = availableSets.get(j);
                                JSONObject selectedCap = availableSets.get(j + 1);
                                String id1 = selectedCoat.getString("id"); // 外套 ID
                                String id2 = selectedCap.getString("id"); // 帽子 ID
                                String ornaments = id1 + "," + id2;
                                possibleOrnaments.add(ornaments);
                            }
                        }
                    }
                }
                // 如果有可保存的套装组合，则随机选择一个进行保存
                if (!possibleOrnaments.isEmpty()) {
                    String ornamentsToSave = possibleOrnaments.get(random.nextInt(possibleOrnaments.size()));
                    String saveResult = AntFarmRpcCall.saveOrnaments(animalId, farmId, ornamentsToSave);
                    JSONObject saveResultJson = new JSONObject(saveResult);
                    // 判断保存是否成功并输出日志
                    if (saveResultJson.getBoolean("success")) {
                        // 获取保存的整套服装名称
                        String[] ornamentIds = ornamentsToSave.split(",");
                        String wholeSetName = ""; // 整套服装名称
                        // 遍历 achievementOrnaments 查找对应的套装名称
                        for (int i = 0; i < achievementOrnaments.length(); i++) {
                            JSONObject ornament = achievementOrnaments.getJSONObject(i);
                            JSONArray sets = ornament.getJSONArray("sets");
                            // 找到对应的整套服装名称
                            if (sets.length() == 2 && sets.getJSONObject(0).getString("id").equals(ornamentIds[0])
                                    && sets.getJSONObject(1).getString("id").equals(ornamentIds[1])) {
                                wholeSetName = ornament.getString("name");
                                break;
                            }
                        }
                        // 输出日志
                        Log.farm("庄园小鸡💞[换装:" + wholeSetName + "]");
                        Status.setOrnamentToday();
                    } else {
                        Log.i(TAG, "保存时装失败，错误码： " + saveResultJson.toString());
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "listOrnaments err: " + t.getMessage());
            Log.printStackTrace(TAG, t);
        }
    }

    public interface DonationCount {

        int ONE = 0;
        int ALL = 1;

        String[] nickNames = {"随机一次", "随机多次"};

    }

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        CharSequence[] nickNames = {"始终召回", "偷吃时召回", "饥饿时召回", "不召回"};
    }

    public interface SendBackAnimalWay {

        int HIT = 0;
        int NORMAL = 1;

        String[] nickNames = {"攻击", "常规"};

    }

    public interface SendBackAnimalType {

        int BACK = 0;
        int NOT_BACK = 1;

        String[] nickNames = {"遣返", "不遣返"};

    }

    public enum AnimalBuff {
        ACCELERATING, INJURED, NONE
    }

    public enum AnimalFeedStatus {
        HUNGRY, EATING, SLEEPY
    }

    public enum AnimalInteractStatus {
        HOME, GOTOSTEAL, STEALING
    }

    public enum SubAnimalType {
        NORMAL, GUEST, PIRATE, WORK
    }

    public enum ToolType {
        STEALTOOL, ACCELERATETOOL, SHARETOOL, FENCETOOL, NEWEGGTOOL;

        public static final CharSequence[] nickNames = {"蹭饭卡", "加速卡", "救济卡", "篱笆卡", "新蛋卡"};

        public CharSequence nickName() {
            return nickNames[ordinal()];
        }
    }

    public enum GameType {
        starGame, jumpGame, flyGame, hitGame;

        public static final CharSequence[] gameNames = {"星星球", "登山赛", "飞行赛", "欢乐揍小鸡"};

        public CharSequence gameName() {
            return gameNames[ordinal()];
        }
    }

    private static class Animal {
        public String animalId, currentFarmId, masterFarmId,
                animalBuff, subAnimalType, animalFeedStatus, animalInteractStatus;
        public String locationType;

        public String currentFarmMasterUserId;

        public Long startEatTime;

        public Double consumeSpeed;

        public Double foodHaveEatten;

    }

    public enum TaskStatus {
        TODO, FINISHED, RECEIVED
    }

    private static class RewardFriend {
        public String consistencyKey, friendId, time;
    }

    private static class FarmTool {
        public ToolType toolType;
        public String toolId;
        public int toolCount, toolHoldLimit;
    }

    public interface HireAnimalType {

        int HIRE = 0;
        int DONT_HIRE = 1;

        String[] nickNames = {"雇佣", "不雇佣"};

    }

    public interface NotifyFriendType {

        int NOTIFY = 0;
        int DONT_NOTIFY = 1;

        String[] nickNames = {"通知", "不通知"};

    }
}
