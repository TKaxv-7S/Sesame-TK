package tkaxv7s.xposed.sesame.task.model.sesameCredit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
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

    @Override
    public String setName() {
        return "芝麻信用";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(sesameCredit = new BooleanModelField("sesameCredit", "开启芝麻信用", false));
        modelFields.addField(collectSecurityFund = new BooleanModelField("collectSecurityFund", "收保障金(可开启持续做)", false));
        modelFields.addField(insBlueBeanExchange = new BooleanModelField("insBlueBeanExchange", "安心豆兑换时光加速器", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 5000));
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
        if (!collectSecurityFund.getValue()) {
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
            str = SesameCreditRpcCall.getValueByPath(jsonObject, "data.processingPromises");
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String recordId = jsonObject.getString("recordId");
                //如果当天任务做完后就结束了，则可以再继续一次，缩短任务时间。
                boolean isRepeat = jsonObject.getInt("totalNums") - jsonObject.getInt("finishNums") == 1;
                if (collectSecurityFund.getValue() && "坚持攒保障金".equals(jsonObject.getString("promiseName"))) {
                    promiseQueryDetail(recordId);
                    securityFund(isRepeat, recordId);
                    promiseQueryDetail(recordId);
                    promiseQueryDetail(recordId);
                }
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

            // 使用 keys() 方法获取所有键
            Iterator<String> keys = jsonObject.getJSONObject("data").keys();
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

    private boolean gainMyAndFamilySumInsured(JSONObject jsonObject, boolean isRepeat, String recordId) throws JSONException {
        JSONObject jo = new JSONObject(SesameCreditRpcCall.gainMyAndFamilySumInsured(jsonObject));
        if (!jo.getBoolean("success")) {
            Log.i(TAG + ".gainMyAndFamilySumInsured", jo.optString("errorMsg"));
            return true;
        }
        Log.other("领取保障金💰[" + SesameCreditRpcCall.getValueByPath(jo, "data.gainSumInsuredDTO.gainSumInsuredYuan") + "]" + "元");
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
    private void promiseQueryDetail(String recordId) throws JSONException {
        JSONObject jo = new JSONObject(SesameCreditRpcCall.promiseQueryDetail(recordId));
        if (!jo.getBoolean("success")) {
            Log.i(TAG + ".promiseQueryDetail", jo.optString("errorMsg"));
        }
    }

    /**
     * 收芝麻粒
     */
    private static void collectSesame() {
        try {
            JSONObject jo = new JSONObject(SesameCreditRpcCall.queryCreditFeedback());
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".collectSesame.queryCreditFeedback", jo.optString("resultView"));
                return;
            }
            JSONArray creditFeedbackVOS = jo.getJSONArray("creditFeedbackVOS");
            for (int i = 0; i < creditFeedbackVOS.length(); i++) {
                jo = creditFeedbackVOS.getJSONObject(i);
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
        }
    }

    /**
     * 安心豆任务
     */
    private void insBlueBean() {
        try {
            String s = SesameCreditRpcCall.pageRender();
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.record("pageRender" + " " + s);
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            JSONArray modules = result.getJSONArray("modules");
            for (int i = 0; i < modules.length(); i++) {
                jo = modules.getJSONObject(i);
                if ("签到配置".equals(jo.getString("name"))) {
                    String appletId = jo.getJSONObject("content").getJSONObject("signConfig")
                            .getString("appletId");
                    insBlueBeanSign(appletId);
                } else if ("兑换时光加速器".equals(jo.getString("name"))) {
                    String oneStopId = jo.getJSONObject("content").getJSONObject("beanDeductBanner")
                            .getString("oneStopId");
                    if (insBlueBeanExchange.getValue()) {
                        insBlueBeanExchange(oneStopId);
                    }
                }
            }
            insBlueBeanTask();
        } catch (Throwable t) {
            Log.i(TAG, "anXinDou err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void insBlueBeanTask() {
        //TODO:ssss
    }

    private static void insBlueBeanSign(String appletId) {
        try {
            String s = SesameCreditRpcCall.taskProcess(appletId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                if (result.getBoolean("canPush")) {
                    s = SesameCreditRpcCall.taskTrigger(appletId, "insportal-marketing");
                    JSONObject joTrigger = new JSONObject(s);
                    if (joTrigger.getBoolean("success")) {
                        Log.other("安心豆🥔[签到成功]");
                    }
                }
            } else {
                Log.record("taskProcess" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "insBlueBeanSign err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void insBlueBeanExchange(String itemId) {
        try {
            String s = SesameCreditRpcCall.queryUserAccountInfo();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                int userCurrentPoint = result.optInt("userCurrentPoint", 0);
                if (userCurrentPoint > 0) {
                    jo = new JSONObject(SesameCreditRpcCall.exchangeDetail(itemId));
                    if (jo.getBoolean("success")) {
                        JSONObject exchangeDetail = jo.getJSONObject("result").getJSONObject("rspContext")
                                .getJSONObject("params").getJSONObject("exchangeDetail");
                        if ("ITEM_GOING".equals(exchangeDetail.getString("status"))) {
                            JSONObject itemExchangeConsultDTO = exchangeDetail.getJSONObject("itemExchangeConsultDTO");
                            int pointAmount = itemExchangeConsultDTO.getInt("realConsumePointAmount");
                            if (itemExchangeConsultDTO.getBoolean("canExchange") && userCurrentPoint >= pointAmount) {
                                jo = new JSONObject(SesameCreditRpcCall.exchange(itemId, pointAmount));
                                if (jo.getBoolean("success")) {
                                    Log.other("安心豆🥔[兑换" + exchangeDetail.getString("itemName") + "]");
                                } else {
                                    Log.record("exchange");
                                    Log.i(jo.toString());
                                }
                            }
                        }
                    } else {
                        Log.record("exchangeDetail");
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record("queryUserAccountInfo" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "insBlueBeanExchange err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
