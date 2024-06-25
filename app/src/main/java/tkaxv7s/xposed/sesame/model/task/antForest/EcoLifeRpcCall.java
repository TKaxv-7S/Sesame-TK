package tkaxv7s.xposed.sesame.model.task.antForest;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;

/**
 * @author Xiong
 */
public class EcoLifeRpcCall {

    /**
     * 查询绿色行动
     *
     * @return 结果
     */
    public static String queryHomePage() {
        return ApplicationHook.requestString("alipay.ecolife.rpc.h5.queryHomePage",
                "[{\"channel\":\"ALIPAY\",\"source\":\"search_brandbox\"}]");
    }

    /**
     * 开通绿色行动
     *
     * @return 结果
     */
    public static String openEcolife() {
        return ApplicationHook.requestString("alipay.ecolife.rpc.h5.openEcolife",
                "[{\"channel\":\"ALIPAY\",\"source\":\"renwuGD\"}]");
    }

    /**
     * 执行任务
     *
     * @param actionId actionId
     * @param dayPoint 当前日期
     * @param source   来源renwuGD,photo-comparison,search_brandbox
     * @return 结果
     */
    public static String tick(String actionId, String dayPoint, String source) {
        String args1 = "[{\"actionId\":\"" + actionId + "\",\"channel\":\"ALIPAY\",\"dayPoint\":\""
                + dayPoint + "\",\"generateEnergy\":false,\"source\":\"" + source + "\"}]";
        return ApplicationHook.requestString("alipay.ecolife.rpc.h5.tick", args1);
    }

    /**
     * 查询任务信息
     *
     * @param source   来源renwuGD,photo-comparison,search_brandbox
     * @param dayPoint 当前日期
     * @return 结果
     */
    public static String queryDish(String source, String dayPoint) {
        return ApplicationHook.requestString("alipay.ecolife.rpc.h5.queryDish",
                "[{\"channel\":\"ALIPAY\",\"dayPoint\":\"" + dayPoint
                        + "\",\"source\":\"" + source + "\"}]");
    }

    /**
     * 上传照片
     *
     * @param operateType 类型：餐前、餐后
     * @param imageId     图片id
     * @param conf1       位移值？
     * @param conf2       conf2
     * @param conf3       conf3
     * @return 结果
     */
    public static String uploadDishImage(String operateType, String imageId,
                                         double conf1, double conf2, double conf3, String dayPoint) {
        return ApplicationHook.requestString("alipay.ecolife.rpc.h5.uploadDishImage",
                "[{\"channel\":\"ALIPAY\",\"dayPoint\":\"" + dayPoint +
                        "\",\"source\":\"photo-comparison\",\"uploadParamMap\":{\"AIResult\":[{\"conf\":" + conf1 + ",\"kvPair\":false," +
                        "\"label\":\"other\",\"pos\":[1.0002995,0.22104378,0.0011976048,0.77727276],\"value\":\"\"}," +
                        "{\"conf\":" + conf2 + ",\"kvPair\":false,\"label\":\"guangpan\",\"pos\":[1.0002995,0.22104378,0.0011976048,0.77727276]," +
                        "\"value\":\"\"},{\"conf\":" + conf3 + ",\"kvPair\":false,\"label\":\"feiguangpan\"," +
                        "\"pos\":[1.0002995,0.22104378,0.0011976048,0.77727276],\"value\":\"\"}],\"existAIResult\":true,\"imageId\":\"" +
                        imageId + "\",\"imageUrl\":\"https://mdn.alipayobjects.com/afts/img/" + imageId +
                        "/original?bz=APM_20000067\",\"operateType\":\"" + operateType + "\"}}]");
    }
}
