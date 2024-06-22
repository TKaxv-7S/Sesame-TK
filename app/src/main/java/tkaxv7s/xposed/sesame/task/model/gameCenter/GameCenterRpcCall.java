package tkaxv7s.xposed.sesame.task.model.gameCenter;

import org.json.JSONArray;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.util.UserIdMap;
import tkaxv7s.xposed.sesame.task.model.BaseRpcCall;

public class GameCenterRpcCall extends BaseRpcCall {

    public static String greenFinanceIndex() {
        return ApplicationHook.requestString(
                "com.alipay.mcaplatformunit.common.mobile.newservice.GreenFinancePageQueryService.indexV2",
                "[{\"clientVersion\":\"VERSION2\",\"custType\":\"MERCHANT\"}]");
    }

    public static String batchSelfCollect(JSONArray bsnIds) {
        return ApplicationHook.requestString("com.alipay.mcaplatformunit.common.mobile.service.GreenFinancePointCollectService.batchSelfCollect",
                "[{\"bsnIds\":" + bsnIds + ",\"clientVersion\":\"VERSION2\",\"custType\":\"MERCHANT\",\"uid\":\""
                        + UserIdMap.getCurrentUid() + "\"}]");
    }

    public static String signInQuery(String sceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.signin.query",
                "[{\"cycleCount\":7,\"cycleType\":\"d\",\"extInfo\":{},\"needContinuous\":1,\"sceneId\":\"" + sceneId + "\"}]");
    }

    public static String signInTrigger(String sceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.signin.trigger",
                "[{\"extInfo\":{},\"sceneId\":\"" + sceneId + "\"}]");
    }

}
