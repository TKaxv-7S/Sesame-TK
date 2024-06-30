package tkaxv7s.xposed.sesame.model.task.antMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.*;

import java.util.Iterator;

/**
 * 会员
 *
 * @author xiong
 */
public class AntMember extends ModelTask {
    private static final String TAG = AntMember.class.getSimpleName();

    @Override
    public String getName() {
        return "会员";
    }

    private BooleanModelField memberSign;
    private BooleanModelField collectSesame;
    private BooleanModelField collectSecurityFund;
    private BooleanModelField promiseSportsRoute;
    private BooleanModelField enableKb;
    private BooleanModelField enableGoldTicket;
    private BooleanModelField enableGameCenter;
    private BooleanModelField zcjSignIn;
    private BooleanModelField merchantKmdk;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(memberSign = new BooleanModelField("memberSign", "会员签到", false));
        modelFields.addField(collectSesame = new BooleanModelField("collectSesame", "芝麻粒领取", false));
        modelFields.addField(collectSecurityFund = new BooleanModelField("collectSecurityFund", "芝麻粒坚持攒保障金(可开启持续做)", false));
        modelFields.addField(promiseSportsRoute = new BooleanModelField("promiseSportsRoute", "芝麻粒坚持锻炼，走运动路线(只自动加入任务)", false));
        modelFields.addField(enableKb = new BooleanModelField("enableKb", "口碑签到", false));
        modelFields.addField(enableGoldTicket = new BooleanModelField("enableGoldTicket", "黄金票签到", false));
        modelFields.addField(enableGameCenter = new BooleanModelField("enableGameCenter", "游戏中心签到", false));
        modelFields.addField(zcjSignIn = new BooleanModelField("zcjSignIn", "招财金签到", false));
        modelFields.addField(merchantKmdk = new BooleanModelField("merchantKmdk", "商户开门打卡", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (memberSign.getValue()) {
                memberSign();
            }
            if (collectSesame.getValue()) {
                collectSesame();
            }
            if (collectSecurityFund.getValue()) {
                collectSecurityFund();
            }
            if (enableKb.getValue()) {
                kbMember();
            }
            if (enableGoldTicket.getValue()) {
                goldTicket();
            }
            if (enableGameCenter.getValue()) {
                enableGameCenter();
            }
            if (zcjSignIn.getValue() || merchantKmdk.getValue()) {
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
                    taskListQuery();
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private void memberSign() {
        try {
            if (Status.canMemberSignInToday(UserIdMap.getCurrentUid())) {
                String s = AntMemberRpcCall.queryMemberSigninCalendar();
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.other("每日签到📅[" + jo.getString("signinPoint") + "积分]#已签到" + jo.getString("signinSumDay")
                            + "天");
                    Status.memberSignInToday(UserIdMap.getCurrentUid());
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(s);
                }
            }

            queryPointCert(1, 8);

            signPageTaskList();

            queryAllStatusTaskList();
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
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
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
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
            do {
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
                        continue;
                    }
                    break;
                } finally {
                    TimeUtil.sleep(1000);
                }
            } while (true);
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

    private void collectSecurityFund() {
        try {
            //模拟从生活记录->明细->任务->明细（两次，不知原因）
            String str = AntMemberRpcCall.promiseQueryHome();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".doPromise.promiseQueryHome", jsonObject.optString("errorMsg"));
                return;
            }
            JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.processingPromises");
            if (jsonArray == null) {
                return;
            }
            boolean isSportsRoute = true;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String recordId = jsonObject.getString("recordId");
                //如果当天任务做完后就结束了，则可以再继续一次，缩短任务时间。
                boolean isRepeat = jsonObject.getInt("totalNums") - jsonObject.getInt("finishNums") == 1;
                String promiseName = jsonObject.getString("promiseName");
                if ("坚持攒保障金".equals(promiseName) && collectSecurityFund.getValue()) {
                    promiseQueryDetail(recordId);
                    securityFund(isRepeat, recordId);
                    promiseQueryDetail(recordId);
                    promiseQueryDetail(recordId);
                }
                if ("坚持锻炼，走运动路线".equals(promiseName)) {
                    //已经加入了，运动会自动行走，暂不做处理
                    isSportsRoute = false;
                }
            }
            if (isSportsRoute && promiseSportsRoute.getValue()) {
                promiseSportsRoute();
            }
        } catch (Throwable t) {
            Log.i(TAG, "doPromise err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    private void promiseSportsRoute() {
        try {
            String str = AntMemberRpcCall.promiseJoin("{\"autoRenewStatus\":false,\"dataSourceRule\":{\"selectValue\":\"alipay_sports\"}," +
                    "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                    "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"3\"}," +
                    "\"templateId\":\"go_alipay_sports_route\"}");
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".doPromise.promiseJoin", jsonObject.optString("errorMsg"));
                return;
            }
            Log.other("生活记录👟已加入[" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]" + JsonUtil.getValueByPath(jsonObject, "data.dynamicContent.subTitle"));
        } catch (Throwable t) {
            Log.i(TAG, "promiseSportsRoute err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    /**
     * 保障金
     *
     * @param isRepeat 是否领取一个后先查询，再继续领取
     * @param recordId recordId
     */
    private void securityFund(boolean isRepeat, String recordId) {
        try {
            String str = AntMemberRpcCall.queryMultiSceneWaitToGainList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".securityFund.queryMultiSceneWaitToGainList", jsonObject.optString("errorMsg"));
                return;
            }
            jsonObject = jsonObject.getJSONObject("data");
            // 使用 keys() 方法获取所有键
            Iterator<String> keys = jsonObject.keys();
            // 遍历所有键
            while (keys.hasNext()) {
                String key = keys.next();
                // 获取键对应的值
                Object propertyValue = jsonObject.get(key);
                if (propertyValue instanceof JSONArray) {
                    //如eventToWaitDTOList、helpChildSumInsuredDTOList
                    JSONArray jsonArray = ((JSONArray) propertyValue);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        isRepeat = gainMyAndFamilySumInsured(jsonArray.getJSONObject(i), isRepeat, recordId);
                    }
                } else if (propertyValue instanceof JSONObject) {
                    //如signInDTO、priorityChannelDTO
                    JSONObject jo = ((JSONObject) propertyValue);
                    if (jo.length() == 0) {
                        continue;
                    }
                    isRepeat = gainMyAndFamilySumInsured(jo, isRepeat, recordId);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "securityFund err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    /**
     * 领取保障金
     *
     * @param jsonObject 保障金jsonObject
     * @param isRepeat   是否需要刷新明细
     * @param recordId   明细recordId
     * @return 是否已刷新明细
     * @throws JSONException JSONException
     */
    private boolean gainMyAndFamilySumInsured(JSONObject jsonObject, boolean isRepeat, String recordId) throws JSONException {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.gainMyAndFamilySumInsured(jsonObject));
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".gainMyAndFamilySumInsured", jo.optString("errorMsg"));
                return true;
            }
            Log.other("生活记录💰领取保障金[" + JsonUtil.getValueByPath(jo, "data.gainSumInsuredDTO.gainSumInsuredYuan") + "]" + "元");
            if (isRepeat) {
                promiseQueryDetail(recordId);
                promiseQueryDetail(recordId);
                return false;
            }
            return true;
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    /**
     * 查询持续做明细任务
     *
     * @param recordId recordId
     * @throws JSONException JSONException
     */
    private JSONObject promiseQueryDetail(String recordId) throws JSONException {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.promiseQueryDetail(recordId));
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".promiseQueryDetail", jo.optString("errorMsg"));
                return null;
            }
            return jo;
        } finally {
            TimeUtil.sleep(1000);
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
                    try {
                        JSONObject jo = new JSONObject(AntMemberRpcCall.applyTask(name, id));
                        if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                            Log.i(TAG, "signPageTaskList.applyTask err:" + jo.optString("resultDesc"));
                            continue;
                        }
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
                    } finally {
                        TimeUtil.sleep(1000);
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
        return doubleCheck;
    }

    public void kbMember() {
        try {
            if (!Status.canKbSignInToday()) {
                return;
            }
            String s = AntMemberRpcCall.rpcCall_signIn();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success", false)) {
                jo = jo.getJSONObject("data");
                Log.other("口碑签到📅[第" + jo.getString("dayNo") + "天]#获得" + jo.getString("value") + "积分");
                Status.KbSignInToday();
            } else if (s.contains("\"HAS_SIGN_IN\"")) {
                Status.KbSignInToday();
            } else {
                Log.i(TAG, jo.getString("errorMessage"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void goldTicket() {
        try {
            //签到
            goldBillCollect("\"campId\":\"CP1417744\",\"directModeDisableCollect\":true,\"from\":\"antfarm\",");
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
            //收取其他
            goldBillCollect("");
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 收取黄金票
     */
    private void goldBillCollect(String signInfo) {
        try {
            String str = AntMemberRpcCall.goldBillCollect(signInfo);
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
        }
    }

    private void enableGameCenter() {
        try {
            try {
                String str = AntMemberRpcCall.querySignInBall();
                JSONObject jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".signIn.querySignInBall", jsonObject.optString("resultDesc"));
                    return;
                }
                str = JsonUtil.getValueByPath(jsonObject, "data.signInBallModule.signInStatus");
                if (String.valueOf(true).equals(str)) {
                    return;
                }
                str = AntMemberRpcCall.continueSignIn();
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".signIn.continueSignIn", jsonObject.optString("resultDesc"));
                    return;
                }
                Log.other("游戏中心🎮签到成功");
            } catch (Throwable th) {
                Log.i(TAG, "signIn err:");
                Log.printStackTrace(TAG, th);
            }
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
            try {
                String str = AntMemberRpcCall.queryPointBallList();
                JSONObject jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".batchReceive.queryPointBallList", jsonObject.optString("resultDesc"));
                    return;
                }
                JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.pointBallList");
                if (jsonArray == null || jsonArray.length() == 0) {
                    return;
                }
                str = AntMemberRpcCall.batchReceivePointBall();
                jsonObject = new JSONObject(str);
                if (jsonObject.getBoolean("success")) {
                    Log.other("游戏中心🎮全部领取成功[" + JsonUtil.getValueByPath(jsonObject, "data.totalAmount") + "]乐豆");
                } else {
                    Log.i(TAG + ".batchReceive.batchReceivePointBall", jsonObject.optString("resultDesc"));
                }
            } catch (Throwable th) {
                Log.i(TAG, "batchReceive err:");
                Log.printStackTrace(TAG, th);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void collectSesame() {
        try {
            String s = AntMemberRpcCall.queryHome();
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".run.queryHome", jo.optString("errorMsg"));
                return;
            }
            JSONObject entrance = jo.getJSONObject("entrance");
            if (!entrance.optBoolean("openApp")) {
                Log.other("芝麻信用💌未开通");
                return;
            }
            JSONObject jo2 = new JSONObject(AntMemberRpcCall.queryCreditFeedback());
            if (!jo2.getBoolean("success")) {
                Log.i(TAG + ".collectSesame.queryCreditFeedback", jo2.optString("resultView"));
                return;
            }
            JSONArray ojbect = jo2.getJSONArray("creditFeedbackVOS");
            for (int i = 0; i < ojbect.length(); i++) {
                jo2 = ojbect.getJSONObject(i);
                if (!"UNCLAIMED".equals(jo2.getString("status"))) {
                    continue;
                }
                String title = jo2.getString("title");
                String creditFeedbackId = jo2.getString("creditFeedbackId");
                String potentialSize = jo2.getString("potentialSize");
                jo2 = new JSONObject(AntMemberRpcCall.collectCreditFeedback(creditFeedbackId));
                if (!jo2.getBoolean("success")) {
                    Log.i(TAG + ".collectSesame.collectCreditFeedback", jo2.optString("resultView"));
                    continue;
                }
                Log.other("收芝麻粒🙇🏻‍♂️[" + title + "]#" + potentialSize + "粒");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

}
