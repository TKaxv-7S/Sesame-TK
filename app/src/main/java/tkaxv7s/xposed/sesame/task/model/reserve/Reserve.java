package tkaxv7s.xposed.sesame.task.model.reserve;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.entity.AlipayBeach;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.util.BeachIdMap;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.RandomUtil;
import tkaxv7s.xposed.sesame.util.ReserveIdMap;
import tkaxv7s.xposed.sesame.util.Statistics;

public class Reserve extends ModelTask {
    private static final String TAG = Reserve.class.getSimpleName();

    private static boolean isProtecting = false;

    @Override
    public String setName() {
        return "保护地";
    }

    public static BooleanModelField enableReserve;
    public static BooleanModelField beach;
    public static SelectModelField beachList;

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(enableReserve = new BooleanModelField("enableReserve", "开启保护地", false));
        modelFields.addField(beach = new BooleanModelField("beach", "保护海洋", false));
        modelFields.addField(beachList = new SelectModelField("beachList", "保护海洋列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayBeach.getList()));
        return modelFields;
    }

    public Boolean check() {
        if (!enableReserve.getValue() && !beach.getValue()) {
            return false;
        }
        if (TaskCommon.IS_ENERGY_TIME) {
            return false;
        }

        if (isProtecting) {
            Log.record("之前的兑换保护地未结束，本次暂停");
            return false;
        }
        return true;
    }

    public Runnable init() {
        return () -> {
            try {
                Log.record("开始检测保护地");
                isProtecting = true;

                if (enableReserve.getValue()) {
                    animalReserve();
                }

                if (beach.getValue()) {
                    protectBeach();
                }
                isProtecting = false;
            } catch (Throwable t) {
                Log.i(TAG, "start.run err:");
                Log.printStackTrace(TAG, t);
            }
        };
    }

    private static void animalReserve() {
        try {
            String s = ReserveRpcCall.queryTreeItemsForExchange();
            if (s == null) {
                Thread.sleep(RandomUtil.delay());
                s = ReserveRpcCall.queryTreeItemsForExchange();
            }
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("treeItems");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("projectType"))
                        continue;
                    if (!"RESERVE".equals(jo.getString("projectType")))
                        continue;
                    if (!"AVAILABLE".equals(jo.getString("applyAction")))
                        continue;
                    String projectId = jo.getString("itemId");
                    String itemName = jo.getString("itemName");
                    int energy = jo.getInt("energy");
                    ReserveIdMap.putIdMap(projectId, itemName + "(" + energy + "g)");
                    Map<String, Integer> map = beachList.getValue().getKey();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        if (Objects.equals(entry.getKey(), projectId)) {
                            Integer count = entry.getValue();
                            if (count != null && count > 0 && Statistics.canReserveToday(projectId, count)) {
                                exchangeTree(projectId, itemName, count);
                            }
                            break;
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "animalReserve err:");
            Log.printStackTrace(TAG, t);
        }
        ReserveIdMap.saveIdMap();
    }

    private static boolean queryTreeForExchange(String projectId) {
        try {
            String s = ReserveRpcCall.queryTreeForExchange(projectId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String applyAction = jo.getString("applyAction");
                int currentEnergy = jo.getInt("currentEnergy");
                jo = jo.getJSONObject("exchangeableTree");
                if ("AVAILABLE".equals(applyAction)) {
                    if (currentEnergy >= jo.getInt("energy")) {
                        return true;
                    } else {
                        Log.forest("领保护地🏕️[" + jo.getString("projectName") + "]#能量不足停止申请");
                        return false;
                    }
                } else {
                    Log.forest("领保护地🏕️[" + jo.getString("projectName") + "]#似乎没有了");
                    return false;
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryTreeForExchange err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static void exchangeTree(String projectId, String itemName, int count) {
        int appliedTimes = 0;
        try {
            String s;
            JSONObject jo;
            boolean canApply = queryTreeForExchange(projectId);
            if (!canApply)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                s = ReserveRpcCall.exchangeTree(projectId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    int vitalityAmount = jo.optInt("vitalityAmount", 0);
                    appliedTimes = Statistics.getReserveTimes(projectId) + 1;
                    String str = "领保护地🏕️[" + itemName + "]#第" + appliedTimes + "次"
                            + (vitalityAmount > 0 ? "-活力值+" + vitalityAmount : "");
                    Log.forest(str);
                    Statistics.reserveToday(projectId, 1);
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.toString());
                    Log.forest("领保护地🏕️[" + itemName + "]#发生未知错误，停止申请");
                    // Statistics.reserveToday(projectId, count);
                    break;
                }
                Thread.sleep(300);
                canApply = queryTreeForExchange(projectId);
                if (!canApply) {
                    // Statistics.reserveToday(projectId, count);
                    break;
                } else {
                    Thread.sleep(300);
                }
                if (!Statistics.canReserveToday(projectId, count))
                    break;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 保护海洋 */

    private static void protectBeach() {
        try {
            String s = ReserveRpcCall.queryCultivationList();
            if (s == null) {
                Thread.sleep(RandomUtil.delay());
                s = ReserveRpcCall.queryCultivationList();
            }
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("cultivationItemVOList");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("templateSubType"))
                        continue;
                    if (!"BEACH".equals(jo.getString("templateSubType"))
                            && !"COOPERATE_SEA_TREE".equals(jo.getString("templateSubType")) && !"SEA_ANIMAL".equals(jo.getString("templateSubType")))
                        continue;
                    if (!"AVAILABLE".equals(jo.getString("applyAction")))
                        continue;
                    String cultivationName = jo.getString("cultivationName");
                    String templateCode = jo.getString("templateCode");
                    int energy = jo.getInt("energy");
                    JSONObject projectConfig = jo.getJSONObject("projectConfigVO");
                    String projectCode = projectConfig.getString("code");
                    BeachIdMap.putIdMap(templateCode, cultivationName + "(" + energy + "g)");
                    Map<String, Integer> map = beachList.getValue().getKey();
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
        BeachIdMap.saveIdMap();
    }

    private static int queryCultivationDetail(String cultivationCode, String projectCode, int count) {
        int appliedTimes = -1;
        try {
            String s = ReserveRpcCall.queryCultivationDetail(cultivationCode, projectCode);
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

    private static void oceanExchangeTree(String cultivationCode, String projectCode, String itemName, int count) {
        try {
            String s;
            JSONObject jo;
            int appliedTimes = queryCultivationDetail(cultivationCode, projectCode, count);
            if (appliedTimes < 0)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                s = ReserveRpcCall.oceanExchangeTree(cultivationCode, projectCode);
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
}
