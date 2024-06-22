package tkaxv7s.xposed.sesame.task.model.gameCenter;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;

public class GameCenter extends ModelTask {

    private static final String TAG = GameCenter.class.getSimpleName();

    private Integer executeIntervalInt;
    /**
     * 是否启用游戏中心
     */
    private BooleanModelField gameCenter;
    /**
     * 执行间隔时间
     */
    private IntegerModelField executeInterval;

    @Override
    public String setName() {
        return "游戏中心";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(gameCenter = new BooleanModelField("gameCenter", "开启游戏中心", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 5000));
        return modelFields;
    }
    public Boolean check() {
        return gameCenter.getValue()&& !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 5000);
            String s = GameCenterRpcCall.greenFinanceIndex();
            try {
                JSONObject jo = new JSONObject(s);
                if (jo.getBoolean("success")) {
                    JSONObject result = jo.getJSONObject("result");
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } catch (Throwable th) {
                Log.i(TAG, "index err:");
                Log.printStackTrace(TAG, th);
            }

            String appletId = "";
            doTask(appletId);
        };
    }


    private void doTask(String appletId) {
        String s = GameCenterRpcCall.taskQuery(appletId);
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                JSONArray taskDetailList = result.getJSONArray("taskDetailList");
                for (int i = 0; i < taskDetailList.length(); i++) {
                    JSONObject taskDetail = taskDetailList.getJSONObject(i);
                    if ("USER_TRIGGER".equals(taskDetail.getString("sendCampTriggerType"))) {
                        String status = taskDetail.getString("taskProcessStatus");
                        switch (status){
                            case "TO_RECEIVE":
                                //领取奖品
                                break;
                            case "SIGNUP_COMPLETE":
                                //待完成，去领取
                                break;
                            case "RECEIVE_SUCCESS":
                                //一次性已完成的
                                break;
                        }
                        if ("NONE_SIGNUP".equals(status)) {
                            s = GameCenterRpcCall.taskTrigger(taskDetail.getString("taskId"), "signup", appletId);
                            jo = new JSONObject(s);
                            if (jo.getBoolean("success")) {
                                s = GameCenterRpcCall.taskTrigger(taskDetail.getString("taskId"), "send", appletId);
                                jo = new JSONObject(s);
                                if (jo.getBoolean("success")) {
                                    Log.other("绿色经营📊任务完成");
                                }
                            }
                        } else if ("TO_RECEIVE".equals(status)) {
                            //领取奖品

                        }
                    }
                }
            } else {
                Log.i(TAG + ".doTask", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        }
    }
}
