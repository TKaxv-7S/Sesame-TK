package tkaxv7s.xposed.sesame.model.task.antMember;

import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.Statistics;
import tkaxv7s.xposed.sesame.util.TimeUtil;
import tkaxv7s.xposed.sesame.util.UserIdMap;

/**
 * 会员
 * @author xiong
 */
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

    @Override
    public Boolean check() {
        return receivePoint.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
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

                queryAllStatusTaskList();

                if (!merchantKmdk.getValue() && !zcjSignIn.getValue()) {
                    return;
                }
                JSONObject jo = new JSONObject(AntMemberRpcCall.transcodeCheck());
                if (!jo.getBoolean("success")) {
                    return;
                }
                JSONObject data = jo.getJSONObject("data");
                if (!data.optBoolean("isOpened")) {
                    Log.record("商家服务👪未开通");
                    return;
                }
                if (zcjSignIn.getValue()) {
                    zcjSignIn();
                }
                if (merchantKmdk.getValue()) {
                    if (TimeUtil.isNowAfterTimeStr("0600") && TimeUtil.isNowBeforeTimeStr("1200")) {
                        kmdkSignIn();
                    }
                    kmdkSignUp();
                }
                taskListQuery();
            } catch (Throwable t) {
                Log.i(TAG, "init err:");
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
                if (hasNextPage) {
                    queryPointCert(page + 1, pageSize);
                }
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
                    if (!Log.getFormatDate().replace("-", "").equals(activityNo.split("_")[2])) {
                        break;
                    }
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
                    if (!task.has("status")) {
                        continue;
                    }
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
                                    // 逛一逛精彩内容
                                    taskReceive(taskCode, "XCZBJLL_VIEWED", title);
                                    break;
                                case "BBNCLLRWX_TASK":
                                    // 逛一逛芭芭农场
                                    taskReceive(taskCode, "GYG_BBNC_VIEWED", title);
                                    break;
                                case "LLSQMDLB_TASK":
                                    // 浏览收钱码大礼包
                                    taskReceive(taskCode, "LL_SQMDLB_VIEWED", title);
                                    break;
                                case "SYH_CPC_FIXED_2":
                                    // 逛一逛商品橱窗
                                    taskReceive(taskCode, "MRCH_CPC_FIXED_VIEWED", title);
                                    break;
                                case "SYH_CPC_ALMM_1":
                                    taskReceive(taskCode, "MRCH_CPC_ALMM_VIEWED", title);
                                    break;
                                case "TJBLLRW_TASK":
                                    // 逛逛淘金币，购物可抵钱
                                    taskReceive(taskCode, "TJBLLRW_TASK_VIEWED", title);
                                    break;
                                case "HHKLLRW_TASK":
                                    // 49999元花呗红包集卡抽
                                    taskReceive(taskCode, "HHKLLX_VIEWED", title);
                                    break;
                                case "ZCJ_VIEW_TRADE":
                                    // 浏览攻略，赚商家积分
                                    taskReceive(taskCode, "ZCJ_VIEW_TRADE_VIEWED", title);
                                    break;
                            }
                        }
                    }
                }
                if (doubleCheck) {
                    taskListQuery();
                }
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

    /**
     * 做任务赚积分
     */
    private void signPageTaskList() {
        try {
            String s = AntMemberRpcCall.signPageTaskList();
            JSONObject jo = new JSONObject(s);
            boolean doubleCheck = false;
            if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                Log.i(TAG, "queryAllStatusTaskList err:" + jo.getString("resultDesc"));
                return;
            }
            if (!jo.has("categoryTaskList")) {
                return;
            }
            JSONArray categoryTaskList = jo.getJSONArray("categoryTaskList");
            for (int i = 0; i < categoryTaskList.length(); i++) {
                jo = categoryTaskList.getJSONObject(i);
                if (!"BROWSE".equals(jo.getString("type"))) {
                    continue;
                }
                JSONArray taskList = jo.getJSONArray("taskList");
                doubleCheck = doTask(taskList);
            }
            if (doubleCheck) {
                signPageTaskList();
            }
        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 查询所有状态任务列表
     */
    private void queryAllStatusTaskList() {
        try {
            String str = AntMemberRpcCall.queryAllStatusTaskList();
            JSONObject jsonObject = new JSONObject(str);
            if (!"SUCCESS".equals(jsonObject.getString("resultCode"))) {
                Log.i(TAG, "queryAllStatusTaskList err:" + jsonObject.getString("resultDesc"));
                return;
            }
            if (!jsonObject.has("availableTaskList")) {
                return;
            }
            if (doTask(jsonObject.getJSONArray("availableTaskList"))) {
                queryAllStatusTaskList();
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryAllStatusTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 做浏览任务
     *
     * @param taskList 任务列表
     * @return 是否再次检查
     */
    private boolean doTask(JSONArray taskList) {
        boolean doubleCheck = false;
        try {
            for (int j = 0; j < taskList.length(); j++) {
                JSONObject task = taskList.getJSONObject(j);
                int count = 1;
                boolean hybrid = task.getBoolean("hybrid");
                int periodCurrentCount = 0;
                int periodTargetCount = 0;
                if (hybrid) {
                    periodCurrentCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_CURRENT_COUNT"));
                    periodTargetCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_TARGET_COUNT"));
                    count = periodTargetCount > periodCurrentCount ? periodTargetCount - periodCurrentCount : 0;
                }
                if (count <= 0) {
                    continue;
                }
                JSONObject taskConfigInfo = task.getJSONObject("taskConfigInfo");
                String name = taskConfigInfo.getString("name");
                Long id = taskConfigInfo.getLong("id");
                String awardParamPoint = taskConfigInfo.getJSONObject("awardParam")
                        .getString("awardParamPoint");
                String targetBusiness = taskConfigInfo.getJSONArray("targetBusiness").getString(0);
                for (int k = 0; k < count; k++) {
                    JSONObject jo = new JSONObject(AntMemberRpcCall.applyTask(name, id));
                    if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.i(TAG, "signPageTaskList.applyTask err:" + jo.optString("resultDesc"));
                        continue;
                    }
                    TimeUtil.sleep(1500);
                    String[] targetBusinessArray = targetBusiness.split("#");
                    String bizParam;
                    String bizSubType;
                    if (targetBusinessArray.length > 2) {
                        bizParam = targetBusinessArray[2];
                        bizSubType = targetBusinessArray[1];
                    } else {
                        bizParam = targetBusinessArray[1];
                        bizSubType = targetBusinessArray[0];
                    }
                    jo = new JSONObject(AntMemberRpcCall.executeTask(bizParam, bizSubType));
                    if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.i(TAG, "signPageTaskList.executeTask err:" + jo.optString("resultDesc"));
                        continue;
                    }
                    String ex = "";
                    if (hybrid) {
                        ex = "(" + (periodCurrentCount + k + 1) + "/" + periodTargetCount + ")";
                    }
                    Log.other("会员任务🎖️[" + name + ex + "]#" + awardParamPoint + "积分");
                    doubleCheck = true;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
        return doubleCheck;
    }

}
