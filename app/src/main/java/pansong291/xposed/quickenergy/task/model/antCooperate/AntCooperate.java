package pansong291.xposed.quickenergy.task.model.antCooperate;

import org.json.JSONArray;
import org.json.JSONObject;

import pansong291.xposed.quickenergy.task.common.Task;
import pansong291.xposed.quickenergy.task.common.TaskCommon;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.CooperationIdMap;
import pansong291.xposed.quickenergy.util.FriendIdMap;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;
import pansong291.xposed.quickenergy.util.Statistics;

public class AntCooperate extends Task {
    private static final String TAG = AntCooperate.class.getSimpleName();

    public Boolean check() {
        return Config.INSTANCE.isCooperateWater() && !TaskCommon.IS_MORNING;
    }

    public Runnable init() {
        return () -> {
            try {
                String s = AntCooperateRpcCall.queryUserCooperatePlantList();
                if (s == null) {
                    Thread.sleep(RandomUtils.delay());
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
                        if (!Statistics.canCooperateWaterToday(FriendIdMap.getCurrentUid(), cooperationId))
                            continue;
                        int index = -1;
                        for (int j = 0; j < Config.INSTANCE.getCooperateWaterList().size(); j++) {
                            if (Config.INSTANCE.getCooperateWaterList().get(j).equals(cooperationId)) {
                                index = j;
                                break;
                            }
                        }
                        if (index >= 0) {
                            int num = Config.INSTANCE.getCooperateWaterNumList().get(index);
                            if (num > waterDayLimit)
                                num = waterDayLimit;
                            if (num > userCurrentEnergy)
                                num = userCurrentEnergy;
                            if (num > 0)
                                cooperateWater(FriendIdMap.getCurrentUid(), cooperationId, num, name);
                        }
                    }
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } catch (Throwable t) {
                Log.i(TAG, "start.run err:");
                Log.printStackTrace(TAG, t);
            }
            CooperationIdMap.saveIdMap();
        };
    }

    private static void cooperateWater(String uid, String coopId, int count, String name) {
        try {
            String s = AntCooperateRpcCall.cooperateWater(uid, coopId, count);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
                Statistics.cooperateWaterToday(FriendIdMap.getCurrentUid(), coopId);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
