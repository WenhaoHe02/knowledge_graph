package com.knowledgegraph.application.model;

public class QuesList {
    private String id; // 题目 ID
    private String titleContent; // 题目内容
    private String standardAnswer; // 标准答案
    private int type; // 题目类型（1: 简答题, 2: 选择题, 3: 填空题）

    public QuesList(String id, String titleContent, String standardAnswer, int type) {
        this.id = id;
        this.titleContent = titleContent;
        this.standardAnswer = standardAnswer;
        this.type = type;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}