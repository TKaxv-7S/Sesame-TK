package tkaxv7s.xposed.sesame.task.model.antForest;

import de.robv.android.xposed.XposedHelpers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.*;
import tkaxv7s.xposed.sesame.data.modelFieldExt.*;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.entity.RpcEntity;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.hook.FriendManager;
import tkaxv7s.xposed.sesame.hook.Toast;
import tkaxv7s.xposed.sesame.task.base.TaskCommon;
import tkaxv7s.xposed.sesame.task.model.antFarm.AntFarm.TaskStatus;
import tkaxv7s.xposed.sesame.util.*;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 蚂蚁森林V2
 */
public class AntForestV2 extends ModelTask {
    private static final String TAG = AntForestV2.class.getSimpleName();

    private static final Set<String> AntForestTaskTypeSet;
    private static int totalCollected = 0;
    private static int totalHelpCollected = 0;

    private final AtomicLong offsetTime = new AtomicLong(-1);

    private String selfId;

    private Integer tryCountInt;

    private Integer retryIntervalInt;

    private Boolean useCollectIntervalInt = true;

    private Integer collectIntervalInt = 500;

    private Integer collectIntervalMin = 500;

    private Integer collectIntervalMax = 1000;

    private volatile long doubleEndTime = 0;

    private final Object collectEnergyLockObj = new Object();

    private final Object doubleCardLockObj = new Object();

    private final BaseTask timerTask = BaseTask.newInstance("bubbleTimerTask");

    public static BooleanModelField enableAntForest;
    public static BooleanModelField collectEnergy;
    public static BooleanModelField energyRain;
    public static IntegerModelField advanceTime;
    public static IntegerModelField tryCount;
    public static IntegerModelField retryInterval;
    public static SelectModelField dontCollectList;
    public static BooleanModelField collectWateringBubble;
    public static BooleanModelField batchRobEnergy;
    public static BooleanModelField collectProp;
    public static StringModelField collectInterval;
    public static BooleanModelField doubleCard;
    public static ListModelField.ListJoinCommaToStringModelField doubleCardTime;
    public static IntegerModelField doubleCountLimit;
    public static BooleanModelField helpFriendCollect;
    public static BooleanModelField helpFriendCollectType;
    public static SelectModelField dontHelpCollectList;
    public static IntegerModelField returnWater33;
    public static IntegerModelField returnWater18;
    public static IntegerModelField returnWater10;
    public static BooleanModelField receiveForestTaskAward;
    public static SelectModelField waterFriendList;
    public static IntegerModelField waterFriendCount;
    public static SelectModelField giveEnergyRainList;
    public static BooleanModelField exchangeEnergyDoubleClick;
    public static IntegerModelField exchangeEnergyDoubleClickCount;
    public static BooleanModelField userPatrol;
    public static BooleanModelField antdodoCollect;
    public static BooleanModelField totalCertCount;
    public static BooleanModelField collectGiftBox;
    public static BooleanModelField animalConsumeProp;
    public static SelectModelField.SelectOneModelField sendFriendCard;
    public static SelectModelField whoYouWantToGiveTo;
    public static BooleanModelField ecoLifeTick;

    public static Map<String, Integer> dontCollectMap = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor collectEnergyThreadPoolExecutor = new ThreadPoolExecutor(
            0,
            8,
            TimeUnit.SECONDS.toNanos(30)
            , TimeUnit.NANOSECONDS,
            new ArrayBlockingQueue<>(100000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    static {
        AntForestTaskTypeSet = new HashSet<>();
        AntForestTaskTypeSet.add("VITALITYQIANDAOPUSH"); //
        AntForestTaskTypeSet.add("ONE_CLICK_WATERING_V1");// 给随机好友一键浇水
        AntForestTaskTypeSet.add("GYG_YUEDU_2");// 去森林图书馆逛15s
        AntForestTaskTypeSet.add("GYG_TBRS");// 逛一逛淘宝人生
        AntForestTaskTypeSet.add("TAOBAO_tab2_2023");// 去淘宝看科普视频
        AntForestTaskTypeSet.add("GYG_diantao");// 逛一逛点淘得红包
        AntForestTaskTypeSet.add("GYG-taote");// 逛一逛淘宝特价版
        AntForestTaskTypeSet.add("NONGCHANG_20230818");// 逛一逛淘宝芭芭农场
        // AntForestTaskTypeSet.add("GYG_haoyangmao_20240103");//逛一逛淘宝薅羊毛
        // AntForestTaskTypeSet.add("YAOYIYAO_0815");//去淘宝摇一摇领奖励
        // AntForestTaskTypeSet.add("GYG-TAOCAICAI");//逛一逛淘宝买菜
    }

    @Override
    public String setName() {
        return "森林";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(enableAntForest = new BooleanModelField("enableAntForest", "开启森林", true));
        modelFields.addField(collectEnergy = new BooleanModelField("collectEnergy", "收集能量", true));
        modelFields.addField(batchRobEnergy = new BooleanModelField("batchRobEnergy", "一键收取", true));
        modelFields.addField(collectInterval = new StringModelField("collectInterval", "收取间隔(毫秒或毫秒范围)", "500-1000"));
        modelFields.addField(advanceTime = new IntegerModelField("advanceTime", "提前时间(毫秒)", 0, Integer.MIN_VALUE, 500));
        modelFields.addField(tryCount = new IntegerModelField("tryCount", "尝试收取(次数)", 1, 0, 10));
        modelFields.addField(retryInterval = new IntegerModelField("retryInterval", "重试间隔(毫秒)", 500, 0, 6000));
        modelFields.addField(returnWater10 = new IntegerModelField("returnWater10", "浇水10克需收能量(关闭:0)", 0));
        modelFields.addField(returnWater18 = new IntegerModelField("returnWater18", "浇水18克需收能量(关闭:0)", 0));
        modelFields.addField(returnWater33 = new IntegerModelField("returnWater33", "浇水33克需收能量(关闭:0)", 0));
        modelFields.addField(exchangeEnergyDoubleClick = new BooleanModelField("exchangeEnergyDoubleClick", "活力值兑换限时双击卡", true));
        modelFields.addField(exchangeEnergyDoubleClickCount = new IntegerModelField("exchangeEnergyDoubleClickCount", "兑换限时双击卡数量", 6));
        modelFields.addField(doubleCard = new BooleanModelField("doubleCard", "使用双击卡", true));
        modelFields.addField(doubleCountLimit = new IntegerModelField("doubleCountLimit", "使用双击卡次数", 6));
        List<String> doubleCardTimeList = new ArrayList<>();
        doubleCardTimeList.add("0700-0730");
        modelFields.addField(doubleCardTime = new ListModelField.ListJoinCommaToStringModelField("doubleCardTime", "使用双击卡时间(范围)", doubleCardTimeList));
        modelFields.addField(giveEnergyRainList = new SelectModelField("giveEnergyRainList", "赠送能量雨列表", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser.getList()));
        modelFields.addField(waterFriendList = new SelectModelField("waterFriendList", "好友浇水列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayUser.getList()));
        modelFields.addField(waterFriendCount = new IntegerModelField("waterFriendCount", "每次浇水克数(10 18 33 66)", 66));
        modelFields.addField(helpFriendCollect = new BooleanModelField("helpFriendCollect", "复活好友能量", true));
        modelFields.addField(helpFriendCollectType = new BooleanModelField("helpFriendCollectType", "复活好友能量类型(打开:复活列表/关闭:不复活列表)", false));
        modelFields.addField(dontHelpCollectList = new SelectModelField("dontHelpCollectList", "不复活好友能量名单", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser.getList()));
        modelFields.addField(dontCollectList = new SelectModelField("dontCollectList", "不收取能量名单", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser.getList()));
        modelFields.addField(collectProp = new BooleanModelField("collectProp", "收集道具", true));
        modelFields.addField(collectWateringBubble = new BooleanModelField("collectWateringBubble", "收金球", true));
        modelFields.addField(energyRain = new BooleanModelField("energyRain", "能量雨", true));
        modelFields.addField(animalConsumeProp = new BooleanModelField("animalConsumeProp", "派遣动物", false));
        modelFields.addField(userPatrol = new BooleanModelField("userPatrol", "巡护森林", false));
        modelFields.addField(receiveForestTaskAward = new BooleanModelField("receiveForestTaskAward", "收取森林任务奖励", true));
        modelFields.addField(antdodoCollect = new BooleanModelField("antdodoCollect", "神奇物种开卡", false));
        modelFields.addField(totalCertCount = new BooleanModelField("totalCertCount", "记录证书总数", false));
        modelFields.addField(collectGiftBox = new BooleanModelField("collectGiftBox", "领取礼盒", false));
        modelFields.addField(sendFriendCard = new SelectModelField.SelectOneModelField("sendFriendCard", "送好友卡片(赠送当前图鉴所有卡片)", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser.getList()));
        modelFields.addField(whoYouWantToGiveTo = new SelectModelField("whoYouWantToGiveTo", "赠送道具给谁（赠送所有可送道具）", new KVNode<>(new LinkedHashMap<>(), false), AlipayUser.getList()));
        modelFields.addField(ecoLifeTick = new BooleanModelField("ecoLifeTick", "绿色行动打卡", false));
        return modelFields;
    }

    public Boolean check() {
        if (!enableAntForest.getValue() || RuntimeInfo.getInstance().getLong(RuntimeInfo.RuntimeInfoKey.ForestPauseTime) > System.currentTimeMillis()) {
            Log.record("异常等待中，暂不执行检测！");
            return false;
        }
        return true;
    }

    public Runnable init() {
        return () -> {
            try {
                Log.record("执行开始-蚂蚁森林");
                NotificationUtil.setContentTextExec();

                selfId = UserIdMap.getCurrentUid();
                tryCountInt = tryCount.getValue();
                retryIntervalInt = retryInterval.getValue();
                dontCollectMap = dontCollectList.getValue().getKey();
                String collectIntervalStr = collectInterval.getValue();
                if (collectIntervalStr != null) {
                    String[] split = collectIntervalStr.split("-");
                    if (split.length == 2) {
                        try {
                            collectIntervalMin = Math.max(Integer.parseInt(split[0]), 500);
                        } catch (Exception ignored) {
                            collectIntervalMin = 500;
                        }
                        try {
                            collectIntervalMax = Math.min(Integer.parseInt(split[1]), 30_000) + 1;
                        } catch (Exception ignored) {
                            collectIntervalMax = 1001;
                        }
                        if (collectIntervalMin >= collectIntervalMax) {
                            collectIntervalMax = collectIntervalMin + 1;
                        }
                        useCollectIntervalInt = false;
                    } else {
                        try {
                            collectIntervalInt = Math.max(Integer.parseInt(collectIntervalStr), 500);
                        } catch (Exception ignored) {
                            collectIntervalMin = 500;
                            collectInterval.setValue(collectIntervalMin);
                        }
                        useCollectIntervalInt = true;
                    }
                }
                collectUserEnergy(selfId);

                try {
                    JSONObject friendsObject = new JSONObject(AntForestRpcCall.queryEnergyRanking());
                    if ("SUCCESS".equals(friendsObject.getString("resultCode"))) {
                        collectFriendsEnergy(friendsObject);
                        int pos = 20;
                        List<String> idList = new ArrayList<>();
                        JSONArray totalDatas = friendsObject.getJSONArray("totalDatas");
                        while (pos < totalDatas.length()) {
                            JSONObject friend = totalDatas.getJSONObject(pos);
                            idList.add(friend.getString("userId"));
                            pos++;
                            if (pos % 20 == 0) {
                                collectFriendsEnergy(new JSONObject(AntForestRpcCall.fillUserRobFlag(new JSONArray(idList).toString())));
                                idList.clear();
                            }
                        }
                        if (!idList.isEmpty()) {
                            collectFriendsEnergy(new JSONObject(AntForestRpcCall.fillUserRobFlag(new JSONArray(idList).toString())));
                        }
                    } else {
                        Log.record(friendsObject.getString("resultDesc"));
                    }
                    if (helpFriendCollect.getValue() && Statistics.canProtectBubbleToday(selfId)
                            && TimeUtil.isNowAfterOrCompareTimeStr("0800"))
                        Statistics.protectBubbleToday(selfId);
                } catch (Throwable t) {
                    Log.i(TAG, "queryEnergyRanking err:");
                    Log.printStackTrace(TAG, t);
                }

                if (!TaskCommon.IS_ENERGY_TIME) {
                    popupTask();
                    if (energyRain.getValue()) {
                        energyRain();
                    }
                    if (receiveForestTaskAward.getValue()) {
                        receiveTaskAward();
                    }
                    if (ecoLifeTick.getValue()) {
                        ecoLifeTick();
                    }
                    KVNode<Map<String, Integer>, Boolean> waterFriendListValue = waterFriendList.getValue();
                    Map<String, Integer> friendMap = waterFriendListValue.getKey();
                    for (Map.Entry<String, Integer> friendEntry : friendMap.entrySet()) {
                        String uid = friendEntry.getKey();
                        if (selfId.equals(uid))
                            continue;
                        Integer waterCount = friendEntry.getValue();
                        if (waterCount == null || waterCount <= 0) {
                            continue;
                        }
                        if (waterCount > 3)
                            waterCount = 3;
                        if (Statistics.canWaterFriendToday(uid, waterCount)) {
                            waterFriendEnergy(uid, waterCount);
                        }
                    }
                    if (antdodoCollect.getValue()) {
                        antdodoReceiveTaskAward();
                        antdodoPropList();
                        antdodoCollect();
                    }
                    Map<String, Integer> map = whoYouWantToGiveTo.getValue().getKey();
                    if (!map.isEmpty()) {
                        for (String userId : map.keySet()) {
                            if (!Objects.equals(selfId, userId)) {
                                giveProp(userId);
                                break;
                            }
                        }
                    }
                    if (userPatrol.getValue()) {
                        UserPatrol();
                    }
                    if (exchangeEnergyDoubleClick.getValue() && Statistics.canExchangeDoubleCardToday()) {
                        int exchangeCount = exchangeEnergyDoubleClickCount.getValue();
                        exchangeEnergyDoubleClick(exchangeCount);
                    }
                    /* 森林集市 */
                    sendEnergyByAction("GREEN_LIFE");
                    sendEnergyByAction("ANTFOREST");
                }
            } catch (Throwable t) {
                Log.i(TAG, "checkEnergyRanking.run err:");
                Log.printStackTrace(TAG, t);
            } finally {
                Log.record("执行结束-蚂蚁森林");
                NotificationUtil.setContentTextIdle();
            }
        };
    }

    @Override
    public void destroy() {
        timerTask.stopTask();
        ThreadUtil.shutdownAndAwaitTermination(collectEnergyThreadPoolExecutor, -1, TimeUnit.SECONDS);
    }

    private JSONObject collectUserEnergy(String userId) {
        try {
            boolean isSelf = Objects.equals(selfId, userId);
            JSONObject userHomeObject;
            long start = System.currentTimeMillis();
            if (isSelf) {
                userHomeObject = new JSONObject(AntForestRpcCall.queryHomePage());
            } else {
                userHomeObject = new JSONObject(AntForestRpcCall.queryFriendHomePage(userId));
            }
            long end = System.currentTimeMillis();
            long serverTime = userHomeObject.getLong("now");
            offsetTime.set(Math.min((start + end) / 2 - serverTime, -3000));
            Log.i("服务器时间：" + serverTime + "，本地与服务器时间差：" + offsetTime.get());
            Thread.sleep(100L);
            if (!"SUCCESS".equals(userHomeObject.getString("resultCode"))) {
                Log.record(userHomeObject.getString("resultDesc"));
                return userHomeObject;
            }
            JSONObject userEnergy = userHomeObject.optJSONObject("userEnergy");
            String userName;
            if (userEnergy != null) {
                if (isSelf) {
                    userName = userEnergy.optString("displayName");
                    if (userName.isEmpty()) {
                        userName = "我";
                    }
                } else {
                    userName = userEnergy.getString("displayName");
                    if (userName.isEmpty()) {
                        userName = "*null*";
                    }
                    if (userEnergy.has("loginId")) {
                        userName += "(" + userEnergy.getString("loginId") + ")";
                    }
                }
            } else {
                userName = userId;
            }
            UserIdMap.putIdMapIfEmpty(userId, userName);
            Log.record("进入[" + userName + "]的蚂蚁森林");
            UserIdMap.saveIdMap();

            boolean isCollectEnergy = collectEnergy.getValue() && !dontCollectMap.containsKey(userId);

            if (isSelf) {
                if ("CAN_PLAY".equals(userHomeObject.optString("whackMoleStatus"))) {
                    whackMole();
                }
                updateDoubleTime(userHomeObject);
            } else {
                if (isCollectEnergy) {
                    JSONArray jaProps = userHomeObject.optJSONArray("usingUserProps");
                    if (jaProps != null) {
                        for (int i = 0; i < jaProps.length(); i++) {
                            JSONObject joProps = jaProps.getJSONObject(i);
                            if ("energyShield".equals(joProps.getString("type"))) {
                                if (joProps.getLong("endTime") > serverTime) {
                                    Log.record("[" + userName + "]被能量罩保护着哟");
                                    isCollectEnergy = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (isCollectEnergy) {
                String bizNo = userHomeObject.getString("bizNo");
                JSONArray jaBubbles = userHomeObject.getJSONArray("bubbles");
                List<Long> bubbleIdList = new ArrayList<>();
                for (int i = 0; i < jaBubbles.length(); i++) {
                    JSONObject bubble = jaBubbles.getJSONObject(i);
                    long bubbleId = bubble.getLong("id");
                    switch (CollectStatus.valueOf(bubble.getString("collectStatus"))) {
                        case AVAILABLE:
                            bubbleIdList.add(bubbleId);
                            break;
                        case WAITING:
                            long produceTime = bubble.getLong("produceTime");
                            if (BaseModel.getCheckInterval().getValue() > produceTime - serverTime) {
                                String tid = AntForestV2.getTid(userId, bubbleId);
                                if (timerTask.hasChildTask(tid)) {
                                    break;
                                }
                                timerTask.addChildTask(new BubbleTimerTask(userId, bubbleId, produceTime));
                            } else {
                                Log.i(TAG, "用户[" + UserIdMap.getNameById(userId) + "]能量成熟时间: " + produceTime);
                            }
                            break;
                    }
                }
                if (batchRobEnergy.getValue()) {
                    Iterator<Long> iterator = bubbleIdList.iterator();
                    List<Long> batchBubbleIdList = new ArrayList<>();
                    while (iterator.hasNext()) {
                        batchBubbleIdList.add(iterator.next());
                        if (batchBubbleIdList.size() >= 6) {
                            collectUserBatchEnergy(userId, batchBubbleIdList);
                            batchBubbleIdList = new ArrayList<>();
                        }
                    }
                    int size = batchBubbleIdList.size();
                    if (size > 0) {
                        if (size == 1) {
                            collectUserEnergy(userId, batchBubbleIdList.get(0), bizNo);
                        } else {
                            collectUserBatchEnergy(userId, batchBubbleIdList);
                        }
                    }
                } else {
                    for (Long bubbleId : bubbleIdList) {
                        collectUserEnergy(userId, bubbleId, bizNo);
                    }
                }
            }

            if (!TaskCommon.IS_ENERGY_TIME) {
                if (isSelf) {
                    String whackMoleStatus = userHomeObject.optString("whackMoleStatus");
                    if ("CAN_INITIATIVE_PLAY".equals(whackMoleStatus) || "NEED_MORE_FRIENDS".equals(whackMoleStatus)) {
                        whackMole();
                    }
                    if (totalCertCount.getValue()) {
                        JSONObject userBaseInfo = userHomeObject.getJSONObject("userBaseInfo");
                        int totalCertCount = userBaseInfo.optInt("totalCertCount", 0);
                        FileUtil.setCertCount(selfId, Log.getFormatDate(), totalCertCount);
                    }
                    boolean hasMore = false;
                    do {
                        if (hasMore) {
                            userHomeObject = new JSONObject(AntForestRpcCall.queryHomePage());
                        }
                        if (collectWateringBubble.getValue()) {
                            JSONArray wateringBubbles = userHomeObject.has("wateringBubbles")
                                    ? userHomeObject.getJSONArray("wateringBubbles")
                                    : new JSONArray();
                            if (wateringBubbles.length() > 0) {
                                int collected = 0;
                                for (int i = 0; i < wateringBubbles.length(); i++) {
                                    JSONObject wateringBubble = wateringBubbles.getJSONObject(i);
                                    String bizType = wateringBubble.getString("bizType");
                                    if ("jiaoshui".equals(bizType)) {
                                        String str = AntForestRpcCall.collectEnergy(bizType, selfId,
                                                wateringBubble.getLong("id"));
                                        JSONObject joEnergy = new JSONObject(str);
                                        if ("SUCCESS".equals(joEnergy.getString("resultCode"))) {
                                            JSONArray bubbles = joEnergy.getJSONArray("bubbles");
                                            for (int j = 0; j < bubbles.length(); j++) {
                                                collected = bubbles.getJSONObject(j).getInt("collectedEnergy");
                                            }
                                            if (collected > 0) {
                                                String msg = "收取金球🍯浇水[" + collected + "g]";
                                                Log.forest(msg);
                                                Toast.show(msg);
                                                totalCollected += collected;
                                                Statistics.addData(Statistics.DataType.COLLECTED, collected);
                                            } else {
                                                Log.record("收取[我]的浇水金球失败");
                                            }
                                        } else {
                                            Log.record("收取[我]的浇水金球失败:" + joEnergy.getString("resultDesc"));
                                            Log.i(str);
                                        }
                                    } else if ("fuhuo".equals(bizType)) {
                                        String str = AntForestRpcCall.collectRebornEnergy();
                                        JSONObject joEnergy = new JSONObject(str);
                                        if ("SUCCESS".equals(joEnergy.getString("resultCode"))) {
                                            collected = joEnergy.getInt("energy");
                                            String msg = "收取金球🍯复活[" + collected + "g]";
                                            Log.forest(msg);
                                            Toast.show(msg);
                                            totalCollected += collected;
                                            Statistics.addData(Statistics.DataType.COLLECTED, collected);
                                        } else {
                                            Log.record("收取[我]的复活金球失败:" + joEnergy.getString("resultDesc"));
                                            Log.i(str);
                                        }
                                    } else if ("baohuhuizeng".equals(bizType)) {
                                        String friendId = wateringBubble.getString("userId");
                                        String str = AntForestRpcCall.collectEnergy(bizType, selfId,
                                                wateringBubble.getLong("id"));
                                        JSONObject joEnergy = new JSONObject(str);
                                        if ("SUCCESS".equals(joEnergy.getString("resultCode"))) {
                                            JSONArray bubbles = joEnergy.getJSONArray("bubbles");
                                            for (int j = 0; j < bubbles.length(); j++) {
                                                collected = bubbles.getJSONObject(j).getInt("collectedEnergy");
                                            }
                                            if (collected > 0) {
                                                String msg = "收取金球🍯[" + UserIdMap.getNameById(friendId) + "]复活回赠[" + collected + "g]";
                                                Log.forest(msg);
                                                Toast.show(msg);
                                                totalCollected += collected;
                                                Statistics.addData(Statistics.DataType.COLLECTED, collected);
                                            } else {
                                                Log.record("收取[" + UserIdMap.getNameById(friendId) + "]的复活回赠金球失败");
                                            }
                                        } else {
                                            Log.record("收取[" + UserIdMap.getNameById(friendId) + "]的复活回赠金球失败:" + joEnergy.getString("resultDesc"));
                                            Log.i(str);
                                        }
                                    }
                                    Thread.sleep(1000L);
                                }
                                if (wateringBubbles.length() >= 20) {
                                    hasMore = true;
                                }
                            }
                        }
                        if (collectProp.getValue()) {
                            JSONArray givenProps = userHomeObject.has("givenProps")
                                    ? userHomeObject.getJSONArray("givenProps")
                                    : new JSONArray();
                            if (givenProps.length() > 0) {
                                for (int i = 0; i < givenProps.length(); i++) {
                                    JSONObject jo = givenProps.getJSONObject(i);
                                    String giveConfigId = jo.getString("giveConfigId");
                                    String giveId = jo.getString("giveId");
                                    String propName = jo.getJSONObject("propConfig").getString("propName");
                                    jo = new JSONObject(AntForestRpcCall.collectProp(giveConfigId, giveId));
                                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                        Log.forest("领取道具🎭[" + propName + "]");
                                    } else {
                                        Log.record("领取道具失败:" + jo.getString("resultDesc"));
                                        Log.i(jo.toString());
                                    }
                                    Thread.sleep(1000L);
                                }
                                if (givenProps.length() >= 20) {
                                    hasMore = true;
                                }
                            }
                        }
                    } while (hasMore);
                    JSONArray usingUserProps = userHomeObject.has("usingUserProps")
                            ? userHomeObject.getJSONArray("usingUserProps")
                            : new JSONArray();
                    if (usingUserProps.length() > 0) {
                        for (int i = 0; i < usingUserProps.length(); i++) {
                            JSONObject jo = usingUserProps.getJSONObject(i);
                            if (!"animal".equals(jo.getString("type")))
                                continue;
                            JSONObject extInfo = new JSONObject(jo.getString("extInfo"));
                            int energy = extInfo.optInt("energy", 0);
                            if (energy > 0 && !extInfo.optBoolean("isCollected")) {
                                String propId = jo.getString("propSeq");
                                String propType = jo.getString("propType");
                                String shortDay = extInfo.getString("shortDay");
                                jo = new JSONObject(AntForestRpcCall.collectAnimalRobEnergy(propId, propType, shortDay));
                                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                    Log.forest("动物能量🦩[" + energy + "g]");
                                } else {
                                    Log.record("收取动物能量失败:" + jo.getString("resultDesc"));
                                    Log.i(jo.toString());
                                }
                                break;
                            }
                        }
                    }
                }
            }
            return userHomeObject;
        } catch (Throwable t) {
            Log.i(TAG, "collectUserEnergy err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private void collectFriendsEnergy(JSONObject friendsObject) {
        try {
            JSONArray jaFriendRanking = friendsObject.getJSONArray("friendRanking");
            for (int i = 0; i < jaFriendRanking.length(); i++) {
                try {
                    friendsObject = jaFriendRanking.getJSONObject(i);
                    String userId = friendsObject.getString("userId");
                    JSONObject userHomeObject = null;
                    boolean isNotSelfId = !userId.equals(selfId);
                    if ((
                            friendsObject.getBoolean("canCollectEnergy")
                                    || (
                                    friendsObject.getLong("canCollectLaterTime") > 0 && friendsObject.getLong("canCollectLaterTime") - System.currentTimeMillis() < BaseModel.getCheckInterval().getValue()
                            )
                    )
                            && collectEnergy.getValue()
                            && isNotSelfId
                    ) {
                        if (!dontCollectMap.containsKey(userId)) {
                            userHomeObject = collectUserEnergy(userId);
                        }/* else {
                            Log.i("不收取[" + UserIdMap.getNameById(userId) + "], userId=" + userId);
                        }*/
                    }
                    if (!TaskCommon.IS_ENERGY_TIME && isNotSelfId) {
                        if (helpFriendCollect.getValue()) {
                            try {
                                if (friendsObject.optBoolean("canProtectBubble", false)) {
                                    if (userHomeObject == null) {
                                        userHomeObject = new JSONObject(AntForestRpcCall.queryFriendHomePage(userId));
                                    }
                                    if ("SUCCESS".equals(userHomeObject.getString("resultCode"))) {
                                        Map<String, Integer> dontHelpCollectMap = dontHelpCollectList.getValue().getKey();
                                        JSONArray wateringBubbles = userHomeObject.optJSONArray("wateringBubbles");
                                        if (wateringBubbles != null && wateringBubbles.length() > 0) {
                                            for (int j = 0; j < wateringBubbles.length(); j++) {
                                                JSONObject wateringBubble = wateringBubbles.getJSONObject(j);
                                                if ("fuhuo".equals(wateringBubble.getString("bizType"))) {
                                                    if (wateringBubble.getJSONObject("extInfo").optInt("restTimes", 0) == 0) {
                                                        Statistics.protectBubbleToday(selfId);
                                                    }
                                                    if (wateringBubble.getBoolean("canProtect")) {
                                                        boolean isHelpCollect = dontHelpCollectMap.containsKey(userId);
                                                        if (!helpFriendCollectType.getValue()){
                                                            isHelpCollect = !isHelpCollect;
                                                        }
                                                        if (isHelpCollect) {
                                                            JSONObject joProtect = new JSONObject(AntForestRpcCall.protectBubble(userId));
                                                            if ("SUCCESS".equals(joProtect.getString("resultCode"))) {
                                                                int vitalityAmount = joProtect.optInt("vitalityAmount", 0);
                                                                int fullEnergy = wateringBubble.optInt("fullEnergy", 0);
                                                                String str = "复活能量🚑[" + UserIdMap.getNameById(userId) + "-" + fullEnergy
                                                                        + "g]" + (vitalityAmount > 0 ? "#活力值+" + vitalityAmount : "");
                                                                Log.forest(str);
                                                                totalHelpCollected += fullEnergy;
                                                                Statistics.addData(Statistics.DataType.HELPED, fullEnergy);
                                                            } else {
                                                                Log.record(joProtect.getString("resultDesc"));
                                                                Log.i(joProtect.toString());
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        Log.record(userHomeObject.getString("resultDesc"));
                                    }
                                }
                            } catch (Throwable t) {
                                Log.i(TAG, "protectBubble err:");
                                Log.printStackTrace(TAG, t);
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    Log.printStackTrace(e);
                                }
                            }
                        }
                        if (collectGiftBox.getValue()) {
                            try {
                                if (friendsObject.optBoolean("canCollectGiftBox", false)) {
                                    if (userHomeObject == null) {
                                        userHomeObject = new JSONObject(AntForestRpcCall.queryFriendHomePage(userId));
                                    }
                                    if ("SUCCESS".equals(userHomeObject.getString("resultCode"))) {
                                        JSONArray giftBoxList = userHomeObject.getJSONObject("giftBoxInfo").optJSONArray("giftBoxList");
                                        if (giftBoxList != null && giftBoxList.length() > 0) {
                                            for (int ii = 0; ii < giftBoxList.length(); ii++) {
                                                JSONObject giftBox = giftBoxList.getJSONObject(ii);
                                                String giftBoxId = giftBox.getString("giftBoxId");
                                                String title = giftBox.getString("title");
                                                JSONObject giftBoxResult = new JSONObject(AntForestRpcCall.collectFriendGiftBox(giftBoxId, userId));
                                                if ("SUCCESS".equals(giftBoxResult.getString("resultCode"))) {
                                                    int energy = giftBoxResult.optInt("energy", 0);
                                                    Log.forest("收取礼盒🎁[" + UserIdMap.getNameById(userId) + "-" + title + "]#" + energy + "g");
                                                    Statistics.addData(Statistics.DataType.COLLECTED, energy);
                                                } else {
                                                    Log.record(giftBoxResult.getString("resultDesc"));
                                                    Log.i(giftBoxResult.toString());
                                                }
                                            }
                                        }
                                    }else {
                                        Log.record(userHomeObject.getString("resultDesc"));
                                    }
                                }
                            } catch (Throwable t) {
                                Log.i(TAG, "collectFriendGiftBox err:");
                                Log.printStackTrace(TAG, t);
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    Log.printStackTrace(e);
                                }
                            }
                        }
                    }
                } catch (Exception t) {
                    Log.i(TAG, "collectFriendEnergy err:");
                    Log.printStackTrace(TAG, t);
                    try {
                        Thread.sleep(750);
                    } catch (Exception e) {
                        Log.printStackTrace(e);
                    }
                }
            }
        } catch (Exception t) {
            Log.i(TAG, "collectFriendsEnergy err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(750);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void collectUserEnergy(String userId, long bubbleId, String bizNo) {
        collectEnergyThreadPoolExecutor.execute(() -> {
            synchronized (collectEnergyLockObj) {
                try {
                    if (doubleCard.getValue() && !Objects.equals(selfId, userId) && doubleEndTime < System.currentTimeMillis()) {
                        useDoubleCard();
                    }
                    RpcEntity rpcEntity;
                    boolean isDouble = false;
                    String doBizNo = bizNo;
                    boolean needDouble;
                    int thisTryCount = 0;
                    do {
                        thisTryCount++;
                        needDouble = false;
                        rpcEntity = AntForestRpcCall.getCollectEnergyRpcEntity(null, userId, bubbleId);
                        ApplicationHook.requestObject(rpcEntity, 0, retryIntervalInt);
                        if (rpcEntity.getHasError()) {
                            String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResponseObject(), "getString", "error");
                            if ("1004".equals(errorCode)) {
                                if (BaseModel.getWaitWhenException().getValue() > 0) {
                                    long waitTime = System.currentTimeMillis() + BaseModel.getWaitWhenException().getValue();
                                    RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
                                    NotificationUtil.setContentText("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                    Log.record("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                    return;
                                }
                                try {
                                    Thread.sleep(600 + RandomUtil.delay());
                                } catch (InterruptedException e) {
                                    Log.printStackTrace(e);
                                }
                            }
                            continue;
                        }
                        int collected = 0;
                        JSONObject jo = new JSONObject(rpcEntity.getResponseString());
                        String resultCode = jo.getString("resultCode");
                        if (!"SUCCESS".equalsIgnoreCase(resultCode)) {
                            if ("PARAM_ILLEGAL2".equals(resultCode)) {
                                Log.record("[" + UserIdMap.getNameById(userId) + "]" + "能量已被收取,取消重试 错误信息:" + jo.getString("resultDesc"));
                                return;
                            }
                            Log.record("[" + UserIdMap.getNameById(userId) + "]" + jo.getString("resultDesc"));
                            continue;
                        }
                        JSONArray jaBubbles = jo.getJSONArray("bubbles");
                        jo = jaBubbles.getJSONObject(0);
                        collected += jo.getInt("collectedEnergy");
                        FriendManager.friendWatch(userId, collected);
                        if (collected > 0) {
                            String str = "收取能量🪂[" + UserIdMap.getNameById(userId) + "]#" + collected + "g" + (isDouble ? "[双击卡]" : "");
                            Log.forest(str);
                            Toast.show(str);
                            totalCollected += collected;
                            Statistics.addData(Statistics.DataType.COLLECTED, collected);
                        } else {
                            Log.record("收取[" + UserIdMap.getNameById(userId) + "]的能量失败");
                            Log.i("，UserID：" + userId + "，BubbleId：" + bubbleId);
                        }
                        if (jo.getBoolean("canBeRobbedAgain")) {
                            doBizNo = null;
                            isDouble = true;
                            needDouble = true;
                            thisTryCount = 0;
                            continue;
                        }
                        if (doBizNo == null || doBizNo.isEmpty()) {
                            return;
                        }
                        int returnCount = 0;
                        if (returnWater33.getValue() > 0 && collected >= returnWater33.getValue()) {
                            returnCount = 33;
                        } else if (returnWater18.getValue() > 0 && collected >= returnWater18.getValue()) {
                            returnCount = 18;
                        } else if (returnWater10.getValue() > 0 && collected >= returnWater10.getValue()) {
                            returnCount = 10;
                        }
                        if (returnCount > 0) {
                            returnFriendWater(userId, doBizNo, 1, returnCount);
                        }
                        NotificationUtil.setContentText(Log.getFormatTime() + "  收：" + totalCollected + "，帮：" + totalHelpCollected);
                        return;
                    } while (needDouble || thisTryCount < tryCountInt);
                } catch (Throwable t) {
                    Log.i(TAG, "collectUserEnergy err:");
                    Log.printStackTrace(TAG, t);
                } finally {
                    try {
                        Thread.sleep(getCollectInterval());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private void collectUserBatchEnergy(String userId, final List<Long> bubbleIdList) {
        collectEnergyThreadPoolExecutor.execute(() -> {
            synchronized (collectEnergyLockObj) {
                try {
                    if (doubleCard.getValue() && !Objects.equals(selfId, userId) && doubleEndTime < System.currentTimeMillis()) {
                        useDoubleCard();
                    }
                    RpcEntity rpcEntity;
                    boolean isDouble = false;
                    List<Long> doBubbleIdList = bubbleIdList;
                    boolean needDouble;
                    int thisTryCount = 0;
                    do {
                        thisTryCount++;
                        needDouble = false;
                        rpcEntity = AntForestRpcCall.getCollectBatchEnergyRpcEntity(userId, doBubbleIdList);
                        ApplicationHook.requestObject(rpcEntity, 0, retryIntervalInt);
                        if (rpcEntity.getHasError()) {
                            String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResponseObject(), "getString", "error");
                            if ("1004".equals(errorCode)) {
                                if (BaseModel.getWaitWhenException().getValue() > 0) {
                                    long waitTime = System.currentTimeMillis() + BaseModel.getWaitWhenException().getValue();
                                    RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
                                    NotificationUtil.setContentText("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                    Log.record("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                    return;
                                }
                                try {
                                    Thread.sleep(600 + RandomUtil.delay());
                                } catch (InterruptedException e) {
                                    Log.printStackTrace(e);
                                }
                            }
                            continue;
                        }
                        int collected = 0;
                        JSONObject jo = new JSONObject(rpcEntity.getResponseString());
                        String resultCode = jo.getString("resultCode");
                        if (!"SUCCESS".equalsIgnoreCase(resultCode)) {
                            if ("PARAM_ILLEGAL2".equals(resultCode)) {
                                Log.record("[" + UserIdMap.getNameById(userId) + "]" + "能量已被收取,取消重试 错误信息:" + jo.getString("resultDesc"));
                                return;
                            }
                            Log.record("[" + UserIdMap.getNameById(userId) + "]" + jo.getString("resultDesc"));
                            continue;
                        }
                        JSONArray jaBubbles = jo.getJSONArray("bubbles");
                        List<Long> newBubbleIdList = new ArrayList<>();
                        for (int i = 0; i < jaBubbles.length(); i++) {
                            JSONObject bubble = jaBubbles.getJSONObject(i);
                            if (bubble.getBoolean("canBeRobbedAgain")) {
                                newBubbleIdList.add(bubble.getLong("id"));
                            }
                            collected += bubble.getInt("collectedEnergy");
                        }
                        if (collected > 0) {
                            FriendManager.friendWatch(userId, collected);
                            String str = "一键收取🪂[" + UserIdMap.getNameById(userId) + "]#" + collected + "g" + (isDouble ? "[双击卡]" : "");
                            Log.forest(str);
                            Toast.show(str);
                            totalCollected += collected;
                            Statistics.addData(Statistics.DataType.COLLECTED, collected);
                        } else {
                            Log.record("一键收取[" + UserIdMap.getNameById(userId) + "]的能量失败" + " " + "，UserID：" + userId + "，BubbleId：" + newBubbleIdList);
                        }
                        if (!newBubbleIdList.isEmpty()) {
                            doBubbleIdList = newBubbleIdList;
                            isDouble = true;
                            needDouble = true;
                            thisTryCount = 0;
                            continue;
                        }
                        NotificationUtil.setContentText(Log.getFormatTime() + "  收：" + totalCollected + "，帮：" + totalHelpCollected);
                        return;
                    } while (needDouble || thisTryCount < tryCountInt);
                } catch (Exception e) {
                    Log.i(TAG, "collectUserBatchEnergy err:");
                    Log.printStackTrace(TAG, e);
                } finally {
                    try {
                        Thread.sleep(getCollectInterval());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private Integer getCollectInterval() {
        if (useCollectIntervalInt) {
            return collectIntervalInt;
        }
        return ThreadLocalRandom.current().nextInt(collectIntervalMin, collectIntervalMax);
    }

    private void updateDoubleTime() throws JSONException {
        String s = AntForestRpcCall.queryHomePage();
        JSONObject joHomePage = new JSONObject(s);
        updateDoubleTime(joHomePage);
    }

    private void updateDoubleTime(JSONObject joHomePage) throws JSONException {
        JSONArray usingUserPropsNew = joHomePage.getJSONArray("loginUserUsingPropNew");
        if (usingUserPropsNew.length() == 0) {
            usingUserPropsNew = joHomePage.getJSONArray("usingUserPropsNew");
        }
        for (int i = 0; i < usingUserPropsNew.length(); i++) {
            JSONObject userUsingProp = usingUserPropsNew.getJSONObject(i);
            String propType = userUsingProp.getString("propType");
            if ("ENERGY_DOUBLE_CLICK".equals(propType) || "LIMIT_TIME_ENERGY_DOUBLE_CLICK".equals(propType)) {
                doubleEndTime = userUsingProp.getLong("endTime");
                // Log.forest("双倍卡剩余时间⏰" + (doubleEndTime - System.currentTimeMillis()) / 1000);
            }
        }
    }

    /* 6秒拼手速 打地鼠 */
    private static void whackMole() {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.startWhackMole());
            if (jo.getBoolean("success")) {
                JSONArray moleInfo = jo.optJSONArray("moleInfo");
                if (moleInfo != null) {
                    List<String> whackMoleIdList = new ArrayList<>();
                    for (int i = 0; i < moleInfo.length(); i++) {
                        JSONObject mole = moleInfo.getJSONObject(i);
                        long moleId = mole.getLong("id");
                        whackMoleIdList.add(String.valueOf(moleId));
                    }
                    if (!whackMoleIdList.isEmpty()) {
                        String token = jo.getString("token");
                        jo = new JSONObject(AntForestRpcCall.settlementWhackMole(token, whackMoleIdList));
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            int totalEnergy = jo.getInt("totalEnergy");
                            Log.forest("森林能量⚡[获得:6秒拼手速能量" + totalEnergy + "g]");
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getJSONObject("data").toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "whackMole err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 森林集市 */
    private static void sendEnergyByAction(String sourceType) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.consultForSendEnergyByAction(sourceType));
            if (jo.getBoolean("success")) {
                JSONObject data = jo.getJSONObject("data");
                if(data.optBoolean("canSendEnergy",false)){
                    jo = new JSONObject(AntForestRpcCall.sendEnergyByAction(sourceType));
                    if (jo.getBoolean("success")) {
                        data = jo.getJSONObject("data");
                        if(data.optBoolean("canSendEnergy",false)){
                            int receivedEnergyAmount = data.getInt("receivedEnergyAmount");
                            Log.forest("集市逛街👀[获得:能量" + receivedEnergyAmount + "g]");
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getJSONObject("data").getString("resultCode"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "sendEnergyByAction err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void popupTask() {
        try {
            JSONObject resData = new JSONObject(AntForestRpcCall.popupTask());
            if ("SUCCESS".equals(resData.getString("resultCode"))) {
                JSONArray forestSignVOList = resData.optJSONArray("forestSignVOList");
                if (forestSignVOList != null) {
                    for (int i = 0; i < forestSignVOList.length(); i++) {
                        JSONObject forestSignVO = forestSignVOList.getJSONObject(i);
                        String signId = forestSignVO.getString("signId");
                        String currentSignKey = forestSignVO.getString("currentSignKey");
                        JSONArray signRecords = forestSignVO.getJSONArray("signRecords");
                        for (int j = 0; j < signRecords.length(); j++) {
                            JSONObject signRecord = signRecords.getJSONObject(j);
                            String signKey = signRecord.getString("signKey");
                            if (signKey.equals(currentSignKey)) {
                                if (!signRecord.getBoolean("signed")) {
                                    JSONObject resData2 = new JSONObject(
                                            AntForestRpcCall.antiepSign(signId, UserIdMap.getCurrentUid()));
                                    if ("100000000".equals(resData2.getString("code"))) {
                                        Log.forest("过期能量💊[" + signRecord.getInt("awardCount") + "g]");
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
                Log.record(resData.getString("resultDesc"));
                Log.i(resData.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "popupTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void waterFriendEnergy(String userId, int count) {
        try {
            String s = AntForestRpcCall.queryFriendHomePage(userId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String bizNo = jo.getString("bizNo");
                count = returnFriendWater(userId, bizNo, count, waterFriendCount.getValue());
                if (count > 0)
                    Statistics.waterFriendToday(userId, count);
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "waterFriendEnergy err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int returnFriendWater(String userId, String bizNo, int count, int waterEnergy) {
        if (bizNo == null || bizNo.isEmpty())
            return 0;
        int wateredTimes = 0;
        try {
            String s;
            JSONObject jo;
            int energyId = getEnergyId(waterEnergy);
            for (int waterCount = 1; waterCount <= count; waterCount++) {
                s = AntForestRpcCall.transferEnergy(userId, bizNo, energyId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    String currentEnergy = jo.getJSONObject("treeEnergy").getString("currentEnergy");
                    Log.forest("好友浇水🚿[" + UserIdMap.getNameById(userId) + "]#" + waterEnergy + "g，剩余能量["
                            + currentEnergy + "g]");
                    wateredTimes++;
                    Statistics.addData(Statistics.DataType.WATERED, waterEnergy);
                } else if ("WATERING_TIMES_LIMIT".equals(jo.getString("resultCode"))) {
                    Log.record("今日给[" + UserIdMap.getNameById(userId) + "]浇水已达上限");
                    wateredTimes = 3;
                    break;
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.toString());
                }
                Thread.sleep(1000);
            }
        } catch (Throwable t) {
            Log.i(TAG, "returnFriendWater err:");
            Log.printStackTrace(TAG, t);
        }
        return wateredTimes;
    }

    private static int getEnergyId(int waterEnergy) {
        if (waterEnergy <= 0)
            return 0;
        if (waterEnergy >= 66)
            return 42;
        if (waterEnergy >= 33)
            return 41;
        if (waterEnergy >= 18)
            return 40;
        return 39;
    }

    private static void exchangeEnergyDoubleClick(int count) {
        int exchangedTimes;
        try {
            String s = AntForestRpcCall.itemList("SC_ASSETS");
            JSONObject jo = new JSONObject(s);
            String skuId = null;
            String spuId = null;
            double price = 0d;
            if (jo.getBoolean("success")) {
                JSONArray itemInfoVOList = jo.optJSONArray("itemInfoVOList");
                if (itemInfoVOList != null && itemInfoVOList.length() > 0) {
                    for (int i = 0; i < itemInfoVOList.length(); i++) {
                        jo = itemInfoVOList.getJSONObject(i);
                        if ("能量双击卡".equals(jo.getString("spuName"))) {
                            JSONArray skuModelList = jo.getJSONArray("skuModelList");
                            for (int j = 0; j < skuModelList.length(); j++) {
                                jo = skuModelList.getJSONObject(j);
                                if ("LIMIT_TIME_ENERGY_DOUBLE_CLICK_3DAYS_2023"
                                        .equals(jo.getString("rightsConfigId"))) {
                                    skuId = jo.getString("skuId");
                                    spuId = jo.getString("spuId");
                                    price = jo.getJSONObject("price").getDouble("amount");
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                if (skuId != null) {
                    for (int exchangeCount = 1; exchangeCount <= count; exchangeCount++) {
                        if (Statistics.canExchangeDoubleCardToday()) {
                            jo = new JSONObject(AntForestRpcCall.queryVitalityStoreIndex());
                            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                int totalVitalityAmount = jo.getJSONObject("userVitalityInfoVO")
                                        .getInt("totalVitalityAmount");
                                if (totalVitalityAmount > price) {
                                    jo = new JSONObject(AntForestRpcCall.exchangeBenefit(spuId, skuId));
                                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                        Statistics.exchangeDoubleCardToday(true);
                                        exchangedTimes = Statistics.INSTANCE.getExchangeTimes();
                                        Log.forest("活力兑换🎐[限时双击卡]#第" + exchangedTimes + "次");
                                    } else {
                                        Log.record(jo.getString("resultDesc"));
                                        Log.i(jo.toString());
                                        Statistics.exchangeDoubleCardToday(false);
                                        break;
                                    }
                                    Thread.sleep(1000);
                                } else {
                                    Log.record("活力值不足，停止兑换！");
                                    break;
                                }
                            }
                        } else {
                            Log.record("兑换次数已到上限！");
                            break;
                        }
                    }
                }
            } else {
                Log.record(jo.getString("desc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeEnergyDoubleClick err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void receiveTaskAward() {
        try {
            boolean doubleCheck = false;
            String s = AntForestRpcCall.queryTaskList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray forestSignVOList = jo.getJSONArray("forestSignVOList");
                JSONObject forestSignVO = forestSignVOList.getJSONObject(0);
                String currentSignKey = forestSignVO.getString("currentSignKey");
                JSONArray signRecords = forestSignVO.getJSONArray("signRecords");
                for (int i = 0; i < signRecords.length(); i++) {
                    JSONObject signRecord = signRecords.getJSONObject(i);
                    String signKey = signRecord.getString("signKey");
                    if (signKey.equals(currentSignKey)) {
                        if (!signRecord.getBoolean("signed")) {
                            JSONObject joSign = new JSONObject(AntForestRpcCall.vitalitySign());
                            if ("SUCCESS".equals(joSign.getString("resultCode")))
                                Log.forest("森林签到📆");
                        }
                        break;
                    }
                }
                JSONArray forestTasksNew = jo.optJSONArray("forestTasksNew");
                if (forestTasksNew == null)
                    return;
                for (int i = 0; i < forestTasksNew.length(); i++) {
                    JSONObject forestTask = forestTasksNew.getJSONObject(i);
                    JSONArray taskInfoList = forestTask.getJSONArray("taskInfoList");
                    for (int j = 0; j < taskInfoList.length(); j++) {
                        JSONObject taskInfo = taskInfoList.getJSONObject(j);
                        JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
                        JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
                        String taskType = taskBaseInfo.getString("taskType");
                        String taskTitle = bizInfo.optString("taskTitle", taskType);
                        String awardCount = bizInfo.optString("awardCount", "1");
                        String sceneCode = taskBaseInfo.getString("sceneCode");
                        String taskStatus = taskBaseInfo.getString("taskStatus");
                        if (TaskStatus.FINISHED.name().equals(taskStatus)) {
                            JSONObject joAward = new JSONObject(AntForestRpcCall.receiveTaskAward(sceneCode, taskType));
                            if (joAward.getBoolean("success")) {
                                Log.forest("任务奖励🎖️[" + taskTitle + "]#" + awardCount + "个");
                                doubleCheck = true;
                            } else {
                                Log.record("领取失败，" + s);
                                Log.i(joAward.toString());
                            }
                        } else if (TaskStatus.TODO.name().equals(taskStatus)) {
                            if (bizInfo.optBoolean("autoCompleteTask", false)
                                    || AntForestTaskTypeSet.contains(taskType) || taskType.endsWith("_JIASUQI")
                                    || taskType.endsWith("_BAOHUDI") || taskType.startsWith("GYG")) {
                                JSONObject joFinishTask = new JSONObject(
                                        AntForestRpcCall.finishTask(sceneCode, taskType));
                                if (joFinishTask.getBoolean("success")) {
                                    Log.forest("森林任务🧾️[" + taskTitle + "]");
                                    doubleCheck = true;
                                } else {
                                    Log.record("完成任务失败，" + taskTitle);
                                }
                            } else if ("DAKA_GROUP".equals(taskType)) {
                                JSONArray childTaskTypeList = taskInfo.optJSONArray("childTaskTypeList");
                                if (childTaskTypeList != null && childTaskTypeList.length() > 0) {
                                    doChildTask(childTaskTypeList, taskTitle);
                                }
                            } else if ("TEST_LEAF_TASK".equals(taskType)) {
                                JSONArray childTaskTypeList = taskInfo.optJSONArray("childTaskTypeList");
                                if (childTaskTypeList != null && childTaskTypeList.length() > 0) {
                                    doChildTask(childTaskTypeList, taskTitle);
                                    doubleCheck = true;
                                }
                            }
                        }
                    }
                }
                if (doubleCheck)
                    receiveTaskAward();
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void doChildTask(JSONArray childTaskTypeList, String title) {
        try {
            for (int i = 0; i < childTaskTypeList.length(); i++) {
                JSONObject taskInfo = childTaskTypeList.getJSONObject(i);
                JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
                JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
                String taskType = taskBaseInfo.getString("taskType");
                String taskTitle = bizInfo.optString("taskTitle", title);
                String sceneCode = taskBaseInfo.getString("sceneCode");
                String taskStatus = taskBaseInfo.getString("taskStatus");
                if (TaskStatus.TODO.name().equals(taskStatus)) {
                    if (bizInfo.optBoolean("autoCompleteTask")) {
                        JSONObject joFinishTask = new JSONObject(
                                AntForestRpcCall.finishTask(sceneCode, taskType));
                        if (joFinishTask.getBoolean("success")) {
                            Log.forest("完成任务🧾️[" + taskTitle + "]");
                        } else {
                            Log.record("完成任务" + taskTitle + "失败,");
                            Log.i(joFinishTask.toString());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "doChildTask err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void startEnergyRain() {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.startEnergyRain());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String token = jo.getString("token");
                JSONArray bubbleEnergyList = jo.getJSONObject("difficultyInfo")
                        .getJSONArray("bubbleEnergyList");
                int sum = 0;
                for (int i = 0; i < bubbleEnergyList.length(); i++) {
                    sum += bubbleEnergyList.getInt(i);
                }
                Thread.sleep(5000L);
                if ("SUCCESS".equals(
                        new JSONObject(AntForestRpcCall.energyRainSettlement(sum, token)).getString("resultCode"))) {
                    Toast.show("获得了[" + sum + "g]能量[能量雨]");
                    Log.forest("收能量雨🌧️[" + sum + "g]");
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "startEnergyRain err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void energyRain() {
        try {
            JSONObject joEnergyRainHome = new JSONObject(AntForestRpcCall.queryEnergyRainHome());
            if ("SUCCESS".equals(joEnergyRainHome.getString("resultCode"))) {
                if (joEnergyRainHome.getBoolean("canPlayToday")) {
                    startEnergyRain();
                }
                if (joEnergyRainHome.getBoolean("canGrantStatus")) {
                    Log.record("有送能量雨的机会");
                    JSONObject joEnergyRainCanGrantList = new JSONObject(
                            AntForestRpcCall.queryEnergyRainCanGrantList());
                    JSONArray grantInfos = joEnergyRainCanGrantList.getJSONArray("grantInfos");
                    Map<String, Integer> map = giveEnergyRainList.getValue().getKey();
                    String userId;
                    boolean granted = false;
                    for (int j = 0; j < grantInfos.length(); j++) {
                        JSONObject grantInfo = grantInfos.getJSONObject(j);
                        if (grantInfo.getBoolean("canGrantedStatus")) {
                            userId = grantInfo.getString("userId");
                            if (map.containsKey(userId)) {
                                JSONObject joEnergyRainChance = new JSONObject(
                                        AntForestRpcCall.grantEnergyRainChance(userId));
                                Log.record("尝试送能量雨给【" + UserIdMap.getNameById(userId) + "】");
                                granted = true;
                                // 20230724能量雨调整为列表中没有可赠送的好友则不赠送
                                if ("SUCCESS".equals(joEnergyRainChance.getString("resultCode"))) {
                                    Log.forest("送能量雨🌧️[" + UserIdMap.getNameById(userId) + "]#"
                                            + UserIdMap.getNameById(UserIdMap.getCurrentUid()));
                                    startEnergyRain();
                                } else {
                                    Log.record("送能量雨失败");
                                    Log.i(joEnergyRainChance.toString());
                                }
                                break;
                            }
                        }
                    }
                    if (!granted) {
                        Log.record("没有可以送的用户");
                    }
                    // if (userId != null) {
                    // JSONObject joEnergyRainChance = new
                    // JSONObject(AntForestRpcCall.grantEnergyRainChance(userId));
                    // if ("SUCCESS".equals(joEnergyRainChance.getString("resultCode"))) {
                    // Log.forest("送能量雨🌧️[[" + FriendIdMap.getNameById(userId) + "]#" +
                    // FriendIdMap.getNameById(FriendIdMap.getCurrentUid()));
                    // startEnergyRain();
                    // }
                    // }
                }
            }
            joEnergyRainHome = new JSONObject(AntForestRpcCall.queryEnergyRainHome());
            if ("SUCCESS".equals(joEnergyRainHome.getString("resultCode"))
                    && joEnergyRainHome.getBoolean("canPlayToday")) {
                startEnergyRain();
            }
        } catch (Throwable th) {
            Log.i(TAG, "energyRain err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void useDoubleCard() {
        synchronized (doubleCardLockObj) {
            if (doubleCard.getValue() && doubleEndTime < System.currentTimeMillis()) {
                if (hasDoubleCardTime() && Statistics.canDoubleToday()) {
                    try {
                        JSONObject jo = new JSONObject(AntForestRpcCall.queryPropList(false));
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            JSONArray forestPropVOList = jo.getJSONArray("forestPropVOList");
                            String propId = null;
                            String propType = null;
                            String propName = null;
                            for (int i = 0; i < forestPropVOList.length(); i++) {
                                JSONObject forestPropVO = forestPropVOList.getJSONObject(i);
                                String tmpPropType = forestPropVO.getString("propType");
                                if ("LIMIT_TIME_ENERGY_DOUBLE_CLICK".equals(tmpPropType)) {
                                    JSONArray propIdList = forestPropVO.getJSONArray("propIdList");
                                    propId = propIdList.getString(0);
                                    propType = tmpPropType;
                                    propName = "限时双击卡";
                                    break;
                                }
                                if ("ENERGY_DOUBLE_CLICK".equals(tmpPropType)) {
                                    JSONArray propIdList = forestPropVO.getJSONArray("propIdList");
                                    propId = propIdList.getString(0);
                                    propType = tmpPropType;
                                    propName = "双击卡";
                                }
                            }
                            if (!StringUtil.isEmpty(propId)) {
                                jo = new JSONObject(AntForestRpcCall.consumeProp(propId, propType));
                                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                    doubleEndTime = System.currentTimeMillis() + 1000 * 60 * 5;
                                    Log.forest("使用道具🎭[" + propName + "]");
                                    Statistics.DoubleToday();
                                } else {
                                    Log.record(jo.getString("resultDesc"));
                                    Log.i(jo.toString());
                                    updateDoubleTime();
                                }
                            }
                        }
                    } catch (Throwable th) {
                        Log.i(TAG, "useDoubleCard err:");
                        Log.printStackTrace(TAG, th);
                    }
                }
            }
        }
    }

    private boolean hasDoubleCardTime() {
        long currentTimeMillis = System.currentTimeMillis();
        for (String doubleTime : doubleCardTime.getValue()) {
            if (TimeUtil.checkInTimeRange(currentTimeMillis, doubleTime)) {
                return true;
            }
        }
        return false;
    }

    /* 赠送道具 */
    private static void giveProp(String targetUserId) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryPropList(true));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray forestPropVOList = jo.optJSONArray("forestPropVOList");
                if (forestPropVOList != null && forestPropVOList.length() > 0) {
                    jo = forestPropVOList.getJSONObject(0);
                    String giveConfigId = jo.getJSONObject("giveConfigVO").getString("giveConfigId");
                    int holdsNum = jo.optInt("holdsNum", 0);
                    String propName = jo.getJSONObject("propConfigVO").getString("propName");
                    String propId = jo.getJSONArray("propIdList").getString(0);
                    jo = new JSONObject(AntForestRpcCall.giveProp(giveConfigId, propId, targetUserId));
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.forest("赠送道具🎭[" + UserIdMap.getNameById(targetUserId) + "]#" + propName);
                    } else {
                        Log.record(jo.getString("resultDesc"));
                        Log.i(jo.toString());
                    }
                    Thread.sleep(1000L);
                    if (holdsNum > 1 || forestPropVOList.length() > 1) {
                        giveProp(targetUserId);
                    }
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(jo.toString());
            }
        } catch (Throwable th) {
            Log.i(TAG, "giveProp err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /* 绿色行动打卡 */

    private static void ecoLifeTick() {
        try {
            JSONObject jo = new JSONObject(EcoLifeRpcCall.queryHomePage());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                if (!data.has("dayPoint")) {
                    Log.record("绿色打卡失败, dayPoint不存在");
                    return;
                }
                String dayPoint = data.getString("dayPoint");
                JSONArray actionListVO = data.getJSONArray("actionListVO");
                for (int i = 0; i < actionListVO.length(); i++) {
                    JSONObject actionVO = actionListVO.getJSONObject(i);
                    JSONArray actionItemList = actionVO.getJSONArray("actionItemList");
                    for (int j = 0; j < actionItemList.length(); j++) {
                        JSONObject actionItem = actionItemList.getJSONObject(j);
                        if (!actionItem.has("actionId"))
                            continue;
                        if (actionItem.getBoolean("actionStatus"))
                            continue;
                        String actionId = actionItem.getString("actionId");
                        String actionName = actionItem.getString("actionName");
                        boolean isGuangpan = false;
                        if ("photoguangpan".equals(actionId))
                            continue;
                        jo = new JSONObject(EcoLifeRpcCall.tick(actionId, "ALIPAY", dayPoint, isGuangpan));
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            Log.forest("绿色打卡🍀[" + actionName + "]");
                        } else {
                            Log.record(jo.getString("resultDesc"));
                            Log.i(jo.toString());
                        }
                        Thread.sleep(500);
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "ecoLifeTick err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /* 神奇物种 */

    public static boolean antdodoLastDay(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 86400000L;
    }

    public static boolean antdodoIn8Days(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 691200000L;
    }

    private static void antdodoCollect() {
        try {
            String s = AntForestRpcCall.queryAnimalStatus();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                if (data.getBoolean("collect")) {
                    Log.record("神奇物种卡片今日收集完成！");
                } else {
                    collectAnimalCard();
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "antdodoCollect err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void collectAnimalCard() {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.antdodoHomePage());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject animalBook = data.getJSONObject("animalBook");
                String bookId = animalBook.getString("bookId");
                String endDate = animalBook.getString("endDate") + " 23:59:59";
                antdodoReceiveTaskAward();
                if (!antdodoIn8Days(endDate) || antdodoLastDay(endDate))
                    antdodoPropList();
                JSONArray ja = data.getJSONArray("limit");
                int index = -1;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if ("DAILY_COLLECT".equals(jo.getString("actionCode"))) {
                        index = i;
                        break;
                    }
                }
                Map<String, Integer> map = sendFriendCard.getValue().getKey();
                if (index >= 0) {
                    int leftFreeQuota = jo.getInt("leftFreeQuota");
                    for (int j = 0; j < leftFreeQuota; j++) {
                        jo = new JSONObject(AntForestRpcCall.antdodoCollect());
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            data = jo.getJSONObject("data");
                            JSONObject animal = data.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            Log.forest("神奇物种🦕[" + ecosystem + "]#" + name);
                            if (!map.isEmpty()) {
                                for (String userId : map.keySet()) {
                                    if (!UserIdMap.getCurrentUid().equals(userId)) {
                                        int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                        if (fantasticStarQuantity == 3) {
                                            sendCard(animal, userId);
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            Log.i(TAG, jo.getString("resultDesc"));
                        }
                    }
                }
                if (!map.isEmpty()) {
                    for (String userId : map.keySet()) {
                        if (!UserIdMap.getCurrentUid().equals(userId)) {
                            sendAntdodoCard(bookId, userId);
                            break;
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "collect err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void antdodoReceiveTaskAward() {
        try {
            String s = AntForestRpcCall.antdodoTaskList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray taskGroupInfoList = jo.getJSONObject("data").optJSONArray("taskGroupInfoList");
                if (taskGroupInfoList == null)
                    return;
                for (int i = 0; i < taskGroupInfoList.length(); i++) {
                    JSONObject antdodoTask = taskGroupInfoList.getJSONObject(i);
                    JSONArray taskInfoList = antdodoTask.getJSONArray("taskInfoList");
                    for (int j = 0; j < taskInfoList.length(); j++) {
                        JSONObject taskInfo = taskInfoList.getJSONObject(j);
                        JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
                        JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
                        String taskType = taskBaseInfo.getString("taskType");
                        String taskTitle = bizInfo.optString("taskTitle", taskType);
                        String awardCount = bizInfo.optString("awardCount", "1");
                        String sceneCode = taskBaseInfo.getString("sceneCode");
                        String taskStatus = taskBaseInfo.getString("taskStatus");
                        if (TaskStatus.FINISHED.name().equals(taskStatus)) {
                            JSONObject joAward = new JSONObject(
                                    AntForestRpcCall.antdodoReceiveTaskAward(sceneCode, taskType));
                            if (joAward.getBoolean("success"))
                                Log.forest("任务奖励🎖️[" + taskTitle + "]#" + awardCount + "个");
                            else
                                Log.record("领取失败，" + s);
                            Log.i(joAward.toString());
                        } else if (TaskStatus.TODO.name().equals(taskStatus)) {
                            if ("SEND_FRIEND_CARD".equals(taskType)) {
                                JSONObject joFinishTask = new JSONObject(
                                        AntForestRpcCall.antdodoFinishTask(sceneCode, taskType));
                                if (joFinishTask.getBoolean("success")) {
                                    Log.forest("物种任务🧾️[" + taskTitle + "]");
                                    antdodoReceiveTaskAward();
                                    return;
                                } else {
                                    Log.record("完成任务失败，" + taskTitle);
                                }
                            }
                        }
                    }
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "antdodoReceiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void antdodoPropList() {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.antdodoPropList());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray propList = jo.getJSONObject("data").optJSONArray("propList");
                for (int i = 0; i < propList.length(); i++) {
                    JSONObject prop = propList.getJSONObject(i);
                    String propType = prop.getString("propType");
                    if ("COLLECT_TIMES_7_DAYS".equals(propType)) {
                        JSONArray propIdList = prop.getJSONArray("propIdList");
                        String propId = propIdList.getString(0);
                        String propName = prop.getJSONObject("propConfig").getString("propName");
                        int holdsNum = prop.optInt("holdsNum", 0);
                        jo = new JSONObject(AntForestRpcCall.antdodoConsumeProp(propId, propType));
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            JSONObject useResult = jo.getJSONObject("data").getJSONObject("useResult");
                            JSONObject animal = useResult.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            Log.forest("使用道具🎭[" + propName + "]#" + ecosystem + "-" + name);
                            Map<String, Integer> map = sendFriendCard.getValue().getKey();
                            if (!map.isEmpty()) {
                                for (String userId : map.keySet()) {
                                    if (!UserIdMap.getCurrentUid().equals(userId)) {
                                        int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                        if (fantasticStarQuantity == 3) {
                                            sendCard(animal, userId);
                                        }
                                        break;
                                    }
                                }
                            }
                            if (holdsNum > 1) {
                                Thread.sleep(1000L);
                                antdodoPropList();
                                return;
                            }
                        } else {
                            Log.record(jo.getString("resultDesc"));
                            Log.i(jo.toString());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "antdodoPropList err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void sendAntdodoCard(String bookId, String targetUser) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryBookInfo(bookId));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray animalForUserList = jo.getJSONObject("data").optJSONArray("animalForUserList");
                for (int i = 0; i < animalForUserList.length(); i++) {
                    JSONObject animalForUser = animalForUserList.getJSONObject(i);
                    int count = animalForUser.getJSONObject("collectDetail").optInt("count");
                    if (count <= 0)
                        continue;
                    JSONObject animal = animalForUser.getJSONObject("animal");
                    for (int j = 0; j < count; j++) {
                        sendCard(animal, targetUser);
                        Thread.sleep(500L);
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "sendAntdodoCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void sendCard(JSONObject animal, String targetUser) {
        try {
            String animalId = animal.getString("animalId");
            String ecosystem = animal.getString("ecosystem");
            String name = animal.getString("name");
            JSONObject jo = new JSONObject(AntForestRpcCall.antdodoSocial(animalId, targetUser));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("赠送卡片🦕[" + UserIdMap.getNameById(targetUser) + "]#" + ecosystem + "-" + name);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "sendCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /* 巡护保护地 */
    private static void UserPatrol() {
        try {
            boolean canConsumeProp = true;
            String s = AntForestRpcCall.queryHomePage();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray jaProps = jo.optJSONArray("usingUserProps");
                if (jaProps != null) {
                    for (int i = 0; i < jaProps.length(); i++) {
                        JSONObject joProps = jaProps.getJSONObject(i);
                        if ("animal".equals(joProps.getString("type"))) {
                            Log.record("已经有动物在巡护");
                            canConsumeProp = false;
                        }
                    }
                }
                queryUserPatrol();
                queryAnimalAndPiece(canConsumeProp);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "UserPatrol err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryUserPatrol() {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryUserPatrol());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject resData = new JSONObject(AntForestRpcCall.queryMyPatrolRecord());
                if (resData.optBoolean("canSwitch")) {
                    JSONArray records = resData.getJSONArray("records");
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        JSONObject userPatrol = record.getJSONObject("userPatrol");
                        if (userPatrol.getInt("unreachedNodeCount") > 0) {
                            if ("silent".equals(userPatrol.getString("mode"))) {
                                JSONObject patrolConfig = record.getJSONObject("patrolConfig");
                                String patrolId = patrolConfig.getString("patrolId");
                                resData = new JSONObject(AntForestRpcCall.switchUserPatrol(patrolId));
                                if ("SUCCESS".equals(resData.getString("resultCode"))) {
                                    Log.forest("巡逻⚖️-切换地图至" + patrolId);
                                }
                                queryUserPatrol();
                                return;
                            }
                            break;
                        }
                    }
                }

                JSONObject userPatrol = jo.getJSONObject("userPatrol");
                int currentNode = userPatrol.getInt("currentNode");
                String currentStatus = userPatrol.getString("currentStatus");
                int patrolId = userPatrol.getInt("patrolId");
                JSONObject chance = userPatrol.getJSONObject("chance");
                int leftChance = chance.getInt("leftChance");
                int leftStep = chance.getInt("leftStep");
                int usedStep = chance.getInt("usedStep");
                if ("STANDING".equals(currentStatus)) {
                    if (leftChance > 0) {
                        jo = new JSONObject(AntForestRpcCall.patrolGo(currentNode, patrolId));
                        patrolKeepGoing(jo.toString(), currentNode, patrolId);
                        Thread.sleep(500);
                        queryUserPatrol();
                    } else if (leftStep >= 2000 && usedStep < 10000) {
                        jo = new JSONObject(AntForestRpcCall.exchangePatrolChance(leftStep));
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            int addedChance = jo.optInt("addedChance", 0);
                            Log.forest("步数兑换⚖️[巡护次数*" + addedChance + "]");
                            queryUserPatrol();
                        } else {
                            Log.i(TAG, jo.getString("resultDesc"));
                        }
                    }
                } else if ("GOING".equals(currentStatus)) {
                    patrolKeepGoing(null, currentNode, patrolId);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryUserPatrol err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void patrolKeepGoing(String s, int nodeIndex, int patrolId) {
        try {
            if (s == null) {
                s = AntForestRpcCall.patrolKeepGoing(nodeIndex, patrolId, "image");
            }
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray jaEvents = jo.optJSONArray("events");
                if (jaEvents == null || jaEvents.length() == 0)
                    return;
                JSONObject userPatrol = jo.getJSONObject("userPatrol");
                int currentNode = userPatrol.getInt("currentNode");
                JSONObject events = jo.getJSONArray("events").getJSONObject(0);
                JSONObject rewardInfo = events.optJSONObject("rewardInfo");
                if (rewardInfo != null) {
                    JSONObject animalProp = rewardInfo.optJSONObject("animalProp");
                    if (animalProp != null) {
                        JSONObject animal = animalProp.optJSONObject("animal");
                        if (animal != null) {
                            Log.forest("巡护森林🏇🏻[" + animal.getString("name") + "碎片]");
                        }
                    }
                }
                if (!"GOING".equals(jo.getString("currentStatus")))
                    return;
                JSONObject materialInfo = events.getJSONObject("materialInfo");
                String materialType = materialInfo.optString("materialType", "image");
                String str = AntForestRpcCall.patrolKeepGoing(currentNode, patrolId, materialType);
                patrolKeepGoing(str, nodeIndex, patrolId);

            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "patrolKeepGoing err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryAnimalAndPiece(boolean canConsumeProp) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalAndPiece(0));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray animalProps = jo.getJSONArray("animalProps");
                for (int i = 0; i < animalProps.length(); i++) {
                    jo = animalProps.getJSONObject(i);
                    JSONObject animal = jo.getJSONObject("animal");
                    int id = animal.getInt("id");
                    if (canConsumeProp && animalConsumeProp.getValue()) {
                        JSONObject main = jo.optJSONObject("main");
                        if (main != null && main.optInt("holdsNum", 0) > 0) {
                            canConsumeProp = !AnimalConsumeProp(id);
                        }
                    }
                    JSONArray pieces = jo.getJSONArray("pieces");
                    boolean canCombine = true;
                    for (int j = 0; j < pieces.length(); j++) {
                        jo = pieces.optJSONObject(j);
                        if (jo == null || jo.optInt("holdsNum", 0) <= 0) {
                            canCombine = false;
                            break;
                        }
                    }
                    if (canCombine) {
                        combineAnimalPiece(id);
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryAnimalAndPiece err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static boolean AnimalConsumeProp(int animalId) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalAndPiece(animalId));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray animalProps = jo.getJSONArray("animalProps");
                jo = animalProps.getJSONObject(0);
                String name = jo.getJSONObject("animal").getString("name");
                JSONObject main = jo.getJSONObject("main");
                String propGroup = main.getString("propGroup");
                String propType = main.getString("propType");
                String propId = main.getJSONArray("propIdList").getString(0);
                jo = new JSONObject(AntForestRpcCall.AnimalConsumeProp(propGroup, propId, propType));
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.forest("巡护派遣🐆[" + name + "]");
                    return true;
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryAnimalAndPiece err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static void combineAnimalPiece(int animalId) {
        try {
            JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalAndPiece(animalId));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray animalProps = jo.getJSONArray("animalProps");
                jo = animalProps.getJSONObject(0);
                JSONObject animal = jo.getJSONObject("animal");
                int id = animal.getInt("id");
                String name = animal.getString("name");
                JSONArray pieces = jo.getJSONArray("pieces");
                boolean canCombine = true;
                JSONArray piecePropIds = new JSONArray();
                for (int j = 0; j < pieces.length(); j++) {
                    jo = pieces.optJSONObject(j);
                    if (jo == null || jo.optInt("holdsNum", 0) <= 0) {
                        canCombine = false;
                        break;
                    } else {
                        piecePropIds.put(jo.getJSONArray("propIdList").getString(0));
                    }
                }
                if (canCombine) {
                    jo = new JSONObject(AntForestRpcCall.combineAnimalPiece(id, piecePropIds.toString()));
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.forest("合成动物💡[" + name + "]");
                        combineAnimalPiece(id);
                    } else {
                        Log.i(TAG, jo.getString("resultDesc"));
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "combineAnimalPiece err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int forFriendCollectEnergy(String targetUserId, long bubbleId) {
        int helped = 0;
        try {
            String s = AntForestRpcCall.forFriendCollectEnergy(targetUserId, bubbleId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray jaBubbles = jo.getJSONArray("bubbles");
                for (int i = 0; i < jaBubbles.length(); i++) {
                    jo = jaBubbles.getJSONObject(i);
                    helped += jo.getInt("collectedEnergy");
                }
                if (helped > 0) {
                    Log.forest("帮收能量🧺[" + UserIdMap.getNameById(targetUserId) + "]#" + helped + "g");
                    totalHelpCollected += helped;
                    Statistics.addData(Statistics.DataType.HELPED, helped);
                } else {
                    Log.record("帮[" + UserIdMap.getNameById(targetUserId) + "]收取失败");
                    Log.i("，UserID：" + targetUserId + "，BubbleId" + bubbleId);
                }
            } else {
                Log.record("[" + UserIdMap.getNameById(targetUserId) + "]" + jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "forFriendCollectEnergy err:");
            Log.printStackTrace(TAG, t);
        }
        return helped;
    }

    /**
     * The enum Collect status.
     */
    public enum CollectStatus {
        /**
         * Available collect status.
         */
        AVAILABLE,
        /**
         * Waiting collect status.
         */
        WAITING,
        /**
         * Insufficient collect status.
         */
        INSUFFICIENT,
        /**
         * Robbed collect status.
         */
        ROBBED
    }

    /**
     * The type Bubble timer task.
     */
    public class BubbleTimerTask extends BaseTask {

        private final String id;
        /**
         * The User id.
         */
        private final String userId;
        /**
         * The Bubble id.
         */
        private final long bubbleId;
        /**
         * The ProduceTime.
         */
        private final long produceTime;

        /**
         * Instantiates a new Bubble timer task.
         */
        BubbleTimerTask(String ui, long bi, long pt) {
            id = AntForestV2.getTid(ui, bi);
            userId = ui;
            bubbleId = bi;
            produceTime = pt;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Runnable init() {
            return () -> {
                String userName = null;
                try {
                    userName = UserIdMap.getNameById(userId);
                    long sleep = produceTime - 3000 - System.currentTimeMillis() - advanceTime.getValue();
                    Log.record("添加[" + userName + "]蹲点收取任务, 在[" + sleep / 1000 + "]秒后执行");
                    if (sleep < -5000) {
                        return;
                    }
                    if (sleep > 3000) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            Log.i("终止[" + userName + "]蹲点收取任务, 任务ID[" + id + "]");
                            return;
                        }
                    }
                    long readyTime = produceTime + offsetTime.get() - System.currentTimeMillis() - advanceTime.getValue();
                    if (readyTime > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            Log.i("终止[" + userName + "]蹲点收取任务, 任务ID[" + id + "]");
                            return;
                        }
                    }
                    Log.record("执行[" + userName + "]蹲点收取任务, 剩[" + (timerTask.countChildTask() - 1) + "]个任务");
                    collectUserEnergy(userId, bubbleId, null);
                } catch (Throwable t) {
                    Log.i(TAG, "bubbleTimerTask err:");
                    Log.printStackTrace(TAG, t);
                } finally {
                    timerTask.removeChildTask(id);
                    Log.record("删除[" + userName + "]蹲点收取任务");
                }
            };
        }

        @Override
        public Boolean check() {
            return true;
        }
    }

    public static String getTid(String ui, long bi) {
        return ui + "|" + bi;
    }
}
