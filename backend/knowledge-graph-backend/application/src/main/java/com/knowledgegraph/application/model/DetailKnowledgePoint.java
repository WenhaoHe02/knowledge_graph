package com.knowledgegraph.application.model;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailKnowledgePoint extends KnowledgePoint {
    private List<String> prePoint;
    private List<String> postPoint;
    private String cognition;
    private String tag;
    private List<String> relatedPoint;
    private String note;

    // 构造函数
    public DetailKnowledgePoint(String id, String name, String content,
                                List<String> prePoint, List<String> postPoint,
                                String cognition, String tag,
                                List<String> relatedPoint, String note) {
        super.setId(id);
        super.setName(name);
        super.setContent(content);
        if (prePoint != null)
        this.prePoint = new ArrayList<>(prePoint);
        if (postPoint != null)
        this.postPoint = new ArrayList<>(postPoint);
        this.cognition = cognition;
        this.tag = tag;
        if (relatedPoint != null)
        this.relatedPoint = new ArrayList<>(relatedPoint);
        this.note = note;
    }


    public List<String> getPrePoint() {
        return prePoint;
    }


    public List<String> getPostPoint() {
        return postPoint;
    }


    public String getCognition() {
        return cognition;
    }

    public void setCognition(String cognition) {
        this.cognition = cognition;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // relatedPoint 的 getter，返回一个不可修改的副本
    public List<String> getRelatedPoint() {
        // 返回一个副本，避免外部修改原始列表
        return new ArrayList<>(relatedPoint);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

