package tkaxv7s.xposed.sesame.task.model.greenFinance;

import org.json.JSONArray;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.task.common.rpcCall.BaseTaskRpcCall;
import tkaxv7s.xposed.sesame.util.UserIdMap;

public class GreenFinanceRpcCall extends BaseTaskRpcCall {

    /**
     * 绿色经营首页
     *
     * @return 结果
     */
    public static String greenFinanceIndex() {
        return ApplicationHook.requestString(
                "com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinancePageQueryService.indexV2",
                "[{\"clientVersion\":\"VERSION2\",\"custType\":\"MERCHANT\"}]");
    }

    /**
     * 批量收取
     *
     * @param bsnIds bsnIds
     * @return 结果
     */
    public static String batchSelfCollect(JSONArray bsnIds) {
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.service.GreenFinancePointCollectService.batchSelfCollect",
                "[{\"bsnIds\":" + bsnIds + ",\"clientVersion\":\"VERSION2\",\"custType\":\"MERCHANT\",\"uid\":\""
                        + UserIdMap.getCurrentUid() + "\"}]");
    }

    /**
     * 签到查询
     *
     * @param sceneId sceneId
     * @return 结果
     */
    public static String signInQuery(String sceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.signin.query",
                "[{\"cycleCount\":7,\"cycleType\":\"d\",\"extInfo\":{},\"needContinuous\":1,\"sceneId\":\"" + sceneId + "\"}]");
    }

    /**
     * 查询打卡记录
     *
     * @return 结果
     */
    public static String queryUserTickItem(String firstBehaviorType) {
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinanceTickService.queryUserTickItem",
                "[{\"custType\":\"MERCHANT\",\"firstBehaviorType\":\"" + firstBehaviorType + "\",\"uid\":\"" + UserIdMap.getCurrentUid() + "\"}]");
    }

    /**
     * 提交打卡
     *
     * @param behaviorCode behaviorCode
     * @return 结果
     */
    public static String submitTick(String firstBehaviorType, String behaviorCode) {
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinanceTickService.submitTick",
                "[{\"custType\":\"MERCHANT\",\"firstBehaviorType\":\"" + firstBehaviorType + "\",\"uid\":\"" + UserIdMap.getCurrentUid() + "\",\"behaviorCode\":\"" + behaviorCode + "\"}]");
    }

    /**
     * 查询要过期了的金币
     *
     * @param day 多少天后
     * @return 结果
     */
    public static String queryExpireMcaPoint(long day) {
        //{"ariverRpcTraceId":"client`ZWBWO+Zb5kQDAHgksDyLs/tHP11O+Xc_283027","result":{"expirePoint":{"amount":"6762.00","amountInt":"6762","cent":"676200"}},"resultView":"处理成功","success":true}
        //十天后
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinancePageQueryService.queryExpireMcaPoint",
                "[{\"custType\":\"MERCHANT\",\"profitType\":\"MYBK_LOAN_DISCOUNT\",\"uid\":\"" + UserIdMap.getCurrentUid() +
                        "\",\"expireDate\":\"" + (System.currentTimeMillis() + day * 24 * 60 * 60 * 1000) + "\"}]");
    }

    /**
     * 查询可捐助的项目、
     *
     * @return 结果
     */
    public static String queryAllDonationProjectNew() {
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinanceDonationService.queryAllDonationProjectNew",
                "[{\"custType\":\"MERCHANT\",\"subjectType\":\"ALL_DONATION\",\"uid\":\"" + UserIdMap.getCurrentUid() + "\"}]");
    }

    /**
     * 查询要过期了的金币
     *
     * @param projectId CLEAN_ENERGY_00001、WELFARE_PV_0000
     * @return 结果
     */
    public static String donation(String projectId, String amount) {
        //{"ariverRpcTraceId":"client`ZWBWO+Zb5kQDAHgksDyLs/tHP11fNHg_230398","result":{"amount":200,"bsnId":"202406231073250005003700277823650280","certificateId":"MBKO1043330320","custType":"MERCHANT","donateElectricityRatio":2,"donateTime":1719088281085,"gmtCreate":1652176865000,"gmtModify":32487667200000,"outBizNo":"1719088280762","projectId":"CLEAN_ENERGY_00001","projectName":"朝阳县光伏发电项目","showFlag":"Y","targetAmount":1162,"uid":"2088302146583284"},"resultView":"处理成功","success":true}
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinanceDonationService.donation",
                "[{\"custType\":\"MERCHANT\",\"donationGold\":\"" + amount + "\",\"uid\":\"" + UserIdMap.getCurrentUid() +
                        "\",\"outbizNo\":\"" + System.currentTimeMillis() + "\",\"projectId\":\"" + projectId + "\"}]");
    }

}
