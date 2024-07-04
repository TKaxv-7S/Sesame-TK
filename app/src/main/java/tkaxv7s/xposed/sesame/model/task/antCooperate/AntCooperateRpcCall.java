package tkaxv7s.xposed.sesame.model.task.antCooperate;

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

    /**
     * 获取合种浇水量排行
     * @param bizType 参数：D/A,“D”为查询当天，“A”为查询所有
     * @param coopId 合种ID
     * @return
     */
    public static String queryCooperateRank(String bizType, String coopId) {
        return  ApplicationHook.requestString("alipay.antmember.forest.h5.queryCooperateRank",
                "[{\"bizType\":\""+ bizType + "\",\"cooperationId\":\"" + coopId + "\",\"source\":\"chInfo_ch_url-https://render.alipay.com/p/yuyan/180020010001247580/home.html\"}]");

    }
}
