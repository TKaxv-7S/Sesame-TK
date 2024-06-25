package tkaxv7s.xposed.sesame.model.common.rpcCall;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;

/**
 * 公共任务处理
 * @author xiong
 */
public class BaseTaskRpcCall {

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
                Log.other(name + "[" + JsonUtil.getValueByPath(taskDetail, "taskExtProps.TASK_MORPHO_DETAIL.title") + "]任务完成");
            }
        } catch (Throwable th) {
            Log.i(tag, "doTask err:");
            Log.printStackTrace(tag, th);
        }
    }

}
