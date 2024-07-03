package tkaxv7s.xposed.sesame.model.task.antOcean;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.entity.AlipayBeach;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.model.task.antFarm.AntFarm.TaskStatus;
import tkaxv7s.xposed.sesame.model.task.antForest.AntForestRpcCall;
import tkaxv7s.xposed.sesame.model.task.antForest.AntForestV2;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.StringUtil;
import tkaxv7s.xposed.sesame.util.TimeUtil;
import tkaxv7s.xposed.sesame.util.UserIdMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Constanline
 * @since 2023/08/01
 */
public class AntOcean extends ModelTask {
    private static final String TAG = AntOcean.class.getSimpleName();

    @Override
    public String getName() {
        return "海洋";
    }

    private BooleanModelField protectOcean;
    private SelectModelField protectOceanList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(protectOcean = new BooleanModelField("protectOcean", "保护 | 开启", false));
        modelFields.addField(protectOceanList = new SelectModelField("protectOceanList", "保护 | 海洋列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayBeach::getList));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            String s = AntOceanRpcCall.queryOceanStatus();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                if (jo.getBoolean("opened")) {
                    queryHomePage();
                } else {
                    getEnableField().setValue(false);
                    Log.record("请先开启神奇海洋，并完成引导教程");
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
            if (protectOcean.getValue()) {
                protectOcean();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryHomePage() {
        try {
            JSONObject joHomePage = new JSONObject(AntOceanRpcCall.queryHomePage());
            if ("SUCCESS".equals(joHomePage.getString("resultCode"))) {
                if (joHomePage.has("bubbleVOList")) {
                    collectEnergy(joHomePage.getJSONArray("bubbleVOList"));
                }

                JSONObject userInfoVO = joHomePage.getJSONObject("userInfoVO");
                int rubbishNumber = userInfoVO.optInt("rubbishNumber", 0);
                String userId = userInfoVO.getString("userId");
                cleanOcean(userId, rubbishNumber);

                JSONObject ipVO = userInfoVO.optJSONObject("ipVO");
                if (ipVO != null) {
                    int surprisePieceNum = ipVO.optInt("surprisePieceNum", 0);
                    if (surprisePieceNum > 0) {
                        ipOpenSurprise();
                    }
                }

                queryReplicaHome();

                queryMiscInfo();

                queryUserRanking();

                doOceanDailyTask();

                receiveTaskAward();

            } else {
                Log.i(TAG, joHomePage.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryHomePage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void collectEnergy(JSONArray bubbleVOList) {
        try {
            for (int i = 0; i < bubbleVOList.length(); i++) {
                JSONObject bubble = bubbleVOList.getJSONObject(i);
                if (!"ocean".equals(bubble.getString("channel"))) {
                    continue;
                }
                if ("AVAILABLE".equals(bubble.getString("collectStatus"))) {
                    long bubbleId = bubble.getLong("id");
                    String userId = bubble.getString("userId");
                    String s = AntForestRpcCall.collectEnergy(null, userId, bubbleId);
                    JSONObject jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        JSONArray retBubbles = jo.optJSONArray("bubbles");
                        if (retBubbles != null) {
                            for (int j = 0; j < retBubbles.length(); j++) {
                                JSONObject retBubble = retBubbles.optJSONObject(j);
                                if (retBubble != null) {
                                    int collectedEnergy = retBubble.getInt("collectedEnergy");
                                    Log.forest("神奇海洋🐳收取[" + UserIdMap.getMaskName(userId) + "]的海洋能量#"
                                            + collectedEnergy + "g");
                                }
                            }
                        }
                    } else {
                        Log.i(TAG, jo.getString("resultDesc"));
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryHomePage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void cleanOcean(String userId, int rubbishNumber) {
        try {
            for (int i = 0; i < rubbishNumber; i++) {
                String s = AntOceanRpcCall.cleanOcean(userId);
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray cleanRewardVOS = jo.getJSONArray("cleanRewardVOS");
                    checkReward(cleanRewardVOS);
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "cleanOcean err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void ipOpenSurprise() {
        try {
            String s = AntOceanRpcCall.ipOpenSurprise();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray rewardVOS = jo.getJSONArray("surpriseRewardVOS");
                checkReward(rewardVOS);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "ipOpenSurprise err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void combineFish(String fishId) {
        try {
            String s = AntOceanRpcCall.combineFish(fishId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject fishDetailVO = jo.getJSONObject("fishDetailVO");
                String name = fishDetailVO.getString("name");
                Log.forest("神奇海洋🐳[" + name + "]合成成功");
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "combineFish err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void checkReward(JSONArray rewards) {
        try {
            for (int i = 0; i < rewards.length(); i++) {
                JSONObject reward = rewards.getJSONObject(i);
                JSONArray attachReward = reward.getJSONArray("attachRewardBOList");

                if (attachReward.length() > 0) {
                    Log.forest("神奇海洋🐳[获取碎片奖励]");
                    boolean canCombine = true;
                    for (int j = 0; j < attachReward.length(); j++) {
                        JSONObject detail = attachReward.getJSONObject(j);
                        if (detail.optInt("count", 0) == 0) {
                            canCombine = false;
                            break;
                        }
                    }
                    if (canCombine && reward.optBoolean("unlock", false)) {
                        String fishId = reward.getString("id");
                        combineFish(fishId);
                    }
                }

            }
        } catch (Throwable t) {
            Log.i(TAG, "checkReward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void collectReplicaAsset(int canCollectAssetNum) {
        try {
            for (int i = 0; i < canCollectAssetNum; i++) {
                String s = AntOceanRpcCall.collectReplicaAsset();
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.forest("神奇海洋🐳[学习海洋科普知识]#潘多拉能量+1");
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectReplicaAsset err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void unLockReplicaPhase(String replicaCode, String replicaPhaseCode) {
        try {
            String s = AntOceanRpcCall.unLockReplicaPhase(replicaCode, replicaPhaseCode);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String name = jo.getJSONObject("currentPhaseInfo").getJSONObject("extInfo").getString("name");
                Log.forest("神奇海洋🐳迎回[" + name + "]");
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "unLockReplicaPhase err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryReplicaHome() {
        try {
            String s = AntOceanRpcCall.queryReplicaHome();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                if (jo.has("userReplicaAssetVO")) {
                    JSONObject userReplicaAssetVO = jo.getJSONObject("userReplicaAssetVO");
                    int canCollectAssetNum = userReplicaAssetVO.getInt("canCollectAssetNum");
                    collectReplicaAsset(canCollectAssetNum);
                }

                if (jo.has("userCurrentPhaseVO")) {
                    JSONObject userCurrentPhaseVO = jo.getJSONObject("userCurrentPhaseVO");
                    String phaseCode = userCurrentPhaseVO.getString("phaseCode");
                    String code = jo.getJSONObject("userReplicaInfoVO").getString("code");
                    if ("COMPLETED".equals(userCurrentPhaseVO.getString("phaseStatus"))) {
                        unLockReplicaPhase(code, phaseCode);
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryReplicaHome err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryOceanPropList() {
        try {
            String s = AntOceanRpcCall.queryOceanPropList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                AntOceanRpcCall.repairSeaArea();
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryOceanPropList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void switchOceanChapter() {
        String s = AntOceanRpcCall.queryOceanChapterList();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String currentChapterCode = jo.getString("currentChapterCode");
                JSONArray chapterVOs = jo.getJSONArray("userChapterDetailVOList");
                boolean isFinish = false;
                String dstChapterCode = "";
                String dstChapterName = "";
                for (int i = 0; i < chapterVOs.length(); i++) {
                    JSONObject chapterVO = chapterVOs.getJSONObject(i);
                    int repairedSeaAreaNum = chapterVO.getInt("repairedSeaAreaNum");
                    int seaAreaNum = chapterVO.getInt("seaAreaNum");
                    if (chapterVO.getString("chapterCode").equals(currentChapterCode)) {
                        isFinish = repairedSeaAreaNum >= seaAreaNum;
                    } else {
                        if (repairedSeaAreaNum >= seaAreaNum || !chapterVO.getBoolean("chapterOpen")) {
                            continue;
                        }
                        dstChapterName = chapterVO.getString("chapterName");
                        dstChapterCode = chapterVO.getString("chapterCode");
                    }
                }
                if (isFinish && !StringUtil.isEmpty(dstChapterCode)) {
                    s = AntOceanRpcCall.switchOceanChapter(dstChapterCode);
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.forest("神奇海洋🐳切换到[" + dstChapterName + "]系列");
                    } else {
                        Log.i(TAG, jo.getString("resultDesc"));
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryUserRanking err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void querySeaAreaDetailList() {
        try {
            String s = AntOceanRpcCall.querySeaAreaDetailList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                int seaAreaNum = jo.getInt("seaAreaNum");
                int fixSeaAreaNum = jo.getInt("fixSeaAreaNum");
                int currentSeaAreaIndex = jo.getInt("currentSeaAreaIndex");
                if (currentSeaAreaIndex < fixSeaAreaNum && seaAreaNum > fixSeaAreaNum) {
                    queryOceanPropList();
                }
                JSONArray seaAreaVOs = jo.getJSONArray("seaAreaVOs");
                for (int i = 0; i < seaAreaVOs.length(); i++) {
                    JSONObject seaAreaVO = seaAreaVOs.getJSONObject(i);
                    JSONArray fishVOs = seaAreaVO.getJSONArray("fishVO");
                    for (int j = 0; j < fishVOs.length(); j++) {
                        JSONObject fishVO = fishVOs.getJSONObject(j);
                        if (!fishVO.getBoolean("unlock") && "COMPLETED".equals(fishVO.getString("status"))) {
                            String fishId = fishVO.getString("id");
                            combineFish(fishId);
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "querySeaAreaDetailList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryMiscInfo() {
        try {
            String s = AntOceanRpcCall.queryMiscInfo();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject miscHandlerVOMap = jo.getJSONObject("miscHandlerVOMap");
                JSONObject homeTipsRefresh = miscHandlerVOMap.getJSONObject("HOME_TIPS_REFRESH");
                if (homeTipsRefresh.optBoolean("fishCanBeCombined") || homeTipsRefresh.optBoolean("canBeRepaired")) {
                    querySeaAreaDetailList();
                }
                switchOceanChapter();
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void cleanFriendOcean(JSONObject fillFlag) {
        if (!fillFlag.optBoolean("canClean")) {
            return;
        }
        try {
            String userId = fillFlag.getString("userId");
            AntForestV2 task = ModelTask.getModel(AntForestV2.class);
            if (task == null || task.getDontCollectMap().containsKey(userId)) {
                return;
            }
            String s = AntOceanRpcCall.queryFriendPage(userId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                s = AntOceanRpcCall.cleanFriendOcean(userId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray cleanRewardVOS = jo.getJSONArray("cleanRewardVOS");
                    checkReward(cleanRewardVOS);
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryUserRanking() {
        try {
            String s = AntOceanRpcCall.queryUserRanking();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray fillFlagVOList = jo.getJSONArray("fillFlagVOList");
                for (int i = 0; i < fillFlagVOList.length(); i++) {
                    JSONObject fillFlag = fillFlagVOList.getJSONObject(i);
                    cleanFriendOcean(fillFlag);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static boolean isTargetTask(String taskType) {
        // 在这里添加其他任务类型，以便后续扩展
        return "DAOLIU_TAOJINBI".equals(taskType) // 去逛淘金币看淘金仔
                || "DAOLIU_NNYY".equals(taskType) // 逛余额宝新春活动
                || "ANTOCEAN_TASK#DAOLIU_GUANGHUABEIBANGHAI".equals(taskType) // 逛逛花呗活动会场
                || "BUSINESS_LIGHTS01".equals(taskType) // 逛一逛市集15s
                || "DAOLIU_ELEMEGUOYUAN".equals(taskType) // 去逛饿了么夺宝
                || "ZHUANHUA_NONGCHANGYX".equals(taskType); // 去玩趣味小游戏

    }

    private static void doOceanDailyTask() {
        try {
            String s = AntOceanRpcCall.queryTaskList();
            TimeUtil.sleep(1000);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray jaTaskList = jo.getJSONArray("antOceanTaskVOList");
                for (int i = 0; i < jaTaskList.length(); i++) {
                    JSONObject taskJson = jaTaskList.getJSONObject(i);
                    if (!TaskStatus.TODO.name().equals(taskJson.getString("taskStatus")))
                        continue;
                    JSONObject bizInfo = new JSONObject(taskJson.getString("bizInfo"));
                    if (!taskJson.has("taskType"))
                        continue;
                    String taskType = taskJson.getString("taskType");
                    if (bizInfo.optBoolean("autoCompleteTask", false) || taskType.startsWith("DAOLIU_")) {
                        String sceneCode = taskJson.getString("sceneCode");
                        jo = new JSONObject(AntOceanRpcCall.finishTask(sceneCode, taskType));
                        TimeUtil.sleep(1000);
                        if (jo.getBoolean("success")) {
                            String taskTitle = bizInfo.optString("taskTitle", taskType);
                            Log.forest("海洋任务🧾[完成:" + taskTitle + "]");
                            // 答题操作
                            answerQuestion();
                        } else {
                            Log.record(jo.getString("desc"));
                            Log.i(jo.toString());
                        }
                    }
                    // 多个任务类型的处理逻辑
                    if (isTargetTask(taskType)) {
                        String sceneCode = taskJson.getString("sceneCode");
                        jo = new JSONObject(AntOceanRpcCall.finishTask(sceneCode, taskType));
                        TimeUtil.sleep(1000);
                        if (jo.getBoolean("success")) {
                            String taskTitle = bizInfo.optString("taskTitle", taskType);
                            Log.forest("海洋任务🧾[完成:" + taskTitle + "]");
                            // 答题操作
                            answerQuestion();
                        } else {
                            Log.record(jo.getString("desc"));
                            Log.i(jo.toString());
                        }
                    }
                }
            } else {
                Log.record(jo.getString("resultCode"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "doOceanDailyTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void receiveTaskAward() {
        try {
            String s = AntOceanRpcCall.queryTaskList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray jaTaskList = jo.getJSONArray("antOceanTaskVOList");
                for (int i = 0; i < jaTaskList.length(); i++) {
                    jo = jaTaskList.getJSONObject(i);
                    if (!TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
                        continue;
                    JSONObject bizInfo = new JSONObject(jo.getString("bizInfo"));
                    String taskType = jo.getString("taskType");
                    String sceneCode = jo.getString("sceneCode");
                    jo = new JSONObject(AntOceanRpcCall.receiveTaskAward(sceneCode, taskType));
                    TimeUtil.sleep(1000);
                    if (jo.getBoolean("success")) {
                        String taskTitle = bizInfo.optString("taskTitle", taskType);
                        String awardCount = bizInfo.optString("awardCount", "0");
                        Log.forest("海洋奖励🎖️[领取:" + taskTitle + "]获得:" + awardCount + "块碎片");
                        // 潘多拉任务领取
                        doOceanPDLTask();
                    } else {
                        Log.record(jo.getString("desc"));
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record(jo.getString("resultCode"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 海洋答题任务
    private static void answerQuestion() {
        try {
            String questionResponse = AntOceanRpcCall.getQuestion();
            TimeUtil.sleep(1000);
            JSONObject questionJson = new JSONObject(questionResponse);
            if (questionJson.getBoolean("answered")) {
                Log.record("问题已经被回答过，跳过答题流程");
                return;
            }
            if (questionJson.getInt("resultCode") == 200) {
                String questionId = questionJson.getString("questionId");
                JSONArray options = questionJson.getJSONArray("options");
                String answer = options.getString(0);
                String submitResponse = AntOceanRpcCall.submitAnswer(answer, questionId);
                TimeUtil.sleep(1000);
                JSONObject submitJson = new JSONObject(submitResponse);
                if (submitJson.getInt("resultCode") == 200) {
                    Log.record("海洋答题成功");
                } else {
                    Log.record("答题失败：" + submitJson.getString("resultMsg"));
                }
            } else {
                Log.record("获取问题失败：" + questionJson.getString("resultMsg"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "answerQuestion err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 潘多拉海洋任务领取
    private static void doOceanPDLTask() {
        try {
            String homeResponse = AntOceanRpcCall.PDLqueryReplicaHome();
            TimeUtil.sleep(1000);
            JSONObject homeJson = new JSONObject(homeResponse);
            if ("SUCCESS".equals(homeJson.getString("resultCode"))) {
                String taskListResponse = AntOceanRpcCall.PDLqueryTaskList();
                TimeUtil.sleep(1000);
                JSONObject taskListJson = new JSONObject(taskListResponse);
                JSONArray antOceanTaskVOList = taskListJson.getJSONArray("antOceanTaskVOList");
                for (int i = 0; i < antOceanTaskVOList.length(); i++) {
                    JSONObject task = antOceanTaskVOList.getJSONObject(i);
                    String taskStatus = task.getString("taskStatus");
                    if ("FINISHED".equals(taskStatus)) {
                        String bizInfoString = task.getString("bizInfo");
                        JSONObject bizInfo = new JSONObject(bizInfoString);
                        String taskTitle = bizInfo.getString("taskTitle");
                        int awardCount = bizInfo.getInt("awardCount");
                        String taskType = task.getString("taskType");
                        String receiveTaskResponse = AntOceanRpcCall.PDLreceiveTaskAward(taskType);
                        TimeUtil.sleep(1000);
                        JSONObject receiveTaskJson = new JSONObject(receiveTaskResponse);
                        int code = receiveTaskJson.getInt("code");
                        if (code == 100000000) {
                            Log.forest("海洋奖励🎖️[领取:" + taskTitle + "]获得潘多拉能量x" + awardCount);
                        } else {
                            if (receiveTaskJson.has("message")) {
                                Log.record("领取任务奖励失败: " + receiveTaskJson.getString("message"));
                            } else {
                                Log.record("领取任务奖励失败，未返回错误信息");
                            }
                        }
                    }
                }
            } else {
                Log.record("PDLqueryReplicaHome调用失败: " + homeJson.optString("message"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "doOceanPDLTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void protectOcean() {
        try {
            String s = AntOceanRpcCall.queryCultivationList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("cultivationItemVOList");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("templateSubType")) {
                        continue;
                    }
                    if (!"BEACH".equals(jo.getString("templateSubType"))
                            && !"COOPERATE_SEA_TREE".equals(jo.getString("templateSubType")) && !"SEA_ANIMAL".equals(jo.getString("templateSubType"))) {
                        continue;
                    }
                    if (!"AVAILABLE".equals(jo.getString("applyAction"))) {
                        continue;
                    }
                    String cultivationName = jo.getString("cultivationName");
                    String templateCode = jo.getString("templateCode");
                    JSONObject projectConfig = jo.getJSONObject("projectConfigVO");
                    String projectCode = projectConfig.getString("code");
                    Map<String, Integer> map = protectOceanList.getValue().getKey();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        if (Objects.equals(entry.getKey(), templateCode)) {
                            Integer count = entry.getValue();
                            if (count != null && count > 0) {
                                oceanExchangeTree(templateCode, projectCode, cultivationName, count);
                            }
                            break;
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "protectBeach err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void oceanExchangeTree(String cultivationCode, String projectCode, String itemName, int count) {
        try {
            String s;
            JSONObject jo;
            int appliedTimes = queryCultivationDetail(cultivationCode, projectCode, count);
            if (appliedTimes < 0)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                s = AntOceanRpcCall.oceanExchangeTree(cultivationCode, projectCode);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray awardInfos = jo.getJSONArray("rewardItemVOs");
                    StringBuilder award = new StringBuilder();
                    for (int i = 0; i < awardInfos.length(); i++) {
                        jo = awardInfos.getJSONObject(i);
                        award.append(jo.getString("name")).append("*").append(jo.getInt("num"));
                    }
                    String str = "保护海洋🏖️[" + itemName + "]#第" + appliedTimes + "次"
                            + "-获得奖励" + award;
                    Log.forest(str);
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.toString());
                    Log.forest("保护海洋🏖️[" + itemName + "]#发生未知错误，停止申请");
                    break;
                }
                Thread.sleep(300);
                appliedTimes = queryCultivationDetail(cultivationCode, projectCode, count);
                if (appliedTimes < 0) {
                    break;
                } else {
                    Thread.sleep(300);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "oceanExchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int queryCultivationDetail(String cultivationCode, String projectCode, int count) {
        int appliedTimes = -1;
        try {
            String s = AntOceanRpcCall.queryCultivationDetail(cultivationCode, projectCode);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject userInfo = jo.getJSONObject("userInfoVO");
                int currentEnergy = userInfo.getInt("currentEnergy");
                jo = jo.getJSONObject("cultivationDetailVO");
                String applyAction = jo.getString("applyAction");
                int certNum = jo.getInt("certNum");
                if ("AVAILABLE".equals(applyAction)) {
                    if (currentEnergy >= jo.getInt("energy")) {
                        if (certNum < count) {
                            appliedTimes = certNum + 1;
                        }
                    } else {
                        Log.forest("保护海洋🏖️[" + jo.getString("cultivationName") + "]#能量不足停止申请");
                    }
                } else {
                    Log.forest("保护海洋🏖️[" + jo.getString("cultivationName") + "]#似乎没有了");
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCultivationDetail err:");
            Log.printStackTrace(TAG, t);
        }
        return appliedTimes;
    }

}
