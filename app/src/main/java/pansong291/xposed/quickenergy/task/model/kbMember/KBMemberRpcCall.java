package pansong291.xposed.quickenergy.task.model.kbMember;

import pansong291.xposed.quickenergy.hook.ApplicationHook;

public class KBMemberRpcCall {
    private static final String version = "2.0";

    public static String rpcCall_signIn() {
        String args1 = "[{\"sceneCode\":\"KOUBEI_INTEGRAL\",\"source\":\"ALIPAY_TAB\",\"version\":\"" + version + "\"}]";
        return ApplicationHook.requestString("alipay.kbmemberprod.action.signIn", args1);
    }

}
