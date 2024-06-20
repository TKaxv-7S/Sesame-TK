package tkaxv7s.xposed.sesame.task.model.kbMember;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;

public class KBMemberRpcCall {
    private static final String version = "2.0";

    public static String rpcCall_signIn() {
        String args1 = "[{\"sceneCode\":\"KOUBEI_INTEGRAL\",\"source\":\"ALIPAY_TAB\",\"version\":\"" + version + "\"}]";
        return ApplicationHook.requestString("alipay.kbmemberprod.action.signIn", args1);
    }

}
