package tkaxv7s.xposed.sesame.task.model.welfareCenter;

import org.json.JSONArray;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.task.common.rpcCall.BaseTaskRpcCall;

/**
 * 福利金RpcCall类
 * @author xiong
 */
public class WelfareCenterRpcCall extends BaseTaskRpcCall {

    /**
     * 查询待领取的福利金、签到
     *
     * @param signInSceneId signInSceneId
     * @return 结果
     */
    public static String queryEnableVirtualProfitV2(String signInSceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.virtualProfit.queryEnableVirtualProfitV2",
                "[{\"firstSceneCode\":[],\"profitType\":\"ANTBANK_WELFARE_POINT\",\"sceneCode\":[\"FULICenter_JKJML\"," +
                        "\"FULICenter_JZN\",\"BC3_BC3V1\",\"BC3_BC3V2\",\"BC3_BC3V3\",\"SQB_SQBV0\",\"SQB_SQBV1\",\"SQB_SQBV2\"," +
                        "\"SQB_SQBV3\",\"SQB_SQBV4\",\"SQB_SQBV5\",\"SQB_SQBV6\",\"SQB_SQBV7\",\"SQB_SQBV8\",\"SQB_SQBV9\"," +
                        "\"SQB_SQBV10\",\"SQB_SQBV11\",\"SQB_SQBSIGN\",\"FULICenter_JKJQW\",\"FULICenter_WSWF\",\"FULICenter_FLKZS\"," +
                        "\"FULICenter_KGJXBBF\",\"FULICenter_AXHZXB\",\"FULICenter_BBF\",\"FULICenter_V1\",\"FULICenter_V2\"," +
                        "\"FULICenter_V3\",\"FULICenter_V4\",\"FULICenter_V5\",\"FULICenter_V6\",\"FULICenter_V7\"," +
                        "\"FULICenter_YulibaoAUM\",\"FULICenter_PayByMybank\",\"FULICenter_DepositAUM\",\"FULICenter_YYYYH\"," +
                        "\"FULICenter_QYZ\",\"FULICenter_V7PLUS\",\"FULICenter_V6PLUS\",\"FULICenter_V5PLUS\"," +
                        "\"FULICenter_V8\",\"FULICenter_V9\",\"FULICenter_V10\"],\"signInSceneId\":\"" + signInSceneId + "\"}]");
    }

    /**
     * 批量领取福利金
     *
     * @param vpIds Ids
     * @return 结果
     */
    public static String batchUseVirtualProfit(JSONArray vpIds) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.virtualProfit.batchUseVirtualProfit",
                "[{\"virtualProfitIdList\":" + vpIds + "}]");
    }

    /**
     * 签到
     *
     * @param sceneId sceneId
     * @return 结果
     */
    public static String signInTrigger(String sceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.signin.trigger",
                "[{\"extInfo\":{},\"sceneId\":\"" + sceneId + "\"}]");
    }

}
