package tkaxv7s.xposed.sesame.task.model.gameCenter;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.task.base.TaskCommon;
import tkaxv7s.xposed.sesame.task.common.rpcCall.BaseTaskRpcCall;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 游戏中心
 *
 * @author xiong
 */
public class GameCenter extends ModelTask {

    private static final String TAG = GameCenter.class.getSimpleName();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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

    @Override
    public Boolean check() {
        return gameCenter.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
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
            str = BaseTaskRpcCall.getValueByPath(jsonObject, "data.signInBallModule.signInStatus");
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
            str = BaseTaskRpcCall.getValueByPath(jsonObject, "data.pointBallList");
            if (str == null || str.isEmpty() || new JSONArray(str).length() == 0) {
                return;
            }
            str = GameCenterRpcCall.batchReceivePointBall();
            jsonObject = new JSONObject(str);
            if (jsonObject.getBoolean("success")) {
                Log.other("游戏中心🎮全部领取成功[" + BaseTaskRpcCall.getValueByPath(jsonObject, "data.totalAmount") + "]乐豆");
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
            JSONObject object = jsonObject.getJSONObject("data");
            JSONArray taskModuleList = object.getJSONArray("taskModuleList");
            for (int i = 0; i < taskModuleList.length(); i++) {
                JSONObject taskDetail = taskModuleList.getJSONObject(i);
                JSONArray taskList = taskDetail.getJSONArray("taskList");
                int length = taskList.length();
                // 使用 ScheduledExecutorService 循环执行任务
                for (int j = 0; j < length; j++) {
                    // 需要将 j 声明为 final 或 effectively final
                    final int finalJ = j;
                    scheduler.schedule(() -> {
                        try {
                            JSONObject result = taskList.getJSONObject(finalJ);
                            String status = result.getString("taskStatus");
                            String taskId = result.getString("taskId");
                            if (result.getBoolean("needSignUp") && !"SIGNUP_COMPLETE".equals(status)) {
                                String signUpResult = GameCenterRpcCall.doTaskSignup(taskId);
                                JSONObject signUpJson = new JSONObject(signUpResult);
                                if (!signUpJson.getBoolean("success")) {
                                    Log.i(TAG + ".doTask.doTaskSignup", signUpJson.optString("errorMsg"));
                                }
                            }

                            String sendResult = GameCenterRpcCall.doTaskSend(taskId);
                            JSONObject sendJson = new JSONObject(sendResult);
                            if (!sendJson.getBoolean("success")) {
                                Log.i(TAG + ".doTask.doTaskSend", sendJson.optString("errorMsg"));
                                // 跳过本次迭代
                                return;
                            }
                            Log.other("游戏中心🎮[" + result.getString("subTitle") + "-" + result.getString("title") + "]任务完成");
                        } catch (Throwable th) {
                            Log.i(TAG, "doTask err:");
                            Log.printStackTrace(TAG, th);
                        }
                    }, ((long) i * (j + length) + j) * executeIntervalInt, TimeUnit.MILLISECONDS);
                }
            }
            // 关闭线程池，可以使用 awaitTermination 方法等待任务执行完毕
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    Log.i(TAG, "doTask err:线程池超时关闭");
                }
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
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
