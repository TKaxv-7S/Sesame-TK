package tkaxv7s.xposed.sesame.model.task.antDodo;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelGroup;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.model.task.antFarm.AntFarm.TaskStatus;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.TimeUtil;
import tkaxv7s.xposed.sesame.util.UserIdMap;

import java.util.LinkedHashSet;
import java.util.Set;

public class AntDodo extends ModelTask {
    private static final String TAG = AntDodo.class.getSimpleName();

    @Override
    public String getName() {
        return "神奇物种";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private BooleanModelField collectToFriend;
    private ChoiceModelField collectToFriendType;
    private SelectModelField collectToFriendList;
    private SelectModelField sendFriendCard;
    private BooleanModelField useProp;
    private BooleanModelField usePropCollectTimes7Days;
    private BooleanModelField usePropCollectHistoryAnimal7Days;
    private BooleanModelField usePropCollectToFriendTimes7Days;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(collectToFriend = new BooleanModelField("collectToFriend", "帮抽卡 | 开启", false));
        modelFields.addField(collectToFriendType = new ChoiceModelField("collectToFriendType", "帮抽卡 | 动作", CollectToFriendType.COLLECT, CollectToFriendType.nickNames));
        modelFields.addField(collectToFriendList = new SelectModelField("collectToFriendList", "帮抽卡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(sendFriendCard = new SelectModelField("sendFriendCard", "送卡片好友列表(当前图鉴所有卡片)", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(useProp = new BooleanModelField("useProp", "使用道具 | 所有", false));
        modelFields.addField(usePropCollectTimes7Days = new BooleanModelField("usePropCollectTimes7Days", "使用道具 | 抽卡道具", false));
        modelFields.addField(usePropCollectHistoryAnimal7Days = new BooleanModelField("usePropCollectHistoryAnimal7Days", "使用道具 | 抽历史卡道具", false));
        modelFields.addField(usePropCollectToFriendTimes7Days = new BooleanModelField("usePropCollectToFriendTimes7Days", "使用道具 | 抽好友卡道具", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            receiveTaskAward();
            propList();
            collect();
            if (collectToFriend.getValue()) {
                collectToFriend();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /*
     * 神奇物种
     */
    private boolean lastDay(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 86400000L;
    }

    public boolean in8Days(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 691200000L;
    }

    private void collect() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryAnimalStatus());
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
            Log.i(TAG, "AntDodo Collect err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectAnimalCard() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.homePage());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject animalBook = data.getJSONObject("animalBook");
                String bookId = animalBook.getString("bookId");
                String endDate = animalBook.getString("endDate") + " 23:59:59";
                receiveTaskAward();
                if (!in8Days(endDate) || lastDay(endDate))
                    propList();
                JSONArray ja = data.getJSONArray("limit");
                int index = -1;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if ("DAILY_COLLECT".equals(jo.getString("actionCode"))) {
                        index = i;
                        break;
                    }
                }
                Set<String> set = sendFriendCard.getValue();
                if (index >= 0) {
                    int leftFreeQuota = jo.getInt("leftFreeQuota");
                    for (int j = 0; j < leftFreeQuota; j++) {
                        jo = new JSONObject(AntDodoRpcCall.collect());
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            data = jo.getJSONObject("data");
                            JSONObject animal = data.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            Log.forest("神奇物种🦕[" + ecosystem + "]#" + name);
                            if (!set.isEmpty()) {
                                for (String userId : set) {
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
                if (!set.isEmpty()) {
                    for (String userId : set) {
                        if (!UserIdMap.getCurrentUid().equals(userId)) {
                            sendAntDodoCard(bookId, userId);
                            break;
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo CollectAnimalCard err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveTaskAward() {
        try {
            th:do {
                String s = AntDodoRpcCall.taskList();
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray taskGroupInfoList = jo.getJSONObject("data").optJSONArray("taskGroupInfoList");
                    if (taskGroupInfoList == null)
                        return;
                    for (int i = 0; i < taskGroupInfoList.length(); i++) {
                        JSONObject antDodoTask = taskGroupInfoList.getJSONObject(i);
                        JSONArray taskInfoList = antDodoTask.getJSONArray("taskInfoList");
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
                                        AntDodoRpcCall.receiveTaskAward(sceneCode, taskType));
                                if (joAward.getBoolean("success"))
                                    Log.forest("任务奖励🎖️[" + taskTitle + "]#" + awardCount + "个");
                                else
                                    Log.record("领取失败，" + s);
                                Log.i(joAward.toString());
                            } else if (TaskStatus.TODO.name().equals(taskStatus)) {
                                if ("SEND_FRIEND_CARD".equals(taskType)) {
                                    JSONObject joFinishTask = new JSONObject(
                                            AntDodoRpcCall.finishTask(sceneCode, taskType));
                                    if (joFinishTask.getBoolean("success")) {
                                        Log.forest("物种任务🧾️[" + taskTitle + "]");
                                        continue th;
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
                break;
            } while (true);
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo ReceiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void propList() {
        try {
            th:
            do {
                JSONObject jo = new JSONObject(AntDodoRpcCall.propList());
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray propList = jo.getJSONObject("data").optJSONArray("propList");
                    if (propList == null) {
                        return;
                    }
                    for (int i = 0; i < propList.length(); i++) {
                        JSONObject prop = propList.getJSONObject(i);
                        String propType = prop.getString("propType");
                        
                        boolean usePropType = useProp.getValue();
                        if ("COLLECT_TIMES_7_DAYS".equals(propType)) {
                            usePropType = usePropType || usePropCollectTimes7Days.getValue();
                        }
                        if ("COLLECT_HISTORY_ANIMAL_7_DAYS".equals(propType)) {
                            usePropType = usePropType || usePropCollectHistoryAnimal7Days.getValue();
                        }
                        if ("COLLECT_TO_FRIEND_TIMES_7_DAYS".equals(propType)) {
                            usePropType = usePropType || usePropCollectToFriendTimes7Days.getValue();
                        }
                        if (!usePropType) {
                            continue;
                        }

                        JSONArray propIdList = prop.getJSONArray("propIdList");
                        String propId = propIdList.getString(0);
                        String propName = prop.getJSONObject("propConfig").getString("propName");
                        int holdsNum = prop.optInt("holdsNum", 0);
                        jo = new JSONObject(AntDodoRpcCall.consumeProp(propId, propType));
                        TimeUtil.sleep(300);
                        if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                            Log.record(jo.getString("resultDesc"));
                            Log.i(jo.toString());
                            continue;
                        }

                        if ("COLLECT_TIMES_7_DAYS".equals(propType)) {
                            JSONObject useResult = jo.getJSONObject("data").getJSONObject("useResult");
                            JSONObject animal = useResult.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            Log.forest("使用道具🎭[" + propName + "]#" + ecosystem + "-" + name);
                            Set<String> map = sendFriendCard.getValue();
                            for (String userId : map) {
                                if (!UserIdMap.getCurrentUid().equals(userId)) {
                                    int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                    if (fantasticStarQuantity == 3) {
                                        sendCard(animal, userId);
                                    }
                                    break;
                                }
                            }
                        } else {
                            Log.forest("使用道具🎭[" + propName + "]");
                        }
                        if (holdsNum > 1) {
                            continue th;
                        }
                    }
                }
                break;
            } while (true);
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo PropList err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void sendAntDodoCard(String bookId, String targetUser) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookInfo(bookId));
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
            Log.i(TAG, "AntDodo SendAntDodoCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void sendCard(JSONObject animal, String targetUser) {
        try {
            String animalId = animal.getString("animalId");
            String ecosystem = animal.getString("ecosystem");
            String name = animal.getString("name");
            JSONObject jo = new JSONObject(AntDodoRpcCall.social(animalId, targetUser));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("赠送卡片🦕[" + UserIdMap.getMaskName(targetUser) + "]#" + ecosystem + "-" + name);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo SendCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void collectToFriend() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryFriend());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                int count = 0;
                JSONArray limitList = jo.getJSONObject("data").getJSONObject("extend").getJSONArray("limit");
                for (int i = 0; i < limitList.length(); i++) {
                    JSONObject limit = limitList.getJSONObject(i);
                    if (limit.getString("actionCode").equals("COLLECT_TO_FRIEND")) {
                        if (limit.getLong("startTime") > System.currentTimeMillis()) {
                            return;
                        }
                        count = limit.getInt("leftLimit");
                        break;
                    }

                }
                JSONArray friendList = jo.getJSONObject("data").getJSONArray("friends");
                for (int i = 0; i < friendList.length() && count > 0; i++) {
                    JSONObject friend = friendList.getJSONObject(i);
                    if (friend.getBoolean("dailyCollect")) {
                        continue;
                    }
                    String useId = friend.getString("userId");
                    boolean isCollectToFriend = collectToFriendList.getValue().contains(useId);
                    if (collectToFriendType.getValue() == CollectToFriendType.DONT_COLLECT) {
                        isCollectToFriend = !isCollectToFriend;
                    }
                    if (!isCollectToFriend) {
                        continue;
                    }
                    jo = new JSONObject(AntDodoRpcCall.collect(useId));
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        String ecosystem = jo.getJSONObject("data").getJSONObject("animal").getString("ecosystem");
                        String name = jo.getJSONObject("data").getJSONObject("animal").getString("name");
                        String userName = UserIdMap.getMaskName(useId);
                        Log.forest("神奇物种🦕帮好友[" + userName + "]抽卡[" + ecosystem + "]#" + name);
                        count--;
                    } else {
                        Log.i(TAG, jo.getString("resultDesc"));
                    }
                }

            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo CollectHelpFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public interface CollectToFriendType {

        int COLLECT = 0;
        int DONT_COLLECT = 1;

        String[] nickNames = {"选中帮抽卡", "选中不帮抽卡"};

    }
}