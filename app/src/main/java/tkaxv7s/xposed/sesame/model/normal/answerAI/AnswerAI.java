package tkaxv7s.xposed.sesame.model.normal.answerAI;

import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.StringModelField;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.List;

public class AnswerAI extends Model {

    private static final String TAG = AnswerAI.class.getSimpleName();

    @Override
    public String getName() {
        return "AI答题";
    }

    private static AnswerAIInterface answerAIInterface = AnswerAIInterface.getInstance();

    private final BooleanModelField useGeminiAI = new BooleanModelField("useGeminiAI", "GeminiAI | 使用答题", false);

    private final StringModelField setGeminiAIToken = new StringModelField("useGeminiAIToken", "GeminiAI | 设置令牌", "");

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(useGeminiAI);
        modelFields.addField(setGeminiAIToken);
        return modelFields;
    }

    @Override
    public void config(ClassLoader classLoader) {
        if (useGeminiAI.getValue()) {
            answerAIInterface = new GenAI(setGeminiAIToken.getValue());
        }
    }

    /**
     * 获取AI回答结果
     *
     * @param text 问题内容
     * @return AI回答结果
     */
    public static String getAnswer(String text) {
        try {
            return answerAIInterface.getAnswer(text);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        return "";
    }

    /**
     * 获取答案
     *
     * @param text     问题
     * @param answerList 答案集合
     * @return 空没有获取到
     */
    public static String getAnswer(String text, List<String> answerList) {
        try {
            return answerAIInterface.getAnswer(text, answerList);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        return "";
    }

}