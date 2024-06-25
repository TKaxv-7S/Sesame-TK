package tkaxv7s.xposed.sesame.model.task.otherTask;

import org.json.JSONObject;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.model.common.rpcCall.BaseTaskRpcCall;

/**
 * 福利金RpcCall类
 *
 * @author xiong
 */
public class OtherTaskRpcCall extends BaseTaskRpcCall {

    /**
     * 黄金票首页
     *
     * @return 结果
     */
    public static String goldBillIndex() {
        return ApplicationHook.requestString("com.alipay.wealthgoldtwa.needle.goldbill.index",
                "[{\"pageTemplateCode\":\"H5_GOLDBILL\",\"params\":{\"client_pkg_version\":\"0.0.5\"}," +
                        "\"url\":\"https://68687437.h5app.alipay.com/www/index.html\"}]");
    }

    /**
     * 黄金票收取
     *
     * @param str signInfo
     * @return 结果
     */
    public static String goldBillCollect(String str) {
        return ApplicationHook.requestString("com.alipay.wealthgoldtwa.goldbill.v2.index.collect",
                "[{" + str + "\"trigger\":\"Y\"}]");
    }

    /**
     * 黄金票任务
     *
     * @param taskId taskId
     * @return 结果
     */
    public static String goldBillTrigger(String taskId) {
        return ApplicationHook.requestString("com.alipay.wealthgoldtwa.goldbill.v4.task.trigger",
                "[{\"goldBillTaskTransferVersion\":\"v2\",\"taskId\":\"" + taskId + "\"}]");
    }

    /**
     * 查询车神卡奖励
     *
     * @return 结果
     */
    public static String v1benefitQuery() {
        return ApplicationHook.requestString("com.alipay.pcreditbfweb.drpc.cargodcard.v1benefitQuery",
                "[{\"args\":{\"productCode\":\"CAR_MASTER_CARD\"}}]");
    }

    /**
     * 领取车神卡奖励
     *
     * @param jsonObject jsonObject
     * @return 结果
     */
    public static String v1benefitTrigger(JSONObject jsonObject) {
        return ApplicationHook.requestString("com.alipay.pcreditbfweb.drpc.cargodcard.v1benefitTrigger",
                "[" + jsonObject + "]");
    }

    /**
     * 查询车神卡奖励
     *
     * @return 结果
     */
    public static String queryTaskList() {
        return ApplicationHook.requestString("alipay.promoprod.task.query.queryTaskList",
                "[      {\n" +
                        "            \"consultAccessFlag\": true,\n" +
//                        "            \"extInfo\": {\n" +
//                        "                \"paramsForStrategy\": \"{\\\"currentTaskId\\\":\\\"AP14241342\\\",\\\"appId\\\":\\\"2060090000314842\\\",\\\"hookId\\\":\\\"{\\\\\\\"gplusItem\\\\\\\":\\\\\\\"HK256605~CR727186~MI_30482_V2~lf_257\\\\\\\"}\\\"}\",\n" +
//                        "                \"sceneCode\": \"c_balltask\"\n" +
//                        "            },\n" +
                        "            \"planId\": \"AP17187348\"\n" +
                        "        }]");
    }

    /**
     * 领取车神卡奖励
     *
     * @param gplusItem gplusItem
     * @return 结果
     */
    public static String signup(String gplusItem, String taskId) {
        String str = "\"taskCenId\":\"AP17187348\",\"taskId\":\"" + taskId + "\"";
        if (!gplusItem.isEmpty()) {
            str += ",\"extInfo\":{\"gplusItem\":\"" + gplusItem + "\"}";
        }
        return ApplicationHook.requestString("alipay.promoprod.task.query.signup",
                "[{" + str + "}]");
    }

    /**
     * 领取车神卡奖励
     *
     * @param appletId appletId
     * @return 结果
     */
    public static String complete(String appletId) {
        return ApplicationHook.requestString("alipay.promoprod.applet.complete",
                "[{\"appletId\":\"" + appletId + "\"}]");
    }
}
