package com.knowledgegraph.application;

public class Main {
    public static void main(String[] args) {
        // 替换为你的数据库 URI、用户名和密码
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "zstp123456";

        ConnectDataBase neo4jTest = new ConnectDataBase(uri, user, password);

        // 创建节点
        neo4jTest.createNode();

        // 查询节点
        neo4jTest.searchNodes();

        // 关闭连接
        neo4jTest.close();
    }
}