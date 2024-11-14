package com.knowledgegraph.application.model;

import java.util.Map;
import java.util.HashMap;

public class GradingResult {
    Map<String, Integer> scores; // 每题得分
    Map<String, String> feedback; // 每题反馈
    int totalScore; // 总分

    // 构造函数
    public GradingResult() {
        this.scores = new HashMap<>();
        this.feedback = new HashMap<>();
        this.totalScore = 0;
    }

    public void addScore(String questionId, int score) {
        scores.put(questionId, score);
        totalScore += score;
    }

    public void print()
    {
        System.out.println("scores:\n");
        System.out.println(scores);
        System.out.println("feedback:\n");
        System.out.println(feedback);
    }

    public void addFeedback(String questionId, String feedback) {
        this.feedback.put(questionId, feedback);
    }
}