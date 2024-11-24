package com.knowledgegraph.application.model;

import java.util.Arrays;

public class Request {
    private CorrectionResult[] correctionResults;
    private int totalScore;

    public CorrectionResult[] getCorrectionResults() { return correctionResults; }
    public void setCorrectionResults(CorrectionResult[] value) { this.correctionResults = value; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int value) { this.totalScore = value; }

    @Override
    public String toString() {
        return "Request{" +
                "correctionResults=" + Arrays.toString(correctionResults) +
                ", totalScore=" + totalScore +
                '}';
    }
}

