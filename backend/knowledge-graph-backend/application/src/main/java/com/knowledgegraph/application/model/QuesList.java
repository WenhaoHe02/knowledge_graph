package com.knowledgegraph.application.model;

public class QuesList {
    private String titleContent; // 题目内容
    private String standardAnswer; // 标准答案

    public QuesList(String titleContent, String standardAnswer) {
        this.titleContent = titleContent;
        this.standardAnswer = standardAnswer;
    }

    public String getTitleContent() {
        return titleContent;
    }

    public void setTitleContent(String titleContent) {
        this.titleContent = titleContent;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }
}
