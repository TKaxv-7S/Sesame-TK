package tkaxv7s.xposed.sesame.task.model.otherTask;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;


/**
 * 其他任务
 *
 * @author xiong
 */
public class OtherTask extends ModelTask {
    private static final String TAG = OtherTask.class.getSimpleName();

    private Integer executeIntervalInt;
    private BooleanModelField goldTicket;
    private IntegerModelField executeInterval;

    @Override
    public String setName() {
        return "其他任务";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 2000));
        modelFields.addField(goldTicket = new BooleanModelField("goldTicket", "黄金票", true));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 2000);
            if (goldTicket.getValue()) {
                //签到
                goldBillCollect("\"campId\":\"CP1417744\",\"directModeDisableCollect\":true,\"from\":\"antfarm\",");
                //摆设
//                goldTicket();
                //收取其他
                goldBillCollect("");
            }
        };
    }

    /**
     * 黄金票任务
     */
    private void goldTicket() {
        try {
            String str = OtherTaskRpcCall.goldBillIndex();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".goldTicket.goldBillIndex", jsonObject.optString("resultDesc"));
                return;
            }
            jsonObject = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("cardModel");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String cardTypeId = object.getString("cardTypeId");
                if ("H5_GOLDBILL_ASSERT".equals(cardTypeId)) {
                    //我的黄金票
                } else if ("H5_GOLDBILL_TASK".equals(cardTypeId)) {
                    //任务列表，待完成的
                    str = OtherTaskRpcCall.getValueByPath(object, "dataModel.jsonResult.tasks.todo");
                    JSONArray jsonArray2 = new JSONArray(str);
                    for (int j = 0; j < jsonArray2.length(); j++) {
                        JSONObject object2 = jsonArray2.getJSONObject(j);
                        String title = object2.getString("title");
                        if (title.contains("1元起") || title.contains("体验终身")) {
                            //跳过这种傻逼玩意
                            continue;
                        }
                        str = OtherTaskRpcCall.goldBillTrigger(object2.getString("taskId"));
                        jsonObject = new JSONObject(str);
                        if (!jsonObject.getBoolean("success")) {
                            Log.i(TAG + ".goldTicket.goldBillTrigger", jsonObject.optString("resultDesc"));
                            continue;
                        }
                        Log.other("黄金票🏦[" + title + "]" + object2.getString("subTitle"));
                    }
                } else if ("H5_GOIDBILL_EQUITY".equals(cardTypeId)) {
                    //兑换列表
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "goldTicket err:");
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
     * 收取黄金票
     */
    private void goldBillCollect(String signInfo) {
        try {
            String str = OtherTaskRpcCall.goldBillCollect(signInfo);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".goldBillCollect.goldBillCollect", jsonObject.optString("resultDesc"));
                return;
            }
            JSONObject object = jsonObject.getJSONObject("result");
            JSONArray jsonArray = object.getJSONArray("collectedList");
            int length = jsonArray.length();
            if (length == 0) {
                return;
            }
            for (int i = 0; i < length; i++) {
                Log.other("黄金票🏦[" + jsonArray.getString(i) + "]");
            }
            Log.other("黄金票🏦本次总共获得[" + OtherTaskRpcCall.getValueByPath(object, "collectedCamp.amount") + "]");
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
}