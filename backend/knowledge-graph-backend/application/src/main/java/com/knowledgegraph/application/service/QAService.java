package com.knowledgegraph.application.service;

public interface QAService {
    /**
     * 根据用户输入获取答案
     * @param question 用户提问
     * @return 答案
     */
    String getAnswer(String question);
}
