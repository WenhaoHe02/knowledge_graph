package com.knowledgegraph.application.model;

public class CorrectionResult {
    private String feedback;
    private int score;

    public String getFeedback() { return feedback; }
    public void setFeedback(String value) { this.feedback = value; }

    public int getScore() { return score; }
    public void setScore(int value) { this.score = value; }


    @Override
    public String toString() {
        return "CorrectionResult{" +
                "feedback='" + feedback + '\'' +
                ", score=" + score +
                '}';
    }
}