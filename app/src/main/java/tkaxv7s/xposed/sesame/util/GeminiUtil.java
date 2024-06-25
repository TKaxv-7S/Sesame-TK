package tkaxv7s.xposed.sesame.util;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.BaseModel;

import static tkaxv7s.xposed.sesame.util.JsonUtil.getValueByPath;

/**
 * Gemini帮助类
 *
 * @author Xiong
 */
public class GeminiUtil {

    private final String TAG = GeminiUtil.class.getSimpleName();
    private String url = "https://api.genai.gd.edu.kg/google";
    private String key = "AIzaSyA8e1SjuCbZBfx8nDRjmoMHZsWdd3y4-bQ";

    // 私有构造函数，防止外部实例化
    private GeminiUtil() {
        String cKey = BaseModel.getGeminiKey().getValue();
        String cUrl = BaseModel.getGeminiKey().getValue();
        if (cKey != null && !cKey.isEmpty()) {
            key = cKey;
        }
        if (cUrl != null && !cUrl.isEmpty()) {
            url = cUrl.trim().replaceAll("/$", "");
        }
    }

    // 静态内部类，实现懒汉式加载
    private static class GeminiUtilHolder {
        private static final GeminiUtil INSTANCE = new GeminiUtil();
    }

    // 公共静态方法，获取单例实例
    public static GeminiUtil getInstance() {
        return GeminiUtilHolder.INSTANCE;
    }

    /**
     * 获取AI回答结果
     *
     * @param text 问题内容
     * @return AI回答结果
     */
    public String getContentText(String text) {
        String result = "";
        try {
            String content = "{\n" +
                    "    \"contents\": [\n" +
                    "        {\n" +
                    "            \"parts\": [\n" +
                    "                {\n" +
                    "                    \"text\": \"只回答答案 " + text + "\"\n" +
                    "                }\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(content, mediaType);
            String url2 = url + "/v1beta/models/gemini-1.5-flash:generateContent?key=" + key;
            Request request = new Request.Builder()
                    .url(url2)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if (response.body() == null) {
                return result;
            }
            String json = response.body().string();
            if (!response.isSuccessful()) {
                Log.other("Gemini接口异常：" + json);
                //可能key出错了
                return result;
            }
            JSONObject jsonObject = new JSONObject(json);
            result = getValueByPath(jsonObject, "candidates.[0].content.parts.[0].text");
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        return result;
    }

    /**
     * 获取答案
     *
     * @param title     问题
     * @param jsonArray 答案集合
     * @return 空没有获取到
     */
    public String getAnswer(String title, JSONArray jsonArray) {
        try {
            String answer = GeminiUtil.getInstance().getContentText(title + "\n" +
                    jsonArray.toString().replaceAll("\"", ""));
            if (answer != null && !answer.isEmpty()) {
                Log.record("AI🧠回答：" + answer);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String str = jsonArray.getString(i);
                    if (answer.contains(str)) {
                        return str;
                    }
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        return "";
    }
}
