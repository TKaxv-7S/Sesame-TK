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
        return gameCenter.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 5000);
            signIn();
            doTask();
            batchReceive();
        };
    }

    /**
     * 签到
     */
    private void signIn() {
        try {
            String str = GameCenterRpcCall.querySignInBall();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".signIn.querySignInBall", jsonObject.optString("resultDesc"));
                return;
            }
            str = GameCenterRpcCall.getValueByPath(jsonObject, "data.signInBallModule.signInStatus");
            if (String.valueOf(true).equals(str)) {
                return;
            }
            str = GameCenterRpcCall.continueSignIn();
            jsonObject = new JSONObject(str);
            if (jsonObject.getBoolean("success")) {
                Log.other("游戏中心🎮签到成功");
            } else {
                Log.i(TAG + ".signIn.continueSignIn", jsonObject.optString("resultDesc"));
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
     * 全部领取
     */
    private void batchReceive() {
        try {
            String str = GameCenterRpcCall.queryPointBallList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".batchReceive.queryPointBallList", jsonObject.optString("resultDesc"));
                return;
            }
            str = GameCenterRpcCall.getValueByPath(jsonObject, "data.pointBallList");
            if (str == null || str.isEmpty() || new JSONArray(str).length() == 0) {
                return;
            }
            str = GameCenterRpcCall.batchReceivePointBall();
            jsonObject = new JSONObject(str);
            if (jsonObject.getBoolean("success")) {
                Log.other("游戏中心🎮全部领取成功[" + GameCenterRpcCall.getValueByPath(jsonObject, "data.totalAmount") + "]乐豆");
            } else {
                Log.i(TAG + ".batchReceive.batchReceivePointBall", jsonObject.optString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchReceive err:");
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
     * 做任务
     */
    private void doTask() {
        try {
            String str = GameCenterRpcCall.queryModularTaskList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".doTask.queryModularTaskList", jsonObject.optString("resultDesc"));
                return;
            }
            JSONObject result = jsonObject.getJSONObject("data");
            JSONArray taskModuleList = result.getJSONArray("taskModuleList");
            for (int i = 0; i < taskModuleList.length(); i++) {
                JSONObject taskDetail = taskModuleList.getJSONObject(i);
                JSONArray taskList = taskDetail.getJSONArray("taskList");
                for (int j = 0; j < taskList.length(); j++) {
                    result = taskList.getJSONObject(j);
                    String status = result.getString("taskStatus");
                    String taskId = result.getString("taskId");
                    //NOT_DONE
                    if (result.getBoolean("needSignUp") &&!"SIGNUP_COMPLETE".equals(status)) {
                        str = GameCenterRpcCall.doTaskSignup(taskId);
                        jsonObject = new JSONObject(str);
                        if (!jsonObject.getBoolean("success")) {
                            Log.i(TAG + ".doTask.doTaskSignup", jsonObject.optString("errorMsg"));
//                            continue; //不做跳过，尝试直接完成
                        }
                    }
                    str = GameCenterRpcCall.doTaskSend(taskId);
                    jsonObject = new JSONObject(str);
                    if (!jsonObject.getBoolean("success")) {
                        Log.i(TAG + ".doTask.doTaskSend", jsonObject.optString("errorMsg"));
                        continue;
                    }
                    Log.other("游戏中心🎮[" + result.getString("subTitle") + "-" + result.getString("title") + "]任务完成");
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "doTask err:");
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
