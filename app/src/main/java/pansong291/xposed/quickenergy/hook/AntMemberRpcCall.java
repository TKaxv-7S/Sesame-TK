package pansong291.xposed.quickenergy.hook;

import pansong291.xposed.quickenergy.util.RandomUtils;

public class AntMemberRpcCall {

    private static String getUniqueId() {
        return String.valueOf(System.currentTimeMillis()) + RandomUtils.nextLong();
    }

    public static Boolean check() {
        String str = ApplicationHook.request("alipay.antmember.biz.rpc.member.h5.queryPointCert", "[{\"page\":" + 1 + ",\"pageSize\":" + 8 + "}]");
        return str != null && !str.isEmpty();
    }

    /* ant member point */
    public static String queryPointCert(int page, int pageSize) {
        String args1 = "[{\"page\":" + page + ",\"pageSize\":" + pageSize + "}]";
        return ApplicationHook.request("alipay.antmember.biz.rpc.member.h5.queryPointCert", args1);
    }

    public static String receivePointByUser(String certId) {
        String args1 = "[{\"certId\":" + certId + "}]";
        return ApplicationHook.request("alipay.antmember.biz.rpc.member.h5.receivePointByUser", args1);
    }

    public static String queryMemberSigninCalendar() {
        return ApplicationHook.request("com.alipay.amic.biz.rpc.signin.h5.queryMemberSigninCalendar",
                "[{\"autoSignIn\":true,\"invitorUserId\":\"\",\"sceneCode\":\"QUERY\"}]");
    }
    /* 安心豆 */

    public static String pageRender() {
        return ApplicationHook.request("com.alipay.insplatformbff.common.insiopService.pageRender",
                "[\"INS_PLATFORM_BLUEBEAN\",{\"channelType\":\"insplatform_mobilesearch_anxindou\"}]");
    }

    public static String taskProcess(String appletId) {
        return ApplicationHook.request("com.alipay.insmarketingbff.task.taskProcess", "[{\"appletId\":\"" + appletId + "\"}]");
    }

    public static String taskTrigger(String appletId, String scene) {
        return ApplicationHook.request("com.alipay.insmarketingbff.task.taskTrigger",
                "[{\"appletId\":\"" + appletId + "\",\"scene\":\"" + scene + "\"}]");
    }

    public static String queryUserAccountInfo() {
        return ApplicationHook.request("com.alipay.insmarketingbff.point.queryUserAccountInfo",
                "[{\"channel\":\"insplatform_mobilesearch_anxindou\",\"pointProdCode\":\"INS_BLUE_BEAN\",\"pointUnitType\":\"COUNT\"}]");
    }

    public static String exchangeDetail(String itemId) {
        return ApplicationHook.request("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId
                        + "\"},\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchangeDetail\"}]");
    }

    public static String exchange(String itemId, int pointAmount) {
        return ApplicationHook.request("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId + "\",\"pointAmount\":\"" + Integer.toString(pointAmount)
                        + "\"},\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchange\"}]");
    }

    /* 芝麻信用 */
    public static String queryHome() {
        return ApplicationHook.request("com.antgroup.zmxy.zmcustprod.biz.rpc.home.api.HomeV6RpcManager.queryHome",
                "[{\"miniZmGrayInside\":\"\"}]");
    }

    public static String queryCreditFeedback() {
        return ApplicationHook.request(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.queryCreditFeedback",
                "[{\"queryPotential\":false,\"size\":20,\"status\":\"UNCLAIMED\"}]");
    }

    public static String collectCreditFeedback(String creditFeedbackId) {
        return ApplicationHook.request(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.collectCreditFeedback",
                "[{\"collectAll\":false,\"creditFeedbackId\":\"" + creditFeedbackId + "\",\"status\":\"UNCLAIMED\"}]");
    }

    /* 商家服务 */
    public static String transcodeCheck() {
        return ApplicationHook.request("alipay.mrchservbase.mrchbusiness.sign.transcode.check",
                "[{}]");
    }

    public static String zcjSignInQuery() {
        return ApplicationHook.request("alipay.mrchservbase.zcj.view.invoke",
                "[{\"compId\":\"ZCJ_SIGN_IN_QUERY\"}]");
    }

    public static String zcjSignInExecute() {
        return ApplicationHook.request("alipay.mrchservbase.zcj.view.invoke",
                "[{\"compId\":\"ZCJ_SIGN_IN_EXECUTE\"}]");
    }

    public static String taskListQuery() {
        return ApplicationHook.request("alipay.mrchservbase.zcj.taskList.query",
                "[{\"compId\":\"ZCJ_TASK_LIST\",\"params\":{\"activityCode\":\"ZCJ\",\"clientVersion\":\"10.3.36\",\"extInfo\":{},\"platform\":\"Android\",\"underTakeTaskCode\":\"\"}}]");
    }

    public static String queryActivity() {
        return ApplicationHook.request("alipay.merchant.kmdk.query.activity",
                "[{\"scene\":\"activityCenter\"}]");
    }

    public static String signIn(String activityNo) {
        return ApplicationHook.request("alipay.merchant.kmdk.signIn",
                "[{\"activityNo\":\"" + activityNo + "\"}]");
    }

    public static String signUp(String activityNo) {
        return ApplicationHook.request("alipay.merchant.kmdk.signUp",
                "[{\"activityNo\":\"" + activityNo + "\"}]");
    }

    /* 商家服务任务 */

    public static String taskFinish(String bizId) {
        return ApplicationHook.request("com.alipay.adtask.biz.mobilegw.service.task.finish",
                "[{\"bizId\":\"" + bizId + "\"}]");
    }

    public static String taskReceive(String taskCode) {
        return ApplicationHook.request("alipay.mrchservbase.sqyj.task.receive",
                "[{\"compId\":\"ZTS_TASK_RECEIVE\",\"extInfo\":{\"taskCode\":\"" + taskCode + "\"}}]");
    }

    public static String actioncode(String actionCode) {
        return ApplicationHook.request("alipay.mrchservbase.task.query.by.actioncode",
                "[{\"actionCode\":\"" + actionCode + "\"}]");
    }

    public static String produce(String actionCode) {
        return ApplicationHook.request("alipay.mrchservbase.biz.task.action.produce",
                "[{\"actionCode\":\"" + actionCode + "\"}]");
    }

    public static String ballReceive(String ballIds) {
        return ApplicationHook.request("alipay.mrchservbase.mrchpoint.ball.receive",
                "[{\"ballIds\":[\"" + ballIds
                        + "\"],\"channel\":\"MRCH_SELF\",\"outBizNo\":\"" + getUniqueId() + "\"}]");
    }

    /* 会员任务 */
    public static String signPageTaskList() {
        return ApplicationHook.request("alipay.antmember.biz.rpc.membertask.h5.signPageTaskList",
                "[{\"sourceBusiness\":\"antmember\",\"spaceCode\":\"ant_member_xlight_task\"}]");
    }

    public static String applyTask(String darwinName, Long taskConfigId) {
        return ApplicationHook.request("alipay.antmember.biz.rpc.membertask.h5.applyTask",
                "[{\"darwinExpParams\":{\"darwinName\":\"" + darwinName
                        + "\"},\"sourcePassMap\":{\"innerSource\":\"\",\"source\":\"myTab\",\"unid\":\"\"},\"taskConfigId\":"
                        + taskConfigId + "}]");
    }

    public static String executeTask(String bizParam, String bizSubType) {
        return ApplicationHook.request("alipay.antmember.biz.rpc.membertask.h5.executeTask",
                "[{\"bizOutNo\":\"" + String.valueOf(System.currentTimeMillis() - 16000L) + "\",\"bizParam\":\""
                        + bizParam + "\",\"bizSubType\":\"" + bizSubType + "\",\"bizType\":\"BROWSE\"}]");
    }

}
