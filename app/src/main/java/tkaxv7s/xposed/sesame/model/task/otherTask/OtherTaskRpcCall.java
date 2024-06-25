package tkaxv7s.xposed.sesame.model.task.otherTask;

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

}
