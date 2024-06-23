package tkaxv7s.xposed.sesame.task.model.gameCenter;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.task.common.rpcCall.BaseTaskRpcCall;

public class GameCenterRpcCall extends BaseTaskRpcCall {

    /**
     * 全部领取
     * @return 结果
     */
    public static String batchReceivePointBall() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.batchReceivePointBall",
                "[{}]");
    }

    /**
     * 签到查询
     * @return 结果
     */
    public static String querySignInBall() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.querySignInBall",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 签到
     * @return 结果
     */
    public static String continueSignIn() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.continueSignIn",
                "[{\"sceneId\":\"GAME_CENTER\",\"signType\":\"NORMAL_SIGN\",\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 查询待完成的任务
     * @return 结果
     */
    public static String queryModularTaskList(){
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.queryModularTaskList",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 查询待领取乐豆列表
     * @return 结果
     */
    public static String queryPointBallList(){
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.queryPointBallList",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 查询兑换红包列表
     * @return 结果
     */
    public static String queryPointBenefitAggPage(){
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.queryPointBenefitAggPage",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 任务报名
     * @param taskId 任务ID
     * @return 结果
     */
    public static String doTaskSignup(String taskId) {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.doTaskSignup",
                "[{\"source\":\"ch_appcenter__chsub_9patch\",\"taskId\":\"" + taskId + "\"}]");
    }

    /**
     * 任务完成
     * @param taskId 任务ID
     * @return 结果
     */
    public static String doTaskSend(String taskId) {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.doTaskSend",
                "[{\"taskId\":\"" + taskId + "\"}]");
    }

}
