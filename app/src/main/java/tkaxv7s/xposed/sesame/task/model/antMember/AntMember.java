package tkaxv7s.xposed.sesame.task.model.antMember;

import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.Statistics;
import tkaxv7s.xposed.sesame.util.TimeUtil;
import tkaxv7s.xposed.sesame.util.UserIdMap;

public class AntMember extends ModelTask {
    private static final String TAG = AntMember.class.getSimpleName();

    @Override
    public String setName() {
        return "会员";
    }
    public BooleanModelField receivePoint;
    public BooleanModelField zcjSignIn;
    public BooleanModelField merchantKmdk;

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(receivePoint = new BooleanModelField("receivePoint", "开启会员", false));
        modelFields.addField(zcjSignIn = new BooleanModelField("zcjSignIn", "招财金签到", false));
        modelFields.addField(merchantKmdk = new BooleanModelField("merchantKmdk", "商户开门打卡", false));
        return modelFields;
    }

    public Boolean check() {
        return receivePoint.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            try {
                if (Statistics.canMemberSignInToday(UserIdMap.getCurrentUid())) {
                    String s = AntMemberRpcCall.queryMemberSigninCalendar();
                    JSONObject jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.other("每日签到📅[" + jo.getString("signinPoint") + "积分]#已签到" + jo.getString("signinSumDay")
                                + "天");
                        Statistics.memberSignInToday(UserIdMap.getCurrentUid());
                    } else {
                        Log.record(jo.getString("resultDesc"));
                        Log.i(s);
                    }
                }

                queryPointCert(1, 8);

                signPageTaskList();

                if (merchantKmdk.getValue() || zcjSignIn.getValue()) {
                    JSONObject jo = new JSONObject(AntMemberRpcCall.transcodeCheck());
                    if (jo.getBoolean("success")) {
                        JSONObject data = jo.getJSONObject("data");
                        if (data.optBoolean("isOpened")) {
                            if (zcjSignIn.getValue())
                                zcjSignIn();

                            if (merchantKmdk.getValue()) {
                                if (TimeUtil.isNowAfterTimeStr("0600")
                                        && TimeUtil.isNowBeforeTimeStr("1200"))
                                    kmdkSignIn();
                                kmdkSignUp();
                            }
                            taskListQuery();
                        } else {
                            Log.record("商家服务未开通！");
                        }
                    }
                }
            } catch (Throwable t) {
                Log.i(TAG, "receivePoint.run err:");
                Log.printStackTrace(TAG, t);
            }
        };
    }

    private static void queryPointCert(int page, int pageSize) {
        try {
            String s = AntMemberRpcCall.queryPointCert(page, pageSize);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                boolean hasNextPage = jo.getBoolean("hasNextPage");
                JSONArray jaCertList = jo.getJSONArray("certList");
                for (int i = 0; i < jaCertList.length(); i++) {
                    jo = jaCertList.getJSONObject(i);
                    String bizTitle = jo.getString("bizTitle");
                    String id = jo.getString("id");
                    int pointAmount = jo.getInt("pointAmount");
                    s = AntMemberRpcCall.receivePointByUser(id);
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.other("领取奖励🎖️[" + bizTitle + "]#" + pointAmount + "积分");
                    } else {
                        Log.record(jo.getString("resultDesc"));
                        Log.i(s);
                    }
                }
                if (hasNextPage)
                    queryPointCert(page + 1, pageSize);
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryPointCert err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignIn() {
        try {
            String s = AntMemberRpcCall.queryActivity();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                if ("SIGN_IN_ENABLE".equals(jo.getString("signInStatus"))) {
                    String activityNo = jo.getString("activityNo");
                    JSONObject joSignIn = new JSONObject(AntMemberRpcCall.signIn(activityNo));
                    if (joSignIn.getBoolean("success")) {
                        Log.other("商家服务🕴🏻[开门打卡签到成功]");
                    } else {
                        Log.record(joSignIn.getString("errorMsg"));
                        Log.i(joSignIn.toString());
                    }
                }
            } else {
                Log.record("queryActivity" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignUp() {
        try {
            for (int i = 0; i < 5; i++) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.queryActivity());
                if (jo.getBoolean("success")) {
                    String activityNo = jo.getString("activityNo");
                    if (!Log.getFormatDate().replace("-", "").equals(activityNo.split("_")[2]))
                        break;
                    if ("SIGN_UP".equals(jo.getString("signUpStatus"))) {
                        Log.record("开门打卡今日已报名！");
                        break;
                    }
                    if ("UN_SIGN_UP".equals(jo.getString("signUpStatus"))) {
                        String activityPeriodName = jo.getString("activityPeriodName");
                        JSONObject joSignUp = new JSONObject(AntMemberRpcCall.signUp(activityNo));
                        if (joSignUp.getBoolean("success")) {
                            Log.other("商家服务🕴🏻[" + activityPeriodName + "开门打卡报名]");
                            return;
                        } else {
                            Log.record(joSignUp.getString("errorMsg"));
                            Log.i(joSignUp.toString());
                        }
                    }
                } else {
                    Log.record("queryActivity");
                    Log.i(jo.toString());
                }
                Thread.sleep(500);
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignUp err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void zcjSignIn() {
        try {
            String s = AntMemberRpcCall.zcjSignInQuery();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject button = jo.getJSONObject("data").getJSONObject("button");
                if ("UNRECEIVED".equals(button.getString("status"))) {
                    jo = new JSONObject(AntMemberRpcCall.zcjSignInExecute());
                    if (jo.getBoolean("success")) {
                        JSONObject data = jo.getJSONObject("data");
                        int todayReward = data.getInt("todayReward");
                        String widgetName = data.getString("widgetName");
                        Log.other("商家服务🕴🏻[" + widgetName + "]#" + todayReward + "积分");
                    }
                }
            } else {
                Log.record("zcjSignInQuery" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "zcjSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 商家服务任务 */
    private static void taskListQuery() {
        String s = AntMemberRpcCall.taskListQuery();
        try {
            boolean doubleCheck = false;
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONArray taskList = jo.getJSONObject("data").getJSONArray("taskList");
                for (int i = 0; i < taskList.length(); i++) {
                    JSONObject task = taskList.getJSONObject(i);
                    if (!task.has("status"))
                        continue;
                    String title = task.getString("title");
                    String reward = task.getString("reward");
                    String taskStatus = task.getString("status");
                    if ("NEED_RECEIVE".equals(taskStatus)) {
                        if (task.has("pointBallId")) {
                            jo = new JSONObject(AntMemberRpcCall.ballReceive(task.getString("pointBallId")));
                            if (jo.getBoolean("success")) {
                                Log.other("商家服务🕴🏻[" + title + "]#" + reward);
                            }
                        }
                    } else if ("PROCESSING".equals(taskStatus) || "UNRECEIVED".equals(taskStatus)) {
                        if (task.has("extendLog")) {
                            JSONObject bizExtMap = task.getJSONObject("extendLog").getJSONObject("bizExtMap");
                            jo = new JSONObject(AntMemberRpcCall.taskFinish(bizExtMap.getString("bizId")));
                            if (jo.getBoolean("success")) {
                                Log.other("商家服务🕴🏻[" + title + "]#" + reward);
                            }
                            doubleCheck = true;
                        } else {
                            String taskCode = task.getString("taskCode");
                            switch (taskCode) {
                                case "XCZBJLLRWCS_TASK":
                                    taskReceive(taskCode, "XCZBJLL_VIEWED", title);//逛一逛精彩内容
                                    break;
                                case "BBNCLLRWX_TASK":
                                    taskReceive(taskCode, "GYG_BBNC_VIEWED", title);// 逛一逛芭芭农场
                                    break;
                                case "LLSQMDLB_TASK":
                                    taskReceive(taskCode, "LL_SQMDLB_VIEWED", title);//浏览收钱码大礼包
                                    break;
                                case "SYH_CPC_FIXED_2":
                                    taskReceive(taskCode, "MRCH_CPC_FIXED_VIEWED", title);// 逛一逛商品橱窗
                                    break;
                                case "SYH_CPC_ALMM_1":
                                    taskReceive(taskCode, "MRCH_CPC_ALMM_VIEWED", title);
                                    break;
                                case "TJBLLRW_TASK":
                                    taskReceive(taskCode, "TJBLLRW_TASK_VIEWED", title);// 逛逛淘金币，购物可抵钱
                                    break;
                                case "HHKLLRW_TASK":
                                    taskReceive(taskCode, "HHKLLX_VIEWED", title);// 49999元花呗红包集卡抽
                                    break;
                                case "ZCJ_VIEW_TRADE":
                                    taskReceive(taskCode, "ZCJ_VIEW_TRADE_VIEWED", title);// 浏览攻略，赚商家积分
                                    break;
                            }
                        }
                    }
                }
                if (doubleCheck)
                    taskListQuery();
            } else {
                Log.i("taskListQuery err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskListQuery err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void taskReceive(String taskCode, String actionCode, String title) {
        try {
            String s = AntMemberRpcCall.taskReceive(taskCode);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                jo = new JSONObject(AntMemberRpcCall.actioncode(actionCode));
                if (jo.getBoolean("success")) {
                    jo = new JSONObject(AntMemberRpcCall.produce(actionCode));
                    if (jo.getBoolean("success")) {
                        Log.other("完成任务🕴🏻[" + title + "]");
                    }
                }
            } else {
                Log.record("taskReceive" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskReceive err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void signPageTaskList() {
        try {
            String s = AntMemberRpcCall.signPageTaskList();
            JSONObject jo = new JSONObject(s);
            boolean doubleCheck = false;
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                if (!jo.has("categoryTaskList")) {
                    return;
                }
                JSONArray categoryTaskList = jo.getJSONArray("categoryTaskList");
                for (int i = 0; i < categoryTaskList.length(); i++) {
                    jo = categoryTaskList.getJSONObject(i);
                    if (!"BROWSE".equals(jo.getString("type")))
                        continue;
                    JSONArray taskList = jo.getJSONArray("taskList");
                    for (int j = 0; j < taskList.length(); j++) {
                        JSONObject task = taskList.getJSONObject(j);
                        int count = 1;
                        boolean hybrid = task.getBoolean("hybrid");
                        int PERIOD_CURRENT_COUNT = 0;
                        int PERIOD_TARGET_COUNT = 0;
                        if (hybrid) {
                            PERIOD_CURRENT_COUNT = Integer
                                    .parseInt(task.getJSONObject("extInfo").getString("PERIOD_CURRENT_COUNT"));
                            PERIOD_TARGET_COUNT = Integer
                                    .parseInt(task.getJSONObject("extInfo").getString("PERIOD_TARGET_COUNT"));
                            if (PERIOD_TARGET_COUNT > PERIOD_CURRENT_COUNT) {
                                count = PERIOD_TARGET_COUNT - PERIOD_CURRENT_COUNT;
                            } else {
                                count = 0;
                            }
                        }
                        if (count > 0) {
                            JSONObject taskConfigInfo = task.getJSONObject("taskConfigInfo");
                            String name = taskConfigInfo.getString("name");
                            Long id = taskConfigInfo.getLong("id");
                            String awardParamPoint = taskConfigInfo.getJSONObject("awardParam")
                                    .getString("awardParamPoint");
                            String targetBusiness = taskConfigInfo.getJSONArray("targetBusiness").getString(0);
                            for (int k = 0; k < count; k++) {
                                jo = new JSONObject(AntMemberRpcCall.applyTask(name, id));
                                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                    Thread.sleep(1000);
                                    String[] targetBusinessArray = targetBusiness.split("#");
                                    jo = new JSONObject(AntMemberRpcCall.executeTask(targetBusinessArray[2],
                                            targetBusinessArray[1]));
                                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                        String ex = "";
                                        if (hybrid) {
                                            ex = "(" + (PERIOD_CURRENT_COUNT + k + 1) + "/"
                                                    + PERIOD_TARGET_COUNT + ")";
                                        }
                                        Log.other("会员任务🎖️[" + name + ex + "]#" + awardParamPoint + "积分");
                                        doubleCheck = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (doubleCheck)
                    signPageTaskList();
            } else {
                Log.record(jo.getString("resultCode"));
                Log.i(s);
            }

        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
