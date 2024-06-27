package tkaxv7s.xposed.sesame.model.task.antCooperate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.entity.CooperateUser;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.CooperationIdMap;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.RandomUtil;
import tkaxv7s.xposed.sesame.util.Statistics;
import tkaxv7s.xposed.sesame.util.UserIdMap;

public class AntCooperate extends ModelTask {
    private static final String TAG = AntCooperate.class.getSimpleName();

    @Override
    public String getName() {
        return "合种";
    }

    public static BooleanModelField enableAntCooperate;
    public static BooleanModelField cooperateWater;
    public static SelectModelField cooperateWaterList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(enableAntCooperate = new BooleanModelField("enableAntCooperate", "开启合种", true));
        modelFields.addField(cooperateWater = new BooleanModelField("cooperateWater", "合种浇水", true));
        modelFields.addField(cooperateWaterList = new SelectModelField("cooperateWaterList", "合种浇水列表", new KVNode<>(new LinkedHashMap<>(), true), CooperateUser.getList()));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return enableAntCooperate.getValue() && !TaskCommon.IS_ENERGY_TIME;
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
                        CooperationIdMap.putIdMap(cooperationId, name);
                        if (!Statistics.canCooperateWaterToday(UserIdMap.getCurrentUid(), cooperationId))
                            continue;
                        Integer num = cooperateWaterList.getValue().getKey().get(cooperationId);
                        if (num != null) {
                            if (num > waterDayLimit)
                                num = waterDayLimit;
                            if (num > userCurrentEnergy)
                                num = userCurrentEnergy;
                            if (num > 0)
                                cooperateWater(UserIdMap.getCurrentUid(), cooperationId, num, name);
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
        CooperationIdMap.saveIdMap();
    }

    private static void cooperateWater(String uid, String coopId, int count, String name) {
        try {
            String s = AntCooperateRpcCall.cooperateWater(uid, coopId, count);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
                Statistics.cooperateWaterToday(UserIdMap.getCurrentUid(), coopId);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
