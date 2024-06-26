package tkaxv7s.xposed.sesame.model.task.otherTask;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.TimeUtil;


/**
 * 其他任务
 *
 * @author xiong
 */
public class OtherTask extends ModelTask {
    private static final String TAG = OtherTask.class.getSimpleName();

    private BooleanModelField enable;
    /**
     * 间隔时间
     */
    private Integer executeIntervalInt;
    /**
     * 黄金票
     */
    private BooleanModelField goldTicket;
    private IntegerModelField executeInterval;
    /**
     * 车神卡
     */
    private BooleanModelField carGodCard;

    /**
     * 实体红包
     */
    private BooleanModelField promoprodRedEnvelope;

    @Override
    public String setName() {
        return "其他任务";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(enable = new BooleanModelField("enable", "开启其他任务", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 2000));
        modelFields.addField(goldTicket = new BooleanModelField("goldTicket", "开启 | 黄金票", true));
        modelFields.addField(carGodCard = new BooleanModelField("carGodCard", "开启 | 车神卡", true));
        modelFields.addField(promoprodRedEnvelope = new BooleanModelField("promoprodRedEnvelope", "开启 | 实体红包", true));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return enable.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        executeIntervalInt = Math.max(executeInterval.getValue(), 2000);
        if (promoprodRedEnvelope.getValue()) {
            promoprodTaskList();
        }
        if (goldTicket.getValue()) {
            //签到
            goldBillCollect("\"campId\":\"CP1417744\",\"directModeDisableCollect\":true,\"from\":\"antfarm\",");
            //摆设
//                goldTicket();
            //收取其他
            goldBillCollect("");
        }
        if (carGodCard.getValue()) {
            carGodCardbenefit();
        }
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
                    JSONArray jsonArray2 = (JSONArray) JsonUtil.getValueByPathObject(object, "dataModel.jsonResult.tasks.todo");
                    if (jsonArray2 == null) {
                        continue;
                    }
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
                Log.other("黄金票🙈[" + jsonArray.getString(i) + "]");
            }
            Log.other("黄金票🏦本次总共获得[" + JsonUtil.getValueByPath(object, "collectedCamp.amount") + "]");
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
     * 车神卡领奖
     */
    private void carGodCardbenefit() {
        try {
            while (true) {
                String str = OtherTaskRpcCall.v1benefitQuery();
                JSONObject jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".carGodCardbenefit.v1benefitQuery", jsonObject.optString("resultDesc"));
                    return;
                }
                JSONObject object = jsonObject.getJSONObject("data");
                JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(object, "result.aggregationOfferInfos");
                if (jsonArray == null || jsonArray.length() == 0) {
                    return;
                }
                jsonObject = new JSONObject();
                jsonObject.put("args", new JSONObject().put("offerRequest", jsonArray));
                str = OtherTaskRpcCall.v1benefitTrigger(jsonObject);
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".carGodCardbenefit.v1benefitTrigger", jsonObject.optString("resultDesc"));
                    continue;
                }
                jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.result");
                if (jsonArray == null) {
                    continue;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    str = "车神卡🏎获得[" + jsonObject.getString("name");
                    if (jsonObject.has("memo")) {
                        str += "-" + jsonObject.getString("memo");
                    }
                    str += "]" + jsonObject.getString("price") + jsonObject.getString("unit");
                    Log.other(str);
                }
                TimeUtil.sleep(executeIntervalInt);
            }
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
     * 实体红包
     */
    private void promoprodTaskList() {
        try {
            String str = OtherTaskRpcCall.queryTaskList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".queryTaskList", jsonObject.optString("resultDesc"));
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("taskDetailList");
            int length = jsonArray.length();
            if (length == 0) {
                return;
            }
            for (int i = 0; i < length; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if ("SIGNUP_COMPLETE".equals(object.getString("taskProcessStatus"))) {
                    continue;
                }
                str = OtherTaskRpcCall.signup(JsonUtil.getValueByPath(object, "taskParticipateExtInfo.gplusItem"), object.getString("taskId"));
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".queryTaskList.signup", jsonObject.optString("errorMsg"));
                }
                TimeUtil.sleep(executeIntervalInt);
                str = OtherTaskRpcCall.complete(object.getString("taskId"));
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".queryTaskList.complete", jsonObject.optString("errorMsg"));
                    continue;
                }
                Log.other("实体红包🍷获取[" + jsonObject.getString("ariverRpcTraceId") + "]" + JsonUtil.getValueByPath(jsonObject, "prizeSendInfo.price.amount") + "元");
                TimeUtil.sleep(executeIntervalInt);
            }
        } catch (Throwable th) {
            Log.i(TAG, "queryTaskList err:");
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