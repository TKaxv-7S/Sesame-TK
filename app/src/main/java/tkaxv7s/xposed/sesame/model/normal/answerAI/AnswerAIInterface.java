package tkaxv7s.xposed.sesame.model.normal.answerAI;

import java.util.List;

public interface AnswerAIInterface {

    /**
     * 获取AI回答结果
     *
     * @param text 问题内容
     * @return AI回答结果
     */
    String getAnswer(String text);

    /**
     * 获取答案
     *
     * @param title     问题
     * @param answerList 答案集合
     * @return 空没有获取到
     */
    String getAnswer(String title, List<String> answerList);


    static AnswerAIInterface getInstance() {
        return new AnswerAIInterface() {
            @Override
            public String getAnswer(String text) {
                return "";
            }

            @Override
            public String getAnswer(String title, List<String> answerList) {
                return "";
            }
        };
    }
}
