package tkaxv7s.xposed.sesame.model.task.readingDada;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.model.normal.answerAI.AnswerAI;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.StringUtil;

/**
 * @author Constanline
 * @since 2023/08/22
 */
public class ReadingDada {
    private static final String TAG = ReadingDada.class.getSimpleName();

    public static boolean answerQuestion(JSONObject bizInfo) {
        try {
            String taskJumpUrl = bizInfo.optString("taskJumpUrl");
            if (StringUtil.isEmpty(taskJumpUrl)) {
                taskJumpUrl = bizInfo.getString("targetUrl");
            }
            String activityId = taskJumpUrl.split("activityId%3D")[1].split("%26")[0];
            String outBizId;
            if (taskJumpUrl.contains("outBizId%3D")) {
                outBizId = taskJumpUrl.split("outBizId%3D")[1].split("%26")[0];
            } else {
                outBizId = "";
            }
            String s = ReadingDadaRpcCall.getQuestion(activityId);
            JSONObject jo = new JSONObject(s);
            if ("200".equals(jo.getString("resultCode"))) {
                JSONArray jsonArray = jo.getJSONArray("options");
                String question = jo.getString("title");
                String answer = AnswerAI.getAnswer(question, JsonUtil.jsonArrayToList(jsonArray));
                if (answer == null || answer.isEmpty()) {
                    answer = jsonArray.getString(0);
                }
                s = ReadingDadaRpcCall.submitAnswer(activityId, outBizId, jo.getString("questionId"), answer);
                jo = new JSONObject(s);
                if ("200".equals(jo.getString("resultCode"))) {
                    Log.record("答题完成");
                    return true;
                } else {
                    Log.record("答题失败");
                }
            } else {
                Log.record("获取问题失败");
            }
        } catch (Throwable e) {
            Log.i(TAG, "answerQuestion err:");
            Log.printStackTrace(TAG, e);
        }
        return false;
    }
}