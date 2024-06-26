package tkaxv7s.xposed.sesame.model.task.sesameCredit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
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

    /**
     * 是否启用芝麻粒
     */
    private BooleanModelField sesameCredit;
    private BooleanModelField collectSecurityFund;
    public BooleanModelField promiseSportsRoute;
    public BooleanModelField promiseAddComment;

    @Override
    public String setName() {
        return "芝麻粒";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(sesameCredit = new BooleanModelField("sesameCredit", "开启芝麻粒", false));
        modelFields.addField(collectSecurityFund = new BooleanModelField("collectSecurityFund", "记录 | 坚持攒保障金(可开启持续做)", false));
        modelFields.addField(promiseSportsRoute = new BooleanModelField("promiseSportsRoute", "记录 | 坚持锻炼，走运动路线(只自动加入任务)", false));
        modelFields.addField(promiseAddComment = new BooleanModelField("promiseAddComment", "记录 | 坚持陪伴爱宠并记录(只自动发布记录)", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return sesameCredit.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            String s = SesameCreditRpcCall.queryHome();
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
            doPromise();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
            collectSesame();
        } catch (Throwable th) {
            Log.i(TAG, "run err:");
            Log.printStackTrace(TAG, th);
        }
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
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.printStackTrace(e);
                    }
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
        }
    }

}
