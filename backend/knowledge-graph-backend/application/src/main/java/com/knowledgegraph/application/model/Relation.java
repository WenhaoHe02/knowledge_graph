package com.knowledgegraph.application.model;

public class Relation {

    private String pointA;   // 知识点 A 的 ID
    private String pointB;   // 知识点 B 的 ID
    private String relation; // 关系类型 (pre, post, parent, child)

    // 构造函数
    public Relation(String pointA, String pointB, String relation) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.relation = relation;
    }

    // Getter 和 Setter 方法
    public String getPointA() {
        return pointA;
    }

    public void setPointA(String pointA) {
        this.pointA = pointA;
    }

    public String getPointB() {
        return pointB;
    }

    public void setPointB(String pointB) {
        this.pointB = pointB;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    // 重写 toString() 方法，便于调试输出
    @Override
    public String toString() {
        return "Relation{" +
                "pointA='" + pointA + '\'' +
                ", pointB='" + pointB + '\'' +
                ", relation='" + relation + '\'' +
                '}';
    }
}
