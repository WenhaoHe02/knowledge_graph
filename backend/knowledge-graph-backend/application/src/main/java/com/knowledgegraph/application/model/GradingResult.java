package com.knowledgegraph.application.model;

import java.util.Map;
import java.util.HashMap;

public class GradingResult {
    public Map<String, Integer> getScores() {
        return scores;
    }

    public Map<String, String> getFeedback() {
        return feedback;
    }

    public int getTotalScore() {
        return totalScore;
    }

    Map<String, Integer> scores; // 每题得分
    Map<String, String> feedback; // 每题反馈

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

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

    public void print() {
        // 打印总分
        System.out.println("Total Score: " + totalScore + "\n");

        // 打印每个问题的得分
        System.out.println("Scores per Question:");
        if (scores.isEmpty()) {
            System.out.println("No scores available.\n");
        } else {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                String questionId = entry.getKey();
                Integer score = entry.getValue();
                System.out.println("Question ID: " + questionId + " - Score: " + score);
            }
        }

        // 打印每个问题的反馈
        System.out.println("\nFeedback per Question:");
        if (feedback.isEmpty()) {
            System.out.println("No feedback available.\n");
        } else {
            for (Map.Entry<String, String> entry : feedback.entrySet()) {
                String questionId = entry.getKey();
                String feedbackMessage = entry.getValue();
                System.out.println("Question ID: " + questionId + " - Feedback: " + feedbackMessage);
            }
        }
    }


    public void addFeedback(String questionId, String feedback) {
        this.feedback.put(questionId, feedback);
    }
}