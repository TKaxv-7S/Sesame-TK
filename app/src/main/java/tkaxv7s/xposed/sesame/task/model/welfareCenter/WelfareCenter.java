package tkaxv7s.xposed.sesame.task.model.welfareCenter;

import lombok.var;
import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.task.model.greenFinance.GreenFinanceRpcCall;
import tkaxv7s.xposed.sesame.util.Log;

import static tkaxv7s.xposed.sesame.task.model.BaseRpcCall.getValueByPath;

/**
 * 福利金
 */
public class WelfareCenter extends ModelTask {
    private static final String TAG = WelfareCenter.class.getSimpleName();

    private Integer executeIntervalInt;
    private BooleanModelField fuLiCenter;
    private IntegerModelField executeInterval;

    @Override
    public String setName() {
        return "福利金";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(fuLiCenter = new BooleanModelField("fuLiCenter", "开启福利金", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 2000));
        return modelFields;
    }

    public Boolean check() {
        return fuLiCenter.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 2000);
            String s = WelfareCenterRpcCall.welfareIndex("121_welfare_121");
            try {
                JSONObject jo = new JSONObject(s);
                if (!jo.getBoolean("success")) {
                    Log.i(TAG, jo.getString("resultDesc"));
                    return;
                }
                JSONObject result = jo.getJSONObject("result");
                //TODO：待处理，检查未开通的字段
                if (!result.getBoolean("greenFinanceSigned")) {
                    Log.other("福利金💰未开通");
                    return;
                }
            } catch (Throwable th) {
                Log.i(TAG, "index err:");
                Log.printStackTrace(TAG, th);
            }
            batchUseVirtualProfit();
            String appletId = "AP1269301";
            doTask(appletId);
        };
    }

    /**
     * 批量领取、签到
     */
    private void batchUseVirtualProfit() {
        try {
            var str = WelfareCenterRpcCall.queryEnableVirtualProfitV2();
            var jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".batchUseVirtualProfit", str);
                return;
            }
            JSONObject result = jsonObject.getJSONObject("result");
            JSONArray virtualProfitList = result.getJSONArray("virtualProfitList");
            for (int i = 0; i < virtualProfitList.length(); i++) {
                JSONObject object = virtualProfitList.getJSONObject(i);
                if ("signin".equals(object.getString("type"))) {
                    signIn("PLAY102815727");
                    continue;
                }
                JSONArray virtualProfitIds = object.getJSONArray("virtualProfitIds");
                if (virtualProfitIds.length() <= 0) {
                    continue;
                }
                str = WelfareCenterRpcCall.batchUseVirtualProfit(virtualProfitIds);
                result = new JSONObject(str);
                if (!result.getBoolean("success")) {
                    Log.i(TAG + ".batchUseVirtualProfit", str);
                    continue;
                }
                Log.other("福利金💰" + object.getString("sceneDesc") + object.getString("reward") + "×" + virtualProfitIds.length());
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchUseVirtualProfit err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 签到
     *
     * @param sceneId
     */
    private void signIn(String sceneId) {
        try {
            String str = WelfareCenterRpcCall.signInTrigger(sceneId);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".signIn", str);
                return;
            }
            Log.other("福利金💰签到成功" + getValueByPath(jsonObject, "result.prizeOrderDTOList[0].price"));
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 赚福利金
     * @param appletId
     */
    private void doTask(String appletId) {
        try {
            String s = WelfareCenterRpcCall.taskQuery(appletId);
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".doTask", s);
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            JSONArray taskDetailList = result.getJSONArray("taskDetailList");
            for (int i = 0; i < taskDetailList.length(); i++) {
                JSONObject taskDetail = taskDetailList.getJSONObject(i);
                //EVENT_TRIGGER、USER_TRIGGER
                String type = taskDetail.getString("sendCampTriggerType");
                if (!"USER_TRIGGER".equals(type) && !"EVENT_TRIGGER".equals(type)) {
                    continue;
                }

                String status = taskDetail.getString("taskProcessStatus");
                switch (status) {
                    case "TO_RECEIVE":
                        //领取奖品
//                                break;
                    case "SIGNUP_COMPLETE":
                        //待完成，去领取
//                                break;
                    case "RECEIVE_SUCCESS":
                        //一次性已完成的
//                                break;
                    case "NONE_SIGNUP":
                        String taskId = taskDetail.getString("taskId");
                        s = WelfareCenterRpcCall.taskTrigger(taskId, "signup", appletId);
                        jo = new JSONObject(s);
                        if (!jo.getBoolean("success")) {
                            Log.i(TAG + ".doTask", s);
                            continue;
                        }
                        s = WelfareCenterRpcCall.taskTrigger(taskId, "send", appletId);
                        jo = new JSONObject(s);
                        if (!jo.getBoolean("success")) {
                            Log.i(TAG + ".doTask", s);
                            continue;
                        }
                        Log.other("福利金💰" + getValueByPath(taskDetail, "taskExtProps.TASK_MORPHO_DETAIL.title") + " 任务完成");
                        break;
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "doTask err:");
            Log.printStackTrace(TAG, th);
        }
    }
}