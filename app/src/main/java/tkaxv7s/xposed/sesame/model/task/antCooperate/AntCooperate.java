package tkaxv7s.xposed.sesame.model.task.antCooperate;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.entity.CooperateUser;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.*;

import java.util.LinkedHashMap;

public class AntCooperate extends ModelTask {
    private static final String TAG = AntCooperate.class.getSimpleName();

    @Override
    public String getName() {
        return "合种";
    }

    private final BooleanModelField cooperateWater = new BooleanModelField("cooperateWater", "合种浇水", false);
    private final SelectModelField cooperateWaterList = new SelectModelField("cooperateWaterList", "合种浇水列表", new KVNode<>(new LinkedHashMap<>(), true), CooperateUser::getList);
    private final SelectModelField cooperateWaterTotalLimitList = new SelectModelField("cooperateWaterTotalLimitList", "浇水总量限制列表", new KVNode<>(new LinkedHashMap<>(), true), CooperateUser::getList);
    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(cooperateWater);
        modelFields.addField(cooperateWaterList);
        modelFields.addField(cooperateWaterTotalLimitList);
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (cooperateWater.getValue()) {
                String s = AntCooperateRpcCall.queryUserCooperatePlantList();
                if (s == null) {
                    Thread.sleep(RandomUtil.delay());
                    s = AntCooperateRpcCall.queryUserCooperatePlantList();
                }
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    int userCurrentEnergy = jo.getInt("userCurrentEnergy");
                    JSONArray ja = jo.getJSONArray("cooperatePlants");
                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        String cooperationId = jo.getString("cooperationId");
                        if (!jo.has("name")) {
                            s = AntCooperateRpcCall.queryCooperatePlant(cooperationId);
                            jo = new JSONObject(s).getJSONObject("cooperatePlant");
                        }
                        String name = jo.getString("name");
                        int waterDayLimit = jo.getInt("waterDayLimit");
                        CooperationIdMap.add(cooperationId, name);
                        if (!Status.canCooperateWaterToday(UserIdMap.getCurrentUid(), cooperationId))
                            continue;
                        Integer num = cooperateWaterList.getValue().getKey().get(cooperationId);
                        if (num != null) {
                            Integer limitNum = cooperateWaterTotalLimitList.getValue().getKey().get(cooperationId);
                            if (limitNum != null)
                                num = calculatedWaterNum(UserIdMap.getCurrentUid(), cooperationId, num, limitNum);
                            if (num > waterDayLimit)
                                num = waterDayLimit;
                            if (num > userCurrentEnergy)
                                num = userCurrentEnergy;
                            if (num > 0) {
                                cooperateWater(UserIdMap.getCurrentUid(), cooperationId, num, name);
                            }
                        }
                    }
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
        CooperationIdMap.save(UserIdMap.getCurrentUid());
    }

    private static void cooperateWater(String uid, String coopId, int count, String name) {
        try {
            String s = AntCooperateRpcCall.cooperateWater(uid, coopId, count);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
                Status.cooperateWaterToday(UserIdMap.getCurrentUid(), coopId);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int calculatedWaterNum(String uid, String coopId, int num, int limitNum) {
        try{
            String s = AntCooperateRpcCall.queryCooperateRank("A", coopId);
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success", false)) {
                JSONArray jaList = jo.getJSONArray("cooperateRankInfos");
                for (int i = 0; i < jaList.length(); i++) {
                    JSONObject joItem = jaList.getJSONObject(i);
                    String userId = joItem.getString("userId");
                    if (userId.equals(uid)) {
                        int energySummation = joItem.optInt("energySummation", 0);
                        if (num > limitNum - energySummation)
                            num = limitNum - energySummation;
                            break;
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "calculatedWaterNum err:");
            Log.printStackTrace(TAG, t);
        } finally {
            return num;
        }

    }
}
