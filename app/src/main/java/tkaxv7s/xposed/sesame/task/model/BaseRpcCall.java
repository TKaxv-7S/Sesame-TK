package tkaxv7s.xposed.sesame.task.model;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;

import java.util.StringTokenizer;

public class BaseRpcCall {

    /**
     *
     */
    public enum TaskProcessStatusEnum {
        /**
         *
         */
        SIGNUP_COMPLETE,
        /**
         *
         */
        NONE_SIGNUP,
        /**
         * 一次性已完成的
         */
        RECEIVE_SUCCESS,
        /**
         * 领取奖品
         */
        TO_RECEIVE
    }

    /**
     * 查询任务
     * @param appletId appletId
     * @return
     */
    public static String taskQuery(String appletId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.task.taskQuery",
                "[{\"appletId\":\"" + appletId + "\",\"completedBottom\":true}]");
    }

    /**
     * 触发任务
     * @param appletId appletId
     * @param stageCode stageCode
     * @param taskCenId 任务ID
     * @return
     */
    public static String taskTrigger(String appletId, String stageCode, String taskCenId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.task.taskTrigger",
                "[{\"appletId\":\"" + appletId + "\",\"stageCode\":\"" + stageCode + "\",\"taskCenId\":\"" + taskCenId
                        + "\"}]");
    }


    public static String signInTrigger(String sceneId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.signin.trigger",
                "[{\"extInfo\":{},\"sceneId\":\"" + sceneId + "\"}]");
    }


    /**
     * 根据给定的点分隔符路径从JSONObject中获取值。
     *
     * @param jsonObject JSONObject对象
     * @param path      点分隔符或索引形式的路径，例如 "result.prizeOrderDTOList[0].price"
     * @return 找到值的话返回其String表现形式；如果任何层级键不存在或不是预期类型，则返回null。
     */
    public static String getValueByPath(JSONObject jsonObject, String path) {
        StringTokenizer st = new StringTokenizer(path, ".[]");
        try {
            Object current = jsonObject;
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (current instanceof JSONObject) {
                    // 如果当前是JSONObject且token包含索引，则转换为JSONArray并提取对应元素
                    if (token.contains("[")) {
                        JSONArray array = new JSONArray((JSONObject) current);
                        String indexStr = token.substring(token.indexOf('[') + 1, token.indexOf(']'));
                        int index = Integer.parseInt(indexStr); // 处理可能抛出NumberFormatException例外情况
                        current = array.get(index);
                    } else {
                        current = ((JSONObject) current).get(token);
                    }
                }
            }
            return String.valueOf(current); // 返回最后一层的结果
        } catch (Exception e) { // JSONException、NumberFormatException等都被捕获，并返回null作为默认行为。
            return null;
        }
    }
}
