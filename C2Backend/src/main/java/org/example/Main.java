package org.example;

public class Main {
    public static void main(String[] args) {
        String uri = "neo4j://localhost:7687";
        String user = "neo4j";
        String password = "zstp123456";
        try {
            Neo4jUtil.init(uri, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Neo4j 数据库连接已初始化");
        KnowledgeQueryComponent component = new KnowledgeQueryComponent();
        component.test();

    }
}