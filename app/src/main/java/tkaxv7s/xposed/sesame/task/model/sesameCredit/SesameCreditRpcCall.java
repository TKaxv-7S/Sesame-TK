package tkaxv7s.xposed.sesame.task.model.sesameCredit;

import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.task.common.rpcCall.BaseTaskRpcCall;
import tkaxv7s.xposed.sesame.util.UserIdMap;

/**
 * 芝麻信用RpcCall类
 *
 * @author xiong
 */
public class SesameCreditRpcCall extends BaseTaskRpcCall {

    /**
     * 芝麻信用首页
     *
     * @return 结果
     */
    public static String queryHome() {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmcustprod.biz.rpc.home.api.HomeV6RpcManager.queryHome",
                "[{\"miniZmGrayInside\":\"\"}]");
    }

    /**
     * 查询可收取的芝麻粒
     *
     * @return 结果
     */
    public static String queryCreditFeedback() {
        return ApplicationHook.requestString(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.queryCreditFeedback",
                "[{\"queryPotential\":false,\"size\":20,\"status\":\"UNCLAIMED\"}]");
    }

    /**
     * 收取芝麻粒
     *
     * @param creditFeedbackId creditFeedbackId
     * @return 结果
     */
    public static String collectCreditFeedback(String creditFeedbackId) {
        return ApplicationHook.requestString(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.collectCreditFeedback",
                "[{\"collectAll\":false,\"creditFeedbackId\":\"" + creditFeedbackId + "\",\"status\":\"UNCLAIMED\"}]");
    }

    /**
     * 查询生活记录
     *
     * @return 结果
     */
    public static String promiseQueryHome() {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.queryHome", null);
    }

    /**
     * 查询生活记录明细
     *
     * @param recordId recordId
     * @return 结果
     */
    public static String promiseQueryDetail(String recordId) {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.queryDetail",
                "[{\"recordId\":\"" + recordId + "\"}]");
    }

    /**
     * 查询生活记录模板
     *
     * @param templateId 模板ID
     * @return 结果
     */
    public static String querySingleTemplate(String templateId) {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.querySingleTemplate",
                "[{\"templateId\":\"" + templateId + "\"}]");
    }

    /**
     * 生活记录加入新纪录
     *
     * @param data data
     * @return 结果
     */
    public static String promiseJoin(String data) {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.join",
                "[" + data + "]");
    }

    /**
     * 发布宠物动态？
     * @param content content
     * @param img img
     * @return 结果
     */
    public static String promiseAddComment(String content, String img) {
        return ApplicationHook.requestString("alipay.secucommunity.comment.addComment",
                "[{\"content\":\"" + content + "\",\"cover\":\"" + img + "\",\"domain\":\"insurance\"," +
                        "\"extraParam\":{\"paster\":\"[]\",\"petInfo\":\"[]\"},\"images\":[\"" + img +
                        "\"],\"locationInfo\":\"{}\",\"materialList\":[],\"publisherType\":\"PAIBEI_TUWEN\"," +
                        "\"relateEntities\":[],\"tenantId\":\"INSURANCE_PET\",\"topicId\":\"" + UserIdMap.getCurrentUid() + "\"," +
                        "\"topicType\":\"PERSONAL\",\"userId\":\"" + UserIdMap.getCurrentUid() + "\"}]");
    }

    /**
     * 查询待领取的保障金
     *
     * @return 结果
     */
    public static String queryMultiSceneWaitToGainList() {
        return ApplicationHook.requestString("com.alipay.insgiftbff.insgiftMain.queryMultiSceneWaitToGainList",
                "[{\"entrance\":\"jkj_zhima_dairy66\",\"eventToWaitParamDTO\":{\"giftProdCode\":\"GIFT_UNIVERSAL_COVERAGE\"," +
                        "\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\"," +
                        "\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]}," +
                        "\"helpChildParamDTO\":{\"giftProdCode\":\"GIFT_HEALTH_GOLD_CHILD\",\"rightNoList\":[\"UNIVERSAL_ACCIDENT\"," +
                        "\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\",\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\"," +
                        "\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]},\"priorityChannelParamDTO\":{\"giftProdCode\":" +
                        "\"GIFT_UNIVERSAL_COVERAGE\",\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\"," +
                        "\"UNIVERSAL_OUTPATIENT\",\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\"," +
                        "\"UNIVERSAL_FRAUD_LIABILITY\"]},\"signInParamDTO\":{\"giftProdCode\":\"GIFT_UNIVERSAL_COVERAGE\"," +
                        "\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\"," +
                        "\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]}}]");
    }

    /**
     * 领取保障金
     *
     * @param jsonObject jsonObject
     * @return 结果
     */
    public static String gainMyAndFamilySumInsured(JSONObject jsonObject) throws JSONException {
        jsonObject.put("disabled", false);
        jsonObject.put("entrance", "jkj_zhima_dairy66");
        return ApplicationHook.requestString("com.alipay.insgiftbff.insgiftMain.gainMyAndFamilySumInsured",
                "[" + jsonObject + "]");
    }

    /**
     * 安心豆首页
     *
     * @return 结果
     */
    public static String pageRender() {
        return ApplicationHook.requestString("com.alipay.insplatformbff.common.insiopService.pageRender",
                "[\"INS_PLATFORM_BLUEBEAN\",{\"channelType\":\"insplatform_mobilesearch_anxindou\"}]");
    }

    /**
     * 查询安心豆任务
     *
     * @return 结果
     */
    public static String planConsult() {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.onestop.planConsult",
                "[{\"extParams\":{},\"planCode\":\"bluebean_onestop\"}]");
    }

    /**
     * 处理任务
     *
     * @param appletId appletId
     * @return 结果
     */
    public static String taskProcess(String appletId) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.task.taskProcess", "[{\"appletId\":\"" + appletId + "\"}]");
    }

    /**
     * 触发任务
     *
     * @param appletId appletId
     * @param scene    scene
     * @return 结果
     */
    public static String taskTrigger(String appletId, String scene) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.task.taskTrigger",
                "[{\"appletId\":\"" + appletId + "\",\"scene\":\"" + scene + "\"}]");
    }

    /**
     * 安心豆查询用户信息
     *
     * @return 结果
     */
    public static String queryUserAccountInfo() {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.point.queryUserAccountInfo",
                "[{\"channel\":\"insplatform_mobilesearch_anxindou\",\"pointProdCode\":\"INS_BLUE_BEAN\",\"pointUnitType\":\"COUNT\"}]");
    }

    /**
     * 安心豆兑换商品信息
     *
     * @param itemId itemId
     * @return 结果
     */
    public static String exchangeDetail(String itemId) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId
                        + "\"},\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchangeDetail\"}]");
    }

    /**
     * 安心豆兑换
     *
     * @param itemId      itemId
     * @param pointAmount pointAmount
     * @return 结果
     */
    public static String exchange(String itemId, int pointAmount) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId + "\",\"pointAmount\":\"" + pointAmount
                        + "\"},\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchange\"}]");
    }
}
