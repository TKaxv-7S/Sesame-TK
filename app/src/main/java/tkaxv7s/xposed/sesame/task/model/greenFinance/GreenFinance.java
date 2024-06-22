package tkaxv7s.xposed.sesame.task.model.greenFinance;

import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.task.model.welfareCenter.WelfareCenterRpcCall;
import tkaxv7s.xposed.sesame.util.Log;

import static tkaxv7s.xposed.sesame.task.model.BaseRpcCall.getValueByPath;

/**
 * @author Constanline
 * @since 2023/09/08
 */
public class GreenFinance extends ModelTask {
    private static final String TAG = GreenFinance.class.getSimpleName();

    private Integer executeIntervalInt;
    private BooleanModelField greenFinance;
    private IntegerModelField executeInterval;

    @Override
    public String setName() {
        return "绿色经营";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(greenFinance = new BooleanModelField("greenFinance", "开启绿色经营", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 5000));
        return modelFields;
    }

    public Boolean check() {
        return greenFinance.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 5000);
            String s = GreenFinanceRpcCall.greenFinanceIndex();
            try {
                JSONObject jo = new JSONObject(s);
                if (!jo.getBoolean("success")) {
                    Log.i(TAG, jo.getString("resultDesc"));
                    return;
                }
                JSONObject result = jo.getJSONObject("result");
                if (!result.getBoolean("greenFinanceSigned")) {
                    Log.other("绿色经营📊未开通");
                    return;
                }
                JSONObject mcaGreenLeafResult = result.getJSONObject("mcaGreenLeafResult");
                JSONArray greenLeafList = mcaGreenLeafResult.getJSONArray("greenLeafList");
                String currentCode = "";
                JSONArray bsnIds = new JSONArray();
                for (int i = 0; i < greenLeafList.length(); i++) {
                    JSONObject greenLeaf = greenLeafList.getJSONObject(i);
                    String code = greenLeaf.getString("code");
                    if (currentCode.equals(code) || bsnIds.length() == 0) {
                        bsnIds.put(greenLeaf.getString("bsnId"));
                    } else {
                        batchSelfCollect(bsnIds);
                        bsnIds = new JSONArray();
                    }
                }
                if (bsnIds.length() > 0) {
                    batchSelfCollect(bsnIds);
                }
            } catch (Throwable th) {
                Log.i(TAG, "index err:");
                Log.printStackTrace(TAG, th);
            }

            signIn("PLAY102632271");
            signIn("PLAY102932217");
            signIn("PLAY102232206");

            String appletId = "AP13159535";
            doTask(appletId);

            doTick();
        };
    }

    private void batchSelfCollect(JSONArray bsnIds) {
        String s = GreenFinanceRpcCall.batchSelfCollect(bsnIds);
        try {
            JSONObject joSelfCollect = new JSONObject(s);
            if (joSelfCollect.getBoolean("success")) {
                int totalCollectPoint = joSelfCollect.getJSONObject("result").getInt("totalCollectPoint");
                Log.other("绿色经营📊收集获得" + totalCollectPoint);
            } else {
                Log.i(TAG + ".batchSelfCollect", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchSelfCollect err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void signIn(String sceneId) {
        try {
            String s = GreenFinanceRpcCall.signInQuery(sceneId);
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".signIn", s);
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            if (result.getBoolean("isTodaySignin")) {
                return;
            }
            s = GreenFinanceRpcCall.signInTrigger(sceneId);
            jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                Log.other("绿色经营📊签到成功");
            } else {
                Log.i(TAG + ".signIn", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void doTask(String appletId) {
        try {
            String s = GreenFinanceRpcCall.taskQuery(appletId);
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
                        Log.other("绿色经营📊" + getValueByPath(taskDetail, "taskExtProps.TASK_MORPHO_DETAIL.title") + " 任务完成");
                        break;
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "doTask err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /**
     * 记录绿色行为-打卡
     */
    private void doTick() {
        try {
            String str = GreenFinanceRpcCall.queryUserTickItem();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".doTick", str);
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if ("Y".equals(jsonObject.getString("status"))) {
                    continue;
                }
                str = GreenFinanceRpcCall.submitTick(jsonObject.getString("behaviorCode"));
                JSONObject object = new JSONObject(str);
                if (!object.getBoolean("success") ||
                        !String.valueOf(true).equals(getValueByPath(object, "result.result"))) {
                    Log.i(TAG + ".doTick", str);
                    continue;
                }
                Log.other("绿色经营📊" + jsonObject.getString("title") + "打卡成功");
                Thread.sleep(500);
            }
        } catch (Throwable th) {
            Log.i(TAG, "doTick err:");
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