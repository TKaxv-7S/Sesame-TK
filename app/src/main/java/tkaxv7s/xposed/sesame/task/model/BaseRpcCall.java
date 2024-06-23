package tkaxv7s.xposed.sesame.task.model;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.util.Log;

/**
 * 公共任务处理
 * @author xiong
 */
public class BaseRpcCall {

    /**
     * 查询任务
     *
     * @param appletId appletId
     * @return 结果
     */
    public static String taskQuery(String appletId) {
        return ApplicationHook.requestString("com.alipay.loanpromoweb.promo.task.taskQuery",
                "[{\"appletId\":\"" + appletId + "\",\"completedBottom\":true}]");
    }

    /**
     * 触发任务
     *
     * @param appletId  appletId
     * @param stageCode stageCode
     * @param taskCenId 任务ID
     * @return 结果
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
     * 公共做任务
     * 使用taskQuery查询任务，taskTrigger触发任务（根据taskProcessStatus状态，报名signup->完成send->领奖receive）
     *
     * @param appletId appletId
     * @param tag 类名
     * @param name 中文说明
     */
    public static void doTask(String appletId, String tag, String name) {
        try {
            String s = taskQuery(appletId);
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(tag + ".doTask.taskQuery", jo.optString("resultDesc"));
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            JSONArray taskDetailList = result.getJSONArray("taskDetailList");
            for (int i = 0; i < taskDetailList.length(); i++) {
                JSONObject taskDetail = taskDetailList.getJSONObject(i);
                //EVENT_TRIGGER、USER_TRIGGER
                String type = taskDetail.getString("sendCampTriggerType");
                if (!"USER_TRIGGER".equals(type) && !"EVENT_TRIGGER".equals(type)) {
                    continue;
                }

                String status = taskDetail.getString("taskProcessStatus");
                String taskId = taskDetail.getString("taskId");
                if ("TO_RECEIVE".equals(status)) {
                    //领取奖品，任务待领奖
                    s = taskTrigger(taskId, "receive", appletId);
                    jo = new JSONObject(s);
                    if (!jo.getBoolean("success")) {
                        Log.i(tag + ".doTask.receive", jo.optString("resultDesc"));
                        continue;
                    }
                } else if ("NONE_SIGNUP".equals(status)) {
                    //没有报名的，先报名，再完成
                    s = taskTrigger(taskId, "signup", appletId);
                    jo = new JSONObject(s);
                    if (!jo.getBoolean("success")) {
                        Log.i(tag + ".doTask.signup", jo.optString("resultDesc"));
                        continue;
                    }
                }
                if ("SIGNUP_COMPLETE".equals(status) || "NONE_SIGNUP".equals(status)) {
                    //已报名，待完成，去完成
                    s = taskTrigger(taskId, "send", appletId);
                    jo = new JSONObject(s);
                    if (!jo.getBoolean("success")) {
                        Log.i(tag + ".doTask.send", jo.optString("resultDesc"));
                        continue;
                    }
                } else if (!"TO_RECEIVE".equals(status)) {
                    continue;
                }
                //RECEIVE_SUCCESS一次性已完成的
                Log.other(name + "[" + getValueByPath(taskDetail, "taskExtProps.TASK_MORPHO_DETAIL.title") + "]任务完成");
            }
        } catch (Throwable th) {
            Log.i(tag, "doTask err:");
            Log.printStackTrace(tag, th);
        }
    }

    /**
     * 根据给定的点分隔符路径从JSONObject中获取值。
     *
     * @param jsonObject JSONObject对象
     * @param path       点分隔符或包含嵌套属性的形式的路径，例如 "taskExtProps.TASK_MORPHO_DETAIL.[0].title"
     * @return 找到值的话返回其String表现形式；如果任何层级键不存在或不是预期类型，则返回 null。
     */
    public static String getValueByPath(JSONObject jsonObject, String path) {
        // 使用正斜杠/作为Token分隔符号来直接跳过数组下标的解析逻辑部分并直接取嵌套属性
        String[] parts = path.split("\\."); // 使用正则表达式分割，但保留[]内的内容
        try {
            Object current = jsonObject;
            for (String part : parts) {
                if (current instanceof JSONObject) {
                    //对象取属性
                    current = ((JSONObject) current).get(part);
                } else if (current instanceof JSONArray) {
                    //数组取索引
                    JSONArray array = (JSONArray) current;
                    String p = part.replaceAll("\\D", "");
                    int index = Integer.parseInt(p); // 处理可能抛出NumberFormatException例外情况
                    current = array.get(index);
                } else if (part.contains("[")) {
                    //不是对象、数组，当成字符串重新解析，如果字符串是数组
                    JSONArray array = new JSONArray(current.toString());
                    String p = part.replaceAll("\\D", "");
                    int index = Integer.parseInt(p); // 处理可能抛出NumberFormatException例外情况
                    current = array.get(index);
                } else {
                    //不是对象、数组，当成字符串重新解析，再取属性
                    JSONObject object = new JSONObject(current.toString());
                    current = object.get(part);
                }
            }
            // 返回结果时检查是否确实找到了相应的值且非null，并转换成字符串形式返回
            return (current != null) ? String.valueOf(current) : null;
        } catch (Exception e) { // JSONException、NumberFormatException等异常都被捕获，并默认行为是返回null.
            return null;
        }
    }
}
