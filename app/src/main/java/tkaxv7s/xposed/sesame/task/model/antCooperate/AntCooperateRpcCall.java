package tkaxv7s.xposed.sesame.task.model.antCooperate;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;

public class AntCooperateRpcCall {
    private static final String VERSION = "20230501";

    public static String queryUserCooperatePlantList() {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.queryUserCooperatePlantList", "[{}]");
    }

    public static String queryCooperatePlant(String coopId) {
        String args1 = "[{\"cooperationId\":\"" + coopId + "\"}]";
        return ApplicationHook.requestString("alipay.antmember.forest.h5.queryCooperatePlant", args1);
    }

    public static String cooperateWater(String uid, String coopId, int count) {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.cooperateWater",
                "[{\"bizNo\":\"" + uid + "_" + coopId + "_" + System.currentTimeMillis() + "\",\"cooperationId\":\""
                        + coopId + "\",\"energyCount\":" + count + ",\"source\":\"\",\"version\":\"" + VERSION
                        + "\"}]");
    }

}
