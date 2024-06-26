package tkaxv7s.xposed.sesame.model.task.consumeGold;

import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.RuntimeInfo;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;

public class ConsumeGold extends ModelTask {
    private static final String TAG = ConsumeGold.class.getSimpleName();

    @Override
    public String setName() {
        return "消费金";
    }

    private BooleanModelField consumeGold;

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(consumeGold = new BooleanModelField("consumeGold", "开启消费金", false));
        return modelFields;
    }

    public Boolean check() {
        if (!consumeGold.getValue()) {
            return false;
        }
        if (TaskCommon.IS_ENERGY_TIME) {
            return false;
        }
        long executeTime = RuntimeInfo.getInstance().getLong("consumeGold", 0);
        return System.currentTimeMillis() - executeTime >= 21600000;
    }

    public void run() {
        try {
            RuntimeInfo.getInstance().put("consumeGold", System.currentTimeMillis());
            signinCalendar();
            taskV2Index("CG_TASK_LIST");
            taskV2Index("HOME_NAVIGATION");
            taskV2Index("CG_SIGNIN_AD_FEEDS");
            taskV2Index("SURPRISE_TASK");
            taskV2Index("CG_BROWSER_AD_FEEDS");
            consumeGoldIndex();
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void taskV2Index(String taskSceneCode) {
        boolean doubleCheck = false;
        try {
            String s = ConsumeGoldRpcCall.taskV2Index(taskSceneCode);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONArray taskList = jo.getJSONArray("taskList");
                for (int i = 0; i < taskList.length(); i++) {
                    jo = taskList.getJSONObject(i);
                    JSONObject extInfo = jo.getJSONObject("extInfo");
                    String taskStatus = extInfo.getString("taskStatus");
                    String title = extInfo.getString("title");
                    String taskId = extInfo.getString("actionBizId");
                    if ("TO_RECEIVE".equals(taskStatus)) {
                        taskV2TriggerReceive(taskId, title);
                    } else if ("NONE_SIGNUP".equals(taskStatus)) {
                        taskV2TriggerSignUp(taskId);
                        Thread.sleep(1000L);
                        taskV2TriggerSend(taskId);
                        doubleCheck = true;
                    } else if ("SIGNUP_COMPLETE".equals(taskStatus)) {
                        taskV2TriggerSend(taskId);
                        doubleCheck = true;
                    }
                }
                if (doubleCheck)
                    taskV2Index(taskSceneCode);
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskV2Index err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void taskV2TriggerReceive(String taskId, String name) {
        try {
            String s = ConsumeGoldRpcCall.taskV2TriggerReceive(taskId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                int receiveAmount = jo.getInt("receiveAmount");
                Log.other("赚消费金💰[" + name + "]#" + receiveAmount);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskV2TriggerReceive err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void taskV2TriggerSignUp(String taskId) {
        try {
            String s = ConsumeGoldRpcCall.taskV2TriggerSignUp(taskId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {

            }
        } catch (Throwable t) {
            Log.i(TAG, "taskV2TriggerSignUp err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void taskV2TriggerSend(String taskId) {
        try {
            String s = ConsumeGoldRpcCall.taskV2TriggerSend(taskId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {

            }
        } catch (Throwable t) {
            Log.i(TAG, "taskV2TriggerSend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void consumeGoldIndex() {
        try {
            String s = ConsumeGoldRpcCall.consumeGoldIndex();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject homePromoInfoDTO = jo.getJSONObject("homePromoInfoDTO");
                JSONArray homePromoTokenDTOList = homePromoInfoDTO.getJSONArray("homePromoTokenDTOList");
                int tokenLeftAmount = 0;
                int tokenTotalAmount = 0;
                for (int i = 0; i < homePromoTokenDTOList.length(); i++) {
                    jo = homePromoTokenDTOList.getJSONObject(i);
                    String tokenType = jo.getString("tokenType");
                    if ("CONSUME_GOLD".equals(tokenType)) {
                        tokenLeftAmount = jo.getInt("tokenLeftAmount");
                    }
                }
                if (tokenLeftAmount > 0) {
                    for (int j = 0; j < tokenLeftAmount; j++) {
                        jo = new JSONObject(ConsumeGoldRpcCall.promoTrigger());
                        if (jo.getBoolean("success")) {
                            JSONObject homePromoPrizeInfoDTO = jo.getJSONObject("homePromoPrizeInfoDTO");
                            int quantity = homePromoPrizeInfoDTO.getInt("quantity");
                            Log.other("赚消费金💰[投5币抽]#" + quantity);
                            if (homePromoPrizeInfoDTO.has("promoAdvertisementInfo")) {
                                JSONObject promoAdvertisementInfo = homePromoPrizeInfoDTO
                                        .getJSONObject("promoAdvertisementInfo");
                                String outBizNo = promoAdvertisementInfo.getString("outBizNo");
                                jo = new JSONObject(ConsumeGoldRpcCall.advertisement(outBizNo));
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryTreasureBox err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void signinCalendar() {
        try {
            String s = ConsumeGoldRpcCall.signinCalendar();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                boolean signed = jo.getBoolean("isSignInToday");
                if (!signed) {
                    jo = new JSONObject(ConsumeGoldRpcCall.openBoxAward());
                    if (jo.getBoolean("success")) {
                        int amount = jo.getInt("amount");
                        Log.other("消费金签到💰[" + amount + "金币]");
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signinCalendar err:");
            Log.printStackTrace(TAG, t);
        }
    }
}
