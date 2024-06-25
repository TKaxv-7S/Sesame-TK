package tkaxv7s.xposed.sesame.model.task.sesameCredit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.Iterator;

/**
 * 芝麻粒
 *
 * @author xiong
 */
public class SesameCredit extends ModelTask {

    private static final String TAG = SesameCredit.class.getSimpleName();

    private Integer executeIntervalInt;
    /**
     * 是否启用芝麻信用
     */
    private BooleanModelField sesameCredit;

    /**
     * 收保障金
     */
    private BooleanModelField collectSecurityFund;
    /**
     * 执行间隔时间
     */
    private IntegerModelField executeInterval;
    public BooleanModelField insBlueBeanExchange;
    public BooleanModelField insBlueBeanExchangeGoldTicket;

    public BooleanModelField promiseSportsRoute;

    public BooleanModelField promiseAddComment;

    @Override
    public String setName() {
        return "芝麻信用";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(sesameCredit = new BooleanModelField("sesameCredit", "开启芝麻信用", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 5000));
        modelFields.addField(collectSecurityFund = new BooleanModelField("collectSecurityFund", "记录 | 坚持攒保障金(可开启持续做)", false));
        modelFields.addField(promiseSportsRoute = new BooleanModelField("promiseSportsRoute", "记录 | 坚持锻炼，走运动路线(只自动加入任务)", false));
        modelFields.addField(promiseAddComment = new BooleanModelField("promiseAddComment", "记录 | 坚持陪伴爱宠并记录(只自动发布记录)", false));
        modelFields.addField(insBlueBeanExchange = new BooleanModelField("insBlueBeanExchange", "安心豆 | 兑换时光加速器", false));
        modelFields.addField(insBlueBeanExchangeGoldTicket = new BooleanModelField("insBlueBeanExchangeGoldTicket", "安心豆 | 兑换黄金票", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return sesameCredit.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 5000);
            try {
                String s = SesameCreditRpcCall.queryHome();
                JSONObject jo = new JSONObject(s);
                if (!jo.getBoolean("success")) {
                    Log.i(TAG + ".init.queryHome", jo.optString("errorMsg"));
                    return;
                }
                JSONObject entrance = jo.getJSONObject("entrance");
                if (!entrance.optBoolean("openApp")) {
                    Log.other("芝麻信用💌未开通");
                    return;
                }
                doPromise();
                insBlueBean();
                collectSesame();
            } catch (Throwable th) {
                Log.i(TAG, "init err:");
                Log.printStackTrace(TAG, th);
            }
        };
    }

    /**
     * 生活记录
     */
    private void doPromise() {
        if (!collectSecurityFund.getValue() && !promiseSportsRoute.getValue() &&
                !promiseAddComment.getValue()) {
            return;
        }
        try {
            //模拟从生活记录->明细->任务->明细（两次，不知原因）
            String str = SesameCreditRpcCall.promiseQueryHome();
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
                if (collectSecurityFund.getValue() && "坚持攒保障金".equals(promiseName)) {
                    promiseQueryDetail(recordId);
                    securityFund(isRepeat, recordId);
                    promiseQueryDetail(recordId);
                    promiseQueryDetail(recordId);
                }
                if ("坚持锻炼，走运动路线".equals(promiseName)) {
                    //已经加入了，运动会自动行走，暂不做处理
                    isSportsRoute = false;
                } else if ("坚持陪伴爱宠并记录".equals(promiseName) && promiseAddComment.getValue()) {
                    jsonObject = promiseQueryDetail(recordId);
                    if (jsonObject == null || "FINISH".equals(JsonUtil.getValueByPath(jsonObject, "data.subRecordStatus"))) {
                        continue;
                    }
                    str = SesameCreditRpcCall.promiseAddComment("🌈ᑋᵉᑊᑊᵒ ᵕ✨ ◡ 解锁新的一年",
                            "https://mdn.alipayobjects.com/afts/img/A*-JdRRKBx4O4AAAAAAAAAAAAAAQAAAQ/original?bz=APM_68687674&width=864&height=1920");
                    jsonObject = new JSONObject(str);
                    if (!jsonObject.getBoolean("success")) {
                        Log.i(TAG + ".doPromise.promiseAddComment", jsonObject.optString("errorMsg"));
                        continue;
                    }
                    promiseQueryDetail(recordId);
                    Log.other("生活记录🐶[坚持陪伴爱宠并记录]" + jsonObject.getBoolean("resultView"));
                }
            }
            if (isSportsRoute && promiseSportsRoute.getValue()) {
                str = SesameCreditRpcCall.promiseJoin("{\"autoRenewStatus\":false,\"dataSourceRule\":{\"selectValue\":\"alipay_sports\"}," +
                        "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                        "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"3\"}," +
                        "\"templateId\":\"go_alipay_sports_route\"}");
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".doPromise.promiseJoin", jsonObject.optString("errorMsg"));
                    return;
                }
                Log.other("生活记录👟已加入[" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]" +
                        JsonUtil.getValueByPath(jsonObject, "data.dynamicContent.subTitle"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "doPromise err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
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
            String str = SesameCreditRpcCall.queryMultiSceneWaitToGainList();
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
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
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
        JSONObject jo = new JSONObject(SesameCreditRpcCall.gainMyAndFamilySumInsured(jsonObject));
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
    }

    /**
     * 查询持续做明细任务
     *
     * @param recordId recordId
     * @throws JSONException JSONException
     */
    private JSONObject promiseQueryDetail(String recordId) throws JSONException {
        JSONObject jo = new JSONObject(SesameCreditRpcCall.promiseQueryDetail(recordId));
        if (!jo.getBoolean("success")) {
            Log.i(TAG + ".promiseQueryDetail", jo.optString("errorMsg"));
            return null;
        }
        return jo;
    }

    /**
     * 收芝麻粒
     */
    private void collectSesame() {
        try {
            JSONObject jo = new JSONObject(SesameCreditRpcCall.queryCreditFeedback());
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".collectSesame.queryCreditFeedback", jo.optString("resultView"));
                return;
            }
            JSONArray ojbect = jo.getJSONArray("creditFeedbackVOS");
            for (int i = 0; i < ojbect.length(); i++) {
                jo = ojbect.getJSONObject(i);
                if (!"UNCLAIMED".equals(jo.getString("status"))) {
                    continue;
                }
                String title = jo.getString("title");
                String creditFeedbackId = jo.getString("creditFeedbackId");
                String potentialSize = jo.getString("potentialSize");
                jo = new JSONObject(SesameCreditRpcCall.collectCreditFeedback(creditFeedbackId));
                if (!jo.getBoolean("success")) {
                    Log.i(TAG + ".collectSesame.collectCreditFeedback", jo.optString("resultView"));
                    continue;
                }
                Log.other("收芝麻粒🙇🏻‍♂️[" + title + "]#" + potentialSize + "粒");
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectSesame err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 安心豆
     */
    private void insBlueBean() {
        try {
            String s = SesameCreditRpcCall.pageRender();
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".insBlueBean.pageRender", jo.optString("resultView"));
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            JSONArray modules = result.getJSONArray("modules");
            for (int i = 0; i < modules.length(); i++) {
                jo = modules.getJSONObject(i);
                String name = jo.getString("name");
                if ("签到配置".equals(name)) {
                    insBlueBeanTask(JsonUtil.getValueByPath(jo, "content.signConfig.appletId"));
                } else if (("兑换时光加速器".equals(name) && insBlueBeanExchange.getValue())
                        || ("10份黄金票".equals(name) && insBlueBeanExchangeGoldTicket.getValue())) {
                    insBlueBeanExchange(JsonUtil.getValueByPath(jo, "content.beanDeductBanner.oneStopId"));
                } else if ("任务分类".equals(name)) {
                    JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jo, "content.taskClassification");
                    if (jsonArray == null) {
                        continue;
                    }
                    for (int j = 0; j < jsonArray.length(); j++) {
                        jo = jsonArray.getJSONObject(j);
                        JSONArray ja = jo.getJSONArray("taskAppletIdList");
                        for (int h = 0; h < ja.length(); h++) {
                            jo = ja.getJSONObject(h);
                            insBlueBeanTask(jo.getString("appletId"));
                        }
                    }
                }
            }

            insBlueBeanPlanConsult();
        } catch (Throwable t) {
            Log.i(TAG, "insBlueBean err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 查询安心豆任务
     */
    private void insBlueBeanPlanConsult() {
        try {
            String str = SesameCreditRpcCall.planConsult();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanPlanConsult.planConsult", jsonObject.optString("resultView"));
                return;
            }
            JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "result.rspContext.params.taskAppletResult");
            if (jsonArray == null) {
                return;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                insBlueBeanTask(jsonObject.optString("appletId"));
            }

        } catch (Throwable t) {
            Log.i(TAG, "planConsult err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt / 10);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 安心豆任务
     *
     * @param appletId appletId
     */
    private void insBlueBeanTask(String appletId) {
        try {
            String s = SesameCreditRpcCall.taskProcess(appletId);
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanTask.taskProcess", jo.optString("resultView"));
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            if (!result.getBoolean("canPush") || "FULLY_DONE".equals(result.getString("renderStatus"))
                    || "LIFE_TASK".equals(result.getString("taskType"))) {
                //垃圾任务都跳过
                return;
            }
            s = SesameCreditRpcCall.taskTrigger(appletId, "insportal-marketing");
            JSONObject joTrigger = new JSONObject(s);
            if (!joTrigger.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanTask.taskTrigger", joTrigger.optString("resultView"));
                return;
            }
            Log.other("安心豆🥔[" + JsonUtil.getValueByPath(joTrigger, "result.prizeSendOrderDTOList.[0].prizeName") + "]完成");
        } catch (Throwable t) {
            Log.i(TAG, "insBlueBeanTask err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt / 10);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 安心豆兑换
     *
     * @param itemId itemId
     */
    private void insBlueBeanExchange(String itemId) {
        try {
            String s = SesameCreditRpcCall.queryUserAccountInfo();
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanExchange.queryUserAccountInfo", jo.optString("resultView"));
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            int userCurrentPoint = result.optInt("userCurrentPoint", 0);
            if (userCurrentPoint <= 0) {
                return;
            }
            jo = new JSONObject(SesameCreditRpcCall.exchangeDetail(itemId));
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanExchange.exchangeDetail", jo.optString("resultView"));
                return;
            }
            JSONObject exchangeDetail = jo.getJSONObject("result").getJSONObject("rspContext")
                    .getJSONObject("params").getJSONObject("exchangeDetail");
            if (!"ITEM_GOING".equals(exchangeDetail.getString("status"))) {
                return;
            }
            JSONObject itemExchangeConsultDTO = exchangeDetail.getJSONObject("itemExchangeConsultDTO");
            int pointAmount = itemExchangeConsultDTO.getInt("realConsumePointAmount");
            if (!itemExchangeConsultDTO.getBoolean("canExchange") || userCurrentPoint < pointAmount) {
                return;
            }
            jo = new JSONObject(SesameCreditRpcCall.exchange(itemId, pointAmount));
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".insBlueBeanExchange.exchange", jo.optString("resultView"));
                return;
            }
            Log.other("[" + pointAmount + "]安心豆🥔兑换成功[" + exchangeDetail.getString("itemName") + "]");
        } catch (Throwable t) {
            Log.i(TAG, "insBlueBeanExchange err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

}
